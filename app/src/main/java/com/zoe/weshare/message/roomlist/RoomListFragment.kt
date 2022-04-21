package com.zoe.weshare.message.roomlist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.zoe.weshare.data.source.remote.WeShareRemoteDataSource.getRelatedChatRooms
import com.zoe.weshare.databinding.FragmentRoomListBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.util.Const.PATH_CHATROOM
import com.zoe.weshare.util.UserManager

class RoomListFragment : Fragment() {

    private val viewModel by viewModels<RoomListViewModel> { getVmFactory(UserManager.userZoe) }
    lateinit var binding: FragmentRoomListBinding
    lateinit var adapter: RoomListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentRoomListBinding.inflate(inflater, container, false)

        //TODO 監聽？
        viewModel.initChatRoom()

        adapter = RoomListAdapter(
            RoomListAdapter.RoomListOnClickListener { selectedRoom ->
                viewModel.displayRoomDetails(selectedRoom)
            }
        )

        binding.roomlistRecyclerView.adapter = adapter

        viewModel.room.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.navigateToSelectedRoom.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(
                    RoomListFragmentDirections.actionRoomListFragmentToChatRoomFragment(it)
                )
                viewModel.displayRoomDetailsComplete()
            }
        }

        return binding.root
    }

}
