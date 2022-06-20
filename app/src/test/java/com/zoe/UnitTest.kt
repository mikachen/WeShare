package com.zoe

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.zoe.weshare.WeShareApplication
import com.zoe.weshare.data.UserProfile
import com.zoe.weshare.data.source.DefaultWeShareRepository
import com.zoe.weshare.herorank.HeroRankViewModel
import kotlinx.coroutines.Dispatchers
import com.zoe.weshare.data.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class UnitTest {

    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @Mock
    lateinit var mockRepository: DefaultWeShareRepository
    lateinit var viewModel: HeroRankViewModel

    @Mock
    lateinit var mockApplication: WeShareApplication

    @Mock
    lateinit var mockRankObserver: Observer<List<UserProfile>>

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)


    @Before
    fun setUp(){
        MockitoAnnotations.initMocks(this)

        Dispatchers.setMain(testDispatcher)

        WeShareApplication.instance = mockApplication
        Mockito.`when`(WeShareApplication.instance.isLiveDataDesign()).thenReturn(true)


        viewModel = HeroRankViewModel(mockRepository)
        viewModel.ranking.observeForever(mockRankObserver)
    }

    @After
    fun tearDown(){
        testDispatcher.cleanupTestCoroutines()
        testScope.cleanupTestCoroutines()
    }

    @Test
    fun getRankList_isNotNull() = testDispatcher.runBlockingTest {

        Mockito.`when`(mockRepository.getHeroRanking()).thenReturn(Result.Success(listOf()))

        viewModel.getRankResult()

        Mockito.verify(mockRankObserver,times(1)).onChanged(ArgumentMatchers.isNotNull())

    }


    @Test
    fun getRankList_isNull() = testDispatcher.runBlockingTest {

        Mockito.`when`(mockRepository.getHeroRanking()).thenReturn(Result.Fail(""))

        viewModel.getRankResult()

        Mockito.verify(mockRankObserver,times(1)).onChanged(ArgumentMatchers.isNull())

    }

}
