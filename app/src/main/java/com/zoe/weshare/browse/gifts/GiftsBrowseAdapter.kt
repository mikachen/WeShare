package com.zoe.weshare.browse.gifts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.data.GiftPost
import com.zoe.weshare.databinding.ItemHotGiftGridBinding
import com.zoe.weshare.ext.bindImage
import java.util.*

class GiftsBrowseAdapter(val listener: (GiftPost) -> Unit) :
    ListAdapter<GiftPost, GiftsBrowseAdapter.AllGiftsViewHolder>(DiffCallback) {

    private var unfilteredList = listOf<GiftPost>()

    class AllGiftsViewHolder(var binding: ItemHotGiftGridBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(gift: GiftPost) {
            binding.apply {
                textHotGiftLocation.text = gift.location.locationName
                textHotGiftTitle.text = gift.title
                bindImage(imageHotGift, gift.image)
            }
        }

        companion object {
            fun from(parent: ViewGroup): AllGiftsViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                return AllGiftsViewHolder(
                    ItemHotGiftGridBinding
                        .inflate(layoutInflater, parent, false)
                )
            }
        }
    }

    override fun onBindViewHolder(holder: AllGiftsViewHolder, position: Int) {
        val gift = getItem(position)
        holder.bind(gift)

        holder.itemView.setOnClickListener { listener(gift) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllGiftsViewHolder {
        return AllGiftsViewHolder.from(parent)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<GiftPost>() {
        override fun areItemsTheSame(oldItem: GiftPost, newItem: GiftPost): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: GiftPost, newItem: GiftPost): Boolean {
            return oldItem.id == newItem.id
        }
    }

    fun modifyList(list: List<GiftPost>) {
        unfilteredList = list
        submitList(list)
    }

    fun filter(query: CharSequence?, viewModel: GiftsBrowseViewModel) {
        val listResult = mutableListOf<GiftPost>()

        // perform the data filtering
        if (!query.isNullOrEmpty()) {
            val lowercaseQuery = query.toString().lowercase(Locale.getDefault())

            listResult.addAll(
                unfilteredList.filter {

                    it.title.lowercase(Locale.getDefault())
                        .contains(lowercaseQuery) ||

                            it.description.lowercase(Locale.getDefault())
                                .contains(lowercaseQuery)
                }
            )
        } else {
            listResult.addAll(unfilteredList)
        }

        viewModel.onEmptyQuery(listResult.isEmpty())

        submitList(listResult)
    }
}
