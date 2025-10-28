package com.sovnem.bisheng.feature.user

import android.view.View
import com.sovnem.bisheng.*
import com.sovnem.bisheng.feature.user.databinding.ItemUserBinding

/**
 * 用户数据项
 * 实现 BiShengDiffable 接口，支持高效的 DiffUtil 更新
 */
@VHRef(UserViewHolder::class)
data class UserItem(
    val id: String,
    val name: String,
    val email: String,
    val isVip: Boolean = false,
    val status: UserStatus = UserStatus.OFFLINE
) : BiShengDiffable {
    
    override fun getItemId(): Any = id
    
    override fun areContentsTheSame(other: Any): Boolean {
        if (other !is UserItem) return false
        return name == other.name && 
               email == other.email && 
               isVip == other.isVip && 
               status == other.status
    }
    
    override fun getChangePayload(other: Any): Any? {
        if (other !is UserItem) return null
        val changes = mutableMapOf<String, Any>()
        
        if (name != other.name) changes["name"] = other.name
        if (email != other.email) changes["email"] = other.email
        if (isVip != other.isVip) changes["vip"] = other.isVip
        if (status != other.status) changes["status"] = other.status
        
        return if (changes.isEmpty()) null else changes
    }
}

/**
 * 用户状态枚举
 */
enum class UserStatus(val displayName: String, val color: Int) {
    ONLINE("在线", android.graphics.Color.parseColor("#4CAF50")),
    BUSY("忙碌", android.graphics.Color.parseColor("#FF9800")),
    OFFLINE("离线", android.graphics.Color.parseColor("#9E9E9E"))
}

/**
 * 用户 ViewHolder
 * 展示生命周期回调和 Payload 增量更新
 * 
 * 注意：在多模块项目中，由于 R 类跨模块问题，
 * 我们使用 onCreateView() 手动加载布局，而不是在注解中指定 layoutId
 */
@VHLayoutId(0)  // 使用 0 表示没有布局ID，通过 onCreateView() 加载
class UserViewHolder : BiShengBaseVH<UserItem>() {
    
    private val binding: ItemUserBinding by lazy {
        ItemUserBinding.bind(containerView)
    }
    
    override fun onCreateView(parent: android.view.ViewGroup): android.view.View {
        return android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
    }
    
    override fun bindData(
        data: UserItem,
        position: Int,
        payloads: MutableList<Any>?,
        onItemClickListener: OnItemClickListener?
    ) {
        // 如果有 payload，执行增量更新
        if (!payloads.isNullOrEmpty()) {
            val payload = payloads[0] as? Map<*, *>
            payload?.let { updateWithPayload(data, it) }
            return
        }
        
        // 全量更新
        binding.apply {
            // 头像显示用户名首字母
            avatarText.text = data.name.firstOrNull()?.toString()?.uppercase() ?: "U"
            
            nameText.text = data.name
            emailText.text = data.email
            
            // 状态显示
            statusText.text = data.status.displayName
            statusText.setTextColor(data.status.color)
            
            // VIP 标签
            vipBadge.visibility = if (data.isVip) View.VISIBLE else View.GONE
            
            // 设置点击事件
            root.setOnClickListener {
                onItemClickListener?.onItemClick(ItemClickEvent(data, position))
            }
        }
    }
    
    /**
     * 使用 Payload 进行增量更新
     * 只更新变化的部分，性能更好
     */
    private fun updateWithPayload(data: UserItem, payload: Map<*, *>) {
        binding.apply {
            payload["name"]?.let { 
                nameText.text = it as String
                avatarText.text = (it as String).firstOrNull()?.toString()?.uppercase() ?: "U"
            }
            payload["email"]?.let { 
                emailText.text = it as String 
            }
            payload["vip"]?.let { 
                vipBadge.visibility = if (it as Boolean) View.VISIBLE else View.GONE 
            }
            payload["status"]?.let { 
                val status = it as UserStatus
                statusText.text = status.displayName
                statusText.setTextColor(status.color)
            }
        }
    }
    
    /**
     * ViewHolder 被回收时调用
     */
    override fun onViewRecycled() {
        super.onViewRecycled()
        // 清理资源，例如取消图片加载
        binding.root.setOnClickListener(null)
    }
    
    /**
     * ViewHolder 附加到窗口时调用
     */
    override fun onViewAttachedToWindow() {
        super.onViewAttachedToWindow()
        // 可以在这里启动动画
    }
    
    /**
     * ViewHolder 从窗口分离时调用
     */
    override fun onViewDetachedFromWindow() {
        super.onViewDetachedFromWindow()
        // 可以在这里暂停动画
    }
}

