package com.sovnem.bisheng

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.sovnem.bisheng.feature.product.ProductItem
import com.sovnem.bisheng.feature.user.UserItem
import com.sovnem.bisheng.feature.user.UserStatus
import com.sovnem.bisheng.sample.databinding.ActivityMainBinding
import kotlin.random.Random

/**
 * BiSheng 多模块演示主界面
 * 
 * 演示功能：
 * 1. 多模块类型混合使用
 * 2. DiffUtil 高效更新
 * 3. Payload 增量更新
 * 4. 点击事件处理
 * 5. 数据操作（增删改查）
 * 6. 统计信息展示
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: BiShengAdapter
    
    // 模拟数据源
    private val currentData = mutableListOf<Any>()
    private var userIdCounter = 1
    private var productIdCounter = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 启用调试模式，查看多模块加载信息
        BiShengConfig.isDebugMode = true
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupButtons()
        loadInitialData()
        updateStatistics()
    }

    /**
     * 设置 RecyclerView
     */
    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        
        // 创建适配器并设置点击事件
        adapter = BiShengAdapter(onItemClickListener = { event ->
            handleItemClick(event)
        })
        
        binding.recyclerView.adapter = adapter
    }

    /**
     * 设置按钮点击事件
     */
    private fun setupButtons() {
        // 添加用户
        binding.btnAddUser.setOnClickListener {
            addRandomUser()
        }

        // 添加商品
        binding.btnAddProduct.setOnClickListener {
            addRandomProduct()
        }

        // 使用 DiffUtil 更新数据
        binding.btnUpdateWithDiff.setOnClickListener {
            updateDataWithDiffUtil()
        }

        // 清空列表
        binding.btnClear.setOnClickListener {
            clearData()
        }
        
        // 信息按钮
        binding.fabInfo.setOnClickListener {
            showInfo()
        }
    }

    /**
     * 加载初始数据
     * 展示多模块类型混合使用
     */
    private fun loadInitialData() {
        val initialData = listOf(
            // 用户数据（来自 feature-user 模块）
            UserItem(
                id = "user_${userIdCounter++}",
                name = "张三",
                email = "zhangsan@example.com",
                isVip = true,
                status = UserStatus.ONLINE
            ),
            UserItem(
                id = "user_${userIdCounter++}",
                name = "李四",
                email = "lisi@example.com",
                isVip = false,
                status = UserStatus.BUSY
            ),
            
            // 商品数据（来自 feature-product 模块）
            ProductItem(
                id = "product_${productIdCounter++}",
                title = "iPhone 15 Pro Max",
                description = "256GB 原色钛金属 支持5G",
                price = 9999.0,
                stock = 99,
                sales = 1234,
                rating = 4.8f
            ),
            ProductItem(
                id = "product_${productIdCounter++}",
                title = "MacBook Pro 16",
                description = "M3 Max芯片 64GB内存 2TB存储",
                price = 29999.0,
                stock = 25,
                sales = 567,
                rating = 4.9f
            ),
            
            // 混合更多数据
            UserItem(
                id = "user_${userIdCounter++}",
                name = "王五",
                email = "wangwu@example.com",
                isVip = true,
                status = UserStatus.ONLINE
            ),
            ProductItem(
                id = "product_${productIdCounter++}",
                title = "AirPods Pro 2",
                description = "主动降噪 空间音频",
                price = 1899.0,
                stock = 150,
                sales = 3456,
                rating = 4.7f
            )
        )

        currentData.addAll(initialData)
        adapter.setData(currentData)
        
        showToast("✅ 加载了 ${currentData.size} 条数据（多模块混合）")
    }

    /**
     * 添加随机用户
     */
    private fun addRandomUser() {
        val names = listOf("赵六", "钱七", "孙八", "周九", "吴十")
        val statuses = UserStatus.values()
        
        val user = UserItem(
            id = "user_${userIdCounter++}",
            name = names.random(),
            email = "${names.random().lowercase()}@example.com",
            isVip = Random.nextBoolean(),
            status = statuses.random()
        )
        
        currentData.add(user)
        adapter.addItem(user)
        updateStatistics()
        
        showToast("➕ 添加用户: ${user.name}")
    }

    /**
     * 添加随机商品
     */
    private fun addRandomProduct() {
        val products = listOf(
            "iPad Air" to 4999.0,
            "Apple Watch" to 3199.0,
            "HomePod" to 2299.0,
            "Magic Keyboard" to 2399.0,
            "AirTag" to 229.0
        )
        
        val (title, price) = products.random()
        
        val product = ProductItem(
            id = "product_${productIdCounter++}",
            title = title,
            description = "苹果官方原装正品",
            price = price,
            stock = Random.nextInt(10, 200),
            sales = Random.nextInt(100, 5000),
            rating = Random.nextFloat() * 1 + 4.0f
        )
        
        currentData.add(product)
        adapter.addItem(product)
        updateStatistics()
        
        showToast("➕ 添加商品: ${product.title}")
    }

    /**
     * 使用 DiffUtil 更新数据
     * 展示 DiffUtil 的高效更新和 Payload 增量更新
     */
    private fun updateDataWithDiffUtil() {
        if (currentData.isEmpty()) {
            showToast("⚠️ 列表为空，请先添加数据")
            return
        }
        
        // 创建新数据列表（模拟数据变化）
        val newData = currentData.toMutableList()
        
        var changeCount = 0
        
        // 随机修改一些数据
        newData.forEachIndexed { index, item ->
            if (Random.nextFloat() < 0.3f) { // 30% 概率修改
                when (item) {
                    is UserItem -> {
                        newData[index] = item.copy(
                            status = UserStatus.values().random(),
                            isVip = !item.isVip
                        )
                        changeCount++
                    }
                    is ProductItem -> {
                        newData[index] = item.copy(
                            price = item.price * (0.8 + Random.nextFloat() * 0.4), // ±20%
                            stock = maxOf(0, item.stock + Random.nextInt(-10, 20)),
                            sales = item.sales + Random.nextInt(0, 100)
                        )
                        changeCount++
                    }
                }
            }
        }
        
        // 随机删除一些数据
        if (newData.size > 3 && Random.nextBoolean()) {
            newData.removeAt(Random.nextInt(newData.size))
            changeCount++
        }
        
        // 使用 DiffUtil 更新
        adapter.setData(newData, useDiffUtil = true)
        currentData.clear()
        currentData.addAll(newData)
        updateStatistics()
        
        showSnackbar("🔄 DiffUtil 更新完成！修改了 $changeCount 项")
    }

    /**
     * 清空数据
     */
    private fun clearData() {
        currentData.clear()
        adapter.clearData()
        updateStatistics()
        showToast("🗑️ 已清空所有数据")
    }

    /**
     * 处理列表项点击事件
     */
    private fun handleItemClick(event: ItemClickEvent) {
        val message = when (val data = event.data) {
            is UserItem -> {
                "👤 点击用户: ${data.name}\n" +
                "📧 邮箱: ${data.email}\n" +
                "💎 VIP: ${if (data.isVip) "是" else "否"}\n" +
                "📍 位置: ${event.position}"
            }
            is ProductItem -> {
                "🛍️ 点击商品: ${data.title}\n" +
                "💰 价格: ¥${data.price}\n" +
                "📦 库存: ${data.stock}\n" +
                "📍 位置: ${event.position}"
            }
            else -> "点击了位置 ${event.position}"
        }
        
        showSnackbar(message)
    }

    /**
     * 更新统计信息
     */
    private fun updateStatistics() {
        val userCount = currentData.count { it is UserItem }
        val productCount = currentData.count { it is ProductItem }
        val totalCount = currentData.size
        
        binding.totalCountText.text = totalCount.toString()
        binding.userCountText.text = userCount.toString()
        binding.productCountText.text = productCount.toString()
    }

    /**
     * 显示信息对话框
     */
    private fun showInfo() {
        val info = """
            BiSheng 多模块演示
            
            ✨ 功能展示：
            • 多模块类型混合使用
            • DiffUtil 高效更新
            • Payload 增量更新
            • 点击事件处理
            • ServiceLoader 自动发现
            
            📦 模块说明：
            • feature-user: 用户相关功能
            • feature-product: 商品相关功能
            • sample: 主应用（混合使用）
            
            🎯 操作提示：
            • 点击"添加用户/商品"添加数据
            • 点击"DiffUtil更新"体验高效更新
            • 点击列表项查看详情
            • 观察日志查看模块加载信息
        """.trimIndent()
        
        Snackbar.make(binding.root, info, Snackbar.LENGTH_LONG)
            .setAction("知道了") { }
            .show()
    }

    /**
     * 显示 Toast
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * 显示 Snackbar
     */
    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}
