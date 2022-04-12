package com.zoe.weshare

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.util.CurrentFragmentType

class MainViewModel(private val repository: WeShareRepository) : ViewModel() {

    val currentFragmentType = MutableLiveData<CurrentFragmentType>()
}
