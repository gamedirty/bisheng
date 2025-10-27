package com.sovnem.bisheng

import android.view.View

/**
 * BiSheng ViewHolder 基类
 * 
 * 所有自定义的 ViewHolder 都必须继承此类，并指定泛型数据类型
 * 
 * @param T 数据类型
 * 
 * 使用示例：
 * ```kotlin
 * @VHLayoutId(R.layout.item_layout)
 * class MyViewHolder : BiShengBaseVH<MyData>() {
 *     private val binding by lazy { ItemLayoutBinding.bind(containerView) }
 *     
 *     override fun bindData(
 *         data: MyData,
 *         position: Int,
 *         payloads: MutableList<Any>?,
 *         onItemClickListener: OnItemClickListener?
 *     ) {
 *         binding.textView.text = data.text
 *     }
 * }
 * ```
 */
abstract class BiShengBaseVH<T> {
    
    /**
     * ViewHolder 的根布局 View
     * 在 onCreateView 或通过 @VHLayoutId 注解创建后自动赋值
     */
    lateinit var containerView: View
    
    /**
     * 绑定数据到 ViewHolder
     * 
     * @param data 要绑定的数据
     * @param position 当前项在列表中的位置
     * @param payloads 增量更新的负载数据，如果为空则表示全量更新
     * @param onItemClickListener 点击事件监听器
     */
    abstract fun bindData(
        data: T,
        position: Int,
        payloads: MutableList<Any>?,
        onItemClickListener: OnItemClickListener?,
    )

    /**
     * 自定义创建 View
     * 
     * 当不使用 @VHLayoutId 注解指定布局时，可以重写此方法返回自定义创建的 View
     * 
     * @return 自定义创建的 View，如果返回 null 则必须使用 @VHLayoutId 注解
     */
    open fun onCreateView(): View? {
        return null
    }

}