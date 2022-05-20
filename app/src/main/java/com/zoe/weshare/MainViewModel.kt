package com.zoe.weshare

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zoe.weshare.data.*
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.network.LoadApiStatus
import com.zoe.weshare.util.CurrentFragmentType
import com.zoe.weshare.util.UserManager.userInfo
import com.zoe.weshare.util.UserManager.userBlackList
import com.zoe.weshare.util.UserManager.weShareUser
import com.zoe.weshare.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainViewModel(private val repository: WeShareRepository) : ViewModel() {

    val currentFragmentType = MutableLiveData<CurrentFragmentType>()

    var liveNotifications = MutableLiveData<List<OperationLog>>()
    var liveChatRooms = MutableLiveData<List<ChatRoom>>()

    var reObserveNotification = MutableLiveData<Boolean>()
    var reObserveChatRoom = MutableLiveData<Boolean>()

    var roomBadgeCount = MutableLiveData<Int>()

    private val _loginStatus = MutableLiveData<LoadApiStatus?>()
    val loginStatus: LiveData<LoadApiStatus?>
        get() = _loginStatus

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _status = MutableLiveData<LoadApiStatus>()
    val status: LiveData<LoadApiStatus>
        get() = _status

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error


    fun getLiveNotificationResult() {
        liveNotifications = repository.getLiveNotifications(weShareUser.uid)
        reObserveNotification.value = true
    }

    fun getLiveRoomResult() {
        liveChatRooms = repository.getLiveRoomLists(weShareUser.uid)
        reObserveChatRoom.value = true
    }

    fun getUnreadRoom(rooms: List<ChatRoom>) {

        var unReadRoom = 0

        for (room in rooms) {
            if (hasUnreadMsg(room)) {
                unReadRoom += 1
            }
        }
        roomBadgeCount.value = unReadRoom
    }

    fun hasUnreadMsg(room: ChatRoom):Boolean{
        //exclude the room which has been created but no one speak yet.
        return !room.lastMsgRead.contains(weShareUser.uid) && room.lastMsgSentTime != -1L
    }

    fun getUserProfile(uid: String) {
        coroutineScope.launch {
            _loginStatus.value = LoadApiStatus.LOADING

            when (val result = repository.getUserInfo(uid)) {
                is Result.Success -> {
                    _error.value = null

                    val userProfile = result.data

                    if(userProfile != null){

                        getUserInfo(userProfile)

                        getLiveNotificationResult()
                        getLiveRoomResult()

                    }else{
                        _loginStatus.value = LoadApiStatus.ERROR
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
                        Util.getString(R.string.result_fail)
                    _loginStatus.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    fun getUserInfo(user: UserProfile){
        userInfo = UserInfo(
            name = user.name,
            image = user.image,
            uid = user.uid
        )

        userBlackList = user.blackList

        _loginStatus.value = LoadApiStatus.DONE
    }

    fun userLogout(){
        liveNotifications.value = listOf()
        liveChatRooms.value = listOf()
        _loginStatus.value = null

        userInfo = null
        userBlackList.clear()
    }
}
