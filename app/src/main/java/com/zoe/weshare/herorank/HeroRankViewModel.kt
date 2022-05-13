package com.zoe.weshare.herorank

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.Result
import com.zoe.weshare.data.UserProfile
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.network.LoadApiStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HeroRankViewModel(private val repository: WeShareRepository) : ViewModel() {

    private val _ranking = MutableLiveData<List<UserProfile>>()
    val ranking: LiveData<List<UserProfile>>
        get() = _ranking


    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _status = MutableLiveData<LoadApiStatus>()
    val status: LiveData<LoadApiStatus>
        get() = _status

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    private val _selectedUser = MutableLiveData<UserProfile>()
    val selectedUser: LiveData<UserProfile>
        get() = _selectedUser


    init {
        getRankResult()
    }

    private fun getRankResult() {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (val result = repository.getHeroRanking()) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    _ranking.value = result.data ?: emptyList()
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

    fun onNavigateToUserProfile(selectedUser: UserProfile) {
        _selectedUser.value = selectedUser
    }

    fun onNavigateComplete() {
        _selectedUser.value = null
    }
}