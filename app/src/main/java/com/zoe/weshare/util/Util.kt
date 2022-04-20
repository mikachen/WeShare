package com.zoe.weshare.util

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.zoe.weshare.WeShareApplication
import kotlin.reflect.KProperty1

object Util {


    /**
     * Determine and monitor the connectivity status
     *
     * https://developer.android.com/training/monitoring-device-state/connectivity-monitoring
     */
    fun isInternetConnected(): Boolean {

        // TODO
        return true
    }

    fun getString(resourceId: Int): String {
        return WeShareApplication.instance.getString(resourceId)
    }

    fun getStringWithIntParm(resourceId: Int, parms: Int): String {
        return WeShareApplication.instance.getString(resourceId, parms)
    }

    fun getStringWithStrParm(resourceId: Int, parms: String): String {
        return WeShareApplication.instance.getString(resourceId, parms)
    }

    fun getColor(resourceId: Int): Int {
        return WeShareApplication.instance.getColor(resourceId)
    }

    fun showSnackBar(message: String, view: View) {
        Snackbar.make(
            view,
            message,
            Snackbar.LENGTH_LONG
        ).also { snackbar ->
            snackbar.setAction("Ok") {
                snackbar.dismiss()
            }
        }.show()
    }

    fun ContentResolver.getFileName(fileUri: Uri): String {
        var name = ""
        val returnCursor = this.query(fileUri, null, null, null, null)
        if (returnCursor != null) {
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            name = returnCursor.getString(nameIndex)
            returnCursor.close()
        }
        return name
    }

    @Suppress("UNCHECKED_CAST")
    fun <R> readInstanceProperty(instance: Any, propertyName: String): R {
        val property = instance::class.members
            // don't cast here to <Any, R>, it would succeed silently
            .first { it.name == propertyName } as KProperty1<Any, *>
        // force a invalid cast exception if incorrect type here
        return property.get(instance) as R
    }
}
