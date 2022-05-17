package com.zoe.weshare.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.Result
import com.zoe.weshare.data.UserInfo
import com.zoe.weshare.data.ViolationReport
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.network.LoadApiStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ReportViewModel(
    private val repository: WeShareRepository,
    private val userInfo: UserInfo?,
) : ViewModel() {


    private val _reportSendComplete = MutableLiveData<String>()
    val reportSendComplete: LiveData<String>
        get() = _reportSendComplete

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _status = MutableLiveData<LoadApiStatus>()
    val status: LiveData<LoadApiStatus>
        get() = _status

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>?
        get() = _error


    fun onSendReport(target: String, reason: String) {
        val report = ViolationReport(
            targetUid = target,
            operatorUid = userInfo!!.uid,
            reason = reason
        )

        sendViolationReport(report)
    }

    private fun sendViolationReport(report: ViolationReport) {
        coroutineScope.launch {

            _status.value = LoadApiStatus.LOADING

            when (
                val result = repository.sendViolationReport(report)
            ) {

                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    _reportSendComplete.value = result.data

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
                    _error.value = WeShareApplication.instance.getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                }
            }
        }
    }

}