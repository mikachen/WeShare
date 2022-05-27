package com.zoe.weshare.detail.event

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.R
import com.zoe.weshare.data.Comment
import com.zoe.weshare.data.UserProfile
import com.zoe.weshare.databinding.ItemCommentBoardBinding
import com.zoe.weshare.detail.isUserLikedBefore
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.getTimeAgoString
import com.zoe.weshare.util.UserManager.weShareUser
import com.zoe.weshare.util.Util
import com.zoe.weshare.util.Util.getString
import com.zoe.weshare.util.Util.getStringWithIntParm
import com.zoe.weshare.util.Util.getStringWithStrParm

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

    class EventCommentsViewHolder(val binding: ItemCommentBoardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment, viewModel: EventDetailViewModel) {

            val whoLikedList = comment.whoLiked

            binding.apply {

                buttonCommentLike.isLiked = isUserLikedBefore(whoLikedList)

                buttonCommentLike.setOnClickListener {
                    viewModel.onCommentsLikePressed(comment)
                }

                textComment.text = comment.content
                textCreatedTime.text = comment.createdTime.getTimeAgoString()

                imageUserAvatar.setOnClickListener {
                    viewModel.onNavigateToTargetProfile(comment.uid)
                }

                textLikesCount.text =
                    getStringWithIntParm(R.string.number_who_liked, whoLikedList.size)

                textLikesCount.visibility =
                    if (whoLikedList.isEmpty()) { View.INVISIBLE } else { View.VISIBLE }

                buttonMore.visibility =
                    if (isSpeakerUserSelf(comment)) { View.INVISIBLE } else { View.VISIBLE }
            }


            if (viewModel.isGetProfileDone()) {
                val speaker = viewModel.getSpeakerProfile(comment)

                speaker?.let {
                    bindImage(binding.imageUserAvatar, speaker.image)
                    binding.textProfileName.text = speaker.name

                    binding.buttonMore.setOnClickListener {
                        showPopupMenu(it, speaker, viewModel)
                    }
                }
            }
        }

        private fun isSpeakerUserSelf(comment: Comment): Boolean {
            return comment.uid == weShareUser.uid
        }

        private fun showPopupMenu(
            view: View,
            target: UserProfile,
            viewModel: EventDetailViewModel,
        ) {
            val context = view.context
            val popupMenu = PopupMenu(context, view)

            popupMenu.menuInflater.inflate(R.menu.report_user_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_block_user -> {
                        showAlterDialog(target, context, viewModel)
                    }

                    R.id.action_report_violations -> {
                        viewModel.onNavigateToReportDialog(target)
                    }
                }
                false
            }
            popupMenu.show()
        }

        private fun showAlterDialog(
            target: UserProfile,
            context: Context,
            viewModel: EventDetailViewModel,
        ) {
            val builder = AlertDialog.Builder(context)

            builder.apply {
                setTitle(getStringWithStrParm(R.string.block_this_person_title, target.name))
                setMessage(getString(R.string.block_this_person_message))
                setPositiveButton(Util.getString(R.string.confirm_yes)) { dialog, _ ->
                    viewModel.blockUser(target)
                    dialog.cancel()
                }

                setNegativeButton(Util.getString(R.string.confirm_no)) { dialog, _ ->
                    dialog.cancel()
                }
            }

            builder.create().show()
        }

        companion object {
            fun from(parent: ViewGroup): EventCommentsViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemCommentBoardBinding
                    .inflate(layoutInflater, parent, false)

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
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: Comment,
        newItem: Comment,
    ): Boolean {
        return oldItem == newItem
    }
}
