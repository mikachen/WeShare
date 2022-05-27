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
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
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
import com.zoe.weshare.ext.*
import com.zoe.weshare.posting.event.PostEventFragment
import com.zoe.weshare.util.UserManager.weShareUser

class PostGiftFragment : Fragment() {

    companion object {
        const val PICK_IMAGE_REQUEST = 151
    }

    private var imagePathUri: Uri? = null

    private lateinit var sortAdapter: ArrayAdapter<String>
    private lateinit var conditionAdapter: ArrayAdapter<String>

    private val whatToPostAnimate: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.event_checkin_success
        )
    }

    private lateinit var binding: FragmentPostGiftBinding

    private val viewModel by viewModels<PostGiftViewModel> { getVmFactory(weShareUser) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentPostGiftBinding.inflate(inflater, container, false)

        viewModel.draftGiftInput.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(
                    PostGiftFragmentDirections.actionPostGiftToSearchLocation(
                        draftGift = it,
                        draftEvent = null
                    )
                )
                viewModel.navigateNextComplete()
            }
        }

        setupViewNBtn()
        setupDropdownMenu()

        return binding.root
    }

    private fun setupViewNBtn() {
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
            Intent.createChooser(intent, getString(R.string.select_image)),
            PostEventFragment.PICK_IMAGE_REQUEST
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {

            if (data != null) {
                imagePathUri = data.data

                try {
                    binding.buttonImagePreviewHolder.setImageURI(imagePathUri)

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    }

    private fun dataCollecting() {

        val title = binding.editTitle.text.toString().trim()
        val sort = binding.dropdownMenuSort.text.toString().trim()
        val condition = binding.dropdownMenuCondition.text.toString().trim()
        val description = binding.editDescription.text.toString().trim()

        val isCollectComplete = verifyData(title, sort, condition, description)

        if (isCollectComplete) { viewModel.onSaveUserInput(
                title, sort, condition, description, imagePathUri?: return)
        }
    }

    /**
     *  error handle all input data , and must not be empty or null
     * */
    private fun verifyData(
        title: String,
        sort: String,
        condition: String,
        description: String,
    ): Boolean {

        when {
            title.isEmpty() -> {
                activity.showToast(getString(R.string.error_title_isEmpty))
                return false
            }

            sort.isEmpty() -> {
                activity.showToast(getString(R.string.error_sort_isEmpty))
                return false
            }

            condition.isEmpty() -> {
                activity.showToast(getString(R.string.error_condition_isEmpty))
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

        val sortsString = resources.getStringArray(R.array.gift_item_sort)
        val conditionString = resources.getStringArray(R.array.gift_item_condition)

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

        conditionAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1, conditionString
        )

        binding.dropdownMenuCondition.apply {
            setAdapter(conditionAdapter)

            setOnClickListener {
                this.hideKeyboard()
                this.showDropdownMenu(conditionAdapter)
            }
        }

        sortAdapter.setNotifyOnChange(true)
        conditionAdapter.setNotifyOnChange(true)
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
                    token: PermissionToken?,
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
