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
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
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

    companion object{
        const val PICK_IMAGE_REQUEST = 151
    }

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

    private val viewModel by viewModels<PostEventViewModel> { getVmFactory(weShareUser) }

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
                requestLocationPermissions()
            }
        }

        binding.buttonImagePreviewHolder.setOnClickListener {
            selectImage()
        }
    }

    private fun selectImage() {

        val intent = Intent()

        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, getString(R.string.select_image)), PICK_IMAGE_REQUEST
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

        if (checkDataIntegrity(title, sort, volunteerNeeds, description, datePick)) {
            viewModel.fetchUserInput(
                title,
                sort,
                volunteerNeeds,
                description,
                imagePathUri!!,
                startTime!!,
                endTime!!
            )
        } else {
            return
        }
    }

    private fun checkDataIntegrity(
        title: String,
        sort: String,
        volunteerNeeds: String,
        description: String,
        datePick: String,
    ): Boolean {

        when {
            title.isEmpty() -> {
                activity.showToast(getString(R.string.error_title_isEmpty))
                return false
            }

            datePick.isEmpty() -> {
                activity.showToast(getString(R.string.error_date_pick_isEmpty))
                return false
            }

            sort.isEmpty() -> {
                activity.showToast(getString(R.string.error_sort_isEmpty))
                return false
            }

            volunteerNeeds.isEmpty() -> {
                activity.showToast(getString(R.string.error_volunteer_need))
                return false
            }

            description.isEmpty() -> {
                activity.showToast(getString(R.string.error_description_isEmpty))
                return false
            }

            (imagePathUri == null) -> {
                activity.showToast(getString(R.string.error_image_isEmpty))
                return false
            }

            else -> {
                return true
            }
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
                .setTitleText(getString(R.string.error_date_pick_isEmpty))
                .build()

        binding.editDatePicker.setOnClickListener {
            dateRangePicker.show(requireActivity().supportFragmentManager, "date_picker")
        }

        dateRangePicker.addOnPositiveButtonClickListener { datePick ->
            startTime = datePick.first
            endTime = datePick.second

            if (startTime != null && endTime != null) {
                val dateDisplay = getDatePickString(startTime!!, endTime!!)

                binding.editDatePicker.setText(dateDisplay)
            }
        }
    }

    private fun getDatePickString(startTime: Long, endTime: Long): String {
        return WeShareApplication.instance.getString(R.string.preview_event_time,
            startTime.toDisplayFormat(),
            endTime.toDisplayFormat())
    }

    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    dataCollecting()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {

                    showRationaleDialog()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    showRationaleDialog()
                }
            }).check()
    }

    private fun showRationaleDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.alert_require_location_permission))
            .setMessage(getString(R.string.alert_require_location_permission_msg))
            .setPositiveButton(getString(R.string.confirm_yes)) { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(intent, 111)
            }
            .setNegativeButton(getString(R.string.confirm_no)) { _, _ -> }
            .show()
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).setSoftInputMode(SOFT_INPUT_ADJUST_PAN)
    }
}
