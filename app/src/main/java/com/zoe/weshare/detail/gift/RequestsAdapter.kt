package com.zoe.weshare.detail.gift

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
import com.zoe.weshare.detail.hasUserLikedBefore
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.getTimeAgoString
import com.zoe.weshare.util.UserManager
import com.zoe.weshare.util.Util.getString
import com.zoe.weshare.util.Util.getStringWithIntParm
import com.zoe.weshare.util.Util.getStringWithStrParm

class RequestGiftAdapter(val viewModel: GiftDetailViewModel, context: Context) :
    ListAdapter<Comment, RequestGiftAdapter.ViewHolder>(DiffCall()) {

    private val mContext = context

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val comment = getItem(position)
        holder.bind(comment, viewModel, mContext)
    }


    class ViewHolder(val binding: ItemCommentBoardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment, viewModel: GiftDetailViewModel, context: Context) {

            val whoLikedList = comment.whoLiked

            binding.apply {

                textComment.text = comment.content
                textCreatedTime.text = comment.createdTime.getTimeAgoString()

                if (whoLikedList.isNotEmpty()) {
                    textLikesCount.text =
                        getStringWithIntParm(R.string.number_who_liked, whoLikedList.size)

                    textLikesCount.visibility = View.VISIBLE
                } else {
                    textLikesCount.visibility = View.INVISIBLE
                }

                //change button state
                buttonCommentLike.isLiked = hasUserLikedBefore(whoLikedList)

                buttonCommentLike.setOnClickListener {
                    viewModel.onCommentsLikePressed(comment)
                }

                imageUserAvatar.setOnClickListener {
                    viewModel.onNavigateToTargetProfile(comment.uid)
                }

                if (speakerIsUserSelf(comment)) {
                    moreBtn.visibility = View.INVISIBLE
                } else {
                    moreBtn.visibility = View.VISIBLE
                }
            }

            if (viewModel.isGetProfileDone()) {
                val target = viewModel.getSpeakerProfile(comment)

                target?.let {
                    bindImage(binding.imageUserAvatar, target.image)
                    binding.textProfileName.text = target.name

                    binding.moreBtn.setOnClickListener {
                        showPopupMenu(it, target, context, viewModel)
                    }
                }
            }
        }



        private fun speakerIsUserSelf(comment: Comment): Boolean {
            return comment.uid == UserManager.weShareUser.uid
        }

        private fun showPopupMenu(
            view: View,
            target: UserProfile,
            context: Context,
            viewModel: GiftDetailViewModel,
        ) {
            val popupMenu = PopupMenu(view.context, view)
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
            viewModel: GiftDetailViewModel,
        ) {
            val builder = AlertDialog.Builder(context)

            builder.apply {
                setTitle(getStringWithStrParm(R.string.block_this_person_title, target.name))
                setMessage(getString(R.string.block_this_person_message))
                setPositiveButton(getString(R.string.confirm_yes)) { dialog, _ ->
                    viewModel.blockUser(target)
                    dialog.cancel()
                }

                setNegativeButton(getString(R.string.confirm_no)) { dialog, _ ->
                    dialog.cancel()
                }
            }

            builder.create().show()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemCommentBoardBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
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
