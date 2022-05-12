package com.zoe.weshare.ext

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.factory.ViewModelFactory

/***
 * Extension functions for Activity.
 */
fun Activity.getVmFactory(): ViewModelFactory {
    val repository = (applicationContext as WeShareApplication).repository
    return ViewModelFactory(repository)
}

fun Activity?.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).apply {
        setGravity(Gravity.CENTER, 0, 0)
        show()
    }
}

//
//fun Activity.checkLocationPermission(): Boolean {
//    // 檢查權限
//    return ActivityCompat.checkSelfPermission(
//        this,
//        Manifest.permission.ACCESS_FINE_LOCATION
//    ) == PackageManager.PERMISSION_GRANTED
//}
//
//fun Activity.requestLocationPermissions() {
//
//    Dexter.withContext(this)
//        .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
//        .withListener(object : PermissionListener {
//            override fun onPermissionGranted(response: PermissionGrantedResponse) {
//                findNavController(R.id.nav_host_fragment).navigate(
//                    NavGraphDirections.navigateToMapFragment())
//            }
//
//            override fun onPermissionDenied(response: PermissionDeniedResponse) {
//                AlertDialog.Builder(applicationContext)
//                    .setTitle("請開啟位置權限")
//                    .setMessage("此應用程式，位置權限已被關閉，需開啟才能正常使用")
//                    .setPositiveButton("確定") { _, _ ->
//                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//                        startActivityForResult(intent, 111)
//                    }
//                    .setNegativeButton("取消") { _, _ -> }
//                    .show()
//            }
//
//            override fun onPermissionRationaleShouldBeShown(
//                permission: PermissionRequest?,
//                token: PermissionToken?,
//            ) {
//                AlertDialog.Builder(applicationContext)
//                    .setTitle("請開啟位置權限")
//                    .setMessage("此應用程式，位置權限已被關閉，需開啟才能正常使用")
//                    .setPositiveButton("確定") { _, _ ->
//                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//                        startActivityForResult(intent, 111)
//                    }
//                    .setNegativeButton("取消") { _, _ -> }
//                    .show()
//            }
//        }).check()
//}
