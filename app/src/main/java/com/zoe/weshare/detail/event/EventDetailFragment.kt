package com.zoe.weshare.detail.event

import android.graphics.Point
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.BounceInterpolator
import android.view.animation.ScaleAnimation
import android.widget.PopupMenu
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.WriterException
import com.zoe.weshare.MainActivity
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.EventPost
import com.zoe.weshare.databinding.FragmentEventDetailBinding
import com.zoe.weshare.ext.*
import com.zoe.weshare.util.Const.FIELD_EVENT_ATTENDEE
import com.zoe.weshare.util.Const.FIELD_EVENT_VOLUNTEER
import com.zoe.weshare.util.EventStatusType
import com.zoe.weshare.util.LogType
import com.zoe.weshare.util.Logger
import com.zoe.weshare.util.UserManager.weShareUser


class EventDetailFragment : Fragment() {

    private lateinit var binding: FragmentEventDetailBinding
    private lateinit var adapter: EventCommentsAdapter
    private lateinit var commentsBoard: RecyclerView
    private lateinit var selectedEvent: EventPost

    var isUserAttend: Boolean = false
    var isUserVolunteer: Boolean = false
    var isUserCheckedIn: Boolean = false

    val viewModel by viewModels<EventDetailViewModel> { getVmFactory(weShareUser) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentEventDetailBinding.inflate(inflater, container, false)

        selectedEvent = EventDetailFragmentArgs.fromBundle(requireArguments()).selectedEvent
        viewModel.onViewPrepare(selectedEvent)

        viewModel.onEventLiveDisplaying.observe(viewLifecycleOwner) {
            it?.let {
                setupView(it)
                setupBtn(it)
                setupLikeBtn(it)
            }
        }

        viewModel.statusTriggerChanged.observe(viewLifecycleOwner) {
            viewModel.updateEventStatus(it)
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

        viewModel.onNavigateToRoom.observe(viewLifecycleOwner) {
            it?.let {
                viewModel.navigateToRoomComplete()
                findNavController().navigate(NavGraphDirections.actionGlobalChatRoomFragment(it))
            }
        }

        viewModel.liveComments.observe(viewLifecycleOwner) {
            viewModel.filterComment()
        }
        viewModel.filteredComments.observe(viewLifecycleOwner) {
            viewModel.searchUsersProfile(it)
        }

        viewModel.onProfileSearchComplete.observe(viewLifecycleOwner) {
            if (it == 0) {
                adapter.submitList(viewModel.filteredComments.value) {
                    commentsBoard.post {
                        commentsBoard.scrollToPosition(adapter.itemCount - 1)
                    }
                }
            }
        }

        viewModel.targetUser.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(NavGraphDirections.actionGlobalProfileFragment(it))
                viewModel.navigateToProfileComplete()
            }
        }

        viewModel.saveLogComplete.observe(viewLifecycleOwner) {
            if (it.logType == LogType.VOLUNTEER_EVENT.value) {
                sendNotificationToTarget(selectedEvent.author!!.uid, it)
            } else {
                sendNotificationsToFollowers(it)
            }
        }

        viewModel.blockUserComplete.observe(viewLifecycleOwner) {
            it?.let {
                Toast.makeText(requireContext(), "已封鎖用戶", Toast.LENGTH_SHORT).show()

                viewModel.refreshCommentBoard()
            }
        }


