package com.zoe.weshare.profile.userself

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.R
import com.zoe.weshare.data.PostLog
import com.zoe.weshare.data.UserProfile
import com.zoe.weshare.databinding.FragmentSelfBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.util.LogType
import com.zoe.weshare.util.UserManager


class SelfFragment : Fragment() {

    val currentUser = UserManager.userZoe

    lateinit var binding: FragmentSelfBinding

    val viewModel: SelfViewModel by viewModels { getVmFactory(currentUser) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentSelfBinding.inflate(inflater, container, false)



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
                    logs.filter { it.logType == LogType.POSTGIFT.value }.size.toString()

                textGiftSentCount.text =
                    logs.filter { it.logType == LogType.SENDAWAYGIFT.value }.size.toString()

                textEventPostCount.text =
                    logs.filter { it.logType == LogType.POSTEVENT.value }.size.toString()

                textEventVolunteerCount.text =
                    logs.filter { it.logType == LogType.REGIVOLUNTEER.value }.size.toString()

            }

        }
    }

    private fun setupBtn() {
        binding.buttonGiftManage.setOnClickListener {
            findNavController().navigate(NavGraphDirections.actionGlobalPagerFilterFragment())
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
        }
    }
}