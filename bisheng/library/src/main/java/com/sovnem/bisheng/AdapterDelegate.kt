package com.sovnem.bisheng

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

interface AdapterDelegate {
    fun getItemViewType(any: Any): Int

    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder

    fun bindViewHolder(
        holder: RecyclerView.ViewHolder,
        any: Any,
        position: Int,
        payloads: MutableList<Any>? = null,
    )
}