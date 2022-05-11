package com.zoe.weshare.detail.checkin

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.zoe.weshare.MainActivity
import com.zoe.weshare.R
import com.zoe.weshare.data.EventPost
import com.zoe.weshare.databinding.FragmentEventCheckInBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.ext.sendNotificationToTarget
import com.zoe.weshare.util.UserManager.weShareUser
import java.io.IOException

class EventCheckInFragment : Fragment() {

    lateinit var binding: FragmentEventCheckInBinding
    lateinit var event: EventPost

    private val requestCodeCameraPermission = 1001
    private lateinit var cameraSource: CameraSource
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cholder: SurfaceHolder

    private val aniSlide: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.scanner_animation
        )
    }

    private var scannedValue = ""
    private var scanComplete = false

    val viewModel by viewModels<CheckInViewModel> { getVmFactory(weShareUser) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentEventCheckInBinding.inflate(inflater, container, false)

        event = EventCheckInFragmentArgs.fromBundle(requireArguments()).event
        viewModel.event = event

        if (ContextCompat.checkSelfPermission(
                (activity as MainActivity), android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            askForCameraPermission()
        } else {
            setupControls()
        }

        binding.barcodeLine.startAnimation(aniSlide)

        viewModel.saveLogComplete.observe(viewLifecycleOwner) {
            it?.let {
                sendNotificationToTarget(event.author!!.uid, it)

                findNavController().navigate(
                    EventCheckInFragmentDirections
                        .actionEventCheckInFragmentToEventDetailFragment(event)
                )

                viewModel.navigateComplete()
                Toast.makeText(requireContext(), "簽到成功", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun setupControls() {
        barcodeDetector =
            BarcodeDetector.Builder(requireContext()).setBarcodeFormats(Barcode.ALL_FORMATS).build()

        cameraSource = CameraSource.Builder(requireContext(), barcodeDetector)
            .setRequestedPreviewSize(1920, 1080)
            .setAutoFocusEnabled(true) // you should add this feature
            .build()

        binding.cameraSurfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    cholder = holder
                    // Start preview after 1s delay
                    cameraSource.start(holder)

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            @SuppressLint("MissingPermission")
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

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
                Toast.makeText(requireContext(),
                    "Scanner has been closed", Toast.LENGTH_SHORT).show()
            }

            @SuppressLint("MissingPermission")
            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() == 1) {
                    scannedValue = barcodes.valueAt(0).rawValue

                    // Don't forget to add this line printing value or finishing activity must run on main thread
                    (activity as MainActivity).runOnUiThread {
                        cameraSource.stop()
                        binding.barcodeLine.clearAnimation()

                        if (!scanComplete) {
                            scanComplete = true

                            if (scannedValue == event.id) {
                                viewModel.checkInEvent(scannedValue)
                            } else {
                                showErrorMsg(cameraSource)
                            }
                        }
                    }
                }
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun showErrorMsg(cameraSource:CameraSource) {
        val builder = AlertDialog.Builder(requireActivity())

        builder.apply {
//            setTitle("條碼與該活動不相符，請重新確認")
            setMessage("條碼與該活動不相符，請重新確認")
            setPositiveButton("重新掃描") { dialog, id ->
                cameraSource.start(cholder)
                binding.barcodeLine.startAnimation(aniSlide)

                scanComplete = false

                dialog.cancel()
            }

            setNegativeButton("取消返回") { dialog, id ->
                dialog.cancel()
                findNavController().navigateUp()
            }
        }

        val alter: AlertDialog = builder.create()
        alter.show()
    }


    private fun askForCameraPermission() {
        ActivityCompat.requestPermissions(
            (activity as MainActivity),
            arrayOf(android.Manifest.permission.CAMERA),
            requestCodeCameraPermission
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodeCameraPermission && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupControls()
            } else {
                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSource.stop()
    }
}
