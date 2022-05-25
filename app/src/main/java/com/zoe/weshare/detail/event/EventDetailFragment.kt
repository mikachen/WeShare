package com.zoe.weshare.detail.event

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.BounceInterpolator
import android.view.animation.ScaleAnimation
import android.widget.PopupMenu
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
import com.zoe.weshare.detail.hasUserLikedBefore
import com.zoe.weshare.ext.*
import com.zoe.weshare.util.Const.FIELD_EVENT_ATTENDEE
import com.zoe.weshare.util.Const.FIELD_EVENT_VOLUNTEER
import com.zoe.weshare.util.EventStatusType
import com.zoe.weshare.util.LogType
import com.zoe.weshare.util.UserManager.weShareUser

class EventDetailFragment : Fragment() {
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

    private lateinit var binding: FragmentEventDetailBinding
    private lateinit var adapter: EventCommentsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var selectedEvent: EventPost

    private var hasAnimationPlayed: Boolean = false
    private var hasUserAttend: Boolean = false
    private var hasUserVolunteer: Boolean = false
    private var hasUserCheckedIn: Boolean = false
    private var hasCheckedStatus: Boolean = false

    private val viewModel by viewModels<EventDetailViewModel> { getVmFactory(weShareUser) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentEventDetailBinding.inflate(inflater, container, false)

        setupCommentBoard()
        setupCheckInAnimation { }
        setupLikeBtn()


        selectedEvent = EventDetailFragmentArgs.fromBundle(requireArguments()).selectedEvent
        viewModel.onViewPrepare(selectedEvent)

        viewModel.liveEventDetailResult.observe(viewLifecycleOwner) {
            it?.let {
                viewModel.fetchEvent(it)

                checkUserAttendState(it)
                setupView(it)
                setupBtn(it)

                if (!hasCheckedStatus) {
                    hasCheckedStatus = true

                    viewModel.checkEventStatus(it)
                }
            }
        }

        viewModel.liveComments.observe(viewLifecycleOwner) {
            viewModel.excludeBlackListUser()
        }

        viewModel.filteredComments.observe(viewLifecycleOwner) {
            viewModel.onGetUsersProfile(it)
        }

        viewModel.eventStatusChanged.observe(viewLifecycleOwner) {
            it?.let {
                viewModel.updateEventStatus(it)
            }
        }

        viewModel.userAttendType.observe(viewLifecycleOwner) {
            it?.let {
                if (it == FIELD_EVENT_ATTENDEE) {
                    viewModel.onSaveLog(
                        logType = LogType.ATTEND_EVENT.value,
                        logMsg = WeShareApplication.instance.getString(
                            R.string.log_msg_event_attending,
                            weShareUser.name,
                            selectedEvent.title
                        )
                    )
                } else if (it == FIELD_EVENT_VOLUNTEER) {
                    viewModel.onSaveLog(
                        logType = LogType.VOLUNTEER_EVENT.value,
                        logMsg = WeShareApplication.instance.getString(
                            R.string.log_msg_event_volunteering,
                            weShareUser.name,
                            selectedEvent.title
                        )
                    )
                }
            }
        }

        viewModel.profileSearchComplete.observe(viewLifecycleOwner) {
            if (it) {
                adapter.submitList(viewModel.filteredComments.value) {
                    recyclerView.post {
                        recyclerView.scrollToPosition(adapter.itemCount - 1)
                    }
                }
            }
        }

        viewModel.onTargetAvatarClicked.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(
                    EventDetailFragmentDirections.actionEventDetailFragmentToProfileFragment(it)
                )

                viewModel.navigateToProfileComplete()
            }
        }

        viewModel.saveLogComplete.observe(viewLifecycleOwner) {
            it?.let {
                if (it.logType == LogType.VOLUNTEER_EVENT.value) {
                    sendNotificationToTarget(selectedEvent.author.uid, it)
                } else {
                    sendNotificationsToFollowers(it)
                }
                viewModel.saveLogComplete()
            }
        }

        viewModel.onNavigateToRoom.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(NavGraphDirections.actionGlobalChatRoomFragment(it))
                viewModel.navigateToRoomComplete()
            }
        }

        viewModel.onReportUserViolation.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(
                    NavGraphDirections.actionGlobalReportViolationDialog(it))

                viewModel.navigateToReportDialogComplete()
            }
        }

        viewModel.onBlockListUser.observe(viewLifecycleOwner) {
            it?.let {
                activity.showToast(getString(R.string.block_this_person_complete))
                viewModel.refreshCommentBoard()
            }
        }

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupCommentBoard() {
        recyclerView = binding.commentsRecyclerView
        adapter = EventCommentsAdapter(viewModel, requireContext())
        recyclerView.adapter = adapter

        recyclerView.setOnTouchListener { view, _ ->
            view.hideKeyboard()
            false
        }
    }

    private fun setupCheckInAnimation(onEnd: () -> Unit) {
        checkInAnimate.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) = Unit

            override fun onAnimationEnd(p0: Animation?) {

                if (!hasAnimationPlayed) {

                    hasAnimationPlayed = true
                    binding.checkinComplete.startAnimation(sneakyHideAnimate)

                } else {
                    //end at second time play
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

    fun checkUserAttendState(event: EventPost) {
        hasUserAttend = event.whoAttended.contains(weShareUser.uid)
        hasUserVolunteer = event.whoVolunteer.contains(weShareUser.uid)
        hasUserCheckedIn = event.whoCheckedIn.contains(weShareUser.uid)
    }

    private fun setupView(event: EventPost) {
        binding.apply {
            bindImage(eventImage, event.image)

            textEventTitle.text = event.title

            textProfileName.text = event.author.name

            bindImage(imageAuthorAvatar, event.author.image)

            textPostedLocation.text = event.location.locationName

            textCreatedTime.text =
                resources.getString(R.string.posted_time, event.createdTime.toDisplayFormat())

            textSort.text = event.sort

            textAttendeeCount.text = event.whoAttended.size.toString()

            textVolunteerNeed.text = event.volunteerNeeds.toString()

            textVolunteerCount.text = event.whoVolunteer.size.toString()

            textLikedNumber.text =
                getString(R.string.number_who_liked, event.whoLiked.size)

            buttonPressLike.isChecked = hasUserLikedBefore(event.whoLiked)

            textEventDescription.text = event.description

            textStartTime.text = WeShareApplication.instance.getString(
                R.string.preview_event_time,
                event.startTime.toDisplayDateFormat(),
                event.endTime.toDisplayDateFormat()
            )

            if (!hasAnimationPlayed) {
                if (hasUserCheckedIn) {
                    checkinComplete.visibility = View.VISIBLE
                    checkinComplete.startAnimation(checkInAnimate)
                }
            }

            when (event.status) {
                EventStatusType.WAITING.code -> {
                    textStatus.text = EventStatusType.WAITING.tag
                    textStatus.setBackgroundResource(R.color.event_awaiting_tag)
                    countDownTimer(event.startTime - System.currentTimeMillis(),
                        getString(R.string.event_start)).start()
                }

                EventStatusType.ONGOING.code -> {
                    textStatus.text = EventStatusType.ONGOING.tag
                    textStatus.setBackgroundResource(R.color.app_work_orange2)
                    countDownTimer(event.endTime - System.currentTimeMillis(),
                        getString(R.string.event_end)).start()
                }

                EventStatusType.ENDED.code -> {
                    textStatus.text = EventStatusType.ENDED.tag
                    textStatus.setBackgroundResource(R.color.app_work_dark_grey)
                    binding.layoutAttendeeButton.visibility = View.GONE
                }
            }
        }
    }

    private fun setupBtn(event: EventPost) {
        attendBtnStateReset()

        binding.apply {

            buttonAttend.isChecked = hasUserAttend
            buttonVolunteer.isChecked = hasUserVolunteer

            if (eventNotYetEnded(event)) {
                //disable the toggle button check state changed
                if (hasUserAttend) {
                    buttonAttend.setOnCheckedChangeListener { _, checked ->
                        buttonAttend.isChecked = !checked
                    }
                }

                //disable the toggle button check state changed
                if (hasUserVolunteer) {
                    buttonVolunteer.setOnCheckedChangeListener { _, checked ->
                        buttonVolunteer.isChecked = !checked
                    }
                }

                buttonAttend.setOnClickListener {
                    attendBtnClick()
                }
                buttonVolunteer.setOnClickListener {
                    volunteerBtnClick()
                }
            } else {
                // no buttons layout for ended case
                return
            }

            buttonSendComment.setOnClickListener {
                onSendComment()
                it.hideKeyboard()
            }

            editCommentBox.setOnKeyListener { view, keyCode, keyEvent ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_DOWN) {

                    onSendComment()
                    view.hideKeyboard()

                    true
                } else false
            }


            imageAuthorAvatar.setOnClickListener {
                findNavController().navigate(EventDetailFragmentDirections
                    .actionEventDetailFragmentToProfileFragment(event.author)
                )
            }
        }
    }

    private fun attendBtnClick() {
        if (!hasUserAttend) {
            viewModel.onAttendEvent(FIELD_EVENT_ATTENDEE)

        } else {
            if (!hasUserCheckedIn) {
                //only allow user to cancel attendee if user not yet checkIn as volunteer member

                showAttendEventMenu(binding.buttonAttend)
            } else {
                return
            }
        }
    }

    private fun volunteerBtnClick() {
        if (!hasUserVolunteer) {

            // if click volunteer, user must attend event as well
            if (!hasUserAttend) {
                viewModel.onAttendEvent(FIELD_EVENT_ATTENDEE)
                viewModel.onAttendEvent(FIELD_EVENT_VOLUNTEER)

            } else {
                viewModel.onAttendEvent(FIELD_EVENT_VOLUNTEER)
            }

        } else {
            showAttendVolunteerMenu(binding.buttonVolunteer)
        }
    }

    private fun showAttendEventMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.event_attend_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {

                R.id.action_cancel_attend -> {
                    if (hasUserVolunteer) {
                        viewModel.cancelAttendEvent(FIELD_EVENT_ATTENDEE)
                        viewModel.cancelAttendEvent(FIELD_EVENT_VOLUNTEER)
                    } else {
                        viewModel.cancelAttendEvent(FIELD_EVENT_ATTENDEE)
                    }
                }
            }
            false
        }
        popupMenu.show()
    }

    private fun showAttendVolunteerMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.volunteer_attend_menu, popupMenu.menu)

        if (hasUserCheckedIn) {
            popupMenu.menu.removeItem(R.id.action_cancel_volunteer)
            popupMenu.menu.removeItem(R.id.action_check_in)
        }

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {

                R.id.action_check_in -> {
                    when (selectedEvent.status) {

                        EventStatusType.WAITING.code -> {
                            activity.showToast(getString(R.string.toast_check_in_reject))
                        }

                        EventStatusType.ONGOING.code -> {
                            findNavController().navigate(EventDetailFragmentDirections
                                .actionEventDetailFragmentToEventCheckInFragment(selectedEvent)
                            )
                        }

                        else -> {
                            // no button layout for ended case
                        }
                    }
                }

                R.id.action_enter_chatroom -> {
                    viewModel.getChatRoomInfo()
                }

                R.id.action_cancel_volunteer -> {
                    viewModel.cancelAttendEvent(FIELD_EVENT_VOLUNTEER)
                }

            }
            false
        }
        popupMenu.show()
    }

    private fun attendBtnStateReset() {
        binding.buttonAttend.setOnCheckedChangeListener { btn, checked ->
            btn.isChecked = checked
        }

        binding.buttonVolunteer.setOnCheckedChangeListener { btn, checked ->
            btn.isChecked = checked
        }
    }

    private fun onSendComment() {
        val message = binding.editCommentBox.text.toString().trim()

        if (message.isNotEmpty()) {
            viewModel.onSendNewComment(message)
            binding.editCommentBox.text.clear()
        }
    }

    private fun setupLikeBtn() {
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

            viewModel.onPostLikePressed()

            it.startAnimation(scaleAnimation)
            playCreditScene()
        }

        binding.buttonAdditionHeart1.setOnClickListener {
            it.startAnimation(scaleAnimation)
            playCreditScene()
        }

        binding.buttonAdditionHeart2.setOnClickListener {
            it.startAnimation(scaleAnimation)
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
                binding.textCountdownTime.text = state
            }
        }
    }

    private fun eventNotYetEnded(event: EventPost): Boolean {
        return event.status != EventStatusType.ENDED.code
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).setSoftInputMode(SOFT_INPUT_ADJUST_RESIZE)
    }
}
