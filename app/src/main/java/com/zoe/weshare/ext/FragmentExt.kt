package com.zoe.weshare.ext

import android.Manifest
import android.R
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener
import com.karumi.dexter.listener.single.PermissionListener
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.SendNotificationService
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.OperationLog
import com.zoe.weshare.data.UserInfo
import com.zoe.weshare.factory.AuthorViewModelFactory
import com.zoe.weshare.factory.ViewModelFactory


fun Fragment.getVmFactory(userInfo: UserInfo?): AuthorViewModelFactory {
    val repository = (requireContext().applicationContext as WeShareApplication).repository
    return AuthorViewModelFactory(repository, userInfo)
}

fun Fragment.getVmFactory(): ViewModelFactory {
    val repository = (requireContext().applicationContext as WeShareApplication).repository
    return ViewModelFactory(repository)
}

fun Fragment.checkLocationPermission(): Boolean {
    // 檢查權限
    return if (ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        // permission granted
        true
    } else {
        // 詢問要求獲取權限
        requestPermissions()
        false
    }
}

fun Fragment.requestPermissions() {

    Dexter.withContext(requireContext())
        .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        .withListener(object : PermissionListener {
            override fun onPermissionGranted(response: PermissionGrantedResponse) {
                findNavController().navigate(NavGraphDirections.navigateToMapFragment())
            }

            override fun onPermissionDenied(response: PermissionDeniedResponse) {
                AlertDialog.Builder(requireContext())
                    .setTitle("請開啟位置權限")
                    .setMessage("此應用程式，位置權限已被關閉，需開啟才能正常使用")
                    .setPositiveButton("確定") { _, _ ->
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivityForResult(intent, 111)
                    }
                    .setNegativeButton("取消") { _, _ ->
                        findNavController().navigate(NavGraphDirections.navigateToHomeFragment())
                    }
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
                    .setNegativeButton("取消") { _, _ ->
                        findNavController().navigate(NavGraphDirections.navigateToHomeFragment())
                    }
                    .show()

            }
        }).check()
}

fun Fragment.sendNotifications(log: OperationLog){
    val intent = Intent(requireContext(), SendNotificationService::class.java)
    intent.putExtra(SendNotificationService.SEND_NOTIFICATION,log)

    requireContext().startService(intent)
}