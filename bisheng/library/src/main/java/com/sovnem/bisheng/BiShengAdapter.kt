package com.sovnem.bisheng

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class BiShengAdapter(
    list: List<Any>? = null,
    private var onItemClickListener: OnItemClickListener? = null,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dataList by lazy { ArrayList<Any>() }

    init {
        list?.let { dataList.addAll(it) }
    }

    private var delegate: AdapterDelegate = DefaultAdapterDelegate(onItemClickListener)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return delegate.onCreateViewHolder(parent, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        return delegate.getItemViewType(dataList[position])
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        delegate.bindViewHolder(holder, dataList[position], position)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        super.onBindViewHolder(holder, position, payloads)
        delegate.bindViewHolder(holder, dataList[position], position, payloads)
    }

    override fun getItemCount() = dataList.size

    fun addData(list: List<Any>?, hasLoadViewHolder: Boolean = false) {
        val insertIndex =
            if (hasLoadViewHolder) dataList.lastIndex else dataList.lastIndex + 1
        list?.let {
            dataList.addAll(insertIndex, list)
            notifyItemRangeChanged(insertIndex, dataList.size + 1)
        }
    }

    fun setData(list: List<Any>?) {
        list?.let {
            dataList.clear()
            dataList.addAll(it)
            notifyDataSetChanged()
        }
    }

    fun notifyLastItemChange() {
        notifyItemChanged(dataList.size - 1)
    }

    @JvmName("itemClick")
    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
        (delegate as DefaultAdapterDelegate).onItemClickListener = onItemClickListener
    }

}
