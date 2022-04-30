package com.zoe.weshare.posting.gift

import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.Intent
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.storage.FirebaseStorage
import com.zoe.weshare.R
import com.zoe.weshare.databinding.FragmentPostGiftBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.ext.toDisplayFormat
import com.zoe.weshare.util.UserManager.weShareUser

class PostGiftFragment : Fragment() {

    private val PICK_IMAGE_REQUEST = 151
    private lateinit var filePath: Uri

    private lateinit var binding: FragmentPostGiftBinding

    val viewModel by viewModels<PostGiftViewModel> { getVmFactory(weShareUser) }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentPostGiftBinding.inflate(inflater, container, false)

        viewModel.gift.observe(viewLifecycleOwner) {
            findNavController().navigate(
                PostGiftFragmentDirections.actionPostGiftFragmentToSearchLocationFragment(
                    newGift = it,
                    newEvent = null
                )
            )
        }

        setupBtn()
        setupDropdownMenu()

        return binding.root
    }

    private fun setupBtn() {
        binding.nextButton.setOnClickListener {
            dataCollecting()
        }

        binding.buttonImagePreviewHolder.setOnClickListener {
            selectImage()
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

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK &&
            data != null && data.data != null
        ) {
            filePath = data.data!!

            try {
                 val bitmap = MediaStore.Images.Media.getBitmap(
                    requireActivity().contentResolver, filePath)

                binding.buttonImagePreviewHolder.setImageBitmap(bitmap)

                viewModel.imageUri = filePath

            } catch (e: Exception) {
                // Log the exception
                e.printStackTrace()
            }
        }
    }

    private fun dataCollecting() {

        val title = binding.editTitle.text.toString()
        val sort = binding.dropdownMenuSort.text.toString()
        val condition = binding.dropdownMenuCondition.text.toString()
        val description = binding.editDescription.text.toString()

        when (true) {
            title.isEmpty() ->
                Toast.makeText(requireContext(), getString(R.string.error_title_isEmpty), Toast.LENGTH_SHORT).show()

            sort.isEmpty() ->
                Toast.makeText(requireContext(), getString(R.string.error_sort_isEmpty), Toast.LENGTH_SHORT).show()

            condition.isEmpty() ->
                Toast.makeText(requireContext(), getString(R.string.error_condition_isEmpty), Toast.LENGTH_SHORT).show()

            description.isEmpty() ->
                Toast.makeText(requireContext(), getString(R.string.error_description_isEmpty), Toast.LENGTH_SHORT).show()

            (viewModel.imageUri == null) ->
                Toast.makeText(requireContext(), getString(R.string.error_image_isEmpty), Toast.LENGTH_SHORT).show()


            else -> viewModel.onSaveUserInput(title, sort, condition, description)
        }
    }

    private fun setupDropdownMenu() {
        val sortsString = resources.getStringArray(R.array.gift_item_sort)
        val conditionString = resources.getStringArray(R.array.gift_item_condition)

        val sortAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1, sortsString
        )
        binding.dropdownMenuSort.setAdapter(sortAdapter)

        val conditionAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1, conditionString
        )
        binding.dropdownMenuCondition.setAdapter(conditionAdapter)
    }
}
