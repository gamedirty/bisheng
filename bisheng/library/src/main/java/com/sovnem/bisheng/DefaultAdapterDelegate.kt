package com.sovnem.bisheng

import android.util.ArrayMap
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

internal class DefaultAdapterDelegate(var onItemClickListener: OnItemClickListener?) :
    AdapterDelegate {

    private val adapterMap: IAdapterMap by lazy {
        Class.forName(Constants.PACKAGE + "." + Constants.CLASS_NAME).newInstance() as IAdapterMap
    }

    /**
     * [data,itemType]
     */
    private val typeMap: ArrayMap<Class<*>, Int> by lazy { adapterMap.dataToType }

    /**
     * [itemType,ViewHolder]
     */
    private val vhMap: SparseArray<Class<*>> by lazy { adapterMap.typeToViewHolder }


    /**
     * [ViewHolder,layoutRes]
     */
    private val vhLayoutMap by lazy { adapterMap.viewHolderToLayoutRes }

    private val inflaterFactory: LayoutInflater.Factory by lazy {
        LayoutInflater.Factory { name, context, attrs ->

            return@Factory null
        }
    }

    override fun getItemViewType(any: Any): Int {
        val cacheType = typeMap[any.javaClass]
        if (cacheType != null) {
            return cacheType
        }
        val vhType = typeOf(any)
        typeMap[any.javaClass] = vhType
        return vhType
    }

    private fun typeOf(any: Any): Int {
        any.javaClass.apply {
            val vhAno: VHRef = getAnnotation(VHRef::class.java)
                ?: throw IllegalStateException("${this.name}不包含${VHRef::class.java.name}注解")
            check(BiShengBaseVH::class.java.isAssignableFrom(vhAno.ref.java)) { "${vhAno.ref.qualifiedName} 需要继承 ${BiShengBaseVH::class.qualifiedName}" }
            val type = innerTypeOf(this)
            val instance = vhAno.ref.java
            vhMap.put(type, instance)
            return type
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HullViewHolder<*> {
        val layoutId = vhLayoutMap[vhMap[viewType]] ?: (vhMap.get(viewType)
            .getAnnotation(VHLayoutId::class.java)
            ?: null)?.layoutId
        val viewHolder =
            (vhMap.get(viewType).getConstructor().newInstance() as BiShengBaseVH<*>).apply {
                var content: View? = null
                if (layoutId != null) {
                    content =
                        LayoutInflater.from(parent.context)
                            .inflate(layoutId, null, false)
                }
                if (content == null) {
                    content = onCreateView()
                }
                check(content != null) { "${vhMap[viewType].name}没有添加${VHLayoutId::class.qualifiedName}注解，或者重写onCreateView方法" }
                this.containerView = content
            }
        return HullViewHolder(viewHolder)
    }

    override fun bindViewHolder(
        holder: RecyclerView.ViewHolder,
        any: Any,
        position: Int,
        payloads: MutableList<Any>?,
    ) {
        (holder as HullViewHolder<*>).bindDataInternal(any, position, payloads, onItemClickListener)
    }


    companion object {
        @JvmStatic
        fun getTypeOf(clazz: Class<*>): Int {
            return innerTypeOf(clazz)
        }

        private fun innerTypeOf(clazz: Class<*>): Int {
            return clazz.hashCode()
        }
    }
}

