package com.zoe.weshare.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.zoe.weshare.MainActivity
import com.zoe.weshare.data.OperationLog
import com.zoe.weshare.databinding.FragmentNotificationBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.util.UserManager.weShareUser

class NotificationFragment : Fragment() {

    lateinit var binding: FragmentNotificationBinding
    lateinit var notificationAdapter: NotificationAdapter
    lateinit var manager: LinearLayoutManager

    val viewModel by viewModels<NotificationViewModel> { getVmFactory(weShareUser) }

    var currentTabPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentNotificationBinding.inflate(inflater, container, false)

        //fetch notifications live result from MainViewModel
        val notificationLiveResult = (activity as MainActivity).viewModel.liveNotifications
        viewModel.onViewDisplay(notificationLiveResult)

        notificationLiveResult.observe(viewLifecycleOwner) {
            viewModel.filterList(currentTabPosition)
        }

        viewModel.notifications.observe(viewLifecycleOwner) {
            notificationAdapter.submitList(it)
            showNoNewsView(it)
        }

        setupView()
        return binding.root
    }

    private fun setupView() {
        notificationAdapter = NotificationAdapter(viewModel)

        binding.notificationRv.apply{
            adapter = notificationAdapter
            layoutManager = LinearLayoutManager(requireContext(),
                LinearLayoutManager.VERTICAL, false)
        }

        binding.notificationTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {

                currentTabPosition = tab.position
                viewModel.filterList(currentTabPosition)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun showNoNewsView(news: List<OperationLog>) {
        binding.hintNoNews.visibility =
            if (news.isEmpty()) { View.VISIBLE } else { View.INVISIBLE }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideTopBarNotificationIcon()
    }

    override fun onStop() {
        super.onStop()
        showTopBarNotificationIcon()
    }

    private fun hideTopBarNotificationIcon(){
        (activity as MainActivity).binding.apply {
            notification.visibility = View.INVISIBLE
            layoutNotificationBadge.visibility = View.INVISIBLE
        }
    }

    private fun showTopBarNotificationIcon(){
        (activity as MainActivity).binding.notification.visibility = View.VISIBLE
    }
}
