package com.zoe.weshare.message.roomlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.R
import com.zoe.weshare.data.ChatRoom
import com.zoe.weshare.data.UserInfo
import com.zoe.weshare.databinding.ItemRelatedRoomListBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.toDisplaySentTime
import com.zoe.weshare.util.ChatRoomType
import com.zoe.weshare.util.UserManager
import com.zoe.weshare.util.UserManager.weShareUser
import com.zoe.weshare.util.Util

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

        val targetsObj = room.usersInfo.filter { it.uid != weShareUser!!.uid }

        // TODO 對方如果離開可能null
        if (targetsObj.isNotEmpty()) {
            holder.bind(room, targetsObj)
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
        fun bind(room: ChatRoom, targetsObj: List<UserInfo>) {

            binding.apply {
                if (room.type == ChatRoomType.MULTIPLE.value){
                    bindImage(imageRoomImage, room.eventImage)
                    textRoomTargetTitle.text = Util.getStringWithStrParm(R.string.room_list_event_title,room.eventTitle)
                }else{
                    bindImage(imageRoomImage, targetsObj.single().image)
                    textRoomTargetTitle.text = targetsObj.single().name
                }
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
