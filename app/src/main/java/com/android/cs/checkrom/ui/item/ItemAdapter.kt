package com.android.cs.checkrom.ui.item

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.cs.checkrom.R
import com.android.cs.checkrom.entry.CheckEntry

class ItemAdapter(val context: Context, val taskData: MutableList<CheckEntry>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val HOLDER_TYPE_SIMPLE = 0
        const val HOLDER_TYPE_MORE = 1
    }

    private var mOnItemClickListener: AdapterView.OnItemClickListener? = null

    fun setOnItemClickListener(onItemClickListener: AdapterView.OnItemClickListener?) {
        mOnItemClickListener = onItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == HOLDER_TYPE_MORE) {
            val emptyView =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_recyclerview_more, parent, false)
            return MoreHolder(emptyView)
        }

        val inflater: LayoutInflater = LayoutInflater.from(context)
        return ItemHolder(inflater.inflate(R.layout.item_recyclerview_increase, parent, false))
    }

    override fun getItemCount(): Int {
        return if (taskData.size == 2) {
            3
        } else {
            taskData.size
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (taskData.size == 2 && position == 2) {
            return HOLDER_TYPE_MORE
        }
        return HOLDER_TYPE_SIMPLE
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MoreHolder) {
            holder.itemView.setOnClickListener {
//                mOnItemClickListener?.onItemClick(it, position)
            }
            return
        }

        val increaseHolder = holder as ItemHolder

        increaseHolder.tv1.text = taskData[position].content
        if(taskData[position].state){
            increaseHolder.state.text = "异常"
            increaseHolder.state.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.red
                )
            )
        } else {
            increaseHolder.state.text = "正常"
            increaseHolder.state.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.color_4BB93F
                )
            )
        }
    }

    private class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv1: TextView = itemView.findViewById(R.id.tv1)
        val state: TextView = itemView.findViewById(R.id.state)
    }

    private class MoreHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}
}