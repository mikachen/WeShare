package com.zoe.weshare.data.source

import androidx.lifecycle.MutableLiveData



/**
 * Created by Wayne Chen on 2020-01-15.
 *
 * Concrete implementation to load Publisher sources.
 */
class DefaultWeShareRepository(private val remoteDataSource: WeShareDataSource,
                                 private val localDataSource: WeShareDataSource
) : WeShareRepository {


}
