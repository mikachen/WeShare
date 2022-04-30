package com.zoe.weshare.home

import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.data.PostLog
import com.zoe.weshare.databinding.ItemHomeLogTickerBinding

class TickerAdapter : ListAdapter<PostLog, TickerAdapter.TickerViewHolder>(DiffCallback) {

    class TickerViewHolder(var binding: ItemHomeLogTickerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(log: PostLog) {
            binding.textTicker.text = Html.fromHtml(log.logMsg)
        }

        companion object {
            fun from(parent: ViewGroup): TickerViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                return TickerViewHolder(
                    ItemHomeLogTickerBinding
                        .inflate(layoutInflater, parent, false)
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TickerViewHolder {
        return TickerViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: TickerViewHolder, position: Int) {
        val data = getItem(position)
        holder.bind(data)

    }

//    class HotGiftsOnClickListener(val doNothing: (gift: GiftPost) -> Unit) {
//        fun onClick(selectedGift: GiftPost) = doNothing(selectedGift)
//    }

    companion object DiffCallback : DiffUtil.ItemCallback<PostLog>() {
        override fun areItemsTheSame(oldItem: PostLog, newItem: PostLog): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: PostLog, newItem: PostLog): Boolean {
            return oldItem.id == newItem.id
        }
    }
}
