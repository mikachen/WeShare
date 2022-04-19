package com.zoe.weshare.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zoe.weshare.R
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.Cards
import com.zoe.weshare.data.EventPost
import com.zoe.weshare.data.GiftPost
import com.zoe.weshare.data.Result
import com.zoe.weshare.data.source.WeShareRepository
import com.zoe.weshare.network.LoadApiStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: WeShareRepository) : ViewModel() {
}
