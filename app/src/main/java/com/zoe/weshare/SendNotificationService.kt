package com.zoe.weshare

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zoe.weshare.data.OperationLog
import com.zoe.weshare.data.Result
import com.zoe.weshare.network.LoadApiStatus
import com.zoe.weshare.util.Logger
import com.zoe.weshare.util.UserManager.weShareUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SendNotificationService : Service() {

    private var serviceJob = Job()
    private val coroutineScope = CoroutineScope(serviceJob + Dispatchers.Main)

    companion object {
        const val SEND_TO_ALL_FOLLOWERS = "sendToAllFollowers"
        const val SEND_TO_AUTHOR_UID = "sendToAuthorUid"
        const val SEND_TO_AUTHOR_MSG = "sendToAuthorMsg"
    }

    lateinit var followers: List<String>

    private val _status = MutableLiveData<LoadApiStatus>()
    val status: LiveData<LoadApiStatus>
        get() = _status

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    var loopCount = -1

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val notifications = intent?.getParcelableExtra<OperationLog>(SEND_TO_ALL_FOLLOWERS)

        notifications?.let {
            getFollowersList(notifications)
        }

        val requestNotification = intent?.getParcelableExtra<OperationLog>(SEND_TO_AUTHOR_MSG)
        val requestTarget = intent?.getStringExtra(SEND_TO_AUTHOR_UID)

        requestNotification?.let { request ->
            requestTarget?.let { target ->
                loopCount = 1
                sendingNotification(target, request)
            }
        }

        return START_NOT_STICKY
    }

    fun getFollowersList(notifications: OperationLog) {
        coroutineScope.launch {

            _status.value = LoadApiStatus.LOADING

            when (
                val result =
                    WeShareApplication.instance.repository.getUserInfo(weShareUser!!.uid)
            ) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    followers = result.data?.follower ?: emptyList()

                    taskLoop(followers, notifications)
                }
                is Result.Fail -> {
                    _error.value = result.error
                    _status.value = LoadApiStatus.ERROR
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _status.value = LoadApiStatus.ERROR
                }
                else -> {
                    _error.value =
                        WeShareApplication.instance.getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    private fun taskLoop(follower: List<String>, log: OperationLog) {
        loopCount = follower.size

        for (target in follower) {
            sendingNotification(target, log)
        }
    }

    private fun sendingNotification(targetUid: String, log: OperationLog) {
        coroutineScope.launch {

            _status.value = LoadApiStatus.LOADING

            when (
                val result =
                    WeShareApplication.instance.repository.sendNotifications(targetUid, log)
            ) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE
                }
                is Result.Fail -> {
                    _error.value = result.error
                    _status.value = LoadApiStatus.ERROR
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _status.value = LoadApiStatus.ERROR
                }
                else -> {
                    _error.value =
                        WeShareApplication.instance.getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                }
            }
            loopCount -= 1

            if (loopCount == 0) {
                stopSelf()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.d("service task complete onDestroy")
        serviceJob.cancel()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}
