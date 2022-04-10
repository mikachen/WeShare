package com.zoe.weshare.ext

import androidx.fragment.app.Fragment
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.Author
import com.zoe.weshare.factory.AuthorViewModelFactory


fun Fragment.getVmFactory(author: Author?): AuthorViewModelFactory {
    val repository = (requireContext().applicationContext as WeShareApplication).repository
    return AuthorViewModelFactory(repository, author)
}