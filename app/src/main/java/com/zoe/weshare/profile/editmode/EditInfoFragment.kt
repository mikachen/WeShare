package com.zoe.weshare.profile.editmode

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.zoe.weshare.MainActivity
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.data.UserProfile
import com.zoe.weshare.databinding.FragmentEditInfoBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.ext.hideNavigationBar
import com.zoe.weshare.ext.showNavigationBar
import com.zoe.weshare.network.LoadApiStatus
import com.zoe.weshare.util.UserManager.weShareUser

class EditInfoFragment : Fragment() {

    private val PICK_IMAGE_REQUEST = 161

    lateinit var progressBar: ProgressBar
    lateinit var animation: ObjectAnimator

    lateinit var binding: FragmentEditInfoBinding
    val viewModel by viewModels<EditInfoViewModel> { getVmFactory(weShareUser) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentEditInfoBinding.inflate(inflater, container, false)

        val userProfile = EditInfoFragmentArgs.fromBundle(requireArguments()).userProfile
        viewModel.onProfileDisplay(userProfile)

        viewModel.profileUpdate.observe(viewLifecycleOwner) {
            viewModel.updateProfile(it)
        }

        viewModel.updatingProgress.observe(viewLifecycleOwner) {
            animation = ObjectAnimator.ofInt(
                progressBar,
                "progress",
                progressBar.progress,
                it * 100
            )
            animation.duration = 300
            animation.interpolator = DecelerateInterpolator()
            animation.start()
        }

        viewModel.isUploadingImage.observe(viewLifecycleOwner) {
            if (it) {
                (activity as MainActivity).binding.imageUploadingHint.visibility = View.VISIBLE
            }
        }

        viewModel.updateComplete.observe(viewLifecycleOwner) {
            if (it == LoadApiStatus.DONE) {
                (activity as MainActivity).binding.layoutMainProgressBar.visibility = View.INVISIBLE
                (activity as MainActivity).binding.imageUploadingHint.visibility = View.INVISIBLE

                (activity as MainActivity).showNavigationBar()
                findNavController().navigate(
                    NavGraphDirections.actionGlobalProfileFragment(weShareUser)
                )
            }
        }

        setupView(userProfile)
        return binding.root
    }

    private fun setupView(profile: UserProfile) {
        binding.apply {
            bindImage(imageProfileAvatar, profile.image)
            editNickname.setText(profile.name)
            editIntroMsg.setText(profile.introMsg)

            buttonSave.setOnClickListener {
                collectUserInput()
            }

            imageProfileAvatar.setOnClickListener {
                selectImage()
            }
        }

        progressBar = (activity as MainActivity).binding.progressBar
        progressBar.max = 100 * 100
    }

    private fun collectUserInput() {
        val name = binding.editNickname.text.toString().trim()
        val introMsg = binding.editIntroMsg.text.toString().trim()

        if (name.isNotEmpty()) {
            viewModel.checkIfImageChange(name, introMsg)
            (activity as MainActivity).binding.layoutMainProgressBar.visibility = View.VISIBLE

            (activity as MainActivity).hideNavigationBar()
        } else {
            Toast.makeText(requireContext(), "暱稱為必填項目", Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectImage() {
        // Defining Implicit Intent to mobile gallery

        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(
                intent,
                "Select Image from here..."
            ),
            PICK_IMAGE_REQUEST
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK &&
            data != null && data.data != null
        ) {

            val filePath = data.data!!

            try {

                binding.imageProfileAvatar.setImageURI(filePath)
                viewModel.newImage = filePath
            } catch (e: Exception) {
                // Log the exception
                e.printStackTrace()
            }
        }
    }
}
