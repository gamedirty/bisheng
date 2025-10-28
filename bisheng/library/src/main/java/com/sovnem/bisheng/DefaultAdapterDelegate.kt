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

    /**
     * 使用 ServiceLoader 加载所有模块的适配器映射
     * 支持多模块项目
     */
    private val adapterMaps: List<IAdapterMap> by lazy {
        try {
            val loader = java.util.ServiceLoader.load(
                IAdapterMap::class.java, 
                IAdapterMap::class.java.classLoader
            )
            val maps = loader.toList()
            
            if (maps.isEmpty()) {
                // 如果 ServiceLoader 没找到，尝试加载默认类（向后兼容）
                try {
                    val defaultMap = Class.forName(Constants.PACKAGE + "." + Constants.CLASS_NAME)
                        .getDeclaredConstructor()
                        .newInstance() as IAdapterMap
                    listOf(defaultMap)
                } catch (e: Exception) {
                    throw IllegalStateException(
                        "无法找到任何适配器映射类。\n" +
                        "请确保已经正确配置 kapt 注解处理器，并且至少有一个数据类使用了 @VHRef 注解。\n" +
                        "如果使用多模块项目，请确保每个模块都正确配置了 kapt。", e
                    )
                }
            } else {
                if (BiShengConfig.isDebugMode) {
                    Log.d(TAG, "通过 ServiceLoader 加载了 ${maps.size} 个模块的适配器映射")
                }
                maps
            }
        } catch (e: Exception) {
            throw IllegalStateException(
                "加载适配器映射时出错: ${e.message}\n" +
                "请检查项目配置和注解处理器是否正常工作。", e
            )
        }
    }

    /**
     * [data,itemType]
     * 合并所有模块的类型映射
     */
    private val typeMap: ArrayMap<Class<*>, Int> by lazy {
        val mergedMap = ArrayMap<Class<*>, Int>()
        adapterMaps.forEach { map ->
            mergedMap.putAll(map.dataToType)
        }
        if (BiShengConfig.isDebugMode) {
            Log.d(TAG, "合并后的类型映射共 ${mergedMap.size} 个")
        }
        mergedMap
    }

    /**
     * [itemType,ViewHolder]
     * 合并所有模块的 ViewHolder 映射
     */
    private val vhMap: SparseArray<Class<*>> by lazy {
        val mergedMap = SparseArray<Class<*>>()
        adapterMaps.forEach { map ->
            val typeToVH = map.typeToViewHolder
            for (i in 0 until typeToVH.size()) {
                val key = typeToVH.keyAt(i)
                val value = typeToVH.valueAt(i)
                mergedMap.put(key, value)
            }
        }
        if (BiShengConfig.isDebugMode) {
            Log.d(TAG, "合并后的 ViewHolder 映射共 ${mergedMap.size()} 个")
        }
        mergedMap
    }


    /**
     * [ViewHolder,layoutRes]
     * 合并所有模块的布局资源映射
     */
    private val vhLayoutMap: ArrayMap<Class<*>, Int> by lazy {
        val mergedMap = ArrayMap<Class<*>, Int>()
        adapterMaps.forEach { map ->
            mergedMap.putAll(map.viewHolderToLayoutRes)
        }
        if (BiShengConfig.isDebugMode) {
            Log.d(TAG, "合并后的布局映射共 ${mergedMap.size} 个")
        }
        mergedMap
    }

    companion object {
        private const val TAG = BiShengConfig.TAG
        
        @JvmStatic
        fun getTypeOf(clazz: Class<*>): Int {
            return innerTypeOf(clazz)
        }

        private fun innerTypeOf(clazz: Class<*>): Int {
            return clazz.hashCode()
        }
        
        // 条件日志：仅在 debug 模式下输出
        private inline fun logD(message: () -> String) {
            if (BiShengConfig.isDebugMode) {
                Log.d(TAG, message())
            }
        }
        
        private inline fun logE(message: String, throwable: Throwable? = null) {
            Log.e(TAG, message, throwable)
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
                    "数据类 ${this.name} 没有使用 @VHRef 注解。\n" +
                    "请在数据类上添加 @VHRef(YourViewHolder::class) 注解。\n\n" +
                    "示例：\n" +
                    "@VHRef(MyViewHolder::class)\n" +
                    "data class MyData(...)"
                )
            
            // 验证 ViewHolder 类
            val vhClass = vhAno.ref.java
            
            // 1. 检查是否继承 BiShengBaseVH
            if (!BiShengBaseVH::class.java.isAssignableFrom(vhClass)) {
                throw IllegalStateException(
                    "ViewHolder ${vhAno.ref.qualifiedName} 必须继承 BiShengBaseVH<T>。\n\n" +
                    "示例：\n" +
                    "class MyViewHolder : BiShengBaseVH<MyData>() { ... }"
                )
            }
            
            // 2. 检查 ViewHolder 是否有无参构造函数
            try {
                vhClass.getConstructor()
            } catch (e: NoSuchMethodException) {
                throw IllegalStateException(
                    "ViewHolder ${vhClass.name} 缺少无参构造函数。\n" +
                    "请确保 ViewHolder 类不是内部类，并且有一个公共的无参构造函数。", e
                )
            }
            
            // 3. 严格模式：检查 ViewHolder 是否有 @VHLayoutId 注解或重写了 onCreateView
            if (BiShengConfig.isStrictMode) {
                val hasLayoutId = vhClass.isAnnotationPresent(VHLayoutId::class.java)
                val hasOnCreateView = try {
                    val method = vhClass.getMethod("onCreateView", android.view.ViewGroup::class.java)
                    method.declaringClass != BiShengBaseVH::class.java
                } catch (e: Exception) {
                    false
                }
                
                if (!hasLayoutId && !hasOnCreateView) {
                    Log.w(TAG, "警告: ViewHolder ${vhClass.simpleName} 既没有 @VHLayoutId 注解，" +
                            "也没有重写 onCreateView() 方法，运行时可能会失败。")
                }
            }
            
            val type = innerTypeOf(this)
            vhMap.put(type, vhClass)
            
            if (BiShengConfig.isDebugMode) {
                Log.d(TAG, "注册懒加载类型: ${this.simpleName} -> ${vhAno.ref.simpleName} (type=$type)")
            }
            
            return type
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HullViewHolder<*> {
        if (BiShengConfig.isDebugMode) {
            Log.d(TAG, "创建 ViewHolder: viewType=$viewType")
        }
        
        val vhClass = vhMap.get(viewType)
        if (vhClass == null) {
            val errorMsg = "无法找到 viewType=$viewType 对应的 ViewHolder 类。\n" +
                    "可能的原因：\n" +
                    "1. 数据类没有使用 @VHRef 注解\n" +
                    "2. ViewHolder 类没有使用 @VHLayoutId 注解\n" +
                    "3. 使用了懒加载但首次访问时注册失败"
            Log.e(TAG, errorMsg)
            throw IllegalStateException(errorMsg)
        }
        
        if (BiShengConfig.isDebugMode) {
            Log.d(TAG, "找到 ViewHolder 类: ${vhClass.simpleName}")
        }
        
        val layoutId = vhLayoutMap[vhClass] ?: vhClass.getAnnotation(VHLayoutId::class.java)?.layoutId
        if (BiShengConfig.isDebugMode && layoutId != null && layoutId != 0) {
            Log.d(TAG, "使用布局资源: layoutId=$layoutId")
        }
        
        val viewHolder = try {
            vhClass.getConstructor().newInstance() as BiShengBaseVH<*>
        } catch (e: NoSuchMethodException) {
            val errorMsg = "ViewHolder ${vhClass.name} 缺少无参构造函数。\n" +
                    "请确保：\n" +
                    "1. ViewHolder 类不是内部类（使用 inner class）\n" +
                    "2. ViewHolder 类有一个公共的无参构造函数"
            Log.e(TAG, errorMsg, e)
            throw IllegalStateException(errorMsg, e)
        } catch (e: Exception) {
            val errorMsg = "无法实例化 ViewHolder: ${vhClass.name}。\n" +
                    "错误: ${e.message}\n" +
                    "请检查 ViewHolder 类的构造函数是否可访问。"
            Log.e(TAG, errorMsg, e)
            throw IllegalStateException(errorMsg, e)
        }
        
        viewHolder.apply {
            var content: View? = null
            
            // 尝试通过布局 ID 加载
            if (layoutId != null && layoutId != 0) {
                try {
                    content = LayoutInflater.from(parent.context)
                        .inflate(layoutId, parent, false)
                    if (BiShengConfig.isDebugMode) {
                        Log.d(TAG, "成功加载布局: layoutId=$layoutId")
                    }
                } catch (e: Exception) {
                    val errorMsg = "无法加载布局资源。\n" +
                            "layoutId: $layoutId\n" +
                            "ViewHolder: ${vhClass.name}\n" +
                            "错误: ${e.message}\n" +
                            "请检查布局资源 ID 是否正确。"
                    Log.e(TAG, errorMsg, e)
                    throw IllegalStateException(errorMsg, e)
                }
            }
            
            // 尝试通过 onCreateView 创建
            if (content == null) {
                try {
                    content = onCreateView(parent)
                    if (content != null && BiShengConfig.isDebugMode) {
                        Log.d(TAG, "通过 onCreateView() 创建 View")
                    }
                } catch (e: Exception) {
                    val errorMsg = "调用 onCreateView() 时出错。\n" +
                            "ViewHolder: ${vhClass.name}\n" +
                            "错误: ${e.message}"
                    Log.e(TAG, errorMsg, e)
                    throw IllegalStateException(errorMsg, e)
                }
            }
            
            // 检查是否成功创建 View
            if (content == null) {
                val errorMsg = "ViewHolder ${vhClass.name} 没有提供布局。\n" +
                        "请执行以下操作之一：\n" +
                        "1. 在 ViewHolder 类上添加 @VHLayoutId 注解指定布局 ID\n" +
                        "2. 重写 onCreateView(parent) 方法返回自定义 View"
                Log.e(TAG, errorMsg)
                throw IllegalStateException(errorMsg)
            }
            
            this.containerView = content
            if (BiShengConfig.isDebugMode) {
                Log.d(TAG, "ViewHolder 创建成功: ${vhClass.simpleName}")
            }
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
            if (holder !is HullViewHolder<*>) {
                val errorMsg = "ViewHolder 类型不匹配。\n" +
                        "期望: HullViewHolder\n" +
                        "实际: ${holder.javaClass.name}\n" +
                        "这通常是 BiSheng 内部错误，请检查适配器实现。"
                Log.e(TAG, errorMsg)
                throw IllegalStateException(errorMsg)
            }
            
            if (BiShengConfig.isDebugMode) {
                Log.d(TAG, "绑定数据: position=$position, dataType=${any.javaClass.simpleName}, " +
                        "viewType=${holder.itemViewType}, " +
                        "hasPayloads=${payloads?.isNotEmpty() ?: false}")
            }
            
            holder.bindDataInternal(any, position, payloads, onItemClickListener)
        } catch (e: Exception) {
            Log.e(TAG, "绑定数据时出错:\n" +
                    "  位置: position=$position\n" +
                    "  数据类型: ${any.javaClass.name}\n" +
                    "  ViewHolder: ${holder.javaClass.name}\n" +
                    "  错误: ${e.message}", e)
            throw e
        }
    }
}

