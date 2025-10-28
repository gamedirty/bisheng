package com.sovnem.bisheng

import android.util.ArrayMap
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

internal class DefaultAdapterDelegate(var onItemClickListener: OnItemClickListener?) :
    AdapterDelegate {

    private val adapterMap: IAdapterMap by lazy {
        try {
            Class.forName(Constants.PACKAGE + "." + Constants.CLASS_NAME).newInstance() as IAdapterMap
        } catch (e: Exception) {
            throw IllegalStateException(
                "无法找到生成的适配器映射类: ${Constants.PACKAGE}.${Constants.CLASS_NAME}。" +
                "请确保已经正确配置kapt注解处理器，并且至少有一个数据类使用了@VHRef注解。", e
            )
        }
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

    companion object {
        private const val TAG = "BiSheng"
        
        @JvmStatic
        fun getTypeOf(clazz: Class<*>): Int {
            return innerTypeOf(clazz)
        }

        private fun innerTypeOf(clazz: Class<*>): Int {
            return clazz.hashCode()
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
                ?: throw IllegalStateException(
                    "数据类 ${this.name} 没有使用@VHRef注解。" +
                    "请在数据类上添加 @VHRef(YourViewHolder::class) 注解。"
                )
            
            if (!BiShengBaseVH::class.java.isAssignableFrom(vhAno.ref.java)) {
                throw IllegalStateException(
                    "ViewHolder ${vhAno.ref.qualifiedName} 必须继承 ${BiShengBaseVH::class.qualifiedName}。" +
                    "请确保您的ViewHolder类继承自BiShengBaseVH<T>。"
                )
            }
            
            val type = innerTypeOf(this)
            val instance = vhAno.ref.java
            vhMap.put(type, instance)
            Log.d(TAG, "注册懒加载类型: ${this.name} -> ${vhAno.ref.simpleName}")
            return type
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HullViewHolder<*> {
        val vhClass = vhMap.get(viewType)
            ?: throw IllegalStateException("无法找到viewType=$viewType 对应的ViewHolder类")
        
        val layoutId = vhLayoutMap[vhClass] ?: vhClass.getAnnotation(VHLayoutId::class.java)?.layoutId
        
        val viewHolder = try {
            vhClass.getConstructor().newInstance() as BiShengBaseVH<*>
        } catch (e: Exception) {
            throw IllegalStateException(
                "无法实例化ViewHolder: ${vhClass.name}。" +
                "请确保ViewHolder类有无参构造函数且不是内部类或抽象类。", e
            )
        }
        
        viewHolder.apply {
            var content: View? = null
            if (layoutId != null) {
                try {
                    content = LayoutInflater.from(parent.context)
                        .inflate(layoutId, parent, false)
                } catch (e: Exception) {
                    throw IllegalStateException(
                        "无法加载布局: layoutId=$layoutId，ViewHolder=${vhClass.name}。" +
                        "请检查布局资源是否存在。", e
                    )
                }
            }
            if (content == null) {
                content = onCreateView(parent)
            }
            if (content == null) {
                throw IllegalStateException(
                    "ViewHolder ${vhClass.name} 没有提供布局。" +
                    "请在ViewHolder类上添加@VHLayoutId注解，或重写onCreateView()方法返回View。"
                )
            }
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
        try {
            (holder as HullViewHolder<*>).bindDataInternal(any, position, payloads, onItemClickListener)
        } catch (e: ClassCastException) {
            throw IllegalStateException(
                "ViewHolder类型不匹配。期望: HullViewHolder，实际: ${holder.javaClass.name}。" +
                "这通常是BiSheng内部错误，请检查适配器实现。", e
            )
        } catch (e: Exception) {
            Log.e(TAG, "绑定数据时出错: position=$position, data=${any.javaClass.name}", e)
            throw e
        }
    }
}

