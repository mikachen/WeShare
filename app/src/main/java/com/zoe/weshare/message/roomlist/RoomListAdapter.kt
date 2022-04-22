package com.zoe.weshare.message.roomlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.data.ChatRoom
import com.zoe.weshare.data.UserInfo
import com.zoe.weshare.databinding.ItemRelatedRoomListBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.toDisplaySentTime
import com.zoe.weshare.util.UserManager

class RoomListAdapter(private val onClickListener: RoomListOnClickListener) :
    ListAdapter<ChatRoom, RoomListAdapter.RoomListViewHolder>(DiffCall()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RoomListViewHolder {
        return RoomListViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RoomListViewHolder, position: Int) {
        val room = getItem(position)

        val targetObj = room.usersInfo?.single { it.uid != UserManager.userZoe.uid }


        //TODO 對方如果離開可能null
        if (targetObj != null) {
            holder.bind(room, targetObj)
        }

        holder.itemView.setOnClickListener {
            onClickListener.onClick(room)
        }
    }

    class RoomListOnClickListener(val clickListener: (room: ChatRoom) -> Unit) {
        fun onClick(selectedRoom: ChatRoom) = clickListener(selectedRoom)
    }

    class RoomListViewHolder(private val binding: ItemRelatedRoomListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(room: ChatRoom, targetObj: UserInfo) {


            binding.apply {
                bindImage(imageRoomImage, targetObj.image)
                textRoomTargetTitle.text = targetObj.name
                textLastMessage.text = room.lastMsg
                textLastSentTime.text = room.lastMsgSentTime.toDisplaySentTime()

            }
        }

        companion object {
            fun from(parent: ViewGroup): RoomListViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemRelatedRoomListBinding.inflate(layoutInflater, parent, false)

                return RoomListViewHolder(binding)
            }
        }
    }
}

class DiffCall : DiffUtil.ItemCallback<ChatRoom>() {
    override fun areItemsTheSame(
        oldItem: ChatRoom,
        newItem: ChatRoom,
    ): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(
        oldItem: ChatRoom,
        newItem: ChatRoom,
    ): Boolean {
        return oldItem.id == newItem.id
    }
}
