package com.zoe.weshare.profile.editmode

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.Result
import com.zoe.weshare.data.UserInfo
import com.zoe.weshare.data.UserProfile
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.network.LoadApiStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class EditInfoViewModel(val repository: WeShareRepository, val userInfo: UserInfo?) : ViewModel() {

    private var _profileUpdate = MutableLiveData<UserProfile>()
    val profileUpdate: LiveData<UserProfile>
        get() = _profileUpdate

    var newImage: Uri? = null

    lateinit var profile: UserProfile

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _updateComplete = MutableLiveData<LoadApiStatus>()
    val updateComplete: LiveData<LoadApiStatus>
        get() = _updateComplete

    // error: The internal MutableLiveData that stores the error of the most recent request
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    fun onProfileDisplay(userProfile: UserProfile) {
        profile = userProfile
    }

    fun checkIfImageChange(name: String, introMsg: String) {
        // user didn't choose new image
        if (newImage == null) {
            profile.apply {
                this.name = name
                this.introMsg = introMsg
            }
            _profileUpdate.value = profile
        } else {
            uploadImage(newImage!!, name, introMsg)
        }
    }

    fun updateProfile(newProfile: UserProfile) {
        coroutineScope.launch {

            _updateComplete.value = LoadApiStatus.LOADING

            when (val result = repository.updateUserProfile(newProfile)) {
                is Result.Success -> {
                    _error.value = null
                    _updateComplete.value = LoadApiStatus.DONE
                }
                is Result.Fail -> {
                    _error.value = result.error
                    _updateComplete.value = LoadApiStatus.ERROR
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _updateComplete.value = LoadApiStatus.ERROR
                }
                else -> {
                    _error.value = WeShareApplication.instance.getString(R.string.result_fail)
                    _updateComplete.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    fun uploadImage(image: Uri, name: String, introMsg: String) {
        coroutineScope.launch {

            when (val result = repository.uploadImage(image)) {
                is Result.Success -> {
                    _error.value = null

                    profile.apply {
                        this.name = name
                        this.introMsg = introMsg
                        this.image = result.data
                    }

                    _profileUpdate.value = profile
                }
                is Result.Fail -> {
                    _error.value = result.error
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                }
                else -> {
                    _error.value = WeShareApplication.instance.getString(R.string.result_fail)
                }
            }
        }
    }
}
