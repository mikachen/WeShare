package com.zoe.weshare.manage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.zoe.weshare.R
import com.zoe.weshare.databinding.FragmentPagerFilterBinding

class PagerFilterFragment : Fragment() {
    lateinit var binding: FragmentPagerFilterBinding

    private lateinit var pagerAdapter: PagerAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var tabs: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentPagerFilterBinding.inflate(inflater, container, false)

        viewPager = binding.viewpager
        tabs = binding.tabLayout

        pagerAdapter = PagerAdapter(this)
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabs, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.filter_all)
                1 -> tab.text = getString(R.string.filter_ongoing)
                2 -> tab.text = getString(R.string.filter_ended)
                3 -> tab.text = getString(R.string.filter_abandoned)
            }
        }.attach()

        return binding.root
    }
}
