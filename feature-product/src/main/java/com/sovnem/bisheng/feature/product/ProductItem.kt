package com.sovnem.bisheng.feature.product

import com.sovnem.bisheng.*
import com.sovnem.bisheng.feature.product.databinding.ItemProductBinding
import java.text.NumberFormat
import java.util.*

/**
 * 商品数据项
 * 实现 BiShengDiffable 接口，支持高效的 DiffUtil 更新
 */
@VHRef(ProductViewHolder::class)
data class ProductItem(
    val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val stock: Int,
    val sales: Int,
    val rating: Float
) : BiShengDiffable {
    
    override fun getItemId(): Any = id
    
    override fun areContentsTheSame(other: Any): Boolean {
        if (other !is ProductItem) return false
        return title == other.title &&
               description == other.description &&
               price == other.price &&
               stock == other.stock &&
               sales == other.sales &&
               rating == other.rating
    }
    
    override fun getChangePayload(other: Any): Any? {
        if (other !is ProductItem) return null
        val changes = mutableMapOf<String, Any>()
        
        if (title != other.title) changes["title"] = other.title
        if (description != other.description) changes["description"] = other.description
        if (price != other.price) changes["price"] = other.price
        if (stock != other.stock) changes["stock"] = other.stock
        if (sales != other.sales) changes["sales"] = other.sales
        if (rating != other.rating) changes["rating"] = other.rating
        
        return if (changes.isEmpty()) null else changes
    }
}

/**
 * 商品 ViewHolder
 * 展示 DiffUtil 的 Payload 增量更新功能
 */
@VHLayoutId(lazyLoad = false)
class ProductViewHolder : BiShengBaseVH<ProductItem>() {
    
    private val binding: ItemProductBinding by lazy {
        ItemProductBinding.bind(containerView)
    }
    
    private val priceFormatter = NumberFormat.getCurrencyInstance(Locale.CHINA)
    
    override fun onCreateView(parent: android.view.ViewGroup): android.view.View {
        return android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
    }
    
    override fun bindData(
        data: ProductItem,
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
            titleText.text = data.title
            descriptionText.text = data.description
            priceText.text = priceFormatter.format(data.price)
            stockText.text = "库存: ${data.stock}"
            salesText.text = "已售 ${formatSales(data.sales)}"
            ratingText.text = "⭐ ${String.format("%.1f", data.rating)}"
            
            // 设置点击事件
            root.setOnClickListener {
                onItemClickListener?.invoke(ItemClickEvent(data, position))
            }
        }
    }
    
    /**
     * 使用 Payload 进行增量更新
     */
    private fun updateWithPayload(data: ProductItem, payload: Map<*, *>) {
        binding.apply {
            payload["title"]?.let { 
                titleText.text = it as String 
            }
            payload["description"]?.let { 
                descriptionText.text = it as String 
            }
            payload["price"]?.let { 
                priceText.text = priceFormatter.format(it as Double) 
            }
            payload["stock"]?.let { 
                stockText.text = "库存: ${it as Int}" 
            }
            payload["sales"]?.let { 
                salesText.text = "已售 ${formatSales(it as Int)}" 
            }
            payload["rating"]?.let { 
                ratingText.text = "⭐ ${String.format("%.1f", it as Float)}" 
            }
        }
    }
    
    /**
     * 格式化销量显示
     */
    private fun formatSales(sales: Int): String {
        return when {
            sales >= 10000 -> "${sales / 10000}万+"
            sales >= 1000 -> "${sales / 1000}k+"
            else -> sales.toString()
        }
    }
    
    override fun onViewRecycled() {
        super.onViewRecycled()
        binding.root.setOnClickListener(null)
    }
}

