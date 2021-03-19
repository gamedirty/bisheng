package com.sovnem.bisheng

import android.view.View

abstract class BiShengBaseVH<T> {
    lateinit var containerView: View
    abstract fun bindData(
        data: T,
        position: Int,
        payloads: MutableList<Any>?,
        onItemClickListener: OnItemClickListener?,
    )

    fun onCreateView(): View? {
        return null
    }

}