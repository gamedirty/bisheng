package com.sovnem.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API

/**
 * BiSheng Lint 规则注册表
 * 
 * 用于注册自定义的 Lint 检查规则
 */
@Suppress("UnstableApiUsage")
class LintRegistry : IssueRegistry() {
    
    override val issues = listOf(
        MissingVHRefDetector.ISSUE,
        MissingVHLayoutIdDetector.ISSUE
    )
    
    override val api: Int = CURRENT_API
    
    override val minApi: Int = 8
    
    override val vendor: Vendor = Vendor(
        vendorName = "BiSheng",
        identifier = "com.sovnem.bisheng:lint",
        feedbackUrl = "https://github.com/sovnem/bisheng/issues",
        contact = "https://github.com/sovnem/bisheng"
    )
}