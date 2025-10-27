package com.sovnem.bisheng

/**
 * ViewHolder 布局资源 ID 注解
 * 
 * 用于在 ViewHolder 类上指定对应的布局资源 ID
 * 
 * @param layoutId 布局资源 ID，例如 R.layout.item_layout
 * @param lazyLoad 是否延迟加载。
 *                 false: 在编译时注册，性能更好（默认）
 *                 true: 在运行时首次使用时注册，适合动态类型
 * 
 * 使用示例：
 * ```kotlin
 * @VHLayoutId(R.layout.layout_people_item)
 * class PeopleViewHolder : BiShengBaseVH<PeopleListItem>() {
 *     // ...
 * }
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class VHLayoutId(val layoutId: Int, val lazyLoad: Boolean = false)