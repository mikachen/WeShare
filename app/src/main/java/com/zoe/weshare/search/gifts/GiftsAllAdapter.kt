package com.zoe.weshare.search.gifts

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.data.GiftPost
import com.zoe.weshare.databinding.ItemHotGiftGridBinding
import com.zoe.weshare.ext.bindImage
import java.util.*

class GiftsAllAdapter(private val onClickListener: GiftsALLOnClickListener) :
    ListAdapter<GiftPost, GiftsAllAdapter.ALLGiftsViewHolder>(DiffCallback) {

    private var unfilteredList = listOf<GiftPost>()

    class ALLGiftsViewHolder(var binding: ItemHotGiftGridBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(gift: GiftPost) {
            binding.apply {
                textHotGiftLocation.text = gift.location?.locationName ?: ""
                textHotGiftTitle.text = gift.title
                bindImage(imageHotGift, gift.image)
            }
        }

        companion object {
            fun from(parent: ViewGroup): ALLGiftsViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                return ALLGiftsViewHolder(
                    ItemHotGiftGridBinding
                        .inflate(layoutInflater, parent, false)
                )
            }
        }
    }

    override fun onBindViewHolder(holder: ALLGiftsViewHolder, position: Int) {
        val data = getItem(position)
        holder.bind(data)

        holder.itemView.setOnClickListener {
            onClickListener.onClick(data)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ALLGiftsViewHolder {
        return ALLGiftsViewHolder.from(parent)
    }


    class GiftsALLOnClickListener(val doNothing: (gift: GiftPost) -> Unit) {
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

        fun modifyList(list : List<GiftPost>) {
        unfilteredList = list
        submitList(list)
    }

    fun filter(query: CharSequence?, viewModel:GiftsAllViewModel) {
        val list = mutableListOf<GiftPost>()

        // perform the data filtering
        if(!query.isNullOrEmpty()) {
            list.addAll(unfilteredList.filter {
                it.title.toLowerCase(Locale.getDefault()).contains(query.toString().toLowerCase(Locale.getDefault())) ||
                        it.description.toLowerCase(Locale.getDefault()).contains(query.toString().toLowerCase(Locale.getDefault())) })

            viewModel.onSearchEmpty.value = list.isEmpty()
        } else {
            list.addAll(unfilteredList)
            viewModel.onSearchEmpty.value = false
        }

        submitList(list)
    }
}
