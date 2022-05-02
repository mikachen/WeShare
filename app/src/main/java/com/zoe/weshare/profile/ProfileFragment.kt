package com.zoe.weshare.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.R
import com.zoe.weshare.data.OperationLog
import com.zoe.weshare.data.UserInfo
import com.zoe.weshare.data.UserProfile
import com.zoe.weshare.databinding.FragmentProfileBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.util.LogType
import com.zoe.weshare.util.UserManager
import com.zoe.weshare.util.UserManager.weShareUser

class ProfileFragment : Fragment() {

    lateinit var targetUser: UserInfo
    lateinit var binding: FragmentProfileBinding

    val viewModel: ProfileViewModel by viewModels { getVmFactory(targetUser) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        targetUser = ProfileFragmentArgs.fromBundle(requireArguments()).targetUser!!

        binding = FragmentProfileBinding.inflate(inflater, container, false)

        viewModel.user.observe(viewLifecycleOwner) {
            setUpView(it)
        }

        viewModel.userLog.observe(viewLifecycleOwner) {
            setupLogView(it)
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


        setupBtn()
        return binding.root
    }

    private fun setupLogView(logs: List<OperationLog>) {
        if (logs.isNotEmpty()) {
            binding.apply {

                textGiftPostCount.text =
                    logs.filter { it.logType == LogType.POST_GIFT.value }.size.toString()

                textGiftSentCount.text =
                    logs.filter { it.logType == LogType.SEND_GIFT.value }.size.toString()

                textEventPostCount.text =
                    logs.filter { it.logType == LogType.POST_EVENT.value }.size.toString()

                textEventVolunteerCount.text =
                    logs.filter { it.logType == LogType.VOLUNTEER_EVENT.value }.size.toString()
            }
        }
    }

    private fun showPopupMenu(view: View){
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.profile_popup_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.edit_user_info -> findNavController().navigate(ProfileFragmentDirections
                    .actionProfileFragmentToEditInfoFragment(viewModel.user.value!!))

                R.id.action_gifts_manage -> findNavController().navigate(NavGraphDirections
                    .actionGlobalPagerFilterFragment())

                R.id.action_events_manage -> findNavController().navigate(NavGraphDirections
                    .actionGlobalNotificationFragment())
            }
            false
        }
        popupMenu.show()
    }

    private fun setupBtn() {

        binding.buttonFollow.setOnClickListener {
            viewModel.updateTargetFollower(targetUser.uid)
            viewModel.updateUserFollowing(targetUser.uid)
        }

        binding.buttonMessage.setOnClickListener {
            viewModel.searchOnPrivateRoom(UserManager.weShareUser!!)
        }

        if(targetUser.uid == weShareUser!!.uid) {
            binding.buttonSettings.visibility = View.VISIBLE

            binding.buttonSettings.setOnClickListener {
                showPopupMenu(it)
            }
        }
    }

    private fun setUpView(user: UserProfile) {
        binding.apply {
            bindImage(imageProfileAvatar, user.image)
            textProfileName.text = user.name
            textFollowerNumber.text = (user.follower.size).toString()
            textFollowingNumber.text = (user.following.size).toString()
            textIntroMessage.text =
                when (user.introMsg.isBlank()) {
                    true -> getString(R.string.request_leave_intro_message)
                    false -> user.introMsg
                }

            if(targetUser.uid != weShareUser!!.uid){
                layoutSocialButton.visibility = View.VISIBLE

                if (user.follower.contains(weShareUser!!.uid)) {
                    buttonFollow.text = "已追蹤"
                } else {
                    buttonFollow.text = "追蹤他"
                }
            }
        }
    }
}