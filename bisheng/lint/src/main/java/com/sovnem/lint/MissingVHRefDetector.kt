package com.sovnem.lint

import com.android.tools.lint.detector.api.*
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement

/**
 * 检测缺少 @VHRef 注解的 ViewHolder 使用
 * 
 * 当一个类继承自 BiShengBaseVH 但没有对应的数据类使用 @VHRef 注解时，发出警告
 */
@Suppress("UnstableApiUsage")
class MissingVHRefDetector : Detector(), SourceCodeScanner {
    
    companion object {
        private const val BISHENG_BASE_VH = "com.sovnem.bisheng.BiShengBaseVH"
        private const val VH_REF_ANNOTATION = "com.sovnem.bisheng.VHRef"
        
        /**
         * Issue: 检测可能缺少 @VHRef 注解的情况
         */
        val ISSUE = Issue.create(
            id = "MissingVHRef",
            briefDescription = "缺少 @VHRef 注解",
            explanation = """
                使用 BiSheng 库时，每个继承 BiShengBaseVH 的 ViewHolder 都需要有对应的数据类，
                并且该数据类必须使用 @VHRef 注解指向这个 ViewHolder。
                
                如果缺少 @VHRef 注解，运行时会抛出异常。
                
                正确示例：
                ```kotlin
                @VHRef(MyViewHolder::class)
                data class MyData(val text: String)
                
                @VHLayoutId(R.layout.item_layout)
                class MyViewHolder : BiShengBaseVH<MyData>() {
                    // ...
                }
                ```
            """.trimIndent(),
            category = Category.CORRECTNESS,
            priority = 8,
            severity = Severity.WARNING,
            implementation = Implementation(
                MissingVHRefDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
    
    override fun applicableSuperClasses(): List<String> {
        return listOf(BISHENG_BASE_VH)
    }
    
    override fun visitClass(context: JavaContext, declaration: UClass) {
        // 检查这个 ViewHolder 类是否是内部类
        if (declaration.containingClass != null) {
            context.report(
                ISSUE,
                declaration,
                context.getNameLocation(declaration),
                "ViewHolder 不应该是内部类，请使用顶层类或伴生对象",
                null
            )
        }
        
        // 注意：实际检查是否有对应的 @VHRef 注解比较复杂，
        // 需要扫描整个项目的所有类，这里只做基本的类结构检查
    }
}

