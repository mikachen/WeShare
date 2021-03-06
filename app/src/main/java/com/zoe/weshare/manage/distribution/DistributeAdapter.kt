package com.zoe.weshare.manage.distribution

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.data.Comment
import com.zoe.weshare.data.UserInfo
import com.zoe.weshare.databinding.ItemGiftDistributionListBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.getTimeAgoString
import com.zoe.weshare.util.GiftStatusType
import com.zoe.weshare.util.Util

class DistributeAdapter(val viewModel: DistributeViewModel) :
    ListAdapter<Comment, DistributeAdapter.DistributionViewHolder>(DiffCall()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DistributionViewHolder {
        return DistributionViewHolder.from(parent)
    }

    override fun onBindViewHolder(holderGift: DistributionViewHolder, position: Int) {
        val comment = getItem(position)
        holderGift.bind(comment, viewModel)
    }

    class DistributionViewHolder(val binding: ItemGiftDistributionListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment, viewModel: DistributeViewModel) {

            binding.textComment.text = comment.content
            binding.textCreatedTime.text = comment.createdTime.getTimeAgoString()

            binding.imageUserAvatar.setOnClickListener {
                viewModel.onNavigateToTargetProfile(comment.uid)
            }

            // displaying user's image and name
            if (viewModel.onProfileSearchComplete.value == 0) {
                if (viewModel.profileList.isNotEmpty()) {
                    val sender = viewModel.profileList.singleOrNull { it.uid == comment.uid }
                    sender?.let {
                        bindImage(binding.imageUserAvatar, sender.image)
                        binding.textProfileName.text = sender.name
                    }
                }
            }

            when (viewModel.gift.status) {

                GiftStatusType.OPENING.code -> {
                    binding.buttonSendGift.visibility = View.VISIBLE
                    binding.buttonSendGift.setOnClickListener {
                        viewModel.userPressSendGift(comment)
                    }
                }

                GiftStatusType.CLOSED.code -> {
                    val userReceiveGift: Boolean = viewModel.gift.whoGetGift == comment.uid

                    binding.lottieReceivedGift.visibility =
                        if (userReceiveGift) { View.VISIBLE } else { View.INVISIBLE }
                }

                GiftStatusType.ABANDONED.code -> {}

            }
        }

        companion object {
            fun from(parent: ViewGroup): DistributionViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemGiftDistributionListBinding.inflate(layoutInflater, parent, false)

                return DistributionViewHolder(binding)
            }
        }
    }
}

class DiffCall : DiffUtil.ItemCallback<Comment>() {
    override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
        return oldItem == newItem
    }
}
