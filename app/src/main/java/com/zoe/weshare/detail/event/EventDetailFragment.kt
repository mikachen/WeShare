package com.zoe.weshare.detail.event

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.zoe.weshare.R
import com.zoe.weshare.data.EventPost
import com.zoe.weshare.databinding.FragmentEventDetailBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.ext.toDisplayFormat

class EventDetailFragment : Fragment() {

    private lateinit var binding: FragmentEventDetailBinding
    private lateinit var adapter: EventCommentsAdapter

    val viewModel by viewModels<EventDetailViewModel> { getVmFactory() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding =  FragmentEventDetailBinding.inflate(inflater, container, false)

        val selectedEvent = EventDetailFragmentArgs.fromBundle(requireArguments()).selectedEvent

        setUpDetailView(selectedEvent)

        viewModel.getHistoryComments(selectedEvent.id)

        adapter = EventCommentsAdapter(viewModel)
        binding.commentsRecyclerView.adapter = adapter

        viewModel.comments.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            viewModel.searchUsersProfile(it)
        }

        //drawing the user avatar image and nickName after searching user's profile docs
        viewModel.onProfileSearching.observe(viewLifecycleOwner) {
            if (it == 0) {
                adapter.notifyDataSetChanged()
            }
        }


        setUpBtn(selectedEvent.id)

        return binding.root
    }

    private fun setUpBtn(docId: String) {
        binding.buttonLeaveAComment.setOnClickListener {
            findNavController().navigate(EventDetailFragmentDirections.actionEventDetailFragmentToCommentDialogFragment(docId))
        }
    }

    private fun setUpDetailView(selectedEvent: EventPost) {
        binding.apply {
            bindImage(this.images, selectedEvent.image)
            textEventTitle.text = selectedEvent.title
            textProfileName.text = selectedEvent.author?.name
            bindImage(this.imageProfileAvatar, selectedEvent.author?.image)
            textPostedLocation.text = resources.getString(R.string.gift_post_location_name,selectedEvent.location?.locationName)
            textCreatedTime.text = resources.getString(R.string.posted_time,selectedEvent.createdTime.toDisplayFormat())
            textSort.text = resources.getString(R.string.gift_post_sort,selectedEvent.sort)
            textVolunteerNeeds.text = resources.getString(R.string.number_volunteer_needs,selectedEvent.volunteerNeeds)
            textLikedNumber.text = resources.getString(R.string.number_who_liked,selectedEvent.whoLiked?.size)
            textDescription.text = selectedEvent.description
        }
    }
}