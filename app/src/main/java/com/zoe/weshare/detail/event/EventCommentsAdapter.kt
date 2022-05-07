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
import com.zoe.weshare.data.UserInfo
import com.zoe.weshare.data.UserProfile
import com.zoe.weshare.databinding.ItemCommentBoardBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.getTimeAgoString
import com.zoe.weshare.util.Util

class EventCommentsAdapter(val viewModel: EventDetailViewModel, mContext: Context) :
    ListAdapter<Comment, EventCommentsAdapter.EventCommentsViewHolder>(DiffCall()) {

    val context = mContext
    val userInfo = Util.readInstanceProperty<UserInfo>(viewModel, "userInfo")

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): EventCommentsViewHolder {
        return EventCommentsViewHolder.from(parent)
    }

    override fun onBindViewHolder(holderEvent: EventCommentsViewHolder, position: Int) {
        val comment = getItem(position)
        holderEvent.bind(comment, viewModel, context)

        val whoLikedThisComment = comment.whoLiked
        val isUserLikeBefore: Boolean = whoLikedThisComment.contains(userInfo.uid)

        holderEvent.binding.apply {

            if (whoLikedThisComment.isNotEmpty()) {
                textLikesCount.text =
                    Util.getStringWithIntParm(R.string.number_who_liked, whoLikedThisComment.size)
            } else {
                textLikesCount.visibility = View.INVISIBLE
            }


            buttonCommentLike.isLiked = isUserLikeBefore

            buttonCommentLike.setOnClickListener {
                viewModel.onCommentsLikePressed(comment, isUserLikeBefore)
            }
        }
    }

    class EventCommentsViewHolder(val binding: ItemCommentBoardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment, viewModel: EventDetailViewModel, context: Context) {

            binding.textComment.text = comment.content
            binding.textCreatedTime.text = comment.createdTime.getTimeAgoString()

            binding.imageProfileAvatar.setOnClickListener {
                viewModel.onNavigateToTargetProfile(comment.uid)
            }

            if (viewModel.onProfileSearchComplete.value == 0) {
                if (viewModel.profileList.isNotEmpty()) {
                    val speaker = viewModel.profileList.singleOrNull { it.uid == comment.uid }
                    speaker?.let {
                        bindImage(binding.imageProfileAvatar, speaker.image)
                        binding.textProfileName.text = speaker.name

                        binding.moreBtn.setOnClickListener {
                            showPopupMenu(it,speaker,context,viewModel)
                        }

                    }
                }
            }
        }
        private fun showPopupMenu(view: View, sender: UserProfile, context: Context, viewModel: EventDetailViewModel) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.menuInflater.inflate(R.menu.block_popup_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_block -> { showAlterDialog(sender,context,viewModel) }
                }
                false
            }
            popupMenu.show()
        }

        private fun showAlterDialog(target: UserProfile, context : Context, viewModel: EventDetailViewModel) {
            val builder = AlertDialog.Builder(context)

            builder.apply {
                setTitle(Util.getStringWithStrParm(R.string.block_this_person_title, target.name))
                setMessage(Util.getString(R.string.block_this_person_message))
                setPositiveButton(Util.getString(R.string.confirm_yes)) { dialog, _ ->
                    viewModel.blockThisUser(target)
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
