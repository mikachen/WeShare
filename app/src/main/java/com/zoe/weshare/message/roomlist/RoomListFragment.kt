package com.zoe.weshare.message.roomlist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.zoe.weshare.data.Author
import com.zoe.weshare.databinding.FragmentRoomListBinding
import com.zoe.weshare.ext.getVmFactory


class RoomListFragment : Fragment() {

    val author = Author(
        name = "Zoe Lo",
        uid = "zoe1018",
        image = "1231123123"
    )

    private val viewModel by viewModels<RoomListViewModel> { getVmFactory(author) }
    lateinit var binding: FragmentRoomListBinding
    lateinit var adapter: RoomListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentRoomListBinding.inflate(inflater, container, false)

        adapter = RoomListAdapter(RoomListAdapter.RoomListOnClickListener { selectedRoom ->

            Log.d("CLICK", "ONCLICK")
            viewModel.displayRoomDetails(selectedRoom)
        })
        binding.roomlistRecyclerView.adapter = adapter

        viewModel.room.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.navigateToSelectedRoom.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(RoomListFragmentDirections.actionRoomListFragmentToChatRoomFragment(
                    it))
                viewModel.displayRoomDetailsComplete()
            }
        }



        return binding.root
    }

}