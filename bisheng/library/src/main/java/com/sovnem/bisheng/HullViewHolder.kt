package com.sovnem.bisheng

import androidx.recyclerview.widget.RecyclerView

class HullViewHolder<T>(private val bishengViewHolder: BiShengBaseVH<T>) :
    RecyclerView.ViewHolder(bishengViewHolder.containerView) {
    fun bindDataInternal(
        any: Any,
        position: Int,
        payloads: MutableList<Any>? = null,
        onItemClickListener: OnItemClickListener? = null,
    ) {
        bishengViewHolder.bindData(any as T, position, payloads, onItemClickListener)

    }


}