package com.zoe.weshare.detail.event

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.BounceInterpolator
import android.view.animation.ScaleAnimation
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.R
import com.zoe.weshare.data.EventPost
import com.zoe.weshare.databinding.FragmentEventDetailBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.ext.toDisplayFormat
import com.zoe.weshare.network.LoadApiStatus
import com.zoe.weshare.util.Const.FIELD_EVENT_ATTENDEE
import com.zoe.weshare.util.Const.FIELD_EVENT_VOLUNTEER
import com.zoe.weshare.util.EventStatusType
import com.zoe.weshare.util.UserManager.userZoe


class EventDetailFragment : Fragment() {

    private lateinit var binding: FragmentEventDetailBinding
    private lateinit var adapter: EventCommentsAdapter
    private lateinit var commentsBoard: RecyclerView

    val currentUser = userZoe

    val viewModel by viewModels<EventDetailViewModel> { getVmFactory(currentUser) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentEventDetailBinding.inflate(inflater, container, false)

        val selectedEvent = EventDetailFragmentArgs.fromBundle(requireArguments()).selectedEvent

        viewModel.onViewPrepare(selectedEvent)

        viewModel.onEventDisplaying.observe(viewLifecycleOwner) {
            setupView(it)
            setUpBtn(it)
            setupLikeBtn(it)
        }

        viewModel.newComment.observe(viewLifecycleOwner) {
            if (it != null) {
                viewModel.sendComment(selectedEvent.id, it)
            }
        }

        viewModel.sendCommentStatus.observe(viewLifecycleOwner) {
            if (it == LoadApiStatus.DONE) {
                viewModel.onSaveCommentLog(selectedEvent)
            }
        }

        viewModel.saveLogComplete.observe(viewLifecycleOwner) {
            if (it == LoadApiStatus.DONE) {
                //TODO
            }
        }

        viewModel.currentLikedNumber.observe(viewLifecycleOwner) {
            binding.textLikedNumber.text = resources.getString(R.string.number_who_liked, it)
        }

        viewModel.isUserPressedLike.observe(viewLifecycleOwner) {
            binding.buttonPressLike.isChecked = it
        }

        viewModel.currentLikedNumber.observe(viewLifecycleOwner) {
            binding.textLikedNumber.text = resources.getString(R.string.number_who_liked, it)
        }

        viewModel.liveComments.observe(viewLifecycleOwner) {
            viewModel.searchUsersProfile(it)
        }

        viewModel.onProfileSearch.observe(viewLifecycleOwner) {
            if (it == 0) {
                adapter.submitList(viewModel.liveComments.value) {
                    commentsBoard.post {
                        commentsBoard.scrollToPosition(adapter.itemCount - 1)
                    }
                }
            }
        }

        return binding.root
    }

    private fun setUpBtn(event: EventPost) {
        binding.lottieBtnChatMe.visibility = View.GONE

        binding.buttonSendComment.setOnClickListener {
            onSendComment()
        }

        binding.editCommentBox.setOnKeyListener { _, keyCode, keyEvent ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_DOWN) {

                onSendComment()

                true
            } else false
        }

        when (true) {

            (event.status == EventStatusType.ENDED.code) -> {
                binding.layoutAttendeeButton.visibility = View.GONE
            }

            else -> {
                binding.buttonAttend.setOnClickListener {
                    viewModel.onAttendEvent(FIELD_EVENT_ATTENDEE)
                }
                binding.buttonVolunteer.setOnClickListener {
                    viewModel.onAttendEvent(FIELD_EVENT_VOLUNTEER)
                }
            }
        }
    }

    // TODO A. search room doc type = 1, & relatedEventId   B.if result is null, create a new one

    private fun onSendComment() {
        val message = binding.editCommentBox.text

        if (message != null) {
            if (message.isNotEmpty()) {
                viewModel.onSendNewComment(message.toString())
                message.clear()
            } else {
                Toast.makeText(requireContext(), "請填寫留言", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupView(event: EventPost) {

        commentsBoard = binding.commentsRecyclerView

        adapter = EventCommentsAdapter(viewModel)
        commentsBoard.adapter = adapter

        binding.apply {
            bindImage(this.images, event.image)

            textEventTitle.text = event.title

            textProfileName.text = event.author?.name

            bindImage(this.imageProfileAvatar, event.author?.image)

            textPostedLocation.text = event.location?.locationName

            textCreatedTime.text =
                resources.getString(R.string.posted_time, event.createdTime.toDisplayFormat())

            textSort.text = event.sort

            textAttendeeCount.text = event.whoAttended.size.toString()

            textVolunteerNeed.text = event.volunteerNeeds.toString()

            textVolunteerCount.text = event.whoVolunteer.size.toString()

            textLikedNumber.text =
                getString(R.string.number_who_liked, event.whoLiked.size)

            textEventDescription.text = event.description
        }

        when (event.status) {
            EventStatusType.WAITING.code -> {
                binding.textStatus.text = EventStatusType.WAITING.tag
                binding.textStatus.setBackgroundResource(R.color.message_sender_green)
            }
            EventStatusType.ONGOING.code -> {

                binding.textStatus.text = EventStatusType.ONGOING.tag
                binding.textStatus.setBackgroundResource(R.color.app_work_orange3)
            }
            EventStatusType.ENDED.code -> {
                binding.textStatus.text = EventStatusType.ENDED.tag
                binding.textStatus.setBackgroundResource(R.color.app_work_light_grey)
            }
        }
    }

    private fun setupLikeBtn(selectedEvent: EventPost) {
        val scaleAnimation = ScaleAnimation(0.7f,
            1.0f,
            0.7f,
            1.0f,
            Animation.RELATIVE_TO_SELF,
            0.7f,
            Animation.RELATIVE_TO_SELF,
            0.7f)
        scaleAnimation.duration = 500
        val bounceInterpolator = BounceInterpolator()
        scaleAnimation.interpolator = bounceInterpolator

        binding.buttonPressLike.setOnClickListener {

            it.startAnimation(scaleAnimation)

            viewModel.onPostLikePressed(selectedEvent.id)
            playCreditScene()
        }

        binding.buttonAdditionHeart1.setOnClickListener {
            playCreditScene()
        }

        binding.buttonAdditionHeart2.setOnClickListener {
            playCreditScene()
        }
    }

    private fun playCreditScene() {
        if (binding.buttonAdditionHeart2.isChecked &&
            binding.buttonAdditionHeart1.isChecked &&
            binding.buttonPressLike.isChecked
        ) {
            findNavController().navigate(
                EventDetailFragmentDirections.actionEventDetailFragmentToCreditFragment())
        }
    }
}
