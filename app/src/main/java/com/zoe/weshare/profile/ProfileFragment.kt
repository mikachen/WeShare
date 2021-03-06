package com.zoe.weshare.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.zoe.weshare.MainActivity
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.R
import com.zoe.weshare.data.OperationLog
import com.zoe.weshare.data.UserInfo
import com.zoe.weshare.data.UserProfile
import com.zoe.weshare.databinding.FragmentProfileBinding
import com.zoe.weshare.ext.*
import com.zoe.weshare.util.LogType
import com.zoe.weshare.util.UserManager.weShareUser
import com.zoe.weshare.util.Util

class ProfileFragment : Fragment() {

    private lateinit var targetUser: UserInfo
    private lateinit var binding: FragmentProfileBinding
    private var isFollowingTarget = false

    private val viewModel: ProfileViewModel by viewModels { getVmFactory(targetUser) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        targetUser = ProfileFragmentArgs.fromBundle(requireArguments()).targetUser!!

        binding = FragmentProfileBinding.inflate(inflater, container, false)

        viewModel.user.observe(viewLifecycleOwner) {
            it?.let {
                setupView(it)
                setupSocialButtons()
            }
        }

        viewModel.userLog.observe(viewLifecycleOwner) {
            setupBoardCountView(it)
            viewModel.calculateContribution(it)
        }

        viewModel.userChatRooms.observe(viewLifecycleOwner) {
            it?.let {
                if (it.isNotEmpty()) {
                    viewModel.checkIfPrivateRoomExist(it)
                } else {
                    viewModel.onNewRoomPrepare()
                }
            }
        }

        viewModel.navigateToFormerRoom.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(NavGraphDirections.actionGlobalChatRoomFragment(it))
                viewModel.navigateToRoomComplete()
            }
        }

        viewModel.navigateToNewRoom.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(NavGraphDirections.actionGlobalChatRoomFragment(it))
                viewModel.navigateToRoomComplete()
            }
        }

        viewModel.onUpdateContribution.observe(viewLifecycleOwner) {
            it?.let {
                viewModel.updateContribution(it)
            }
        }

        viewModel.notificationMsg.observe(viewLifecycleOwner) {
            it?.let {
                sendNotificationToTarget(targetUser.uid, it)
            }
        }

        viewModel.blockUserComplete.observe(viewLifecycleOwner) {
            it?.let {
                activity.showToast(getString(R.string.block_this_person_complete))
                findNavController().navigate(NavGraphDirections.navigateToHomeFragment())
            }
        }

        return binding.root
    }

    private fun setupBoardCountView(logs: List<OperationLog>) {
        binding.apply {

            textGiftPostCount.text =
                logs.filter { it.logType == LogType.POST_GIFT.value }.size.toString()

            textGiftSentCount.text =
                logs.filter { it.logType == LogType.SEND_GIFT.value }.size.toString()

            textEventPostCount.text =
                logs.filter { it.logType == LogType.ATTEND_EVENT.value }.size.toString()

            textEventVolunteerCount.text =
                logs.filter { it.logType == LogType.VOLUNTEER_EVENT.value }.size.toString()
        }
    }

    private fun setupView(user: UserProfile) {
        isFollowingTarget = user.follower.contains(weShareUser.uid)

        binding.apply {
            bindImage(imageUserAvatar, user.image)

            bindImage(imageUserAvatar, user.image)

            textProfileName.text = user.name

            textFollowerNumber.text = (user.follower.size).toString()

            textFollowingNumber.text = (user.following.size).toString()

            textIntroMessage.text =
                user.introMsg.ifBlank { getString(R.string.request_leave_intro_message) }
        }
    }

    private fun setupSocialButtons() {

        val isVisitOtherUserPage = targetUser.uid != weShareUser.uid

        binding.apply {
            if (!isVisitOtherUserPage) {

                // user self profile page
                binding.buttonSettings.visibility = View.VISIBLE
                binding.buttonSettings.setOnClickListener {
                    showPopupMenu(it, 0)
                }
            } else {

                // target user profile page
                layoutSocialButton.visibility = View.VISIBLE
                buttonFollow.isChecked = isFollowingTarget

                if (isFollowingTarget) {
                    buttonFollow.setOnCheckedChangeListener { btn, checked ->
                        btn.isChecked = !checked
                    }
                }

               buttonFollow.setOnClickListener {
                    followBtnClick()
                }

                buttonMessage.setOnClickListener {
                    viewModel.getUserAllRooms(weShareUser)

                }

                buttonMore.visibility = View.VISIBLE
                buttonMore.setOnClickListener {
                    showReportMenu(it)
                }
            }

            buttonFollow.setOnCheckedChangeListener { btn, checked ->
                btn.isChecked = checked
            }
        }
    }

    private fun followBtnClick() {
        if (isFollowingTarget) {
            showPopupMenu(binding.buttonFollow, 1)
        } else {
            viewModel.updateUserFollowing(targetUser.uid)
        }
    }

    private fun showPopupMenu(view: View, condition: Int) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.profile_popup_menu, popupMenu.menu)

        when (condition) {
            0 -> popupMenu.menu.removeItem(R.id.action_cancel_following)

            1 -> {
                popupMenu.menu.removeItem(R.id.edit_user_info)
                popupMenu.menu.removeItem(R.id.action_gifts_manage)
                popupMenu.menu.removeItem(R.id.action_events_manage)
                popupMenu.menu.removeItem(R.id.action_ugc_policy)
                popupMenu.menu.removeItem(R.id.action_log_out)
            }
        }

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.edit_user_info -> findNavController().navigate(
                    ProfileFragmentDirections.actionProfileToEditInfo(viewModel.userProfile)
                )

                R.id.action_gifts_manage -> findNavController().navigate(
                    ProfileFragmentDirections.actionProfileToGiftManage()
                )

                R.id.action_events_manage -> findNavController().navigate(
                    ProfileFragmentDirections.actionProfileToEventManage()
                )

                R.id.action_cancel_following -> {
                    viewModel.cancelUserFollowing(targetUser.uid)
                }

                R.id.action_ugc_policy -> {
                    findNavController().navigate(
                        ProfileFragmentDirections.actionProfileToPolicyTerm(true)
                    )
                }

                R.id.action_log_out -> {
                    (activity as MainActivity).viewModel.userLogout()

                    findNavController().navigate(
                        NavGraphDirections.actionGlobalLoginFragment(true)
                    )
                }
            }
            false
        }
        popupMenu.show()
    }

    private fun showReportMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.report_user_menu, popupMenu.menu)

        val target = viewModel.user.value!!

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_block_user -> {
                    showAlterDialog(target)
                }
                R.id.action_report_violations -> {

                    findNavController().navigate(NavGraphDirections
                        .actionGlobalReportViolationDialog(target.uid))

                }
            }
            false
        }
        popupMenu.show()
    }

    private fun showAlterDialog(target: UserProfile) {

        val builder = AlertDialog.Builder(requireContext())

        builder.apply {
            setTitle(Util.getStringWithStrParm(R.string.block_this_person_title, target.name))
            setMessage(Util.getString(R.string.block_this_person_message))
            setPositiveButton(Util.getString(R.string.confirm_yes)) { dialog, _ ->
                viewModel.blockThisUser(target)
                dialog.cancel()
            }

            setNegativeButton(Util.getString(R.string.confirm_no)) { dialog, _ ->
                dialog.cancel()
            }
        }

        builder.create().show()
    }

    override fun onResume() {
        super.onResume()

        (activity as MainActivity).setSoftInputMode(SOFT_INPUT_ADJUST_PAN)

    }
}
