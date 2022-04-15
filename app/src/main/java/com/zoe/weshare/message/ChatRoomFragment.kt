package com.zoe.weshare.message

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.zoe.weshare.data.Author
import com.zoe.weshare.data.Comment
import com.zoe.weshare.databinding.FragmentChatroomBinding
import com.zoe.weshare.ext.getVmFactory


class ChatRoomFragment : Fragment() {

    val author = Author(
        name = "Zoe Lo",
        uid = "zoe1018",
        image = "https://www.computerhope.com/jargon/a/android.png"
    )

    lateinit var binding: FragmentChatroomBinding
    private val viewModel by viewModels<MessageViewModel> { getVmFactory(author) }
    lateinit var adapter: MessageAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentChatroomBinding.inflate(inflater, container, false)

        viewModel.getHistoryMessage("0l0LsKb7AnjXSWU1X4QW")

        adapter = MessageAdapter(viewModel)
        binding.messagesRecyclerView.adapter = adapter

        viewModel.messageItems.observe(viewLifecycleOwner){
            adapter.submitList(it)
            binding.messagesRecyclerView.scrollToPosition(adapter.itemCount -1)
        }

        viewModel.newMessage.observe(viewLifecycleOwner) {
            viewModel.sendNewMessage("0l0LsKb7AnjXSWU1X4QW", it)
        }



        setUpBtn()
        return binding.root
    }

    private fun setUpBtn() {
        binding.buttonSend.setOnClickListener {
            val newMessage = binding.editBox.text.toString()

            if (newMessage.isNotEmpty()){
                viewModel._newMessage.value = Comment(
                    uid = author.uid,
                    content = newMessage
                )
            }else{
                Toast.makeText(requireContext(),"comment is empty", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnTesting.setOnClickListener {
            val mockMessage = binding.editBox.text.toString()

            if (mockMessage.isNotEmpty()){
                viewModel._newMessage.value = Comment(
                    uid = "Ken1123",
                    content = mockMessage
                )
            }else{
                Toast.makeText(requireContext(),"comment is empty", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
