package com.zoe.weshare.message

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.zoe.weshare.data.ChatRoom
import com.zoe.weshare.data.Comment
import com.zoe.weshare.databinding.FragmentChatroomBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.util.UserManager.userZoe

class ChatRoomFragment : Fragment() {

    lateinit var chatRoom: ChatRoom
    lateinit var binding: FragmentChatroomBinding
    private val viewModel by viewModels<ChatRoomViewModel> { getVmFactory(userZoe) }
    lateinit var adapter: ChatRoomAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        chatRoom = ChatRoomFragmentArgs.fromBundle(requireArguments()).selectedRoom

        binding = FragmentChatroomBinding.inflate(inflater, container, false)

        viewModel.getHistoryMessage(chatRoom.id)
        viewModel.onChatRoomDisplay(chatRoom)


        adapter = ChatRoomAdapter(viewModel, chatRoom)
        binding.messagesRecyclerView.adapter = adapter

        viewModel.messageItems.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            binding.messagesRecyclerView.scrollToPosition(adapter.itemCount - 1)
        }

        viewModel.targetInfo.observe(viewLifecycleOwner){
            binding.textRoomTargetTitle.text = it.name
        }

        viewModel.newMessage.observe(viewLifecycleOwner) {
            viewModel.sendNewMessage(chatRoom.id, it)
        }

        setUpBtn()
        return binding.root
    }


    private fun setUpBtn() {
        binding.buttonSend.setOnClickListener {
            val newMessage = binding.editBox.text.toString()

            if (newMessage.isNotEmpty()) {
                viewModel.onSending(newMessage)
                binding.editBox.text?.clear()
            } else {
                Toast.makeText(requireContext(), "Msg is empty", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnTesting.setOnClickListener {
            val mockMessage = binding.editBox.text.toString()

            if (mockMessage.isNotEmpty()) {
                viewModel._newMessage.value = Comment(
                    uid = "Ken1123",
                    content = mockMessage
                )
                binding.editBox.text?.clear()
            } else {
                Toast.makeText(requireContext(), "Msg is empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveLastMsgRecord(chatRoom.id, Comment())
    }

}
