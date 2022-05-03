package com.zoe.weshare.message

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.zoe.weshare.MainActivity
import com.zoe.weshare.data.ChatRoom
import com.zoe.weshare.databinding.FragmentChatroomBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.util.UserManager.weShareUser

class ChatRoomFragment : Fragment() {

    private lateinit var chatRoom: ChatRoom
    private lateinit var binding: FragmentChatroomBinding
    private lateinit var adapter: ChatRoomAdapter

    val currentUser = weShareUser

    private val viewModel by viewModels<ChatRoomViewModel> { getVmFactory(currentUser) }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentChatroomBinding.inflate(inflater, container, false)

        chatRoom = ChatRoomFragmentArgs.fromBundle(requireArguments()).selectedRoom

        viewModel.onViewDisplay(chatRoom)

        val recyclerView = binding.messagesRecyclerView

        adapter = ChatRoomAdapter(viewModel, chatRoom)
        recyclerView.adapter = adapter

        viewModel.liveMessages.observe(viewLifecycleOwner) {
            adapter.submitList(it) {
                recyclerView.post { recyclerView.scrollToPosition(adapter.itemCount - 1) }
            }
        }

        viewModel.roomTitle.observe(viewLifecycleOwner) {
            (activity as MainActivity).binding.toolbarFragmentTitleText.text = it
        }

        viewModel.newMessage.observe(viewLifecycleOwner) {
            viewModel.sendNewMessage(chatRoom.id, it)
        }

        setupSendBtn()
        return binding.root
    }

    private fun setupSendBtn() {
        binding.buttonSend.setOnClickListener {
            onSendComment()
        }

        binding.editBox.setOnKeyListener { _, keyCode, keyEvent ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_DOWN) {

                onSendComment()
                true

            } else false
        }
    }

    private fun onSendComment() {

        val newMessage = binding.editBox.text.toString()

        if (newMessage.isNotEmpty()) {
            viewModel.onSending(newMessage)
            binding.editBox.text?.clear()
        }
    }
}
