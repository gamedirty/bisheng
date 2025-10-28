package com.sovnem.bisheng

/**
 * BiSheng 全局配置
 * 
 * 使用示例：
 * ```kotlin
 * // 在 Application 中配置
 * class MyApplication : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         BiShengConfig.isDebugMode = BuildConfig.DEBUG
 *     }
 * }
 * ```
 */
object BiShengConfig {
    
    /**
     * 是否启用调试模式
     * 
     * 启用后会输出详细的日志信息，方便排查问题
     * 建议仅在开发环境启用
     */
    @JvmStatic
    var isDebugMode: Boolean = false
    
    /**
     * 是否启用严格模式
     * 
     * 启用后会对一些可能的错误进行更严格的检查
     * 建议在开发和测试环境启用
     */
    @JvmStatic
    var isStrictMode: Boolean = false
    
    /**
     * 日志标签
     */
    internal const val TAG = "BiSheng"
}

