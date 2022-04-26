package com.zoe.weshare.detail.event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.*
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.network.LoadApiStatus
import com.zoe.weshare.util.Const.PATH_EVENT_POST
import com.zoe.weshare.util.Const.SUB_PATH_EVENT_USER_WHO_COMMENT
import com.zoe.weshare.util.LogType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class EventDetailViewModel(private val repository: WeShareRepository, val userInfo: UserInfo?) :
    ViewModel() {

    private var _comments = MutableLiveData<List<Comment>>()
    val comments: LiveData<List<Comment>>
        get() = _comments

    private var _onEventDisplaying = MutableLiveData<EventPost>()
    val onEventDisplaying: LiveData<EventPost>
        get() = _onEventDisplaying

    var liveComments = MutableLiveData<List<Comment>>()

    //TODO
    private val _refreshStatus = MutableLiveData<Boolean>()
    val refreshStatus: LiveData<Boolean>
        get() = _refreshStatus

    private var _currentLikedNumber = MutableLiveData<Int>()
    val currentLikedNumber: LiveData<Int>
        get() = _currentLikedNumber

    private var _isUserPressedLike = MutableLiveData<Boolean>()
    val isUserPressedLike: LiveData<Boolean>
        get() = _isUserPressedLike

    private var _onProfileSearch = MutableLiveData<Int>()
    val onProfileSearch: LiveData<Int>
        get() = _onProfileSearch

    private var _newComment = MutableLiveData<Comment>()
    val newComment: LiveData<Comment>
        get() = _newComment


    val profileList = mutableListOf<UserProfile>()

    var updateCommentLike = mutableListOf<Comment>()

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)


    private val _sendCommentStatus = MutableLiveData<LoadApiStatus>()
    val sendCommentStatus: LiveData<LoadApiStatus>
        get() = _sendCommentStatus

    private val _saveLogComplete = MutableLiveData<LoadApiStatus>()
    val saveLogComplete: LiveData<LoadApiStatus>
        get() = _saveLogComplete

    private val _status = MutableLiveData<LoadApiStatus>()
    val status: LiveData<LoadApiStatus>
        get() = _status

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error


    fun getHistoryComments(docId: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (val result = repository.getAllComments(
                collection = PATH_EVENT_POST,
                docId = docId,
                subCollection = SUB_PATH_EVENT_USER_WHO_COMMENT)) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE
                    _comments.value = result.data ?: emptyList()
                    updateCommentLike = result.data as MutableList<Comment>
                }
                is Result.Fail -> {
                    _error.value = result.error
                    _status.value = LoadApiStatus.ERROR
                    _comments.value = emptyList()
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _status.value = LoadApiStatus.ERROR
                    _comments.value = emptyList()
                }
                else -> {
                    _error.value =
                        WeShareApplication.instance.getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                    _comments.value = emptyList()
                }
            }
        }
    }

    fun searchUsersProfile(comments: List<Comment>) {
        if (comments.isNotEmpty()) {

            val newUserUid = mutableListOf<String>()
            val filteredUser = comments.distinctBy { it.uid }


            if (profileList.isNotEmpty()) {
                for (i in filteredUser) {
                    if (!profileList.any { it.uid == i.uid }) {
                        newUserUid.add(i.uid)
                    }
                }

            }
            //first time is empty
            else {
                for (i in filteredUser) {
                    newUserUid.add(i.uid)
                }
            }

            _onProfileSearch.value = newUserUid.size

            for (uid in newUserUid) {
                getUserInfo(uid)
            }
        }
    }

    private fun getUserInfo(uid: String) {
        coroutineScope.launch {

            _status.value = LoadApiStatus.LOADING

            when (val result = repository.getUserInfo(uid)) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    profileList.add(result.data)
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
            _onProfileSearch.value = _onProfileSearch.value?.minus(1)
        }
    }

    private fun sendLike(doc: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (val result = repository.likeOnPost(collection = PATH_EVENT_POST,
                docId = doc,
                uid = userInfo!!.uid)) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    _currentLikedNumber.value = _currentLikedNumber.value?.plus(1)
                    _isUserPressedLike.value = true
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

    private fun cancelLike(doc: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (val result = repository.cancelLikeOnPost(collection = PATH_EVENT_POST,
                docId = doc,
                uid = userInfo!!.uid)) {
                is Result.Success -> {
                    _error.value = null
                    _status.value = LoadApiStatus.DONE

                    _currentLikedNumber.value = _currentLikedNumber.value?.minus(1)
                    _isUserPressedLike.value = false
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

    fun onViewPrepare(event: EventPost) {
        _onEventDisplaying.value = event
        _currentLikedNumber.value = event.whoLiked.size
        _isUserPressedLike.value = event.whoLiked.contains(userInfo!!.uid) == true

        getLiveCommentResult()
    }

    fun onPostLikePressed(doc: String) {
        if (_isUserPressedLike.value == false) {
            sendLike(doc)
        } else {
            cancelLike(doc)
        }
    }

    fun onSendNewComment(message: String) {
        _newComment.value = Comment(
            uid = userInfo!!.uid,
            content = message
        )
    }

    fun sendComment(docId: String, comment: Comment) {
        coroutineScope.launch {

            _sendCommentStatus.value = LoadApiStatus.LOADING

            when (val result = repository.sendComment(
                collection = PATH_EVENT_POST,
                docId = docId,
                comment = comment,
                subCollection = SUB_PATH_EVENT_USER_WHO_COMMENT)) {
                is Result.Success -> {
                    _error.value = null
                    _sendCommentStatus.value = LoadApiStatus.DONE
                }
                is Result.Fail -> {
                    _error.value = result.error
                    _sendCommentStatus.value = LoadApiStatus.ERROR
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _sendCommentStatus.value = LoadApiStatus.ERROR
                }
                else -> {
                    _error.value = WeShareApplication.instance.getString(R.string.result_fail)
                    _sendCommentStatus.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    fun onSaveCommentLog(event: EventPost) {
        val log = PostLog(
            postDocId = event.id,
            logType = LogType.COMMENTEVENT.value,
            operatorUid = userInfo!!.uid,
            logMsg = WeShareApplication.instance.getString(R.string.log_msg_send_event_comment,
                userInfo.name,
                event.title)
        )
        saveCommentLog(log)
    }

    private fun saveCommentLog(log: PostLog) {
        coroutineScope.launch {

            _saveLogComplete.value = LoadApiStatus.LOADING

            when (val result = repository.saveLog(log)) {
                is Result.Success -> {
                    _error.value = null
                    _saveLogComplete.value = LoadApiStatus.DONE

                }
                is Result.Fail -> {
                    _error.value = result.error
                    _saveLogComplete.value = LoadApiStatus.ERROR
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                    _saveLogComplete.value = LoadApiStatus.ERROR
                }
                else -> {
                    _error.value = WeShareApplication.instance.getString(R.string.result_fail)
                    _saveLogComplete.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    fun onCommentsLikePressed(comment: Comment, isUserLiked: Boolean) {
        if (!isUserLiked) {
            sendLikeOnComment(comment.id)
        } else {
            cancelLikeOnComment(comment.id)
        }
    }

    private fun sendLikeOnComment(subDoc: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (
                val result = repository.likeOnPostComment(
                    collection = PATH_EVENT_POST,
                    docId = onEventDisplaying.value!!.id,
                    subCollection = SUB_PATH_EVENT_USER_WHO_COMMENT,
                    subDocId = subDoc,
                    uid = userInfo!!.uid
                )
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
                    _error.value = WeShareApplication.instance.getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    private fun cancelLikeOnComment(subDoc: String) {
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (
                val result = repository.cancelLikeOnPostComment(
                    collection = PATH_EVENT_POST,
                    docId = onEventDisplaying.value!!.id,
                    subCollection = SUB_PATH_EVENT_USER_WHO_COMMENT,
                    subDocId = subDoc,
                    uid = userInfo!!.uid
                )
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
                    _error.value = WeShareApplication.instance.getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    fun onAttendEvent(fieldType: String){
        coroutineScope.launch {
            _status.value = LoadApiStatus.LOADING

            when (val result = repository.updateEventAttendee(
                    docId = onEventDisplaying.value!!.id,
                    field = fieldType,
                    uid = userInfo!!.uid
                )) {
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
                    _error.value = WeShareApplication.instance.getString(R.string.result_fail)
                    _status.value = LoadApiStatus.ERROR
                }
            }
        }
    }

    fun getLiveCommentResult() {
        liveComments = repository.getLiveMessages(
            collection = PATH_EVENT_POST,
            docId = onEventDisplaying.value!!.id,
            subCollection = SUB_PATH_EVENT_USER_WHO_COMMENT)

        _status.value = LoadApiStatus.DONE
        _refreshStatus.value = false
    }
}
