package com.zoe.weshare.message

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.R
import com.zoe.weshare.data.Comment
import com.zoe.weshare.data.MessageItem
import com.zoe.weshare.databinding.ItemMessageReceiveBinding
import com.zoe.weshare.databinding.ItemMessageSendBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.toDisplaySentTime
import com.zoe.weshare.util.ChatRoomType
import com.zoe.weshare.util.UserManager.weShareUser
import com.zoe.weshare.util.Util.getStringWithIntParm

class ChatRoomAdapter(val viewModel: ChatRoomViewModel) :
    ListAdapter<MessageItem, RecyclerView.ViewHolder>(DiffCallback) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is MessageItem.OnSendSide -> ITEM_VIEW_TYPE_SEND
            is MessageItem.OnReceiveSide -> ITEM_VIEW_TYPE_RECEIVE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_SEND -> SendViewHolder(
                ItemMessageSendBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            ITEM_VIEW_TYPE_RECEIVE -> ReceiveViewHolder(
                ItemMessageReceiveBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemType = getItem(position)
        when (holder) {
            is SendViewHolder -> {
                (itemType as MessageItem.OnSendSide).message?.let {
                    holder.bind(it, viewModel)
                }
            }
            is ReceiveViewHolder -> {
                (itemType as MessageItem.OnReceiveSide).message?.let {
                    holder.bind(it, viewModel)
                }
            }
        }
    }

    class SendViewHolder(private var binding: ItemMessageSendBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment, viewModel: ChatRoomViewModel) {

            val roomType = viewModel.chatRoom.type
            val allUsersList = viewModel.chatRoom.usersInfo

            binding.apply {
                textMessage.text = comment.content
                textSentTime.text = comment.createdTime.toDisplaySentTime()

                when (roomType) {
                    ChatRoomType.PRIVATE.value -> {
                        if (allUsersList.size == 2) {
                            val target = allUsersList.single { it.uid != weShareUser!!.uid }

                            if (comment.whoRead.contains(target.uid)) {
                                unreadHint.text = "已讀"
                            } else {
                                unreadHint.text = "未讀"
                            }
                        } else {
                            unreadHint.text = ""
                        }
                    }

                    ChatRoomType.MULTIPLE.value -> {

                        val whoReadList = comment.whoRead as MutableList
                        whoReadList.remove(weShareUser!!.uid)

                        if (whoReadList.isNotEmpty()) {
                            unreadHint.text = getStringWithIntParm(
                                R.string.message_whoRead_count,
                                whoReadList.size
                            )
                        } else {
                            unreadHint.text = "未讀"
                        }
                    }
                }
            }
        }
    }

    class ReceiveViewHolder(private var binding: ItemMessageReceiveBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            comment: Comment,
            viewModel: ChatRoomViewModel,
        ) {
            val roomType = viewModel.chatRoom.type
            val targetUsersList = viewModel.chatRoom.usersInfo

            binding.apply {
                textMessage.text = comment.content
                textSentTime.text = comment.createdTime.toDisplaySentTime()

                baseTargetImage.setOnClickListener {
                    viewModel.onNavigateToTargetProfile(comment.uid)
                }
            }

            when (roomType) {
                ChatRoomType.PRIVATE.value -> {
                    if (targetUsersList.size == 2) {
                        val target = targetUsersList.single { it.uid != weShareUser!!.uid }

                        bindImage(binding.imageTargeImage, target.image)
                        binding.textTargetName.visibility = View.GONE
                    } else {
                        // target has left the room
                        binding.textTargetName.visibility = View.VISIBLE
                        binding.textTargetName.text = "此人已離開聊天室"
                    }
                }

                ChatRoomType.MULTIPLE.value -> {
                    val speaker = targetUsersList.single { it.uid == comment.uid }

                    bindImage(binding.imageTargeImage, speaker.image)
                    binding.textTargetName.text = speaker.name
                    binding.textTargetName.visibility = View.VISIBLE

                    // TODO deal with user who leave this room
                }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<MessageItem>() {
        override fun areItemsTheSame(oldItem: MessageItem, newItem: MessageItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MessageItem, newItem: MessageItem): Boolean {
            return oldItem == newItem
        }

        private const val ITEM_VIEW_TYPE_SEND = 0x00
        private const val ITEM_VIEW_TYPE_RECEIVE = 0x01
    }
}
