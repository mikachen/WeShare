package com.zoe.weshare.message

import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.zoe.weshare.MainActivity
import com.zoe.weshare.data.ChatRoom
import com.zoe.weshare.data.Comment
import com.zoe.weshare.databinding.FragmentChatroomBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.util.Const.PATH_CHATROOM
import com.zoe.weshare.util.Const.SUB_PATH_CHATROOM_MESSAGE
import com.zoe.weshare.util.Logger
import com.zoe.weshare.util.UserManager.userZoe

class ChatRoomFragment : Fragment() {

    private lateinit var chatRoom: ChatRoom
    private lateinit var binding: FragmentChatroomBinding
    private lateinit var adapter: ChatRoomAdapter

    private val viewModel by viewModels<ChatRoomViewModel> { getVmFactory(userZoe) }

    private val db = FirebaseFirestore.getInstance()
    lateinit var newMsgQuery: Query

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentChatroomBinding.inflate(inflater, container, false)

        chatRoom = ChatRoomFragmentArgs.fromBundle(requireArguments()).selectedRoom

        viewModel.getHistoryMessage(chatRoom.id)
        viewModel.onUserInfoDisplay(chatRoom)

        val recyclerView = binding.messagesRecyclerView

        adapter = ChatRoomAdapter(viewModel, chatRoom)
        recyclerView.adapter = adapter

        viewModel.messageItems.observe(viewLifecycleOwner) {
            adapter.submitList(it){
                recyclerView.post { recyclerView.scrollToPosition(adapter.itemCount - 1) }
            }
        }

        viewModel.targetInfo.observe(viewLifecycleOwner) {
//            binding.textRoomTargetTitle.text = it.name
            (activity as MainActivity).binding.toolbarFragmentTitleText.text = it.name
        }

        viewModel.newMessage.observe(viewLifecycleOwner) {
            viewModel.sendNewMessage(chatRoom.id, it)
        }

        newMsgQuery = db.collection(PATH_CHATROOM).document(chatRoom.id)
            .collection(SUB_PATH_CHATROOM_MESSAGE)
            .orderBy("createdTime", Query.Direction.DESCENDING).limit(1)

        newMsgQuery.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Logger.d("SnapshotListen failed: $error")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = mutableListOf<Comment>()

                for (document in snapshot.documents) {
                    Logger.d(document.id + " => " + document.data)

                    document.toObject(Comment::class.java)?.let {
                        list.add(it)
                    }
                }

                viewModel.onNewMsgListened(list)

            } else {
                Logger.d("SnapshotListen Current data: null")
            }
        }


        setupSendBtn()
        return binding.root
    }


    private fun setupSendBtn() {
        binding.buttonSend.setOnClickListener {
            val newMessage = binding.editBox.text.toString()

            if (newMessage.isNotEmpty()) {
                viewModel.onSending(newMessage)
                    binding.editBox.text?.clear()
            }
        }

        binding.btnTesting.setOnClickListener {
            val mockMessage = binding.editBox.text.toString()

            if (mockMessage.isNotEmpty()) {
                viewModel._newMessage.value = Comment(
                    uid = "Ken1123",
                    content = mockMessage,
                    createdTime = Calendar.getInstance().timeInMillis

                )
                binding.editBox.text?.clear()
            } else {
                Toast.makeText(requireContext(), "Msg is empty", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
