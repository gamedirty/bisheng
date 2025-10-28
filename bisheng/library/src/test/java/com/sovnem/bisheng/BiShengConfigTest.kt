package com.sovnem.bisheng

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Test

/**
 * BiShengConfig 配置测试
 */
class BiShengConfigTest {
    
    @After
    fun tearDown() {
        // 重置配置到默认值
        BiShengConfig.isDebugMode = false
        BiShengConfig.isStrictMode = false
    }
    
    @Test
    fun `默认情况下调试模式应该关闭`() {
        assertThat(BiShengConfig.isDebugMode).isFalse()
    }
    
    @Test
    fun `默认情况下严格模式应该关闭`() {
        assertThat(BiShengConfig.isStrictMode).isFalse()
    }
    
    @Test
    fun `可以启用调试模式`() {
        BiShengConfig.isDebugMode = true
        assertThat(BiShengConfig.isDebugMode).isTrue()
    }
    
    @Test
    fun `可以启用严格模式`() {
        BiShengConfig.isStrictMode = true
        assertThat(BiShengConfig.isStrictMode).isTrue()
    }
    
    @Test
    fun `可以关闭调试模式`() {
        BiShengConfig.isDebugMode = true
        BiShengConfig.isDebugMode = false
        assertThat(BiShengConfig.isDebugMode).isFalse()
    }
    
    @Test
    fun `TAG常量应该正确设置`() {
        assertThat(BiShengConfig.TAG).isEqualTo("BiSheng")
    }
}

