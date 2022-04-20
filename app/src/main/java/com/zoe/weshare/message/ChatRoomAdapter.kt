package com.zoe.weshare.message

import android.view.LayoutInflater
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
import com.zoe.weshare.util.UserManager

class ChatRoomAdapter(val viewModel: ChatRoomViewModel, chatRoom: ChatRoom) :
    ListAdapter<MessageItem, RecyclerView.ViewHolder>(DiffCallback) {

    val target = chatRoom.usersInfo?.single { it.uid != UserManager.userZoe.uid }

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
                (itemType as MessageItem.OnSendSide).message?.let { holder.bind(it) }
            }
            is ReceiveViewHolder -> {
                (itemType as MessageItem.OnReceiveSide).message?.let { holder.bind(it, viewModel, target) }
            }
        }
    }

    class SendViewHolder(private var binding: ItemMessageSendBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment) {
            binding.textMessage.text = comment.content
            binding.textSentTime.text = comment.createdTime.toDisplaySentTime()
        }
    }

    class ReceiveViewHolder(private var binding: ItemMessageReceiveBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment, viewModel: ChatRoomViewModel, targetInfo: UserInfo?) {
            binding.textMessage.text = comment.content
            binding.textSentTime.text = comment.createdTime.toDisplaySentTime()

            if (targetInfo != null) {
                bindImage(binding.imageTargeImage, targetInfo.image)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<MessageItem>() {
        override fun areItemsTheSame(oldItem: MessageItem, newItem: MessageItem): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: MessageItem, newItem: MessageItem): Boolean {
            return oldItem.id == newItem.id
        }

        private const val ITEM_VIEW_TYPE_SEND = 0x00
        private const val ITEM_VIEW_TYPE_RECEIVE = 0x01
    }
}
