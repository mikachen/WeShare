package com.zoe.weshare.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zoe.getOrAwaitValue
import com.zoe.weshare.data.UserInfo
import com.zoe.weshare.detail.requestgift.RequestGiftViewModel
import com.zoe.weshare.util.ServiceLocator
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*


@RunWith(AndroidJUnit4::class)
class RequestGiftViewModelTest {


    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun clickedOnGift_navigateToDetail() {
        val repository =
            ServiceLocator.provideRepository(ApplicationProvider.getApplicationContext())
        val user = UserInfo()

        val requestGiftVm = RequestGiftViewModel(repository, user)


        // when requestGiftComplete
        requestGiftVm.requestGiftComplete()

        // then saveLogComplete live data is triggered with null

        val value = requestGiftVm.saveLogComplete.getOrAwaitValue()

        assertThat(value, nullValue())

    }
}
