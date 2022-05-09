package com.zoe.weshare.posting.gift

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.zoe.weshare.MainActivity
import com.zoe.weshare.R
import com.zoe.weshare.databinding.FragmentPostGiftBinding
import com.zoe.weshare.ext.checkLocationPermission
import com.zoe.weshare.ext.getVmFactory
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
            it?.let {
                findNavController().navigate(
                    PostGiftFragmentDirections.actionPostGiftFragmentToSearchLocationFragment(
                        newGift = it,
                        newEvent = null
                    )
                )
                viewModel.navigateNextComplete()
            }
        }

        viewModel.imageUri.observe(viewLifecycleOwner) {
            binding.buttonImagePreviewHolder.setImageURI(it)
        }

        setupBtn()
        setupDropdownMenu()

        return binding.root
    }

    private fun setupBtn() {
        binding.nextButton.setOnClickListener {
            if (checkLocationPermission()) {
                dataCollecting()
            }
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
                viewModel.imageUri.value = filePath
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
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_title_isEmpty),
                    Toast.LENGTH_SHORT
                ).show()

            sort.isEmpty() ->
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_sort_isEmpty),
                    Toast.LENGTH_SHORT
                ).show()

            condition.isEmpty() ->
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_condition_isEmpty),
                    Toast.LENGTH_SHORT
                ).show()

            description.isEmpty() ->
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_description_isEmpty),
                    Toast.LENGTH_SHORT
                ).show()

            (viewModel.imageUri.value == null) ->
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_image_isEmpty),
                    Toast.LENGTH_SHORT
                ).show()

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

    fun checkLocationPermission(): Boolean {
        // 檢查權限
        return if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            true
        } else {

            // 詢問要求獲取權限
            requestPermissions()
            false
        }
    }

    fun requestPermissions() {

        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("請開啟位置權限")
                        .setMessage("此應用程式，位置權限已被關閉，需開啟才能正常使用")
                        .setPositiveButton("確定") { _, _ ->
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            startActivityForResult(intent, 111)
                        }
                        .setNegativeButton("取消") { _, _ -> }
                        .show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?,
                ) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("請開啟位置權限")
                        .setMessage("此應用程式，位置權限已被關閉，需開啟才能正常使用")
                        .setPositiveButton("確定") { _, _ ->
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            startActivityForResult(intent, 111)
                        }
                        .setNegativeButton("取消") { _, _ -> }
                        .show()
                }
            }).check()
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
    }
}