        return binding.root
    }

    private fun setupView(event: EventPost) {
        isUserAttend = event.whoAttended.contains(weShareUser!!.uid)
        isUserVolunteer = event.whoVolunteer.contains(weShareUser!!.uid)
        isUserCheckedIn = event.whoCheckedIn.contains(weShareUser!!.uid)

        commentsBoard = binding.commentsRecyclerView
        adapter = EventCommentsAdapter(viewModel, requireContext())
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

            buttonPressLike.isChecked = event.whoLiked.contains(weShareUser!!.uid) == true

            textEventDescription.text = event.description

            textStartTime.text = WeShareApplication.instance.getString(R.string.preview_event_time,
                event.startTime.toDisplayDateFormat(),
                event.endTime.toDisplayDateFormat())

            buttonAttend.isChecked = isUserAttend

            buttonVolunteer.isChecked = isUserVolunteer

            when (event.status) {
                EventStatusType.WAITING.code ->
                    countDownTimer(event.startTime - System.currentTimeMillis(), "開始").start()

                EventStatusType.ONGOING.code ->
                    countDownTimer(event.endTime - System.currentTimeMillis(), "結束").start()

                else -> binding.textCountdownTime.text = ""
            }
        }

        when (true) {
            (event.status == EventStatusType.WAITING.code) -> {
                binding.textStatus.text = EventStatusType.WAITING.tag
                binding.textStatus.setBackgroundResource(R.color.event_awaiting_tag)
            }
            (event.status == EventStatusType.ONGOING.code) -> {
                binding.textStatus.text = EventStatusType.ONGOING.tag
                binding.textStatus.setBackgroundResource(R.color.app_work_orange2)
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

            if(isUserAttend){
                binding.buttonAttend.setOnCheckedChangeListener { _, checked ->
                    binding.buttonAttend.isChecked = !checked
                }
            }

            if(isUserVolunteer){
                binding.buttonVolunteer.setOnCheckedChangeListener { _, checked ->
                    binding.buttonVolunteer.isChecked = !checked
                }
            }

            binding.buttonAttend.setOnClickListener {
                attendBtnClick()
            }
            binding.buttonVolunteer.setOnClickListener {
                volunteerBtnClick()
            }
        }


        binding.imageProfileAvatar.setOnClickListener {
            findNavController().navigate(NavGraphDirections.actionGlobalProfileFragment(event.author))
        }


        binding.eventOut.setOnClickListener {
            generateQrcode()
        }
    }

    fun attendBtnClick() {
        if (isUserAttend) {
            showPopupMenu(binding.buttonAttend, 0)
        } else {
            viewModel.onAttendEvent(FIELD_EVENT_ATTENDEE)
        }
    }

    fun volunteerBtnClick() {
        if (isUserVolunteer) {
            showPopupMenu(binding.buttonVolunteer, 1)
        } else {
            viewModel.onAttendEvent(FIELD_EVENT_VOLUNTEER)
        }
    }

    private fun showPopupMenu(view: View, adjustCode: Int) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.event_more_menu, popupMenu.menu)


        when (adjustCode) {

            0 -> {
                popupMenu.menu.removeItem(R.id.action_cancel_volunteer)
                popupMenu.menu.removeItem(R.id.action_check_in)
                popupMenu.menu.removeItem(R.id.action_enter_chatroom)
            }

            1 -> {
                popupMenu.menu.removeItem(R.id.action_cancel_attend)
            }

        }


        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_check_in -> {

                    when (selectedEvent.status) {

                        EventStatusType.WAITING.code -> {
                            Toast.makeText(requireContext(), "活動尚未開始！不能簽到", Toast.LENGTH_SHORT)
                                .show()
                        }

                        EventStatusType.ONGOING.code -> {
                            findNavController().navigate(EventDetailFragmentDirections
                                .actionEventDetailFragmentToEventCheckInFragment(selectedEvent))
                        }

                        else -> {}
                    }
                }


                R.id.action_enter_chatroom -> viewModel.getChatRoomInfo()

                R.id.action_cancel_volunteer -> {}

                R.id.action_cancel_attend -> {}
            }
            false
        }
        popupMenu.show()
    }


    fun generateQrcode() {

        val display = (activity as MainActivity).windowManager.defaultDisplay

        val point = Point()
        display.getSize(point)

        val width: Int = point.x
        val height: Int = point.y

        // generating dimension from width and height.
        var dimen = if (width < height) width else height
        dimen = dimen * 3 / 4

        val qrgEncoder = QRGEncoder("5PbKfTLSRUy2i17UThFY", null, QRGContents.Type.TEXT, dimen)
        try {

            val bitmap = qrgEncoder.encodeAsBitmap()

            binding.images.setImageBitmap(bitmap)

        } catch (e: WriterException) {
            // this method is called for
            // exception handling.
            Log.e("Tag", e.toString())
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


    private fun setupLikeBtn(event: EventPost) {
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

            viewModel.onPostLikePressed(event.id, event.whoLiked.contains(weShareUser!!.uid))
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

    private fun countDownTimer(millisInFuture: Long, state: String): CountDownTimer {

        return object : CountDownTimer(millisInFuture, 1000) {
            override fun onTick(millisUntilFinished: Long) {

                val timeRemaining = getCountDownTimeString(millisUntilFinished, state)

                binding.textCountdownTime.text = timeRemaining
            }

            override fun onFinish() {
                binding.textCountdownTime.text = ""
                viewModel.checkEventStatus(selectedEvent)
            }
        }
    }
}
