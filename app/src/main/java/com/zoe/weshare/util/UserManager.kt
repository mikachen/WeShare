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

    private const val CHECKED_NOTIFICATION = "checked_notification"

    var hasCheckNotification: Boolean
        get() = preferences.getBoolean(CHECKED_NOTIFICATION, false)
        set(hasCheckNotification) = preferences.edit {
            it.putBoolean(CHECKED_NOTIFICATION, hasCheckNotification)
        }

    var userInfo: UserInfo? = null

    val weShareUser: UserInfo
        get() = userInfo!!

    // checked logged success before user enter this app
    val hasUserLoggedIn: Boolean
        get() = userInfo != null

    var userBlackList = mutableListOf<String>()
    var userConsentPolicy: Boolean = false
}
