package com.zoe.weshare.profile

import androidx.lifecycle.ViewModel
import com.zoe.weshare.data.Author
import com.zoe.weshare.data.source.WeShareRepository

class ProfileViewModel(private val repository: WeShareRepository, val author: Author?): ViewModel() {
}