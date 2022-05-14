package com.zoe.weshare.message

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.data.ChatRoom
import com.zoe.weshare.data.Comment
import com.zoe.weshare.data.MessageItem
import com.zoe.weshare.data.UserInfo
import com.zoe.weshare.databinding.ItemMessageReceiveBinding
import com.zoe.weshare.databinding.ItemMessageSendBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.toDisplaySentTime
import com.zoe.weshare.util.ChatRoomType
import com.zoe.weshare.util.UserManager.weShareUser

class ChatRoomAdapter(val viewModel: ChatRoomViewModel, chatRoom: ChatRoom) :
    ListAdapter<MessageItem, RecyclerView.ViewHolder>(DiffCallback) {

    var targetUsersList = listOf<UserInfo>()
    var roomType: Int = -1

    init {
        getTargetUsers(chatRoom)
    }

    fun getTargetUsers(room: ChatRoom) {
        targetUsersList = room.usersInfo.filter { it.uid != weShareUser!!.uid }
        roomType = room.type
    }

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
                    holder.bind(it, targetUsersList, roomType)
                }
            }
            is ReceiveViewHolder -> {
                (itemType as MessageItem.OnReceiveSide).message?.let {
                    holder.bind(it, targetUsersList, roomType, viewModel)
                }
            }
        }
    }

    class SendViewHolder(private var binding: ItemMessageSendBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment, targetUsersList: List<UserInfo>, roomType: Int) {

            binding.apply {
                textMessage.text = comment.content
                textSentTime.text = comment.createdTime.toDisplaySentTime()

                when (roomType) {
                    ChatRoomType.PRIVATE.value -> {
                        val target = targetUsersList.single()

                        if (comment.whoRead.contains(target.uid)) {
                            unreadHint.text = "已讀"
                        } else {
                            unreadHint.text = "未讀"
                        }

                    }
                    ChatRoomType.MULTIPLE.value -> {

                        val isRead =
                            comment.whoRead.any { user -> targetUsersList.any { user == it.uid } }

                        if (isRead) {
                            unreadHint.text = "已讀"
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
            usersList: List<UserInfo>,
            roomType: Int,
            viewModel: ChatRoomViewModel,
        ) {
            binding.apply {
                textMessage.text = comment.content
                textSentTime.text = comment.createdTime.toDisplaySentTime()
                baseTargetImage.setOnClickListener {
                    viewModel.onNavigateToTargetProfile(comment.uid)
                }
            }

            if (usersList.isNotEmpty()) {
                val speaker = usersList.single { it.uid == comment.uid }

                bindImage(binding.imageTargeImage, speaker.image)

                if (roomType == ChatRoomType.MULTIPLE.value) {
                    binding.textTargetName.visibility = View.VISIBLE
                    binding.textTargetName.text = speaker.name
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
