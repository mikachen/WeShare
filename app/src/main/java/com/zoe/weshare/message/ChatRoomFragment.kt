package com.zoe.weshare.message

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.MainActivity
import com.zoe.weshare.R
import com.zoe.weshare.data.ChatRoom
import com.zoe.weshare.databinding.FragmentChatroomBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.ext.hideKeyboard
import com.zoe.weshare.util.ChatRoomType
import com.zoe.weshare.util.UserManager.weShareUser
import com.zoe.weshare.util.Util.getStringWithStrParm

class ChatRoomFragment : Fragment() {

    private lateinit var chatRoom: ChatRoom
    private lateinit var binding: FragmentChatroomBinding
    private lateinit var adapter: ChatRoomAdapter
    private lateinit var recyclerView: RecyclerView

    private var hasLayoutSetup = false

    private val viewModel by viewModels<ChatRoomViewModel> { getVmFactory(weShareUser) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentChatroomBinding.inflate(inflater, container, false)

        chatRoom = ChatRoomFragmentArgs.fromBundle(requireArguments()).selectedRoom

        viewModel.setRoomAndMessages(chatRoom)


        viewModel.liveMessages.observe(viewLifecycleOwner) {

            if (!hasLayoutSetup && it.isNotEmpty()) {
                    setupLayoutAct()
            }

            /**
             * everytime getting new message list, getUnReadItems and update to read first
             * set flag on loopSize == 0 to prevent trigger when updating msg read.
             * */
            if (viewModel.loopSize == 0) {
                viewModel.getUnReadItems(it)
            }
        }

        viewModel.msgDisplay.observe(viewLifecycleOwner) {

            adapter.submitList(it) {
                recyclerView.post { recyclerView.scrollToPosition(adapter.itemCount - 1) }
            }
        }

        viewModel.navigateToTargetUser.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(
                    ChatRoomFragmentDirections.actionChatRoomFragmentToProfileFragment(it)
                )

                viewModel.navigateToProfileComplete()
            }
        }

        setupView()
        setupSendBtn()
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupView() {

        recyclerView = binding.messagesRecyclerView
        adapter = ChatRoomAdapter(viewModel)
        recyclerView.adapter = adapter

        recyclerView.setOnTouchListener { view, event ->
            view.hideKeyboard()
            false
        }

        val targetsInfo = chatRoom.usersInfo.filter { it.uid != weShareUser!!.uid }

        when (chatRoom.type) {
            ChatRoomType.PRIVATE.value ->
                if (chatRoom.participants.size == 2) {

                    setupTopBarTitle(targetsInfo.single().name)
//                    binding.textRoomTargetTitle.text = targetsInfo.single().name
                } else {
                    setupTopBarTitle("不明")
//                    binding.textRoomTargetTitle.text = "不明"
                }

            ChatRoomType.MULTIPLE.value -> {

                setupTopBarTitle(getStringWithStrParm(
                    R.string.room_list_event_title, chatRoom.eventTitle))
//
//                binding.textRoomTargetTitle.text =
//                    getStringWithStrParm(
//                        R.string.room_list_event_title,
//                        chatRoom.eventTitle
//                    )
            }
        }
    }

    fun setupTopBarTitle(title: String){
        (activity as MainActivity).binding.toolbarFragmentTitleText.text = title
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

    private fun setupLayoutAct() {
        recyclerView.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View?,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int,
            ) {
                if (bottom < oldBottom) {
                    recyclerView.postDelayed({
                        recyclerView.smoothScrollToPosition(
                            recyclerView.adapter!!.itemCount - 1
                        )
                    }, 100)
                }
            }
        })

        hasLayoutSetup = true
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

        (activity as MainActivity).window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }
}
