package com.zoe.weshare.manage.distribution

import androidx.lifecycle.ViewModel
import com.zoe.weshare.data.UserInfo
import com.zoe.weshare.data.source.WeShareRepository

class DistributeViewModel(
    val repository: WeShareRepository,
    private val userInfo: UserInfo?,
) : ViewModel() {



}