package com.zoe.weshare.ext

import androidx.fragment.app.Fragment
import com.zoe.weshare.WeShareApplication
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

