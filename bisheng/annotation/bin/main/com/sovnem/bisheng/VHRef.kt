package com.sovnem.bisheng

import kotlin.reflect.KClass

/**
 * ViewHolder 引用注解
 * 
 * 用于在数据类上指定对应的 ViewHolder 类
 * 
 * @param ref ViewHolder 类的引用，必须继承自 BiShengBaseVH<T>
 * @param lazyLoad 是否延迟加载。
 *                 false: 在编译时注册，性能更好（默认）
 *                 true: 在运行时首次使用时注册，适合动态类型
 * 
 * 使用示例：
 * ```kotlin
 * @VHRef(PeopleViewHolder::class)
 * data class PeopleListItem(val name: String, val age: Int)
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class VHRef(val ref: KClass<*>, val lazyLoad: Boolean = false)