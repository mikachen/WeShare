package com.zoe.weshare.util

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.zoe.weshare.data.source.DefaultWeShareRepository
import com.zoe.weshare.data.source.WeShareDataSource
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.data.source.local.WeShareLocalDataSource
import com.zoe.weshare.data.source.remote.WeShareRemoteDataSource

/**
 * A Service Locator for the [WeShareRepository].
 */
object ServiceLocator {

    @Volatile
    var repository: WeShareRepository? = null
        @VisibleForTesting set

    fun provideRepository(context: Context): WeShareRepository {
        synchronized(this) {
            return repository
                ?: repository
                ?: createPublisherRepository(context)
        }
    }

    private fun createPublisherRepository(context: Context): WeShareRepository {
        return DefaultWeShareRepository(
            WeShareRemoteDataSource,
            createLocalDataSource(context)
        )
    }

    private fun createLocalDataSource(context: Context): WeShareDataSource {
        return WeShareLocalDataSource(context)
    }
}
