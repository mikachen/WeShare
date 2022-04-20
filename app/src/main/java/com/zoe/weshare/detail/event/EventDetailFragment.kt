package com.zoe.weshare.detail.event

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.zoe.weshare.R
import com.zoe.weshare.data.EventPost
import com.zoe.weshare.databinding.FragmentEventDetailBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.ext.toDisplayFormat
import com.zoe.weshare.util.UserManager.userZoe
import com.zoe.weshare.util.Util.getColor

class EventDetailFragment : Fragment() {

    private lateinit var binding: FragmentEventDetailBinding
    private lateinit var adapter: EventCommentsAdapter

    val viewModel by viewModels<EventDetailViewModel> { getVmFactory(userZoe) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentEventDetailBinding.inflate(inflater, container, false)

        val selectedEvent = EventDetailFragmentArgs.fromBundle(requireArguments()).selectedEvent

        viewModel.onViewPrepare(selectedEvent)
        viewModel.getHistoryComments(selectedEvent.id)

        adapter = EventCommentsAdapter(viewModel)
        binding.commentsRecyclerView.adapter = adapter

        viewModel.onViewDisplaying.observe(viewLifecycleOwner) {
            setupView(it)
            setUpBtn(it)
        }

        viewModel.onCommentLikePressed.observe(viewLifecycleOwner) {
            adapter.notifyItemChanged(it)
        }

        viewModel.comments.observe(viewLifecycleOwner) {

            adapter.submitList(it)

            // make sure it only run one time in the beginning
            if (viewModel.onProfileSearchComplete.value == null) {
                viewModel.searchUsersProfile(it)
            }
        }

        // drawing the user avatar image and nickName after searching user's profile docs
        viewModel.onProfileSearchComplete.observe(viewLifecycleOwner) {
            if (it == 0) {
                adapter.notifyDataSetChanged()
            }
        }

        viewModel.currentLikedNumber.observe(viewLifecycleOwner) {
            binding.textLikedNumber.text = resources.getString(R.string.number_who_liked, it)
        }

        viewModel.isUserPressedLike.observe(viewLifecycleOwner) {
            if (it) {
                binding.buttonPressLike.text = "已點讚 <3"
                binding.buttonPressLike.backgroundTintList = ColorStateList.valueOf(getColor(R.color.yellowTestColor))
            } else {
                binding.buttonPressLike.text = "感恩讚"
                binding.buttonPressLike.backgroundTintList = ColorStateList.valueOf(getColor(R.color.lightBlueTestColor))
            }
        }

        return binding.root
    }

    private fun setUpBtn(event: EventPost) {
        binding.buttonPressLike.setOnClickListener {
            viewModel.onPostLikePressed(event.id)
        }
        binding.buttonLeaveAComment.setOnClickListener {
            findNavController().navigate(
                EventDetailFragmentDirections.actionEventDetailFragmentToCommentDialogFragment(
                    event.id
                )
            )
        }
    }

    private fun setupView(event: EventPost) {
        binding.apply {
            bindImage(this.images, event.image)
            textEventTitle.text = event.title
            textProfileName.text = event.author?.name
            bindImage(this.imageProfileAvatar, event.author?.image)
            textPostedLocation.text =
                resources.getString(R.string.gift_post_location_name, event.location?.locationName)
            textCreatedTime.text =
                resources.getString(R.string.posted_time, event.createdTime.toDisplayFormat())
            textSort.text = resources.getString(R.string.gift_post_sort, event.sort)
            textVolunteerNeeds.text =
                resources.getString(R.string.number_volunteer_needs, event.volunteerNeeds)
            textDescription.text = event.description
        }
    }
}
