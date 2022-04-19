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
import com.zoe.weshare.util.Util.author

class ChatRoomFragment : Fragment() {

    lateinit var binding: FragmentChatroomBinding
    private val viewModel by viewModels<ChatRoomViewModel> { getVmFactory(author) }
    lateinit var adapter: ChatRoomAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        val selectedRoom = ChatRoomFragmentArgs.fromBundle(requireArguments()).selectedRoom

        binding = FragmentChatroomBinding.inflate(inflater, container, false)

        viewModel.getHistoryMessage(selectedRoom.id)

        selectedRoom.participants?.let { viewModel.getUserList(it) }

        adapter = ChatRoomAdapter(viewModel)
        binding.messagesRecyclerView.adapter = adapter

        viewModel.messageItems.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            binding.messagesRecyclerView.scrollToPosition(adapter.itemCount - 1)
        }

        // drawing the user avatar image and nickName after searching user's profile docs
        viewModel.onProfileSearching.observe(viewLifecycleOwner) {
            if (it == 0) {
                adapter.notifyDataSetChanged()
            }
        }

        viewModel.newMessage.observe(viewLifecycleOwner) {
            viewModel.sendNewMessage(selectedRoom.id, it)
        }

        setUpTitle(selectedRoom)
        setUpBtn()
        return binding.root
    }

    private fun setUpTitle(room: ChatRoom) {
        binding.textRoomTitle.text = room.title
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
}
