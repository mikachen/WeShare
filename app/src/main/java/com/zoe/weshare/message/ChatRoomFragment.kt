package com.zoe.weshare.message

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.MainActivity
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.R
import com.zoe.weshare.data.ChatRoom
import com.zoe.weshare.databinding.FragmentChatroomBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.util.ChatRoomType
import com.zoe.weshare.util.UserManager.weShareUser
import com.zoe.weshare.util.Util.getStringWithStrParm


class ChatRoomFragment : Fragment() {

    private lateinit var chatRoom: ChatRoom
    private lateinit var binding: FragmentChatroomBinding
    private lateinit var adapter: ChatRoomAdapter
    private lateinit var recyclerView: RecyclerView

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

        viewModel.liveMessages.observe(viewLifecycleOwner) {
            adapter.submitList(it) {
                adapter.notifyDataSetChanged()
                recyclerView.post { recyclerView.scrollToPosition(adapter.itemCount - 1) }
            }
        }

        viewModel.newMessage.observe(viewLifecycleOwner) {
            viewModel.sendNewMessage(chatRoom.id, it)
        }

        viewModel.navigateToTargetUser.observe(viewLifecycleOwner){
            it?.let{
                findNavController().navigate(NavGraphDirections.actionGlobalProfileFragment(it))
                viewModel.navigateToProfileComplete()
            }
        }

        setupView()
        setupSendBtn()
        return binding.root
    }

    private fun setupView() {

        recyclerView = binding.messagesRecyclerView

        adapter = ChatRoomAdapter(viewModel, chatRoom)

        recyclerView.adapter = adapter

        binding.textRoomTargetTitle.text =
            when (chatRoom.type) {
                ChatRoomType.PRIVATE.value ->
                    chatRoom.usersInfo.single { it.uid != weShareUser!!.uid }.name

                ChatRoomType.MULTIPLE.value -> getStringWithStrParm(
                    R.string.room_list_event_title,
                    chatRoom.eventTitle
                )

                else -> "unKnow"
            }

        recyclerView.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View?,
                left: Int, top: Int, right: Int, bottom: Int,
                oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int,
            ) {
                if (bottom < oldBottom) {
                    recyclerView.postDelayed({
                        recyclerView.smoothScrollToPosition(
                            recyclerView.adapter!!.itemCount - 1)
                    }, 100)
                }
            }
        })
    }

    private fun setupSendBtn() {

        binding.toolbarArrowBack.setOnClickListener {
            findNavController().navigateUp()
        }

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

    override fun onResume() {
        super.onResume()
//        (activity as MainActivity).window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
//        (activity as MainActivity).window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        (activity as MainActivity).window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

    }
}
