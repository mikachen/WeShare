package com.zoe.weshare.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.R
import com.zoe.weshare.data.PostLog
import com.zoe.weshare.data.UserInfo
import com.zoe.weshare.data.UserProfile
import com.zoe.weshare.databinding.FragmentProfileBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.posting.SearchLocationFragmentArgs
import com.zoe.weshare.util.LogType
import com.zoe.weshare.util.UserManager

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

        setupBtn()
        return binding.root
    }

    private fun setupLogView(logs: List<PostLog>) {
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

    private fun setupBtn() {
        binding.buttonGiftManage.setOnClickListener {
            findNavController().navigate(NavGraphDirections.actionGlobalPagerFilterFragment())
        }

        binding.buttonFollow.setOnClickListener {
            viewModel.updateTargetFollower(targetUser.uid)
            viewModel.updateUserFollowing(targetUser.uid)
        }

        binding.buttonMessage.setOnClickListener {  }
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

            if(user.follower.contains(UserManager.weShareUser!!.uid)) {
                buttonFollow.text = "已追蹤"
            }else{
//                buttonFollow.text = "已追蹤"
            }
        }
    }
}