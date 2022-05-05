package com.zoe.weshare.manage.giftsItem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.zoe.weshare.R
import com.zoe.weshare.data.GiftPost
import com.zoe.weshare.databinding.ItemGiftManageBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.toDisplayFormat
import com.zoe.weshare.util.GiftStatusType
import com.zoe.weshare.util.Logger
import com.zoe.weshare.util.Util.getStringWithStrParm

class GiftItemsAdapter(
    val viewModel: GiftManageViewModel,
    private val onClickListener: OnClickListener,
) : ListAdapter<GiftPost, GiftItemsAdapter.GiftViewHolder>(DiffCallback) {

     val viewBinderHelper = ViewBinderHelper()

    class GiftViewHolder(var binding: ItemGiftManageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(gift: GiftPost) {

            binding.apply {
                bindImage(imageGift, gift.image)
                textTitle.text = gift.title
                textGiftPostedTime.text =
                    getStringWithStrParm(R.string.posted_time, gift.createdTime.toDisplayFormat())
                textPostedLocation.text = gift.location!!.locationName
            }

            when (gift.status) {
                GiftStatusType.OPENING.code -> {
                    binding.textStatus.text = GiftStatusType.OPENING.tag
                    binding.textStatus.setBackgroundResource(R.color.app_work_light_green)
                }
                GiftStatusType.CLOSED.code -> {
                    binding.buttonAbandon.visibility = View.GONE
                    binding.btnCheckWhoRequest.visibility = View.GONE
                    binding.textStatus.text = GiftStatusType.CLOSED.tag
                    binding.textStatus.setBackgroundResource(R.color.app_work_orange3)
                }
                GiftStatusType.ABANDONED.code -> {
                    binding.buttonAbandon.visibility = View.GONE
                    binding.buttonAbandon.visibility = View.GONE
                    binding.textStatus.text = GiftStatusType.ABANDONED.tag
                    binding.textStatus.setBackgroundResource(R.color.app_work_dark_grey)
                }

                else -> Logger.d("unKnow status")
            }
        }

        companion object {
            fun from(parent: ViewGroup): GiftViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                return GiftViewHolder(
                    ItemGiftManageBinding
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

        viewBinderHelper.setOpenOnlyOne(true)
        viewBinderHelper.bind(holder.binding.swipeLayout, gift.id)

        gift?.let {
            holder.bind(gift)

            holder.itemView.setOnClickListener {
                onClickListener.onClick(gift)
            }

            holder.binding.buttonAbandon.setOnClickListener {
                viewModel.userClickAbandon(gift)
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
