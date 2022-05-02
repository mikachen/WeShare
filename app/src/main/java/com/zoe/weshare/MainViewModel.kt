package com.zoe.weshare

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zoe.weshare.data.ChatRoom
import com.zoe.weshare.data.MessageItem
import com.zoe.weshare.data.OperationLog
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.util.CurrentFragmentType

class MainViewModel(private val repository: WeShareRepository) : ViewModel() {

    val currentFragmentType = MutableLiveData<CurrentFragmentType>()

    var liveNotifications = MutableLiveData<List<OperationLog>>()



    // call this function right after user login
    fun getLiveNotificationResult(uid: String) {
        liveNotifications = repository.getLiveNotifications(uid)
    }
}
