package com.zoe.weshare.detail.event

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
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
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.R
import com.zoe.weshare.SendNotificationService
import com.zoe.weshare.SendNotificationService.Companion.SEND_NOTIFICATION
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.EventPost
import com.zoe.weshare.databinding.FragmentEventDetailBinding
import com.zoe.weshare.ext.*
import com.zoe.weshare.util.Const.FIELD_EVENT_ATTENDEE
import com.zoe.weshare.util.Const.FIELD_EVENT_VOLUNTEER
import com.zoe.weshare.util.EventStatusType
import com.zoe.weshare.util.LogType
import com.zoe.weshare.util.Logger
import com.zoe.weshare.util.UserManager
import com.zoe.weshare.util.UserManager.weShareUser

class EventDetailFragment : Fragment() {

    private lateinit var binding: FragmentEventDetailBinding
    private lateinit var adapter: EventCommentsAdapter
    private lateinit var commentsBoard: RecyclerView

    val viewModel by viewModels<EventDetailViewModel> { getVmFactory(weShareUser) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentEventDetailBinding.inflate(inflater, container, false)

        val selectedEvent = EventDetailFragmentArgs.fromBundle(requireArguments()).selectedEvent

        viewModel.onViewPrepare(selectedEvent)

        viewModel.onEventDisplaying.observe(viewLifecycleOwner) {
            it?.let {
                setupView(it)
                setupBtn(it)
                setupLikeBtn(it)
            }
        }

        viewModel.eventStatusChanged.observe(viewLifecycleOwner) {
            viewModel.updateEventStatus(it)
        }

        viewModel.newComment.observe(viewLifecycleOwner) {
            if (it != null) {
                viewModel.sendComment(selectedEvent.id, it)
            }
        }

        viewModel.userAttendType.observe(viewLifecycleOwner) {
            if (it == FIELD_EVENT_ATTENDEE) {
                viewModel.onSaveOperateLog(
                    logType = LogType.ATTEND_EVENT.value,
                    logMsg = WeShareApplication.instance.getString(
                        R.string.log_msg_event_attending, weShareUser!!.name, selectedEvent.title
                    )
                )
            } else if (it == FIELD_EVENT_VOLUNTEER) {
                viewModel.onSaveOperateLog(
                    logType = LogType.VOLUNTEER_EVENT.value,
                    logMsg = WeShareApplication.instance.getString(
                        R.string.log_msg_event_volunteering, weShareUser!!.name, selectedEvent.title
                    )
                )
            }
        }

        viewModel.room.observe(viewLifecycleOwner) {
            it?.let {
                viewModel.checkUserInRoomBefore(it)
            }
        }

        viewModel.updateRoomStatus.observe(viewLifecycleOwner) {
            viewModel.getChatRoomInfo()
        }

        viewModel.onNavigateToRoom.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(NavGraphDirections.actionGlobalChatRoomFragment(it))
                viewModel.navigateToRoomComplete()
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

        viewModel.targetUser.observe(viewLifecycleOwner){
            it?.let {
                findNavController().navigate(NavGraphDirections.actionGlobalProfileFragment(it))
                viewModel.navigateToProfileComplete()
            }
        }

        viewModel.saveLogComplete.observe(viewLifecycleOwner){
            sendNotifications(it)
        }

        return binding.root
    }

    private fun setupBtn(event: EventPost) {

        binding.buttonSendComment.setOnClickListener {
            onSendComment()
        }

        binding.editCommentBox.setOnKeyListener { _, keyCode, keyEvent ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_DOWN) {

                onSendComment()

                true
            } else false
        }

        if (event.status == EventStatusType.ENDED.code) {

            binding.layoutAttendeeButton.visibility = View.GONE

        } else {
            binding.buttonAttend.setOnClickListener {
                viewModel.onAttendEvent(FIELD_EVENT_ATTENDEE)
            }
            binding.buttonVolunteer.setOnClickListener {
                viewModel.onAttendEvent(FIELD_EVENT_VOLUNTEER)
            }
        }

        /**
         * when user click on enter room :
         * 1) getEventRoom
         * 2) check if user has been in chat before
         * 3) if true -> navigate to room
         * 4) if false -> update room doc, return to (1~2~3 steps)
         */

        binding.buttonEnterEventChatroom.setOnClickListener {
            viewModel.getChatRoomInfo()
        }

        binding.imageProfileAvatar.setOnClickListener {
            findNavController().navigate(NavGraphDirections.actionGlobalProfileFragment(event.author))
        }
    }

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

            textStartTime.text = event.startTime.toDisplayFormat()

            when (event.whoAttended.contains(weShareUser!!.uid)) {
                true -> buttonAttend.text = "參與中"
                false -> buttonAttend.text = "我要參加"
            }

            when (event.whoVolunteer.contains(weShareUser!!.uid)) {
                true -> buttonVolunteer.text = "已登記"
                false -> buttonVolunteer.text = "志工參與"
            }

            when (event.status) {
                EventStatusType.WAITING.code ->
                    countDownTimer(event.startTime - System.currentTimeMillis()).start()

                EventStatusType.ONGOING.code ->
                    countDownTimer(event.endTime - System.currentTimeMillis()).start()

                else -> binding.textCountdownTime.text = ""
            }
        }

        when (true) {
            (event.status == EventStatusType.WAITING.code) -> {
                binding.textStatus.text = EventStatusType.WAITING.tag
                binding.textStatus.setBackgroundResource(R.color.message_sender_green)
            }
            (event.status == EventStatusType.ONGOING.code) -> {
                binding.textStatus.text = EventStatusType.ONGOING.tag
                binding.textStatus.setBackgroundResource(R.color.app_work_orange3)
            }
            (event.status == EventStatusType.ENDED.code) -> {
                binding.textStatus.text = EventStatusType.ENDED.tag
                binding.textStatus.setBackgroundResource(R.color.app_work_dark_grey)
            }
            else -> {
                Logger.d("unKnow status")
            }
        }
    }

    private fun setupLikeBtn(selectedEvent: EventPost) {
        val scaleAnimation = ScaleAnimation(
            0.7f,
            1.0f,
            0.7f,
            1.0f,
            Animation.RELATIVE_TO_SELF,
            0.7f,
            Animation.RELATIVE_TO_SELF,
            0.7f
        )
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
                EventDetailFragmentDirections.actionEventDetailFragmentToCreditFragment()
            )
        }
    }

    private fun countDownTimer(millisInFuture: Long): CountDownTimer {

        return object : CountDownTimer(millisInFuture, 1000) {
            override fun onTick(millisUntilFinished: Long) {

                val timeRemaining = getCountDownTimeString(millisUntilFinished)

                binding.textCountdownTime.text = timeRemaining
            }

            override fun onFinish() {
                binding.textCountdownTime.text = "活動開始"
            }
        }
    }
}
