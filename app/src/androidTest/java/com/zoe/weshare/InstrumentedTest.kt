package com.zoe.weshare

import android.app.Instrumentation
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class InstrumentedTest {

    @Test
    fun useAppContext(){
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    }

}