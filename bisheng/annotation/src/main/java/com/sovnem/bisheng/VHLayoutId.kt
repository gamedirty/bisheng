package com.sovnem.bisheng

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class VHLayoutId(val layoutId: Int, val lazyLoad: Boolean = false)