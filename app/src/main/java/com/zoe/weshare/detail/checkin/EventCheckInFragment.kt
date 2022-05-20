package com.zoe.weshare.detail.checkin

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.zoe.weshare.MainActivity
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.R
import com.zoe.weshare.data.EventPost
import com.zoe.weshare.databinding.FragmentEventCheckInBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.ext.sendNotificationToTarget
import com.zoe.weshare.ext.showToast
import com.zoe.weshare.util.UserManager.weShareUser
import java.io.IOException

class EventCheckInFragment : Fragment() {
    companion object {
        const val requestCameraPermission = 1001
    }

    private val aniSlide: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.scanner_animation
        )
    }

    private lateinit var binding: FragmentEventCheckInBinding
    private lateinit var event: EventPost

    private lateinit var cameraSource: CameraSource
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var surfaceHolder: SurfaceHolder
    private var isCameraPermissionGranted = false
    private var scannedValue = ""
    private var scanComplete = false

    private val viewModel by viewModels<CheckInViewModel> { getVmFactory(weShareUser) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCameraPermissionGranted = checkCameraPermission()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentEventCheckInBinding.inflate(inflater, container, false)

        event = EventCheckInFragmentArgs.fromBundle(requireArguments()).event

        viewModel.fetchArg(event)

        if (isCameraPermissionGranted) {

            binding.barcodeLine.startAnimation(aniSlide)
            setupDetector()
            setupSurfaceCallBack()


            viewModel.saveLogComplete.observe(viewLifecycleOwner) {
                it?.let {
                    sendNotificationToTarget(event.author.uid, it)

                    findNavController().navigate(EventCheckInFragmentDirections
                        .actionEventCheckInFragmentToEventDetailFragment(event)
                    )

                    activity.showToast(getString(R.string.toast_check_in_successfully))

                    viewModel.checkInComplete()
                }
            }
        } else {
            requestCameraPermissions()
        }

        return binding.root
    }

    @SuppressLint("MissingPermission")
    private fun setupDetector() {
        barcodeDetector = BarcodeDetector.Builder(requireContext())
            .setBarcodeFormats(Barcode.ALL_FORMATS).build()

        cameraSource = CameraSource.Builder(requireContext(), barcodeDetector)
            .setRequestedPreviewSize(1920, 1080)
            .setAutoFocusEnabled(true) // you should add this feature
            .build()

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
                activity.showToast(getString(R.string.scanner_has_been_closed))
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems

                if (barcodes.size() == 1) {
                    scannedValue = barcodes.valueAt(0).rawValue

                    //  must run on main thread
                    (activity as MainActivity).runOnUiThread {

                        cameraSource.stop()
                        binding.barcodeLine.clearAnimation()

                        //prevent double acquire data
                        if (!scanComplete) {
                            scanComplete = true

                            if (isQrcodeDataCorrect()) {
                                viewModel.checkInEvent(scannedValue)
                            } else {
                                showErrorMsg(cameraSource)
                            }
                        }
                    } }
            }
        })
    }

    @SuppressLint("MissingPermission")
    fun setupSurfaceCallBack() {
        binding.cameraSurfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    surfaceHolder = holder
                    // Start preview after 1s delay
                    cameraSource.start(holder)

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int,
            ) {
                try {
                    cameraSource.start(holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }
        })

    }

    private fun isQrcodeDataCorrect(): Boolean {
        return scannedValue == event.id
    }

    @SuppressLint("MissingPermission")
    private fun showErrorMsg(cameraSource: CameraSource) {

        AlertDialog.Builder(requireActivity())
            .setMessage(getString(R.string.error_check_in_with_wrong_id))
            .setPositiveButton(getString(R.string.error_re_chceck_int)) { dialog, id ->
                cameraSource.start(surfaceHolder)
                binding.barcodeLine.startAnimation(aniSlide)

                scanComplete = false

                dialog.cancel()
            }

            .setNegativeButton(getString(R.string.force_end_no)) { dialog, id ->
                dialog.cancel()
                findNavController().navigateUp()
            }.show()
    }

    private fun checkCameraPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermissions() {
        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    findNavController().navigate(
                        EventCheckInFragmentDirections.actionEventCheckInFragmentSelf(event)
                    )
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
            .setTitle(getString(R.string.alert_require_camera_permission))
            .setMessage(getString(R.string.alert_require_camera_permission_msg))
            .setPositiveButton(getString(R.string.confirm_yes)) { _, _ ->

                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(intent, requestCameraPermission)
            }
            .setNegativeButton(getString(R.string.confirm_no)) { _, _ ->

                findNavController().navigate(
                    NavGraphDirections.navigateToHomeFragment())
            }.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isCameraPermissionGranted) {
            cameraSource.stop()
        }
    }
}
