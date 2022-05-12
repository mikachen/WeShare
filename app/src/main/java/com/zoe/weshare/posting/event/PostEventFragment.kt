package com.zoe.weshare.posting.event

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.zoe.weshare.MainActivity
import com.zoe.weshare.R
import com.zoe.weshare.databinding.FragmentPostEventBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.util.UserManager.weShareUser

class PostEventFragment : Fragment() {

    private val PICK_IMAGE_REQUEST = 151
    private lateinit var filePath: Uri

    private lateinit var binding: FragmentPostEventBinding

    private val whatToPostAnimate: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.event_checkin_success
        )
    }

    val viewModel by viewModels<PostEventViewModel> { getVmFactory(weShareUser) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentPostEventBinding.inflate(inflater, container, false)

        viewModel.event.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(
                    PostEventFragmentDirections.actionPostEventFragmentToSearchLocationFragment(
                        newEvent = it,
                        newGift = null
                    )
                )
                viewModel.navigateNextComplete()
            }
        }

        viewModel.datePick.observe(viewLifecycleOwner) {
            binding.editDatePicker.setText(it)
        }

        viewModel.imageUri.observe(viewLifecycleOwner) {
            binding.buttonImagePreviewHolder.setImageURI(it)
        }

        setupViewNBtn()
        setupDropdownMenu()
        setupDatePicker()

        return binding.root
    }

    private fun setupViewNBtn() {
        whatToPostAnimate.duration = 500

        binding.titleWhatToPost.startAnimation(whatToPostAnimate)

        binding.nextButton.setOnClickListener {
            if (checkPermission()) {
                dataCollecting()
            }else{
                requestPermissions()
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

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK &&
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

        val title = binding.editTitle.text.toString().trim()
        val sort = binding.dropdownMenuSort.text.toString().trim()
        val volunteerNeeds = binding.editVolunteer.text.toString().trim()
        val description = binding.editDescription.text.toString().trim()
        val time = binding.editDatePicker.text.toString()

        when (true) {
            title.isEmpty() ->
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_title_isEmpty), Toast.LENGTH_SHORT
                ).show()

            sort.isEmpty() ->
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_sort_isEmpty), Toast.LENGTH_SHORT
                ).show()

            volunteerNeeds.isEmpty() ->
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_volunteer_need), Toast.LENGTH_SHORT
                ).show()

            description.isEmpty() ->
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_description_isEmpty), Toast.LENGTH_SHORT
                ).show()

            time.isEmpty() ->
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_date_pick_isEmpty), Toast.LENGTH_SHORT
                ).show()

            (viewModel.imageUri.value == null) ->
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_image_isEmpty), Toast.LENGTH_SHORT
                ).show()

            else -> viewModel.onSaveUserInput(title, sort, volunteerNeeds, description)
        }
    }

    private fun setupDropdownMenu() {
        val sortsString = resources.getStringArray(R.array.event_type_sort)

        val sortAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1, sortsString
        )
        binding.dropdownMenuSort.setAdapter(sortAdapter)
    }

    private fun setupDatePicker() {

        val dateRangePicker =
            MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select dates")
                .build()

        dateRangePicker.addOnPositiveButtonClickListener { datePick ->

            val startDate = datePick.first
            val secondDate = datePick.second

            viewModel.onDatePickDisplay(startDate, secondDate)
        }

        binding.editDatePicker.setOnClickListener {
            dateRangePicker.show(requireActivity().supportFragmentManager, "date_picker")
        }
    }

    private fun checkPermission(): Boolean {
        // 檢查權限
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    dataCollecting()
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
