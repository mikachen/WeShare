package com.zoe.weshare.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zoe.weshare.MainViewModel
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.home.HomeViewModel
import com.zoe.weshare.login.LoginViewModel
import com.zoe.weshare.map.MapViewModel
import com.zoe.weshare.search.gifts.GiftsAllViewModel

/**
 * Factory for all ViewModels.
 */
@Suppress("UNCHECKED_CAST")
class ViewModelFactory constructor(
    private val repository: WeShareRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(MainViewModel::class.java) ->
                    MainViewModel(repository)

                isAssignableFrom(HomeViewModel::class.java) ->
                    HomeViewModel(repository)

                isAssignableFrom(MapViewModel::class.java) ->
                    MapViewModel(repository)

                isAssignableFrom(LoginViewModel::class.java) ->
                    LoginViewModel(repository)

                isAssignableFrom(GiftsAllViewModel::class.java) ->
                    GiftsAllViewModel(repository)

                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}
