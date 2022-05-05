package com.zoe.weshare.notification

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.zoe.weshare.MainActivity
import com.zoe.weshare.databinding.FragmentNotificationBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.util.UserManager.weShareUser

class NotificationFragment : Fragment() {

    lateinit var binding: FragmentNotificationBinding
    lateinit var adapter: NotificationAdapter
    lateinit var manager: LinearLayoutManager


    val viewModel by viewModels<NotificationViewModel> { getVmFactory(weShareUser) }

    var currentTabPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentNotificationBinding.inflate(inflater, container, false)

        val liveData = (activity as MainActivity).viewModel.liveNotifications
        viewModel.onViewDisplay(liveData)

        liveData.observe(viewLifecycleOwner) {
            viewModel.filterList(currentTabPosition)
        }

        viewModel.notifications.observe(viewLifecycleOwner) {
            adapter.submitList(it)

            if(it.isEmpty()){
                binding.hintNoNews.visibility = View.VISIBLE
            }else{
                binding.hintNoNews.visibility = View.INVISIBLE
            }
        }

        binding.notificationTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentTabPosition = tab.position

                viewModel.filterList(currentTabPosition)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })


        setupView()
        return binding.root
    }

    private fun setupView() {
        adapter = NotificationAdapter(viewModel)

        manager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL, false
        )

        binding.notificationRv.adapter = adapter
        binding.notificationRv.layoutManager = manager

    }
}