package com.zoe.weshare.message.roomlist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.SimpleItemAnimator
import com.zoe.weshare.MainActivity
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.databinding.FragmentRoomListBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.util.UserManager.weShareUser

class RoomListFragment : Fragment() {

    private val viewModel by viewModels<RoomListViewModel> { getVmFactory(weShareUser) }
    lateinit var binding: FragmentRoomListBinding
    lateinit var adapter: RoomListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentRoomListBinding.inflate(inflater, container, false)

        // get live roomList
        val liveData = (activity as MainActivity).viewModel.liveChatRooms
        viewModel.onViewDisplay(liveData)


        viewModel.allRooms.observe(viewLifecycleOwner){
            viewModel.orderByTime(it)
        }

        viewModel.room.observe(viewLifecycleOwner) {
            adapter.modifyList(it)
        }

        viewModel.navigateToSelectedRoom.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(
                    NavGraphDirections.actionGlobalChatRoomFragment(it))

                viewModel.displayRoomDetailsComplete()
            }
        }

        setUpView()
        return binding.root
    }

    fun setUpView(){
        adapter = RoomListAdapter(
            RoomListAdapter.RoomListOnClickListener { selectedRoom ->
                viewModel.displayRoomDetails(selectedRoom)
            }
        )

        binding.roomlistRecyclerView.adapter = adapter
    }
}
