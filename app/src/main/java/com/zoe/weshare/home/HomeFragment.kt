package com.zoe.weshare.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.MainActivity
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.databinding.FragmentHomeBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.ext.smoothSnapToPosition
import com.zoe.weshare.util.UserManager
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var headerRv: RecyclerView
    private lateinit var headerAdapter: HeaderAdapter

    private lateinit var hotGiftsRv: RecyclerView
    private lateinit var hotGiftAdapter: HotGiftsAdapter

    private lateinit var logTickerRv: RecyclerView
    private lateinit var tickerAdapter: TickerAdapter

    val viewModel by viewModels<HomeViewModel> { getVmFactory() }
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentHomeBinding.inflate(inflater, container, false)


        viewModel.gifts.observe(viewLifecycleOwner) {
            hotGiftAdapter.submitList(it)
        }

        viewModel.events.observe(viewLifecycleOwner) {
            // zero will cause headerAdapter crash cause i set infinity items adapter
            if (it.isNotEmpty()) {
                headerAdapter.submitEvents(it)
            }
        }

        viewModel.allLogs.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                tickerAdapter.submitList(it)
            }
        }

        viewModel.navigateToSelectedGift.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(
                    NavGraphDirections.actionGlobalGiftDetailFragment(it)
                )

                viewModel.displayGiftDetailsComplete()
            }
        }

        viewModel.navigateToSelectedEvent.observe(viewLifecycleOwner) {
            it?.let {
                findNavController()
                    .navigate(NavGraphDirections.actionGlobalEventDetailFragment(it))

                viewModel.displayEventDetailsComplete()
            }
        }


        setupHeaderGallery()
        setupHotGiftsGallery()
        setupLogTicker()

        return binding.root
    }

    private fun setupHeaderGallery() {

        headerRv = binding.headerEventRv

        headerAdapter = HeaderAdapter(
            HeaderAdapter.HeaderOnClickListener { selectedEvent ->
                viewModel.displayEventDetails(selectedEvent)
            }
        )

        val manager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL, false
        )

        headerRv.adapter = headerAdapter
        headerRv.layoutManager = manager

        val linearSnapHelper = LinearSnapHelper()
        linearSnapHelper.attachToRecyclerView(headerRv)

        val timer = Timer()
        timer.schedule(
            object : TimerTask() {
                override fun run() {
                    if (manager.findLastVisibleItemPosition() < (headerAdapter.itemCount - 1)) {
                        manager.smoothScrollToPosition(
                            headerRv,
                            RecyclerView.State(),
                            manager.findLastVisibleItemPosition() + 1
                        )
                    } else if (manager.findLastVisibleItemPosition() < (headerAdapter.itemCount - 1)) {
                        manager.smoothScrollToPosition(
                            headerRv,
                            RecyclerView.State(), 0
                        )
                    }
                }
            },
            0, 3000
        )
    }

    private fun setupHotGiftsGallery() {

        hotGiftsRv = binding.hotGiftRv

        hotGiftAdapter = HotGiftsAdapter(
            HotGiftsAdapter.HotGiftsOnClickListener { selectedGift ->
                viewModel.displayGiftDetails(selectedGift)
            }
        )

        val manager = GridLayoutManager(requireContext(), 2)

        hotGiftsRv.adapter = hotGiftAdapter
        hotGiftsRv.layoutManager = manager
    }

    private fun setupLogTicker() {

        logTickerRv = binding.tickerRv
        tickerAdapter = TickerAdapter()

        val manager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL, false
        )

        logTickerRv.adapter = tickerAdapter
        logTickerRv.layoutManager = manager

        val linearSnapHelper = LinearSnapHelper()
        linearSnapHelper.attachToRecyclerView(logTickerRv)

        val timer = Timer()
        timer.schedule(
            object : TimerTask() {
                override fun run() {
                    if (manager.findLastVisibleItemPosition() < (tickerAdapter.itemCount - 1)) {

                        logTickerRv.smoothSnapToPosition(manager.findLastVisibleItemPosition() + 1)

                    } else if (manager.findLastVisibleItemPosition() < (tickerAdapter.itemCount - 1)) {

                        logTickerRv.smoothSnapToPosition(0)

                    }
                }
            }, 0, 5000
        )
    }

    private fun requireLogIn() {
        if (!UserManager.isLoggedIn) {
            findNavController().navigate(NavGraphDirections.actionGlobalLoginFragment())
        }
    }
}