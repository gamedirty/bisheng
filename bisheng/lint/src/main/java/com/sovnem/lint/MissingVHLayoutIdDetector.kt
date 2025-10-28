package com.sovnem.lint

import com.android.tools.lint.detector.api.*
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UMethod

/**
 * 检测 ViewHolder 是否提供了布局
 * 
 * ViewHolder 必须通过以下方式之一提供布局：
 * 1. 使用 @VHLayoutId 注解指定布局资源
 * 2. 重写 onCreateView() 方法返回自定义 View
 */
@Suppress("UnstableApiUsage")
class MissingVHLayoutIdDetector : Detector(), SourceCodeScanner {
    
    companion object {
        private const val BISHENG_BASE_VH = "com.sovnem.bisheng.BiShengBaseVH"
        private const val VH_LAYOUT_ID_ANNOTATION = "com.sovnem.bisheng.VHLayoutId"
        private const val ON_CREATE_VIEW_METHOD = "onCreateView"
        
        /**
         * Issue: 检测 ViewHolder 是否缺少布局定义
         */
        val ISSUE = Issue.create(
            id = "MissingVHLayoutId",
            briefDescription = "ViewHolder 缺少布局定义",
            explanation = """
                ViewHolder 必须提供布局，可以通过以下两种方式之一：
                
                1. 使用 @VHLayoutId 注解指定布局资源 ID：
                ```kotlin
                @VHLayoutId(R.layout.item_layout)
                class MyViewHolder : BiShengBaseVH<MyData>() {
                    // ...
                }
                ```
                
                2. 重写 onCreateView() 方法返回自定义 View：
                ```kotlin
                class MyViewHolder : BiShengBaseVH<MyData>() {
                    override fun onCreateView(parent: ViewGroup): View {
                        return MyCustomView(parent.context)
                    }
                }
                ```
                
                如果两者都不提供，运行时会抛出异常。
            """.trimIndent(),
            category = Category.CORRECTNESS,
            priority = 9,
            severity = Severity.ERROR,
            implementation = Implementation(
                MissingVHLayoutIdDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
    
    override fun applicableSuperClasses(): List<String> {
        return listOf(BISHENG_BASE_VH)
    }
    
    override fun visitClass(context: JavaContext, declaration: UClass) {
        // 检查是否有 @VHLayoutId 注解
        val hasLayoutIdAnnotation = declaration.findAnnotation(VH_LAYOUT_ID_ANNOTATION) != null
        
        // 检查是否重写了 onCreateView 方法
        val hasOnCreateViewOverride = declaration.methods.any { method ->
            isOnCreateViewOverride(method)
        }
        
        // 如果两者都没有，报告错误
        if (!hasLayoutIdAnnotation && !hasOnCreateViewOverride) {
            context.report(
                ISSUE,
                declaration,
                context.getNameLocation(declaration),
                "ViewHolder `${declaration.name}` 必须使用 @VHLayoutId 注解或重写 onCreateView() 方法",
                createQuickFix(declaration.name ?: "")
            )
        }
    }
    
    private fun isOnCreateViewOverride(method: UMethod): Boolean {
        if (method.name != ON_CREATE_VIEW_METHOD) return false
        if (method.parameterList.parametersCount != 1) return false
        // 简化检查：只要有这个方法名就认为是重写
        return true
    }
    
    private fun createQuickFix(className: String): LintFix {
        return fix()
            .name("添加 @VHLayoutId 注解")
            .replace()
            .text("class $className")
            .with("@VHLayoutId(R.layout.TODO)\nclass $className")
            .build()
    }
}

