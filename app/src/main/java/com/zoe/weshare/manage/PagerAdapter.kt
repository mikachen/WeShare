package com.zoe.weshare.manage

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.zoe.weshare.manage.giftsItem.GiftManageFragment

class PagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {

        return GiftManageFragment.getInstance(position)
    }
}
