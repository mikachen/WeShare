package com.zoe.weshare.report

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.R
import com.zoe.weshare.databinding.FragmentPolicyTermBinding
import com.zoe.weshare.util.UserManager.userConsentPolicy

class PolicyTermFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        val binding = FragmentPolicyTermBinding.inflate(inflater, container, false)


        binding.userConsent.setOnClickListener {

            binding.consentBox.isChecked = true
            userConsentPolicy = true

            findNavController().navigate(NavGraphDirections.actionGlobalLoginFragment())
        }


        return binding.root
    }
}