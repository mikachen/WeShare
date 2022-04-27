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

            // 取得位置、移動量百分比後，就可以 render View 的變化量了
            val percent = offset - position
        }
    }
}
