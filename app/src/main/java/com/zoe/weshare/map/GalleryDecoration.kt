package com.zoe.weshare.map

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GalleryDecoration : RecyclerView.ItemDecoration() {

    var mItemConsumeX = 0

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildAdapterPosition(view)
        val itemCount = parent.adapter?.itemCount ?: 0

        val lp = view.layoutParams as RecyclerView.LayoutParams

        val oneSideVisibleWith = 70
        val itemWidth = parent.width - 2 * oneSideVisibleWith

        if (lp.width != itemWidth) {
            lp.width = itemWidth
        }
    }
}
