package com.zoe.weshare.util

import android.content.Context
import android.content.SharedPreferences
import com.zoe.weshare.data.UserInfo

object UserManager {

    private const val APP_NAME = "WeShare"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences

    fun init(context: Context) {
        preferences = context.getSharedPreferences(APP_NAME, MODE)
    }

    /**
     * SharedPreferences extension function, so we won't need to call edit() and apply()
     * ourselves on every SharedPreferences operation.
     */
    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    private const val USER_UID = "user_uid"
    private const val USER_INFO = "user_info"

    var userToken: String?
        get() = preferences.getString(USER_UID, null)
        set(token) = preferences.edit {
            it.putString(USER_UID, token)
        }

//    var currentUser: UserInfo?
//        get() = preferences.getString(USER_INFO, UserInfo())
//        set(UserInfo) = preferences.edit {
//            it.putString(USER_INFO, com.zoe.weshare.data.UserInfo)
//        }

    var weShareUser: UserInfo? = null

    var userBlackList = mutableListOf<String>()

    val isLoggedIn: Boolean
        get() = weShareUser != null
}
