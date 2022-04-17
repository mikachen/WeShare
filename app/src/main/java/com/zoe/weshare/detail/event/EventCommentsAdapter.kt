package com.zoe.weshare.detail.event

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.data.Comment
import com.zoe.weshare.databinding.ItemCommentBoardBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.toDisplaySentTime

class EventCommentsAdapter(val viewModel: EventDetailViewModel) :
    ListAdapter<Comment, EventCommentsAdapter.EventCommentsViewHolder>(DiffCall()) {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): EventCommentsViewHolder {
        return EventCommentsViewHolder.from(parent)
    }

    override fun onBindViewHolder(holderEvent: EventCommentsViewHolder, position: Int) {
        val comment = getItem(position)
        holderEvent.bind(comment, viewModel)
    }


    class EventCommentsViewHolder(private val binding: ItemCommentBoardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment, viewModel: EventDetailViewModel) {

            binding.textComment.text = comment.content
            binding.textCreatedTime.text = comment.createdTime.toDisplaySentTime()

            if (viewModel.onProfileSearching.value == 0) {
                if (viewModel.profileList.isNotEmpty()) {
                    val speaker = viewModel.profileList.singleOrNull { it.uid == comment.uid }
                    speaker?.let {
                        bindImage(binding.imageProfileAvatar, speaker.image)
                        binding.textProfileName.text = speaker.name
                    }
                }
            }

        }

        companion object {
            fun from(parent: ViewGroup): EventCommentsViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemCommentBoardBinding.inflate(layoutInflater, parent, false)

                return EventCommentsViewHolder(binding)
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

