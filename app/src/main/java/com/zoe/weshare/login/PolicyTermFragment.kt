package com.zoe.weshare.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.zoe.weshare.R
import com.zoe.weshare.databinding.FragmentPolicyTermBinding
import com.zoe.weshare.ext.showToast
import com.zoe.weshare.util.UserManager.userConsentPolicy

class PolicyTermFragment : Fragment() {

    private lateinit var binding:FragmentPolicyTermBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentPolicyTermBinding.inflate(inflater, container, false)

        val navigateFromProfile = PolicyTermFragmentArgs.fromBundle(requireArguments()).fromProfile


        if (navigateFromProfile){
            binding.layoutConsentSection.visibility = View.GONE
        }else {
            binding.layoutConsentSection.visibility = View.VISIBLE
            setupBtn()
        }

        return binding.root
    }

    private fun setupBtn(){

        binding.apply {

            layoutConsentButton.setOnClickListener {
                this.consentBox.isChecked = !hasUserAgreed()
            }

            buttonAgree.setOnClickListener {
                if (hasUserAgreed()){
                    findNavController().navigate(
                        PolicyTermFragmentDirections.actionPolicyTermFragmentToLoginFragment())
                    userConsentPolicy = true
                }else{
                    activity.showToast(getString(R.string.toast_must_check_agree))
                }
            }

            buttonDisagree.setOnClickListener {
                findNavController().navigate(
                    PolicyTermFragmentDirections.actionPolicyTermFragmentToLoginFragment())
            }
        }
    }

    fun hasUserAgreed(): Boolean {
        return binding.consentBox.isChecked
    }
}