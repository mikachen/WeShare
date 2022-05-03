package com.zoe.weshare.message.roomlist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
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

        // update room list whenever go back to RoomListFragment
        viewModel.searchChatRooms()

        adapter = RoomListAdapter(
            RoomListAdapter.RoomListOnClickListener { selectedRoom ->
                viewModel.displayRoomDetails(selectedRoom)
            }
        )

        binding.roomlistRecyclerView.adapter = adapter

        viewModel.room.observe(viewLifecycleOwner) {
            Log.d("roomlist","$it")
            adapter.submitList(it)
        }

        viewModel.navigateToSelectedRoom.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(
                    NavGraphDirections.actionGlobalChatRoomFragment(it)
                )
                viewModel.displayRoomDetailsComplete()
            }
        }

        return binding.root
    }
}
