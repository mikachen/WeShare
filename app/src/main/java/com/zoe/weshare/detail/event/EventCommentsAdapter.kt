package com.zoe.weshare.detail.event

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.R
import com.zoe.weshare.data.UserInfo
import com.zoe.weshare.data.Comment
import com.zoe.weshare.databinding.ItemCommentBoardBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.toDisplaySentTime
import com.zoe.weshare.util.Util

class EventCommentsAdapter(val viewModel: EventDetailViewModel) :
    ListAdapter<Comment, EventCommentsAdapter.EventCommentsViewHolder>(DiffCall()) {

    val userInfo = Util.readInstanceProperty<UserInfo>(viewModel, "userInfo")

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): EventCommentsViewHolder {
        return EventCommentsViewHolder.from(parent)
    }

    override fun onBindViewHolder(holderEvent: EventCommentsViewHolder, position: Int) {
        val comment = getItem(position)
        holderEvent.bind(comment, viewModel)

        val whoLikedList = viewModel.updateCommentLike[position].whoLiked
        val isUserLiked: Boolean = whoLikedList?.contains(userInfo.uid) == true

        holderEvent.binding.apply {

            if (!whoLikedList.isNullOrEmpty()) {
                textLikesCount.text =
                    Util.getStringWithIntParm(R.string.number_who_liked, whoLikedList.size)
            } else {
                textLikesCount.text = ""
            }

            if (isUserLiked) {
                buttonCommentLike.setTextColor(Util.getColor(R.color.lightBlueTestColor))
            } else {
                buttonCommentLike.setTextColor(Util.getColor(R.color.greyTestColor))
            }

            buttonCommentLike.setOnClickListener {
                viewModel.onCommentsLikePressed(comment, isUserLiked, position)
            }
        }
    }

    class EventCommentsViewHolder(val binding: ItemCommentBoardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment, viewModel: EventDetailViewModel) {

            binding.textComment.text = comment.content
            binding.textCreatedTime.text = comment.createdTime.toDisplaySentTime()

            if (viewModel.onProfileSearchComplete.value == 0) {
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
