package com.zoe.weshare.message.roomlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.zoe.weshare.MainActivity
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.R
import com.zoe.weshare.databinding.FragmentRoomListBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.ext.showToast
import com.zoe.weshare.util.UserManager.weShareUser

class RoomListFragment : Fragment() {

    private val viewModel by viewModels<RoomListViewModel> { getVmFactory(weShareUser) }
    lateinit var binding: FragmentRoomListBinding
    lateinit var adapter: RoomListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRoomListBinding.inflate(inflater, container, false)

        // get live roomList
        val liveRoomList = (activity as MainActivity).viewModel.liveChatRooms
        viewModel.setRoomList(liveRoomList)

        viewModel.allRooms.observe(viewLifecycleOwner) {
            adapter.modifyList(it)
        }

        viewModel.navigateToSelectedRoom.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(NavGraphDirections.actionGlobalChatRoomFragment(it))
                viewModel.displayRoomDetailsComplete()
            }
        }

        viewModel.leaveRoomComplete.observe(viewLifecycleOwner) {
            it?.let {
                activity.showToast(getString(R.string.toast_has_leave_room))
                viewModel.showToastDone()
            }
        }

        setUpView()
        return binding.root
    }

    fun setUpView() {
        adapter = RoomListAdapter(viewModel)
        binding.roomlistRecyclerView.adapter = adapter
    }
}
