package com.zoe.weshare.message.roomlist

import android.view.LayoutInflater
import android.view.View
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
                if (room.type == ChatRoomType.MULTIPLE.value) {
                    bindImage(imageRoomImage, room.eventImage)
                    textRoomTargetTitle.text = Util.getStringWithStrParm(R.string.room_list_event_title, room.eventTitle)
                } else {
                    bindImage(imageRoomImage, targetsObj.single().image)
                    textRoomTargetTitle.text = targetsObj.single().name
                }
                textLastMessage.text = room.lastMsg
                textLastSentTime.text = room.lastMsgSentTime.toDisplaySentTime()

                if (room.lastMsgRead.contains(weShareUser!!.uid)){
                    unreadHint.visibility = View.INVISIBLE
                }else{
                    unreadHint.visibility = View.VISIBLE
                }
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

    fun modifyList(rooms: List<ChatRoom>){
        val list = rooms.filterNot { it.lastMsgSentTime == -1L }

        submitList(list)
    }

}

class DiffCall : DiffUtil.ItemCallback<ChatRoom>() {
    override fun areItemsTheSame(
        oldItem: ChatRoom,
        newItem: ChatRoom,
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: ChatRoom,
        newItem: ChatRoom,
    ): Boolean {
        return oldItem == newItem
    }
}
