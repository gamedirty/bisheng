package com.sovnem.bisheng

import android.util.Log
import androidx.recyclerview.widget.RecyclerView

class HullViewHolder<T>(private val bishengViewHolder: BiShengBaseVH<T>) :
    RecyclerView.ViewHolder(bishengViewHolder.containerView) {
    
    companion object {
        private const val TAG = "BiSheng"
    }
    
    fun bindDataInternal(
        any: Any,
        position: Int,
        payloads: MutableList<Any>? = null,
        onItemClickListener: OnItemClickListener? = null,
    ) {
        try {
            bishengViewHolder.bindData(any as T, position, payloads, onItemClickListener)
        } catch (e: Exception) {
            Log.e(TAG, "绑定数据失败: position=$position, data=${any.javaClass.simpleName}", e)
            bishengViewHolder.onBindFailed(e)
        }
    }
    
    /**
     * 暴露给 Adapter 调用的生命周期方法
     */
    fun onViewRecycledInternal() {
        try {
            bishengViewHolder.onViewRecycled()
        } catch (e: Exception) {
            Log.e(TAG, "ViewHolder 回收失败: ${bishengViewHolder.javaClass.simpleName}", e)
        }
    }
    
    fun onViewAttachedToWindowInternal() {
        try {
            bishengViewHolder.onViewAttachedToWindow()
        } catch (e: Exception) {
            Log.e(TAG, "ViewHolder 附加到窗口失败: ${bishengViewHolder.javaClass.simpleName}", e)
        }
    }
    
    fun onViewDetachedFromWindowInternal() {
        try {
            bishengViewHolder.onViewDetachedFromWindow()
        } catch (e: Exception) {
            Log.e(TAG, "ViewHolder 从窗口分离失败: ${bishengViewHolder.javaClass.simpleName}", e)
        }
    }
}