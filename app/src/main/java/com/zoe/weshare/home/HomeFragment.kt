package com.zoe.weshare.home

import android.os.Build
import android.os.Bundle
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
import com.github.amlcurran.showcaseview.ShowcaseView
import com.github.amlcurran.showcaseview.targets.Target.NONE
import com.github.amlcurran.showcaseview.targets.ViewTarget
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.databinding.FragmentHomeBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.ext.smoothSnapToPosition
import java.util.*


class HomeFragment : Fragment(),View.OnClickListener {

    private lateinit var headerRv: RecyclerView
    private lateinit var headerAdapter: HeaderAdapter

    private lateinit var hotGiftsRv: RecyclerView
    private lateinit var hotGiftAdapter: HotGiftsAdapter

    private lateinit var logTickerRv: RecyclerView
    private lateinit var tickerAdapter: TickerAdapter
    private lateinit var showcaseView: ShowcaseView

    val viewModel by viewModels<HomeViewModel> { getVmFactory() }
    private lateinit var binding: FragmentHomeBinding

    var counter = 0

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
            viewModel.onFilteringLog(it)
        }

        viewModel.filteredLogs.observe(viewLifecycleOwner) {
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
        setupButton()
        return binding.root
    }

    private fun setupButton() {
        binding.buttonNewbieHint.setOnClickListener {
            setShowCase()
        }

    }

    fun setShowCase(){
        showcaseView = ShowcaseView.Builder(requireActivity())
            .setTarget(ViewTarget(binding.buttonNewbieHint))
            .setOnClickListener(this)
            .setContentTitle("ShowcaseView")
            .setContentText("This is highlighting the Home button")
            .hideOnTouchOutside()
            .build()

        showcaseView.setButtonText("NEXT")
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

    override fun onClick(p0: View?) {
        when (counter) {
            0 -> showcaseView.setShowcase(ViewTarget(binding.buttonCheckEvents), true)
            1 -> showcaseView.setShowcase(ViewTarget(binding.buttonCheckGifts), true)
            2 -> {
                showcaseView.setTarget(NONE)
                showcaseView.setContentTitle("Check it out")
                showcaseView.setContentText("You don't always need a target to showcase")
                showcaseView.setButtonText("END")
                setAlpha(0.4f, binding.buttonNewbieHint, binding.buttonCheckEvents, binding.buttonCheckGifts)
            }
            3 -> {
                showcaseView.hide()
                setAlpha(1.0f, binding.buttonNewbieHint, binding.buttonCheckEvents, binding.buttonCheckGifts)
            }
        }
        counter++
    }

    private fun setAlpha(alpha: Float, vararg views: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            for (view in views) {
                view.alpha = alpha
            }
        }
    }

}