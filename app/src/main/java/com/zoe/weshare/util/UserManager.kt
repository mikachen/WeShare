package com.zoe.weshare.util

import android.content.Context
import android.content.SharedPreferences
import com.zoe.weshare.data.UserInfo

object UserManager {

    val userLora = UserInfo(
        name = "蘿拉卡芙特",
        uid = "lora0987",
        image = "https://images2.gamme.com.tw/news2/2016/26/12/q52SpaablqCbqA.jpeg")


    val userZoe = UserInfo(
        name = "Zoe Lo",
        uid = "zoe1018",
        image = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1686&q=80"
    )

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

    var userToken: String?
        get() = preferences.getString(USER_UID, null)
        set(token) = preferences.edit {
            it.putString(USER_UID, token)
        }


}