package com.zoe.weshare.ext

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.zoe.weshare.MainActivity
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.Author
import com.zoe.weshare.factory.AuthorViewModelFactory
import com.zoe.weshare.factory.ViewModelFactory

fun Fragment.getVmFactory(author: Author?): AuthorViewModelFactory {
    val repository = (requireContext().applicationContext as WeShareApplication).repository
    return AuthorViewModelFactory(repository, author)
}

fun Fragment.getVmFactory(): ViewModelFactory {
    val repository = (requireContext().applicationContext as WeShareApplication).repository
    return ViewModelFactory(repository)
}

