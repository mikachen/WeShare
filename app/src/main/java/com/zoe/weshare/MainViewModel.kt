package com.zoe.weshare

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zoe.weshare.data.OperationLog
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.util.CurrentFragmentType
import com.zoe.weshare.util.UserManager.weShareUser

class MainViewModel(private val repository: WeShareRepository) : ViewModel() {

    val currentFragmentType = MutableLiveData<CurrentFragmentType>()

    var liveNotifications = MutableLiveData<List<OperationLog>>()

    var reObserveNotification = MutableLiveData<Boolean>()


    fun getLiveNotificationResult() {
        liveNotifications = repository.getLiveNotifications(weShareUser!!.uid)

        reObserveNotification.value = true
    }
}
