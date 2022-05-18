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
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.databinding.FragmentPostEventBinding
import com.zoe.weshare.ext.*
import com.zoe.weshare.util.UserManager.weShareUser

class PostEventFragment : Fragment() {

    private val PICK_IMAGE_REQUEST = 151

    private var imagePathUri: Uri? = null
    private var startTime: Long? = null
    private var endTime: Long? = null

    private lateinit var binding: FragmentPostEventBinding
    private lateinit var sortAdapter: ArrayAdapter<String>

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


        setupViewAndBtn()
        setupDropdownMenu()
        setupDatePicker()

        return binding.root
    }

    private fun setupViewAndBtn() {
        whatToPostAnimate.duration = 500

        binding.titleWhatToPost.startAnimation(whatToPostAnimate)

        binding.nextButton.setOnClickListener {
            if (checkPermission()) {
                dataCollecting()
            } else {
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
            imagePathUri = data.data!!

            try {

                binding.buttonImagePreviewHolder.setImageURI(imagePathUri)

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
        val datePick = binding.editDatePicker.text.toString()

        when (true) {
            title.isEmpty() -> {
                activity.showToast(getString(R.string.error_title_isEmpty))
            }

            sort.isEmpty() -> {
                activity.showToast(getString(R.string.error_sort_isEmpty))
            }

            volunteerNeeds.isEmpty() -> {
                activity.showToast(getString(R.string.error_volunteer_need))
            }

            description.isEmpty() -> {
                activity.showToast(getString(R.string.error_description_isEmpty))
            }
            datePick.isEmpty() -> {
                activity.showToast(getString(R.string.error_date_pick_isEmpty))
            }

            (imagePathUri == null) ->
                activity.showToast(getString(R.string.error_image_isEmpty))

            else -> viewModel.fetchUserInput(
                title, sort, volunteerNeeds, description, imagePathUri!!, startTime!!, endTime!!)
        }
    }

    private fun setupDropdownMenu() {
        val sortsString = resources.getStringArray(R.array.event_type_sort)

        sortAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1, sortsString
        )

        binding.dropdownMenuSort.apply {
            setAdapter(sortAdapter)

            setOnClickListener {
                this.hideKeyboard()
                this.showDropdownMenu(sortAdapter)
            }
        }
    }

    private fun setupDatePicker() {

        val dateRangePicker =
            MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select dates")
                .build()

        binding.editDatePicker.setOnClickListener {
            dateRangePicker.show(requireActivity().supportFragmentManager, "date_picker")
        }

        dateRangePicker.addOnPositiveButtonClickListener { datePick ->

            startTime = datePick.first
            endTime = datePick.second


            if (startTime != null && endTime != null) {
                onDatePickDisplay(startTime!!, endTime!!)
            }
        }
    }

    fun onDatePickDisplay(startTime: Long, endTime: Long) {

        val dateDisplay: String = WeShareApplication.instance.getString(
            R.string.preview_event_time,
            startTime.toDisplayFormat(),
            endTime.toDisplayFormat())

        binding.editDatePicker.setText(dateDisplay)
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
