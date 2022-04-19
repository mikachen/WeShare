package com.zoe.weshare.map

import androidx.recyclerview.widget.RecyclerView

class CardScrollerListener(private val itemWith: Int) : RecyclerView.OnScrollListener() {

        private var scrolledWidth = 0

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            scrolledWidth += dx
            recyclerView.post {
                val offset = scrolledWidth.toFloat() / itemWith.toFloat()

                val position = offset.toInt()

                val percent = offset - position
            }

        }
    }