package com.zoe.weshare.login

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
import com.zoe.weshare.util.UserManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: WeShareRepository) : ViewModel() {

    private var _loginSuccess = MutableLiveData<UserInfo>()
    val loginSuccess: LiveData<UserInfo>
        get() = _loginSuccess

    private val _loginStatus = MutableLiveData<LoadApiStatus>()
    val loginStatus: LiveData<LoadApiStatus>
        get() = _loginStatus

    private val _status = MutableLiveData<LoadApiStatus>()
    val status: LiveData<LoadApiStatus>
        get() = _status

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    /**
     * 1) search User collection with uid document
     * 2) if doc doesn't exist => create new User profile ; if exist => get user profile
     * */
    fun checkIfMemberExist(user: UserInfo) {
        coroutineScope.launch {

            _loginStatus.value = LoadApiStatus.LOADING

            when (val result = repository.getUserInfo(user.uid)) {
                is Result.Success -> {
                    _error.value = null
                    _loginStatus.value = LoadApiStatus.DONE

                    if (result.data == null) {
                        onCreateNewUser(user)
                    } else {
                        getMemberUserInfo(result.data)
                    }
                }
                is Result.Fail -> {
                    _error.value = result.error
                    _loginStatus.value = LoadApiStatus.ERROR
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _loginStatus.value = LoadApiStatus.ERROR
                }
                else -> {
                    _error.value =
                        WeShareApplication.instance.getString(R.string.result_fail)
                    _loginStatus.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    fun onCreateNewUser(user: UserInfo) {
        val profile = UserProfile()

        profile.name = user.name
        profile.image = user.image
        profile.uid = user.uid

        createNewUserProfile(profile)
    }

    fun createNewUserProfile(profile: UserProfile) {
        coroutineScope.launch {

            _status.value = LoadApiStatus.LOADING

            when (val result = repository.newUserRegister(profile)) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    getMemberUserInfo(profile)
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

    private fun getMemberUserInfo(profile: UserProfile) {

        val user = UserInfo(
            name = profile.name,
            image = profile.image,
            uid = profile.uid
        )

        UserManager.weShareUser = user
        UserManager.userBlackList = profile.blackList

        _loginSuccess.value = user
    }

    fun loginComplete() {
        _loginSuccess.value = null
    }
}
