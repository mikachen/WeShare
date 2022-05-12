package com.zoe.weshare.detail.event

import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.BounceInterpolator
import android.view.animation.ScaleAnimation
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
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

    private val checkInAnimate: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.event_checkin_success
        )
    }

    private val sneakyHideAnimate: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.checkin_sneaky_hide
        )
    }
    private var isAnimateShown: Boolean = false
    private var isUserAttend: Boolean = false
    private var isUserVolunteer: Boolean = false
    private var isUserCheckedIn: Boolean = false

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
                viewModel.checkEventStatus(it)
            }
        }

        viewModel.statusTriggerChanged.observe(viewLifecycleOwner) {
            it?.let {
                viewModel.updateEventStatus(it)
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


        setupCommentBoard()
        return binding.root
    }

    private fun setupCommentBoard() {
        commentsBoard = binding.commentsRecyclerView
        adapter = EventCommentsAdapter(viewModel, requireContext())
        commentsBoard.adapter = adapter

    }

    private fun setupView(event: EventPost) {
        isUserAttend = event.whoAttended.contains(weShareUser!!.uid)
        isUserVolunteer = event.whoVolunteer.contains(weShareUser!!.uid)
        isUserCheckedIn = event.whoCheckedIn.contains(weShareUser!!.uid)
        setUpAnimation { }

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

            textStartTime.text = WeShareApplication.instance.getString(
                R.string.preview_event_time,
                event.startTime.toDisplayDateFormat(),
                event.endTime.toDisplayDateFormat()
            )

            buttonAttend.isChecked = isUserAttend

            buttonVolunteer.isChecked = isUserVolunteer

            if (isUserCheckedIn) {
                checkinComplete.visibility = View.VISIBLE
            }
        }

        when (true) {
            (event.status == EventStatusType.WAITING.code) -> {
                binding.textStatus.text = EventStatusType.WAITING.tag
                binding.textStatus.setBackgroundResource(R.color.event_awaiting_tag)
                countDownTimer(event.startTime - System.currentTimeMillis(), "開始").start()
            }

            (event.status == EventStatusType.ONGOING.code) -> {
                binding.textStatus.text = EventStatusType.ONGOING.tag
                binding.textStatus.setBackgroundResource(R.color.app_work_orange2)
                countDownTimer(event.endTime - System.currentTimeMillis(), "結束").start()
            }
            (event.status == EventStatusType.ENDED.code) -> {
                binding.textStatus.text = EventStatusType.ENDED.tag
                binding.textStatus.setBackgroundResource(R.color.app_work_dark_grey)
                binding.textCountdownTime.text = ""
                binding.layoutAttendeeButton.visibility = View.GONE
            }
            else -> {
                Logger.d("unKnow status")
            }
        }

        if (!isAnimateShown) {
            if (isUserCheckedIn) {
                binding.checkinComplete.startAnimation(checkInAnimate)
            }
        }
    }


    fun setUpAnimation( onEnd: () -> Unit) {
        checkInAnimate.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) = Unit

            override fun onAnimationEnd(p0: Animation?) {
                if(!isAnimateShown){
                    isAnimateShown = true
                    binding.checkinComplete.startAnimation(sneakyHideAnimate)
                }else{
                    onEnd()
                }
            }

            override fun onAnimationRepeat(p0: Animation?) = Unit
        })

        sneakyHideAnimate.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) = Unit

            override fun onAnimationEnd(p0: Animation?) {
                binding.checkinComplete.startAnimation(checkInAnimate)
            }

            override fun onAnimationRepeat(p0: Animation?) = Unit
        })
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

        if (event.status != EventStatusType.ENDED.code) {

            if (isUserAttend) {
                binding.buttonAttend.setOnCheckedChangeListener { _, checked ->
                    binding.buttonAttend.isChecked = !checked
                }
            }

            if (isUserVolunteer) {
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
            findNavController().navigate(
                NavGraphDirections.actionGlobalProfileFragment(event.author)
            )
        }
    }

    fun attendBtnClick() {
        if (isUserAttend) {
            if (!isUserCheckedIn) {
                showPopupMenu(binding.buttonAttend, 0)
            }
        } else {
            viewModel.onAttendEvent(FIELD_EVENT_ATTENDEE)
        }
    }

    fun volunteerBtnClick() {
        if (isUserVolunteer) {
            showPopupMenu(binding.buttonVolunteer, 1)
        } else {

            // if click volunteer, user must attend event as well
            if (!isUserAttend) {
                viewModel.onAttendEvent(FIELD_EVENT_ATTENDEE)
                viewModel.onAttendEvent(FIELD_EVENT_VOLUNTEER)
            } else {
                viewModel.onAttendEvent(FIELD_EVENT_VOLUNTEER)
            }
        }
    }

    private fun showPopupMenu(view: View, condition: Int) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.event_more_menu, popupMenu.menu)

        when (condition) {

            0 -> {
                popupMenu.menu.removeItem(R.id.action_cancel_volunteer)
                popupMenu.menu.removeItem(R.id.action_check_in)
                popupMenu.menu.removeItem(R.id.action_enter_chatroom)
            }

            1 -> {
                popupMenu.menu.removeItem(R.id.action_cancel_attend)

                if (isUserCheckedIn) {
                    popupMenu.menu.removeItem(R.id.action_cancel_volunteer)
                    popupMenu.menu.removeItem(R.id.action_check_in)
                }
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
                            findNavController().navigate(
                                EventDetailFragmentDirections
                                    .actionEventDetailFragmentToEventCheckInFragment(selectedEvent)
                            )
                        }

                        else -> {
                            Logger.d("unKnow")
                        }
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
            it.startAnimation(scaleAnimation)

            playCreditScene()
        }

        binding.buttonLike.setOnClickListener {
            it.startAnimation(scaleAnimation)

            playCreditScene()
        }
    }

    private fun playCreditScene() {
        if (binding.buttonLike.isChecked &&
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
                binding.textCountdownTime.text = "活動" + state
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
    }
}
