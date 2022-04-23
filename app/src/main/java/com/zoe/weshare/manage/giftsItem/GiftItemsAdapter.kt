package com.zoe.weshare.manage.giftsItem

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.R
import com.zoe.weshare.data.GiftPost
import com.zoe.weshare.databinding.ItemGiftManageBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.util.GiftStatusType
import com.zoe.weshare.util.Logger

class GiftItemsAdapter(
    val viewModel: GiftManageViewModel,
    private val onClickListener: OnClickListener,
) :
    ListAdapter<GiftPost, GiftItemsAdapter.GiftViewHolder>(DiffCallback) {

    class GiftViewHolder(var binding: ItemGiftManageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(gift: GiftPost) {

            binding.apply {
                bindImage(imageGift, gift.image)
                textTitle.text = gift.title
                textDiscontinuedCountdown.text = gift.sort
                textPostedLocation.text = gift.location!!.locationName
            }


            when (gift.status) {
                GiftStatusType.OPENING.code -> {
                    binding.textStatus.text = GiftStatusType.OPENING.tag
                    binding.textStatus.setBackgroundResource(R.color.message_sender_green)
                }
                GiftStatusType.CLOSED.code -> {
                    binding.textStatus.text = GiftStatusType.CLOSED.tag
                    binding.textStatus.setBackgroundResource(R.color.app_work_orange3)
                }
                GiftStatusType.ABANDONED.code -> {
                    binding.textStatus.text = GiftStatusType.ABANDONED.tag
                    binding.textStatus.setBackgroundResource(R.color.app_work_light_grey)
                }

                else -> Logger.d("unknow status")
            }

        }

        companion object {
            fun from(parent: ViewGroup): GiftViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                return GiftViewHolder(ItemGiftManageBinding
                    .inflate(layoutInflater, parent, false)
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GiftViewHolder {
        return GiftViewHolder.from(parent)
    }


    override fun onBindViewHolder(holder: GiftViewHolder, position: Int) {
        val gift = getItem(position)
        gift?.let {
            holder.bind(gift)

            holder.itemView.setOnClickListener {
                onClickListener.onClick(gift)
            }

            holder.binding.btnAbandoned.setOnClickListener {
                viewModel.userPressAbandon(gift)
            }

            holder.binding.btnCheckWhoRequest.setOnClickListener {
                viewModel.userCheckWhoRequest(gift)
            }
        }
    }

    class OnClickListener(val clickListener: (gift: GiftPost) -> Unit) {
        fun onClick(selectedGift: GiftPost) = clickListener(selectedGift)
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
