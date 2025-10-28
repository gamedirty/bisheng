# BiSheng Library ProGuard Rules
# 这些规则确保 BiSheng 库在混淆后仍能正常工作

#===============================================================================
# 保留调试信息
#===============================================================================
# 保留源文件名和行号信息，方便调试崩溃日志
-keepattributes SourceFile,LineNumberTable
# 重命名源文件为统一名称，隐藏真实文件名
-renamesourcefileattribute SourceFile

#===============================================================================
# 保留注解
#===============================================================================
# BiSheng 依赖运行时注解，必须保留
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes AnnotationDefault

# 保留所有注解类
-keep @interface com.sovnem.bisheng.** { *; }

# 保留使用了 BiSheng 注解的类
-keep @com.sovnem.bisheng.VHRef class * { *; }
-keep @com.sovnem.bisheng.VHLayoutId class * { *; }

#===============================================================================
# 保留 BiSheng 核心类
#===============================================================================
# 保留 BiShengAdapter 及其公共 API
-keep public class com.sovnem.bisheng.BiShengAdapter {
    public <methods>;
    public <fields>;
}

# 保留 BiShengBaseVH 及其子类
-keep public class com.sovnem.bisheng.BiShengBaseVH {
    public <methods>;
    protected <fields>;
}
-keep class * extends com.sovnem.bisheng.BiShengBaseVH { *; }

# 保留 BiShengConfig
-keep public class com.sovnem.bisheng.BiShengConfig {
    public static <fields>;
    public static <methods>;
}

# 保留 BiShengDiffable 接口及其实现
-keep interface com.sovnem.bisheng.BiShengDiffable { *; }
-keep class * implements com.sovnem.bisheng.BiShengDiffable { *; }

# 保留生成的适配器映射类
-keep class com.sovnem.bisheng.BiShengAdapterMapImpl { *; }
-keep interface com.sovnem.bisheng.IAdapterMap { *; }

#===============================================================================
# 保留 ViewHolder 相关
#===============================================================================
# 保留所有 ViewHolder 的无参构造函数（反射创建实例需要）
-keepclassmembers class * extends com.sovnem.bisheng.BiShengBaseVH {
    public <init>();
}

# 保留 ViewHolder 的 containerView 字段
-keepclassmembers class * extends com.sovnem.bisheng.BiShengBaseVH {
    public android.view.View containerView;
}

# 保留 ViewHolder 的 bindData 方法
-keepclassmembers class * extends com.sovnem.bisheng.BiShengBaseVH {
    public void bindData(...);
}

# 保留 ViewHolder 的生命周期方法
-keepclassmembers class * extends com.sovnem.bisheng.BiShengBaseVH {
    public void onViewRecycled();
    public void onViewAttachedToWindow();
    public void onViewDetachedFromWindow();
    public void onBindFailed(java.lang.Exception);
}

# 保留 ViewHolder 的 onCreateView 方法
-keepclassmembers class * extends com.sovnem.bisheng.BiShengBaseVH {
    public android.view.View onCreateView(android.view.ViewGroup);
}

#===============================================================================
# 保留数据类
#===============================================================================
# 保留所有使用 @VHRef 注解的数据类及其字段
# 数据类的字段需要被 DiffUtil 和反射访问
-keepclassmembers @com.sovnem.bisheng.VHRef class * {
    <fields>;
    <init>(...);
}

# 保留实现 BiShengDiffable 接口的类的所有方法
-keepclassmembers class * implements com.sovnem.bisheng.BiShengDiffable {
    public <methods>;
}

#===============================================================================
# 保留泛型签名
#===============================================================================
# 保留泛型签名，用于反射获取泛型类型
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod

#===============================================================================
# Kotlin 相关
#===============================================================================
# 保留 Kotlin 元数据注解
-keepattributes *Annotation*

# 保留 Kotlin 数据类的 copy 方法和组件方法
-keepclassmembers class **$$DataClass {
    public ** copy(...);
    public ** component*();
}

# 保留 Kotlin companion object
-keepclassmembers class * {
    public static ** Companion;
}
-keepclassmembers class * {
    ** Companion;
}

#===============================================================================
# AndroidX RecyclerView
#===============================================================================
# BiSheng 依赖 RecyclerView，保留相关类
-keep class androidx.recyclerview.widget.RecyclerView { *; }
-keep class androidx.recyclerview.widget.RecyclerView$ViewHolder { *; }
-keep class androidx.recyclerview.widget.RecyclerView$Adapter { *; }

#===============================================================================
# 警告抑制
#===============================================================================
# 忽略某些库的警告
-dontwarn javax.annotation.**
-dontwarn org.jetbrains.annotations.**

#===============================================================================
# 优化选项
#===============================================================================
# 启用优化（R8 默认启用，ProGuard 可能需要手动启用）
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# 不混淆内部类的外部类信息
-keepattributes InnerClasses

# 保留异常信息
-keepattributes Exceptions