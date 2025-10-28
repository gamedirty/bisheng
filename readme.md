# BiSheng - 毕升

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-purple.svg)](https://kotlinlang.org)
[![Android SDK](https://img.shields.io/badge/Android-21%2B-green.svg)](https://developer.android.com)

一个让 RecyclerView 多类型适配器变得简单的 Android 库

## 📖 项目介绍

灵感来自活字印刷术，毕升是活字印刷术的发明人，因此用他的名字来命名。

RecyclerView 作为一个容器，就像是纸张；Adapter 作为数据和 View 的桥梁，负责把数据渲染到容器中。数据就像是活字，活字上的图案就是这个汉字对应的图案。我们需要做的就是提供活字，即不同 type 的数据及这个 type 的数据对应渲染出来的 View。

剩下的一切可以交给 BiSheng 来完成！

## ✨ 特性

- ✅ **零样板代码** - 通过注解处理器自动生成适配器映射代码
- ✅ **类型安全** - 编译时类型检查，避免运行时错误
- ✅ **多类型支持** - 轻松支持多种数据类型的列表
- ✅ **多模块支持** - 完美支持多 module 项目，自动合并类型映射 🆕
- ✅ **DiffUtil 支持** - 高效的数据更新
- ✅ **懒加载** - 支持运行时动态注册类型
- ✅ **ViewBinding 支持** - 与最新的 Android 技术栈兼容
- ✅ **详细的错误提示** - 清晰的编译时和运行时错误信息

## 🚀 快速开始

### 1. 添加依赖

```gradle
// 项目根目录 build.gradle
buildscript {
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.22"
    }
}

// app 模块 build.gradle
dependencies {
    implementation project(':bisheng:library')
    kapt project(':bisheng:compiler')
}
```

### 2. 定义数据类和 ViewHolder

```kotlin
// 1. 定义数据类并使用 @VHRef 注解指定 ViewHolder
@VHRef(PeopleViewHolder::class)
data class PeopleListItem(val name: String, val age: Int)

// 2. 定义 ViewHolder 并继承 BiShengBaseVH<T>，使用 @VHLayoutId 注解指定布局
@VHLayoutId(R.layout.layout_people_item)
class PeopleViewHolder : BiShengBaseVH<PeopleListItem>() {
    
    private val binding: LayoutPeopleItemBinding by lazy {
        LayoutPeopleItemBinding.bind(containerView)
    }
    
    override fun bindData(
        data: PeopleListItem,
        position: Int,
        payloads: MutableList<Any>?,
        onItemClickListener: OnItemClickListener?
    ) {
        binding.name.text = "名字：${data.name}"
        binding.age.text = "年龄：${data.age}"
    }
}

// 支持多种数据类型
@VHRef(AnimalViewHolder::class)
data class AnimalListItem(val animalName: String)

@VHLayoutId(R.layout.layout_animal_item)
class AnimalViewHolder : BiShengBaseVH<AnimalListItem>() {
    override fun bindData(
        data: AnimalListItem,
        position: Int,
        payloads: MutableList<Any>?,
        onItemClickListener: OnItemClickListener?
    ) {
        // 绑定动物数据
    }
}
```

### 3. 使用 Adapter

```kotlin
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 创建混合类型的数据列表
        val dataList = buildList {
            add(PeopleListItem("张三", 25))
            add(AnimalListItem("小狗"))
            add(PeopleListItem("李四", 30))
            add(AnimalListItem("小猫"))
        }
        
        // 创建并设置 Adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = BiShengAdapter(dataList)
    }
}
```

## 📚 进阶用法

### DiffUtil 支持

```kotlin
val adapter = BiShengAdapter()

// 使用 DiffUtil 高效更新数据
adapter.setData(newDataList, useDiffUtil = true)
```

### 数据操作

```kotlin
val adapter = BiShengAdapter()

// 添加数据
adapter.addData(listOf(item1, item2))
adapter.addItem(item)
adapter.addItem(0, item)

// 移除数据
adapter.removeItem(position)
adapter.removeItem(item)

// 更新数据
adapter.updateItem(position, newItem)

// 清空数据
adapter.clearData()

// 获取数据
val item = adapter.getItem(position)
val allData = adapter.getData()
```

### 懒加载类型（运行时注册）

```kotlin
// 使用 lazyLoad = true 延迟注册
@VHRef(DynamicViewHolder::class, lazyLoad = true)
data class DynamicItem(val content: String)

@VHLayoutId(R.layout.layout_dynamic, lazyLoad = true)
class DynamicViewHolder : BiShengBaseVH<DynamicItem>() {
    // ...
}
```

### 点击事件

```kotlin
adapter.setOnItemClickListener { event ->
    val data = event.data
    val position = event.position
    // 处理点击事件
}
```

### 自定义 View（不使用布局文件）

```kotlin
@VHRef(CustomViewHolder::class)
data class CustomItem(val text: String)

class CustomViewHolder : BiShengBaseVH<CustomItem>() {
    
    override fun onCreateView(parent: ViewGroup): View {
        // 返回自定义创建的 View
        return TextView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }
    
    override fun bindData(
        data: CustomItem,
        position: Int,
        payloads: MutableList<Any>?,
        onItemClickListener: OnItemClickListener?
    ) {
        (containerView as TextView).text = data.text
    }
}
```

### 🏗️ 多模块项目支持

BiSheng 完美支持多 module 项目！每个 module 可以独立定义自己的数据类型和 ViewHolder，运行时会自动合并。

#### 配置方式

在**每个需要使用 BiSheng 的 module** 中添加依赖：

```kotlin
// feature-user/build.gradle.kts
plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")  // ⚠️ 必须添加
}

dependencies {
    implementation(project(":bisheng:library"))
    kapt(project(":bisheng:compiler"))  // ⚠️ 每个模块都要添加
}
```

#### 使用示例

**feature-user 模块：**

```kotlin
@VHRef(UserViewHolder::class)
data class UserItem(val name: String)

@VHLayoutId(R.layout.item_user)
class UserViewHolder : BiShengBaseVH<UserItem>() {
    // ...
}
```

**feature-order 模块：**

```kotlin
@VHRef(OrderViewHolder::class)
data class OrderItem(val orderId: String)

@VHLayoutId(R.layout.item_order)
class OrderViewHolder : BiShengBaseVH<OrderItem>() {
    // ...
}
```

**在 app 模块中混合使用：**

```kotlin
// 可以混合使用不同模块的数据类型！
val mixedData = listOf(
    UserItem("张三"),      // 来自 feature-user
    OrderItem("ORD123"),   // 来自 feature-order
    UserItem("李四"),      // 来自 feature-user
    OrderItem("ORD124")    // 来自 feature-order
)

val adapter = BiShengAdapter(mixedData)
```

**工作原理：**

1. 每个 module 生成唯一命名的映射类（如 `BiShengAdapterMapImpl_user`）
2. 使用 Java ServiceLoader 自动发现所有模块的映射
3. 运行时自动合并所有映射关系

详细说明请查看 [多模块支持文档](多模块支持说明.md)。

### 🎛️ 配置选项

BiSheng 提供了全局配置来控制日志和行为：

```kotlin
// 在 Application 中配置
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 启用调试模式，输出详细日志
        BiShengConfig.isDebugMode = BuildConfig.DEBUG
        
        // 启用严格模式，进行更严格的运行时检查
        BiShengConfig.isStrictMode = BuildConfig.DEBUG
    }
}
```

### 🔄 ViewHolder 生命周期回调

BiSheng 支持 ViewHolder 生命周期回调，方便管理资源：

```kotlin
@VHLayoutId(R.layout.item_video)
class VideoViewHolder : BiShengBaseVH<VideoItem>() {
    
    private val videoPlayer = VideoPlayer()
    
    override fun bindData(
        data: VideoItem,
        position: Int,
        payloads: MutableList<Any>?,
        onItemClickListener: OnItemClickListener?
    ) {
        videoPlayer.load(data.videoUrl)
    }
    
    // 当 ViewHolder 被回收时调用
    override fun onViewRecycled() {
        videoPlayer.release()
    }
    
    // 当 ViewHolder 附加到窗口时调用
    override fun onViewAttachedToWindow() {
        videoPlayer.resume()
    }
    
    // 当 ViewHolder 从窗口分离时调用
    override fun onViewDetachedFromWindow() {
        videoPlayer.pause()
    }
    
    // 当绑定失败时调用
    override fun onBindFailed(e: Exception) {
        // 显示错误状态
        binding.errorView.visibility = View.VISIBLE
    }
}
```

## 🔧 注意事项

1. **必须配置 kapt**：确保在 app 模块中添加了 `kapt` 插件（如果不使用懒加载模式）
2. **ViewHolder 必须有无参构造函数**：ViewHolder 类不能是内部类或抽象类
3. **数据类必须使用 @VHRef 注解**：每个数据类都需要指定对应的 ViewHolder
4. **ViewHolder 必须继承 BiShengBaseVH**：并使用泛型指定数据类型
5. **调试建议**：开发时启用 `BiShengConfig.isDebugMode` 查看详细日志

### 💡 增强的 DiffUtil 支持

数据类可以实现 `BiShengDiffable` 接口来自定义比较逻辑：

```kotlin
@VHRef(UserViewHolder::class)
data class UserItem(
    val id: String,
    val name: String,
    val avatar: String
) : BiShengDiffable {
    
    override fun getItemId(): Any = id
    
    override fun areContentsTheSame(other: Any): Boolean {
        if (other !is UserItem) return false
        return name == other.name && avatar == other.avatar
    }
    
    override fun getChangePayload(other: Any): Any? {
        if (other !is UserItem) return null
        val changes = mutableMapOf<String, Any>()
        if (name != other.name) changes["name"] = other.name
        if (avatar != other.avatar) changes["avatar"] = other.avatar
        return if (changes.isEmpty()) null else changes
    }
}
```

使用增强的 DiffUtil：
```kotlin
adapter.setData(newDataList, useDiffUtil = true)
// BiShengDiffable 接口会自动被使用，实现精确的增量更新
```

## 📝 更新日志

### v2.1.0 (2025-10-28)

**🎉 多模块支持：**
- ✅ **完美支持多 module 项目** - 每个模块自动生成唯一的映射类
- ✅ **ServiceLoader 自动发现** - 运行时自动合并所有模块的类型映射
- ✅ **零配置使用** - 只需在每个模块添加 kapt 依赖即可
- ✅ **性能优化** - 修复反射 API 废弃警告
- ✅ **向后兼容** - 单模块项目无需任何修改

### v2.0.0 (2025)

**高优先级改进：**
- ✅ 升级到 Kotlin 1.9.22
- ✅ 升级到 Android SDK 34
- ✅ 升级 Gradle 到 8.9，AGP 到 8.2.2
- ✅ 升级到 Java 11
- ✅ 统一使用 Kotlin DSL 构建脚本
- ✅ 增强 DiffUtil 支持 - 添加 `BiShengDiffable` 接口
- ✅ 注解改为 RUNTIME 保留，支持更灵活的使用场景
- ✅ 改进 `onCreateView()` 方法签名，传入 parent 参数
- ✅ 优化依赖版本管理

**中优先级改进：**
- ✅ 优化注解处理器代码质量 - 使用 JavaPoet 的 ClassName API
- ✅ 添加 ViewHolder 完整生命周期回调
  - `onViewRecycled()` - ViewHolder 被回收时
  - `onViewAttachedToWindow()` - ViewHolder 附加到窗口时
  - `onViewDetachedFromWindow()` - ViewHolder 从窗口分离时
  - `onBindFailed(Exception)` - 绑定失败时
- ✅ 改进错误处理和日志系统
  - 添加详细的错误信息和解决建议
  - 支持调试模式 - `BiShengConfig.isDebugMode`
  - 支持严格模式 - `BiShengConfig.isStrictMode`
  - 条件日志输出，避免生产环境性能损失
- ✅ 增强类型安全性
  - 编译时类型检查优化
  - 运行时验证 ViewHolder 合法性
  - 更好的异常提示信息

### v1.0.0
- ✅ 升级到 Kotlin 1.8.22
- ✅ 移除已废弃的 kotlin-android-extensions
- ✅ 支持 ViewBinding
- ✅ 添加 DiffUtil 支持
- ✅ 完善错误处理和日志
- ✅ 添加更多实用方法
- ✅ 修复多个 bug

## 📄 License

```
Copyright 2025 BiSheng

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
