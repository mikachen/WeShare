package com.zoe.weshare.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.zoe.weshare.R
import com.zoe.weshare.databinding.FragmentProfileBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.posting.gift.PostGiftViewModel
import com.zoe.weshare.util.UserManager


class ProfileFragment : Fragment() {

    lateinit var binding : FragmentProfileBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentProfileBinding.inflate(inflater, container, false)


        val viewModel by viewModels<ProfileViewModel> { getVmFactory(UserManager.author) }


        return binding.root
    }


}
