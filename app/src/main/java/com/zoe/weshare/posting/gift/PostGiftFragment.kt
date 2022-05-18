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
import android.view.animation.Animation
import android.view.animation.AnimationUtils
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
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.ext.hideKeyboard
import com.zoe.weshare.ext.showDropdownMenu
import com.zoe.weshare.ext.showToast
import com.zoe.weshare.util.UserManager.weShareUser

class PostGiftFragment : Fragment() {

    private val PICK_IMAGE_REQUEST = 151
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

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK &&
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
        val condition = binding.dropdownMenuCondition.text.toString().trim()
        val description = binding.editDescription.text.toString().trim()

        when (true) {
            title.isEmpty() ->{
                activity.showToast(getString(R.string.error_title_isEmpty))
            }

            sort.isEmpty() ->{
                activity.showToast(getString(R.string.error_sort_isEmpty))
            }

            condition.isEmpty() ->{
                activity.showToast(getString(R.string.error_condition_isEmpty))
            }

            description.isEmpty() ->{
                activity.showToast(getString(R.string.error_description_isEmpty))
            }

            (imagePathUri == null) -> {
                activity.showToast(getString(R.string.error_image_isEmpty))
            }

            else -> viewModel.onSaveUserInput(title, sort, condition, description, imagePathUri!!)
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
