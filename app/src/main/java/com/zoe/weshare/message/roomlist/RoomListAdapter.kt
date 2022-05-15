package com.zoe.weshare.message.roomlist

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.R
import com.zoe.weshare.data.ChatRoom
import com.zoe.weshare.databinding.ItemRelatedRoomListBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.getPhoneVibrate
import com.zoe.weshare.ext.toDisplaySentTime
import com.zoe.weshare.util.ChatRoomType
import com.zoe.weshare.util.UserManager.weShareUser
import com.zoe.weshare.util.Util
import com.zoe.weshare.util.Util.getString

class RoomListAdapter(val viewModel: RoomListViewModel, private val mContext: Context) :
    ListAdapter<ChatRoom, RoomListAdapter.RoomListViewHolder>(DiffCall()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RoomListViewHolder {
        return RoomListViewHolder.from(parent, mContext)
    }

    override fun onBindViewHolder(holder: RoomListViewHolder, position: Int) {
        val room = getItem(position)

        holder.bind(room, viewModel)

        holder.itemView.setOnClickListener {
            when (room.type) {
                ChatRoomType.MULTIPLE.value -> {
                    viewModel.displayRoomDetails(room)

                }
                ChatRoomType.PRIVATE.value -> {
                    val privateRooms =
                        viewModel.roomsWithTargetProfile.filter { it.id == room.id }

                    viewModel.displayRoomDetails(privateRooms.single())
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    class RoomListViewHolder(
        private val binding: ItemRelatedRoomListBinding,
        val context: Context,
    ) : RecyclerView.ViewHolder(binding.root), View.OnTouchListener {

        private lateinit var room: ChatRoom
        private lateinit var viewModel: RoomListViewModel
        private var onPressTime: Long = 0


        fun bind(room: ChatRoom, viewModel: RoomListViewModel) {
            this.room = room
            this.viewModel = viewModel

            binding.apply {
                textLastMessage.text = room.lastMsg
                textLastSentTime.text = room.lastMsgSentTime.toDisplaySentTime()

                //drawing room image, room title
                when (room.type) {
                    ChatRoomType.MULTIPLE.value -> {
                        bindImage(imageRoomImage, room.eventImage)
                        textRoomTargetTitle.text =
                            Util.getStringWithStrParm(R.string.room_list_event_title,
                                room.eventTitle)
                    }

                    ChatRoomType.PRIVATE.value -> {

                        if (viewModel.searchCount.value == 0) {
                            val privateRooms =
                                viewModel.roomsWithTargetProfile.filter { it.id == room.id }

                            if (privateRooms.isNotEmpty()) {
                                val targetsObj = privateRooms.single().targetProfile

                                //targetsObj is an array, can be empty if target leave the room
                                if (targetsObj.isNotEmpty()) {
                                    bindImage(imageRoomImage, targetsObj.single().image)
                                    textRoomTargetTitle.text = targetsObj.single().name
                                } else {
                                    textRoomTargetTitle.text = "此用戶已離開聊天室"
                                }
                            } else {
                                //naturally won't draw anything
                            }
                        }
                    }
                }

                // onCheck if there's unRead msg for weShareUser
                if (room.lastMsgRead.contains(weShareUser!!.uid)) {
                    unreadHint.visibility = View.INVISIBLE
                } else {
                    unreadHint.visibility = View.VISIBLE
                }
            }
        }

        init {
            binding.root.setOnTouchListener(this)
        }

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_DOWN) {
                onPressTime = System.currentTimeMillis()
                view.tag = true

            } else if (view.isPressed && view.tag == true) {
                val eventDuration = event.eventTime - event.downTime

                if (eventDuration > 300) {
                    view.tag = false

                    showPopupMenu(binding.textLastSentTime, room, context, viewModel)
                    getPhoneVibrate(context)

                    return true
                }
            }
            return false
        }

        fun showPopupMenu(
            view: View,
            room: ChatRoom,
            context: Context,
            viewModel: RoomListViewModel,
        ) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.menuInflater.inflate(R.menu.chatroom_popup_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.leave_chatroom -> {
                        showAlterDialog(room, context, viewModel)
                    }
                }
                false
            }
            popupMenu.show()
        }

        private fun showAlterDialog(
            room: ChatRoom,
            context: Context,
            viewModel: RoomListViewModel,
        ) {
            val builder = AlertDialog.Builder(context)

            builder.apply {
                setMessage(getString(R.string.leave_this_chatroom_message))
                setPositiveButton(getString(R.string.confirm_yes)) { dialog, _ ->
                    viewModel.onLeaveRoom(room)

                    dialog.cancel()
                }

                setNegativeButton(getString(R.string.confirm_no)) { dialog, _ ->
                    dialog.cancel()
                }
            }

            builder.create().show()
        }

        companion object {
            fun from(parent: ViewGroup, mContext: Context): RoomListViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemRelatedRoomListBinding.inflate(layoutInflater, parent, false)

                return RoomListViewHolder(binding, mContext)
            }
        }
    }

    fun modifyList(rooms: List<ChatRoom>) {
        rooms as MutableList
        rooms.sortByDescending { it.lastMsgSentTime }

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
