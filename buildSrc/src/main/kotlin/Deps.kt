/**
 * 依赖版本配置
 * 
 * 注意：推荐使用 libs.versions.toml (Gradle Version Catalog) 管理依赖
 * 此文件保留是为了向后兼容，未来版本将移除
 */
@Deprecated(
    message = "使用 Gradle Version Catalog (libs.versions.toml) 替代",
    replaceWith = ReplaceWith("libs.versions.toml")
)
object Deps {
    // Android SDK 版本
    const val compileSdk = 34
    const val minSdk = 21
    const val targetSdk = 34
    const val buildTools = "34.0.0"
    
    // 版本号（已迁移到 libs.versions.toml）
    const val kotlin_version: String = "1.9.22"
    const val agp_version = "8.2.2"
    const val androidx_core = "1.12.0"
    const val androidx_appcompat = "1.6.1"
    const val androidx_recyclerview = "1.3.2"
    const val material = "1.11.0"
    const val constraintlayout = "2.1.4"
}