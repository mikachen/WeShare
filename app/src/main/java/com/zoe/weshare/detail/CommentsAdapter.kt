package com.zoe.weshare.detail

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.data.Comment
import com.zoe.weshare.data.UserProfile
import com.zoe.weshare.databinding.ItemCommentBoardBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.toDisplayFormat

class CommentsAdapter(val viewModel: GiftDetailViewModel) :
    ListAdapter<Comment, CommentsAdapter.CommentsViewHolder>(DiffCall()) {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): CommentsViewHolder {
        return CommentsViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: CommentsViewHolder, position: Int) {
        val comment = getItem(position)
        val userList = viewModel.user.value

        holder.bind(comment, userList)
    }


    class CommentsViewHolder(private val binding: ItemCommentBoardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment, userList: List<UserProfile>?) {

            binding.textComment.text = comment.content
            binding.textCreatedTime.text = comment.createdTime.toDisplayFormat()

            if (!userList.isNullOrEmpty()) {
                for (element in userList) {
                    if (element.uid == comment.uid) {
                        binding.textProfileName.text = element.name
                        bindImage(binding.imageProfileAvatar, element.image)
                    }
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): CommentsViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemCommentBoardBinding.inflate(layoutInflater, parent, false)

                return CommentsViewHolder(binding)
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
