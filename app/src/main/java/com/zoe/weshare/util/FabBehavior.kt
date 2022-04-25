package com.zoe.weshare.util

import com.google.android.material.bottomappbar.BottomAppBar


abstract class FabBehavior : BottomAppBar.Behavior() {

    private enum class State {
        SCROLLED_DOWN, SCROLLED_UP
    }

    private var currentState: State? = null

    abstract fun onSlideDown()
    abstract fun onSlideUp()

    override fun slideDown(child: BottomAppBar) {
        super.slideDown(child)
        if (currentState == State.SCROLLED_DOWN) return
        currentState = State.SCROLLED_DOWN
        onSlideDown()
    }

    override fun slideUp(child: BottomAppBar) {
        super.slideUp(child)
        if (currentState == State.SCROLLED_UP) return
        currentState = State.SCROLLED_UP
        onSlideUp()
    }

}
