package com.zoe.weshare.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.data.GiftPost
import com.zoe.weshare.databinding.ItemHotGiftGridBinding
import com.zoe.weshare.ext.bindImage

class HotGiftsAdapter(private val onClickListener: HotGiftsOnClickListener) :
    ListAdapter<GiftPost, HotGiftsAdapter.HotGiftsViewHolder>(DiffCallback) {

    class HotGiftsViewHolder(var binding: ItemHotGiftGridBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(gift: GiftPost) {
            binding.apply {
                textHotGiftLocation.text = gift.location?.locationName ?: ""
                textHotGiftTitle.text = gift.title
                bindImage(imageHotGift, gift.image)
            }
        }

        companion object {
            fun from(parent: ViewGroup): HotGiftsViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                return HotGiftsViewHolder(
                    ItemHotGiftGridBinding
                        .inflate(layoutInflater, parent, false)
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotGiftsViewHolder {
        return HotGiftsViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: HotGiftsViewHolder, position: Int) {
        val data = getItem(position)
        holder.bind(data)

        holder.itemView.setOnClickListener {
            onClickListener.onClick(data)
        }
    }

    class HotGiftsOnClickListener(val doNothing: (gift: GiftPost) -> Unit) {
        fun onClick(selectedGift: GiftPost) = doNothing(selectedGift)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<GiftPost>() {
        override fun areItemsTheSame(oldItem: GiftPost, newItem: GiftPost): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: GiftPost, newItem: GiftPost): Boolean {
            return oldItem.id == newItem.id
        }
    }
}
