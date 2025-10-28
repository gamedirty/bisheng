# BiSheng Consumer ProGuard Rules
# 这些规则会自动应用到使用 BiSheng 库的应用中

#===============================================================================
# 必须保留的注解
#===============================================================================
-keepattributes RuntimeVisibleAnnotations
-keepattributes Signature
-keepattributes *Annotation*

#===============================================================================
# 保留 BiSheng 注解的类
#===============================================================================
# 保留使用了 @VHRef 注解的类（数据类）
-keep @com.sovnem.bisheng.VHRef class * { *; }

# 保留使用了 @VHLayoutId 注解的类（ViewHolder）
-keep @com.sovnem.bisheng.VHLayoutId class * { *; }

#===============================================================================
# 保留 ViewHolder 类
#===============================================================================
# 保留所有继承自 BiShengBaseVH 的类
-keep class * extends com.sovnem.bisheng.BiShengBaseVH { *; }

# 保留 ViewHolder 的无参构造函数（必须，用于反射创建实例）
-keepclassmembers class * extends com.sovnem.bisheng.BiShengBaseVH {
    public <init>();
}

#===============================================================================
# 保留 BiShengDiffable 实现
#===============================================================================
# 保留实现了 BiShengDiffable 接口的类及其方法
-keep class * implements com.sovnem.bisheng.BiShengDiffable { *; }

#===============================================================================
# 保留生成的映射类
#===============================================================================
# 保留注解处理器生成的适配器映射实现类
-keep class com.sovnem.bisheng.BiShengAdapterMapImpl { *; }

