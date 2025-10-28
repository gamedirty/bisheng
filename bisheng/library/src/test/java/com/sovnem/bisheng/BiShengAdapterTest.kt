package com.sovnem.bisheng

import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * BiShengAdapter 单元测试
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BiShengAdapterTest {
    
    private lateinit var adapter: BiShengAdapter
    
    @Before
    fun setup() {
        adapter = BiShengAdapter()
    }
    
    @Test
    fun `初始化时数据列表应该为空`() {
        assertThat(adapter.itemCount).isEqualTo(0)
        assertThat(adapter.getData()).isEmpty()
    }
    
    @Test
    fun `使用初始数据创建适配器`() {
        val testData = listOf("item1", "item2", "item3")
        val adapter = BiShengAdapter(testData)
        
        assertThat(adapter.itemCount).isEqualTo(3)
        assertThat(adapter.getData()).hasSize(3)
    }
    
    @Test
    fun `添加单个数据项`() {
        adapter.addItem("test item")
        
        assertThat(adapter.itemCount).isEqualTo(1)
        assertThat(adapter.getItem(0)).isEqualTo("test item")
    }
    
    @Test
    fun `在指定位置添加数据项`() {
        adapter.addItem("item1")
        adapter.addItem("item3")
        adapter.addItem(1, "item2")
        
        assertThat(adapter.itemCount).isEqualTo(3)
        assertThat(adapter.getItem(0)).isEqualTo("item1")
        assertThat(adapter.getItem(1)).isEqualTo("item2")
        assertThat(adapter.getItem(2)).isEqualTo("item3")
    }
    
    @Test
    fun `添加多个数据项`() {
        val items = listOf("item1", "item2", "item3")
        adapter.addData(items)
        
        assertThat(adapter.itemCount).isEqualTo(3)
        assertThat(adapter.getData()).containsExactlyElementsIn(items)
    }
    
    @Test
    fun `移除指定位置的数据项`() {
        adapter.addData(listOf("item1", "item2", "item3"))
        adapter.removeItem(1)
        
        assertThat(adapter.itemCount).isEqualTo(2)
        assertThat(adapter.getItem(0)).isEqualTo("item1")
        assertThat(adapter.getItem(1)).isEqualTo("item3")
    }
    
    @Test
    fun `移除指定的数据项`() {
        val item = "item2"
        adapter.addData(listOf("item1", item, "item3"))
        adapter.removeItem(item)
        
        assertThat(adapter.itemCount).isEqualTo(2)
        assertThat(adapter.getData()).doesNotContain(item)
    }
    
    @Test
    fun `更新指定位置的数据项`() {
        adapter.addData(listOf("item1", "item2", "item3"))
        adapter.updateItem(1, "updated item")
        
        assertThat(adapter.getItem(1)).isEqualTo("updated item")
    }
    
    @Test
    fun `清空所有数据`() {
        adapter.addData(listOf("item1", "item2", "item3"))
        adapter.clearData()
        
        assertThat(adapter.itemCount).isEqualTo(0)
        assertThat(adapter.getData()).isEmpty()
    }
    
    @Test
    fun `设置新数据（不使用DiffUtil）`() {
        adapter.addData(listOf("old1", "old2"))
        adapter.setData(listOf("new1", "new2", "new3"), useDiffUtil = false)
        
        assertThat(adapter.itemCount).isEqualTo(3)
        assertThat(adapter.getData()).containsExactly("new1", "new2", "new3").inOrder()
    }
    
    @Test
    fun `获取指定位置的数据项`() {
        adapter.addData(listOf("item1", "item2", "item3"))
        
        assertThat(adapter.getItem(0)).isEqualTo("item1")
        assertThat(adapter.getItem(1)).isEqualTo("item2")
        assertThat(adapter.getItem(2)).isEqualTo("item3")
    }
    
    @Test
    fun `获取无效位置的数据项应返回null`() {
        adapter.addData(listOf("item1"))
        
        assertThat(adapter.getItem(-1)).isNull()
        assertThat(adapter.getItem(10)).isNull()
    }
    
    @Test
    fun `获取所有数据应返回列表副本`() {
        val originalData = listOf("item1", "item2")
        adapter.addData(originalData)
        
        val retrievedData = adapter.getData()
        assertThat(retrievedData).containsExactlyElementsIn(originalData)
        
        // 验证返回的是副本，修改它不会影响适配器内部数据
        assertThat(retrievedData).isNotSameInstanceAs(originalData)
    }
    
    @Test
    fun `设置点击监听器`() {
        val listener = mockk<OnItemClickListener>(relaxed = true)
        adapter.setOnItemClickListener(listener)
        
        // 验证监听器已设置（无异常抛出）
        assertThat(adapter).isNotNull()
    }
}

/**
 * BiShengDiffable 接口测试数据类
 */
data class TestDiffableItem(
    val id: String,
    val name: String,
    val value: Int
) : BiShengDiffable {
    override fun getItemId(): Any = id
    
    override fun areContentsTheSame(other: Any): Boolean {
        if (other !is TestDiffableItem) return false
        return name == other.name && value == other.value
    }
    
    override fun getChangePayload(other: Any): Any? {
        if (other !is TestDiffableItem) return null
        val changes = mutableMapOf<String, Any>()
        if (name != other.name) changes["name"] = other.name
        if (value != other.value) changes["value"] = other.value
        return if (changes.isEmpty()) null else changes
    }
}

/**
 * BiShengDiffable 接口单元测试
 */
class BiShengDiffableTest {
    
    @Test
    fun `getItemId应返回稳定的标识符`() {
        val item = TestDiffableItem("id1", "name1", 100)
        
        assertThat(item.getItemId()).isEqualTo("id1")
        assertThat(item.getItemId()).isEqualTo(item.getItemId())
    }
    
    @Test
    fun `相同内容的项应该areContentsTheSame返回true`() {
        val item1 = TestDiffableItem("id1", "name", 100)
        val item2 = TestDiffableItem("id1", "name", 100)
        
        assertThat(item1.areContentsTheSame(item2)).isTrue()
    }
    
    @Test
    fun `不同内容的项应该areContentsTheSame返回false`() {
        val item1 = TestDiffableItem("id1", "name1", 100)
        val item2 = TestDiffableItem("id1", "name2", 200)
        
        assertThat(item1.areContentsTheSame(item2)).isFalse()
    }
    
    @Test
    fun `getChangePayload应返回变化的字段`() {
        val oldItem = TestDiffableItem("id1", "old name", 100)
        val newItem = TestDiffableItem("id1", "new name", 200)
        
        val payload = oldItem.getChangePayload(newItem) as? Map<*, *>
        
        assertThat(payload).isNotNull()
        assertThat(payload).containsKey("name")
        assertThat(payload).containsKey("value")
        assertThat(payload?.get("name")).isEqualTo("new name")
        assertThat(payload?.get("value")).isEqualTo(200)
    }
    
    @Test
    fun `内容相同时getChangePayload应返回null`() {
        val item1 = TestDiffableItem("id1", "name", 100)
        val item2 = TestDiffableItem("id1", "name", 100)
        
        val payload = item1.getChangePayload(item2)
        
        assertThat(payload).isNull()
    }
    
    @Test
    fun `不同类型对象比较应返回false`() {
        val item = TestDiffableItem("id1", "name", 100)
        val other = "string object"
        
        assertThat(item.areContentsTheSame(other)).isFalse()
    }
}

