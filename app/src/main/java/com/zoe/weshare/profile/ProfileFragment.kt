package com.zoe.weshare.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.zoe.weshare.R
import com.zoe.weshare.data.Author
import com.zoe.weshare.data.UserProfile
import com.zoe.weshare.databinding.FragmentProfileBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.util.UserManager


class ProfileFragment : Fragment() {

    lateinit var binding : FragmentProfileBinding

    lateinit var userArg: Author

    val viewModel: ProfileViewModel by viewModels { getVmFactory(userArg)  }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentProfileBinding.inflate(inflater, container, false)

        userArg = arguments?.let { ProfileFragmentArgs.fromBundle(it).weshareUser } ?: Author(uid = UserManager.mockUserUid)


        viewModel.user.observe(viewLifecycleOwner) {
            setUpView(it)
        }



        return binding.root
    }


    private fun setUpView(user: UserProfile){

        binding.apply {
            bindImage(imageProfileAvatar,user.image)
            textProfileName.text = user.name
            textFollowerNumber.text = (user.follower?.size?: 0).toString()
            textFollowingNumber.text = (user.following?.size?: 0).toString()
            textIntroMessage.text =
                when (user.introMsg.isBlank()){
                    true -> getString(R.string.request_leave_intro_message)
                    false -> user.introMsg
                }

        }

    }

}
