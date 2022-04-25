package com.zoe.weshare.profile.userself

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.R
import com.zoe.weshare.data.UserInfo
import com.zoe.weshare.data.UserProfile
import com.zoe.weshare.databinding.FragmentSelfBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.util.UserManager


class SelfFragment : Fragment() {

    val currentUser = UserManager.userZoe

    lateinit var binding: FragmentSelfBinding
    lateinit var userArg: UserInfo

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



        setupBtn()
        return binding.root
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
