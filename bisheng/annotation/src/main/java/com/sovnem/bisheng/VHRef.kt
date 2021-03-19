package com.sovnem.bisheng

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class VHRef(val ref: KClass<*>)