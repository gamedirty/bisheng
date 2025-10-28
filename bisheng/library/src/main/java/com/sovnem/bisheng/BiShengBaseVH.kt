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
     * @param parent 父容器，用于获取 Context 和创建 View
     * @return 自定义创建的 View，如果返回 null 则必须使用 @VHLayoutId 注解
     */
    open fun onCreateView(parent: android.view.ViewGroup): View? {
        return null
    }
    
    /**
     * 当 ViewHolder 被回收时调用
     * 
     * 可以在此方法中释放资源、取消订阅、清理引用等
     * 
     * 使用示例：
     * ```kotlin
     * override fun onViewRecycled() {
     *     // 取消图片加载
     *     imageLoader.cancel(binding.imageView)
     *     // 清理动画
     *     binding.root.clearAnimation()
     * }
     * ```
     */
    open fun onViewRecycled() {
        // 子类可重写此方法进行清理
    }
    
    /**
     * 当 ViewHolder 附加到窗口时调用
     * 
     * 可以在此方法中启动动画、开始监听等
     * 
     * 使用示例：
     * ```kotlin
     * override fun onViewAttachedToWindow() {
     *     // 启动动画
     *     binding.root.startAnimation(fadeInAnimation)
     * }
     * ```
     */
    open fun onViewAttachedToWindow() {
        // 子类可重写此方法
    }
    
    /**
     * 当 ViewHolder 从窗口分离时调用
     * 
     * 可以在此方法中停止动画、暂停视频播放等
     * 
     * 使用示例：
     * ```kotlin
     * override fun onViewDetachedFromWindow() {
     *     // 停止动画
     *     binding.root.clearAnimation()
     *     // 暂停视频
     *     videoPlayer.pause()
     * }
     * ```
     */
    open fun onViewDetachedFromWindow() {
        // 子类可重写此方法
    }
    
    /**
     * 当 ViewHolder 绑定失败时调用
     * 
     * 可以在此方法中处理绑定异常，显示错误状态等
     * 
     * @param e 绑定时发生的异常
     */
    open fun onBindFailed(e: Exception) {
        // 子类可重写此方法处理绑定失败
    }

}