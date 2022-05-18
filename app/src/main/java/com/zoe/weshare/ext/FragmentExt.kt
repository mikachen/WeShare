package com.zoe.weshare.ext

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.R
import com.zoe.weshare.SendNotificationService
import com.zoe.weshare.SendNotificationService.Companion.SEND_TO_ALL_FOLLOWERS
import com.zoe.weshare.SendNotificationService.Companion.SEND_TO_AUTHOR_MSG
import com.zoe.weshare.SendNotificationService.Companion.SEND_TO_AUTHOR_UID
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
    return ActivityCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

fun Fragment.requestLocationPermissions() {

    Dexter.withContext(requireContext())
        .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        .withListener(object : PermissionListener {
            override fun onPermissionGranted(response: PermissionGrantedResponse) {
                findNavController().navigate(
                    NavGraphDirections.navigateToMapFragment()
                )
            }

            override fun onPermissionDenied(response: PermissionDeniedResponse) {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.alert_require_location_permission))
                    .setMessage(getString(R.string.alert_require_location_permission_msg))
                    .setPositiveButton(getString(R.string.confirm_yes)) { _, _ ->
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivityForResult(intent, 111)
                    }
                    .setNegativeButton(getString(R.string.confirm_no)) { _, _ ->
                        findNavController().navigate(NavGraphDirections.navigateToHomeFragment())
                    }
                    .show()
            }

            override fun onPermissionRationaleShouldBeShown(
                permission: PermissionRequest?,
                token: PermissionToken?,
            ) {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.alert_require_location_permission))
                    .setMessage(getString(R.string.alert_require_location_permission_msg))
                    .setPositiveButton(getString(R.string.confirm_yes)) { _, _ ->
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivityForResult(intent, 111)
                    }
                    .setNegativeButton(getString(R.string.confirm_no)) { _, _ ->
                        findNavController().navigate(NavGraphDirections.navigateToHomeFragment())
                    }
                    .show()
            }
        }).check()
}

fun Fragment.sendNotificationsToFollowers(log: OperationLog) {
    val intent = Intent(requireContext(), SendNotificationService::class.java)
    intent.putExtra(SEND_TO_ALL_FOLLOWERS, log)

    requireContext().startService(intent)
}

fun Fragment.sendNotificationToTarget(targetUid: String, log: OperationLog) {
    val intent = Intent(requireContext(), SendNotificationService::class.java)
    intent.putExtra(SEND_TO_AUTHOR_MSG, log)
    intent.putExtra(SEND_TO_AUTHOR_UID, targetUid)

    requireContext().startService(intent)
}


