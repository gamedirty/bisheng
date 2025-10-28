package com.sovnem.bisheng

/**
 * 可差异化接口
 * 
 * 数据类实现此接口以提供自定义的 DiffUtil 比较逻辑
 * 
 * 实现此接口可以：
 * 1. 提供稳定的唯一标识符，用于判断是否是同一项
 * 2. 自定义内容比较逻辑，用于判断内容是否发生变化
 * 3. 提供增量更新的负载数据
 * 
 * 示例：
 * ```kotlin
 * @VHRef(UserViewHolder::class)
 * data class UserItem(
 *     val id: String,
 *     val name: String,
 *     val avatar: String
 * ) : BiShengDiffable {
 *     
 *     override fun getItemId(): Any = id
 *     
 *     override fun areContentsTheSame(other: Any): Boolean {
 *         if (other !is UserItem) return false
 *         return name == other.name && avatar == other.avatar
 *     }
 *     
 *     override fun getChangePayload(other: Any): Any? {
 *         if (other !is UserItem) return null
 *         val changes = mutableMapOf<String, Any>()
 *         if (name != other.name) changes["name"] = other.name
 *         if (avatar != other.avatar) changes["avatar"] = other.avatar
 *         return if (changes.isEmpty()) null else changes
 *     }
 * }
 * ```
 */
interface BiShengDiffable {
    
    /**
     * 返回此项的唯一标识符
     * 
     * DiffUtil 使用此方法判断两个项是否代表同一个对象。
     * 通常返回数据的唯一 ID（如数据库主键、服务器 ID 等）。
     * 
     * 注意：
     * - 必须返回稳定的标识符，同一对象的多次调用应返回相同的值
     * - 不同对象应该返回不同的标识符
     * 
     * @return 唯一标识符，推荐使用 String、Long、Int 等基本类型
     */
    fun getItemId(): Any
    
    /**
     * 判断内容是否相同
     * 
     * 当 DiffUtil 确定两项代表同一对象后（通过 getItemId），
     * 会调用此方法判断内容是否发生了变化。
     * 
     * 如果返回 true，则不会触发视图更新；
     * 如果返回 false，则会调用 getChangePayload 获取变化内容，并触发视图更新。
     * 
     * @param other 另一个要比较的对象
     * @return true 表示内容完全相同，false 表示内容有变化
     */
    fun areContentsTheSame(other: Any): Boolean
    
    /**
     * 获取变化的负载数据
     * 
     * 当内容不相同时，此方法会被调用以获取具体的变化信息。
     * ViewHolder 可以在 bindData 方法中通过 payloads 参数获取这些变化，
     * 从而实现增量更新，避免重新绑定所有数据。
     * 
     * @param other 新的对象
     * @return 变化的负载数据，可以是任何对象；返回 null 则执行全量更新
     */
    fun getChangePayload(other: Any): Any? = null
}

