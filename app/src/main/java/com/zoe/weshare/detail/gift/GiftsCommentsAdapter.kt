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
import com.zoe.weshare.data.UserInfo
import com.zoe.weshare.data.UserProfile
import com.zoe.weshare.databinding.ItemCommentBoardBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.getTimeAgoString
import com.zoe.weshare.util.UserManager
import com.zoe.weshare.util.Util.getString
import com.zoe.weshare.util.Util.getStringWithIntParm
import com.zoe.weshare.util.Util.getStringWithStrParm
import com.zoe.weshare.util.Util.readInstanceProperty

class GiftsCommentsAdapter(val viewModel: GiftDetailViewModel, mContext: Context) :
    ListAdapter<Comment, GiftsCommentsAdapter.GiftCommentsViewHolder>(DiffCall()) {

    val context = mContext
    val userInfo = readInstanceProperty<UserInfo>(viewModel, "userInfo")

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): GiftCommentsViewHolder {
        return GiftCommentsViewHolder.from(parent)
    }

    override fun onBindViewHolder(holderGift: GiftCommentsViewHolder, position: Int) {
        val comment = getItem(position)
        holderGift.bind(comment, viewModel, context)

        val whoLikedList = viewModel.updateCommentLike[position].whoLiked
        val isUserLiked: Boolean = whoLikedList.contains(userInfo.uid)

        val userReceiveGift: Boolean =
            viewModel.selectedGiftDisplay.value?.whoGetGift == comment.uid

        holderGift.binding.apply {

            if (whoLikedList.isNotEmpty()) {
                textLikesCount.text =
                    getStringWithIntParm(R.string.number_who_liked, whoLikedList.size)
            } else {
                textLikesCount.visibility = View.INVISIBLE
            }

            buttonCommentLike.isLiked = isUserLiked

            buttonCommentLike.setOnClickListener {
                viewModel.onCommentsLikePressed(comment, isUserLiked, position)
            }

            if (userReceiveGift) {
                lottieReceivedGift.visibility = View.VISIBLE
            } else {
                lottieReceivedGift.visibility = View.INVISIBLE
            }
        }
    }

    class GiftCommentsViewHolder(val binding: ItemCommentBoardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment, viewModel: GiftDetailViewModel, context: Context) {

            binding.textComment.text = comment.content
            binding.textCreatedTime.text = comment.createdTime.getTimeAgoString()

            binding.imageProfileAvatar.setOnClickListener {
                viewModel.onNavigateToTargetProfile(comment.uid)
            }

            if(comment.uid == UserManager.weShareUser!!.uid){
                binding.moreBtn.visibility = View.INVISIBLE
            }else{
                binding.moreBtn.visibility = View.VISIBLE
            }

            // displaying user's image and name
            if (viewModel.onProfileSearchComplete.value == 0) {
                if (viewModel.profileList.isNotEmpty()) {
                    val sender = viewModel.profileList.singleOrNull { it.uid == comment.uid }
                    sender?.let {
                        bindImage(binding.imageProfileAvatar, sender.image)
                        binding.textProfileName.text = sender.name

                        binding.moreBtn.setOnClickListener {
                            showPopupMenu(it, sender, context, viewModel)
                        }
                    }
                }
            }
        }

        private fun showPopupMenu(
            view: View,
            sender: UserProfile,
            context: Context,
            viewModel: GiftDetailViewModel
        ) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.menuInflater.inflate(R.menu.block_popup_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_block -> { showAlterDialog(sender, context, viewModel) }
                }
                false
            }
            popupMenu.show()
        }

        private fun showAlterDialog(
            target: UserProfile,
            context: Context,
            viewModel: GiftDetailViewModel
        ) {
            val builder = AlertDialog.Builder(context)

            builder.apply {
                setTitle(getStringWithStrParm(R.string.block_this_person_title, target.name))
                setMessage(getString(R.string.block_this_person_message))
                setPositiveButton(getString(R.string.confirm_yes)) { dialog, _ ->
                    viewModel.blockThisUser(target)
                    dialog.cancel()
                }

                setNegativeButton(getString(R.string.confirm_no)) { dialog, _ ->
                    dialog.cancel()
                }
            }

            builder.create().show()
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
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: Comment,
        newItem: Comment,
    ): Boolean {
        return oldItem == newItem
    }
}
