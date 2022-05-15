package com.zoe.weshare.ext

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.widget.Toast
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

fun Activity.hideNavigationBar() {
    window.decorView.apply {
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
    }
}

fun Activity.showNavigationBar() {
    window.decorView.apply {
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
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
