package com.sovnem.bisheng

import androidx.recyclerview.widget.RecyclerView

class HullViewHolder<T>(private val bishengVhewHolder: BiShengBaseVH<T>) :
    RecyclerView.ViewHolder(bishengVhewHolder.containerView) {
    fun bindDataInternal(
        any: Any,
        position: Int,
        payloads: MutableList<Any>? = null,
        onItemClickListener: OnItemClickListener? = null,
    ) {
        bishengVhewHolder.bindData(any as T, position, payloads, onItemClickListener)
    }
}