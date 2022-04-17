package com.zoe.weshare.detail.gift

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.data.Comment
import com.zoe.weshare.databinding.ItemCommentBoardBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.toDisplaySentTime

class GiftsCommentsAdapter (val viewModel: GiftDetailViewModel) :
    ListAdapter<Comment, GiftsCommentsAdapter.GiftCommentsViewHolder>(DiffCall()) {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): GiftCommentsViewHolder {
        return GiftCommentsViewHolder.from(parent)
    }

    override fun onBindViewHolder(holderGift: GiftCommentsViewHolder, position: Int) {
        val comment = getItem(position)
        holderGift.bind(comment, viewModel)
    }


    class GiftCommentsViewHolder(private val binding: ItemCommentBoardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment, viewModel: GiftDetailViewModel) {

            binding.textComment.text = comment.content
            binding.textCreatedTime.text = comment.createdTime.toDisplaySentTime()

            if (viewModel.onProfileSearching.value == 0) {
                if (viewModel.profileList.isNotEmpty()) {
                    val sender = viewModel.profileList.singleOrNull { it.uid == comment.uid }
                    sender?.let {
                        bindImage(binding.imageProfileAvatar, sender.image)
                        binding.textProfileName.text = sender.name
                    }
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): GiftCommentsViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemCommentBoardBinding.inflate(layoutInflater, parent, false)

                return GiftCommentsViewHolder(binding)
            }
        }
    }
}

class DiffCall : DiffUtil.ItemCallback<Comment>() {
    override fun areItemsTheSame(
        oldItem: Comment,
        newItem: Comment,
    ): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(
        oldItem: Comment,
        newItem: Comment,
    ): Boolean {
        return oldItem == newItem
    }
}

