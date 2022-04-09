package com.zoe.weshare.ext

import android.app.Activity
import android.view.Gravity
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
