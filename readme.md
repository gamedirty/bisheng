# BiSheng - 毕升

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.8.22-purple.svg)](https://kotlinlang.org)

一个让 RecyclerView 多类型适配器变得简单的 Android 库

## 📖 项目介绍

灵感来自活字印刷术，毕升是活字印刷术的发明人，因此用他的名字来命名。

RecyclerView 作为一个容器，就像是纸张；Adapter 作为数据和 View 的桥梁，负责把数据渲染到容器中。数据就像是活字，活字上的图案就是这个汉字对应的图案。我们需要做的就是提供活字，即不同 type 的数据及这个 type 的数据对应渲染出来的 View。

剩下的一切可以交给 BiSheng 来完成！

## ✨ 特性

- ✅ **零样板代码** - 通过注解处理器自动生成适配器映射代码
- ✅ **类型安全** - 编译时类型检查，避免运行时错误
- ✅ **多类型支持** - 轻松支持多种数据类型的列表
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
    
    override fun onCreateView(): View {
        // 返回自定义创建的 View
        return TextView(containerView.context).apply {
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

## 🔧 注意事项

1. **必须配置 kapt**：确保在 app 模块中添加了 `kapt` 插件
2. **ViewHolder 必须有无参构造函数**：ViewHolder 类不能是内部类或抽象类
3. **数据类必须使用 @VHRef 注解**：每个数据类都需要指定对应的 ViewHolder
4. **ViewHolder 必须继承 BiShengBaseVH**：并使用泛型指定数据类型

## 📝 更新日志

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
