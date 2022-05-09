package com.zoe.weshare.profile.editmode

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.storage.FirebaseStorage
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.data.UserProfile
import com.zoe.weshare.databinding.FragmentEditInfoBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.util.UserManager.weShareUser

class EditInfoFragment : Fragment() {

    private val PICK_IMAGE_REQUEST = 161
    val storage = FirebaseStorage.getInstance()
    val storageReference = storage.reference

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

        viewModel.updateComplete.observe(viewLifecycleOwner) {
            findNavController().navigate(
                NavGraphDirections.actionGlobalProfileFragment(weShareUser)
            )
        }

        binding.buttonSave.setOnClickListener {
            collectUserInput()
        }

        binding.imageProfileAvatar.setOnClickListener {
            selectImage()
        }

        setupView(userProfile)
        return binding.root
    }

    private fun setupView(profile: UserProfile) {
        binding.apply {
            bindImage(imageProfileAvatar, profile.image)
            editNickname.setText(profile.name)
            editIntroMsg.setText(profile.introMsg)
        }
    }

    private fun collectUserInput() {
        val name = binding.editNickname.text.toString()
        val introMsg = binding.editIntroMsg.text.toString()

        if (name.isNotEmpty()) {
            viewModel.checkIfImageChange(name, introMsg)
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
