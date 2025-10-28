package com.sovnem.bisheng

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class BiShengAdapter(
    list: List<Any>? = null,
    private var onItemClickListener: OnItemClickListener? = null,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dataList = ArrayList<Any>()

    init {
        list?.let { dataList.addAll(it) }
    }

    private var delegate: AdapterDelegate = AdapterDelegate(onItemClickListener)

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
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            delegate.bindViewHolder(holder, dataList[position], position, payloads)
        }
    }

    override fun getItemCount() = dataList.size

    /**
     * 在列表末尾添加数据
     */
    fun addData(list: List<Any>?) {
        list?.let {
            val insertIndex = dataList.size
            dataList.addAll(it)
            notifyItemRangeInserted(insertIndex, it.size)
        }
    }

    /**
     * 在指定位置添加单个数据
     */
    fun addItem(position: Int, item: Any) {
        dataList.add(position, item)
        notifyItemInserted(position)
    }

    /**
     * 在列表末尾添加单个数据
     */
    fun addItem(item: Any) {
        dataList.add(item)
        notifyItemInserted(dataList.size - 1)
    }

    /**
     * 移除指定位置的数据
     */
    fun removeItem(position: Int) {
        if (position in 0 until dataList.size) {
            dataList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    /**
     * 移除指定数据
     */
    fun removeItem(item: Any) {
        val index = dataList.indexOf(item)
        if (index >= 0) {
            removeItem(index)
        }
    }

    /**
     * 清空所有数据
     */
    fun clearData() {
        val size = dataList.size
        dataList.clear()
        notifyItemRangeRemoved(0, size)
    }

    /**
     * 设置数据列表（使用DiffUtil优化）
     */
    fun setData(list: List<Any>?, useDiffUtil: Boolean = false) {
        list?.let {
            if (useDiffUtil && dataList.isNotEmpty()) {
                val diffCallback = BiShengDiffCallback(dataList, it)
                val diffResult = DiffUtil.calculateDiff(diffCallback)
                dataList.clear()
                dataList.addAll(it)
                diffResult.dispatchUpdatesTo(this)
            } else {
                dataList.clear()
                dataList.addAll(it)
                notifyDataSetChanged()
            }
        }
    }

    /**
     * 获取指定位置的数据
     */
    fun getItem(position: Int): Any? {
        return if (position in 0 until dataList.size) {
            dataList[position]
        } else {
            null
        }
    }

    /**
     * 获取所有数据
     */
    fun getData(): List<Any> {
        return dataList.toList()
    }

    /**
     * 更新指定位置的数据
     */
    fun updateItem(position: Int, item: Any) {
        if (position in 0 until dataList.size) {
            dataList[position] = item
            notifyItemChanged(position)
        }
    }

    fun notifyLastItemChange() {
        if (dataList.isNotEmpty()) {
            notifyItemChanged(dataList.size - 1)
        }
    }

    @JvmName("itemClick")
    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
        if (delegate is DefaultAdapterDelegate) {
            (delegate as DefaultAdapterDelegate).onItemClickListener = onItemClickListener
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is HullViewHolder<*>) {
            // 可以在这里添加清理逻辑
        }
    }

    /**
     * DiffUtil 回调实现
     * 
     * 支持两种比较模式：
     * 1. 如果数据类实现了 BiShengDiffable 接口，使用其自定义的比较逻辑
     * 2. 否则，使用默认的 equals() 方法比较
     */
    private class BiShengDiffCallback(
        private val oldList: List<Any>,
        private val newList: List<Any>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            
            // 类型必须相同
            if (oldItem.javaClass != newItem.javaClass) {
                return false
            }
            
            // 如果实现了 BiShengDiffable 接口，使用 getItemId 比较
            if (oldItem is BiShengDiffable && newItem is BiShengDiffable) {
                return try {
                    oldItem.getItemId() == newItem.getItemId()
                } catch (e: Exception) {
                    // 如果 getItemId 抛出异常，降级为 equals 比较
                    oldItem == newItem
                }
            }
            
            // 否则使用 equals() 方法
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            
            // 如果实现了 BiShengDiffable 接口，使用 areContentsTheSame 比较
            if (oldItem is BiShengDiffable && newItem is BiShengDiffable) {
                return try {
                    oldItem.areContentsTheSame(newItem)
                } catch (e: Exception) {
                    // 如果比较方法抛出异常，降级为 equals 比较
                    oldItem == newItem
                }
            }
            
            // 否则使用 equals() 方法
            return oldItem == newItem
        }

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            
            // 如果实现了 BiShengDiffable 接口，获取变化负载
            if (oldItem is BiShengDiffable && newItem is BiShengDiffable) {
                return try {
                    oldItem.getChangePayload(newItem)
                } catch (e: Exception) {
                    // 如果获取负载失败，返回 null 表示全量更新
                    null
                }
            }
            
            return super.getChangePayload(oldItemPosition, newItemPosition)
        }
    }

}
