package com.zoe.weshare

import android.app.Application
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.util.ServiceLocator
import kotlin.properties.Delegates

/**
 * An application that lazily provides a repository. Note that this Service Locator pattern is
 * used to simplify the sample. Consider a Dependency Injection framework.
 */

class WeShareApplication : Application() {

    // Depends on the flavor,
    val repository: WeShareRepository
        get() = ServiceLocator.provideRepository(this)

    companion object {
        var instance: WeShareApplication by Delegates.notNull()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    fun isLiveDataDesign() = true

}
