package com.zoe.weshare.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
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
import com.zoe.weshare.MainActivity
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.R
import com.zoe.weshare.databinding.FragmentHomeBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.ext.smoothSnapToPosition
import java.util.*

class HomeFragment : Fragment(), View.OnClickListener {

    private lateinit var headerRv: RecyclerView
    private lateinit var headerAdapter: HeaderAdapter

    private lateinit var hotGiftsRv: RecyclerView
    private lateinit var hotGiftAdapter: HotGiftsAdapter

    private lateinit var logTickerRv: RecyclerView
    private lateinit var tickerAdapter: TickerAdapter
    private lateinit var showcaseView: ShowcaseView

    private var refreshHomePage: Boolean = false

    val viewModel by viewModels<HomeViewModel> { getVmFactory() }
    private lateinit var binding: FragmentHomeBinding

    private var hintStepCounts = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentHomeBinding.inflate(inflater, container, false)

        if (refreshHomePage) {
            refreshHomePage = false
            viewModel.refreshHomeDataResult()
        }

        viewModel.gifts.observe(viewLifecycleOwner) {
            hotGiftAdapter.submitList(it)
        }

        viewModel.events.observe(viewLifecycleOwner) {
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
            displayShowCase()
        }
        binding.buttonCheckEvents.setOnClickListener {
            findNavController().navigate(NavGraphDirections.actionGlobalEventsBrowseFragment())
        }
        binding.buttonCheckGifts.setOnClickListener {
            findNavController().navigate(NavGraphDirections.actionGlobalGiftsBrowseFragment())
        }
        binding.buttonCurrentHeros.setOnClickListener {
            findNavController().navigate(NavGraphDirections.actionGlobalHeroRankFragment())
        }
    }

    fun displayShowCase() {

        val lps = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)

        val margin = ((resources.displayMetrics.density * 24) as Number).toInt()
        lps.setMargins(margin, margin, margin * 10, margin * 2)

        showcaseView = ShowcaseView.Builder(requireActivity())
            .setTarget(NONE)
            .withNewStyleShowcase()
            .setStyle(R.style.CustomShowcaseTheme)
            .setContentTitle(getString(R.string.hint_welcome_msg))
            .setContentText(getString(R.string.hint_welcome_msg_sub))
            .setOnClickListener(this)
            .build()

        showcaseView.setButtonPosition(lps)
        showcaseView.setButtonText(getString(R.string.hint_next_btn_text))

        showHintCoverView()
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
                    } else if (manager.findLastVisibleItemPosition() < (headerAdapter.itemCount - 1))
                    {
                        manager.smoothScrollToPosition(
                            headerRv,
                            RecyclerView.State(), 0
                        )
                    }
                }
            },
            0, 5000
        )
    }

    private fun setupHotGiftsGallery() {

        hotGiftsRv = binding.hotGiftRv

        hotGiftAdapter = HotGiftsAdapter(
            HotGiftsAdapter.OnClickListener { selectedGift ->
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

        // disable touch event
        logTickerRv.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                return true
            }
        })

        val linearSnapHelper = LinearSnapHelper()
        linearSnapHelper.attachToRecyclerView(logTickerRv)

        val timer = Timer()
        timer.schedule( object : TimerTask() {
                override fun run() {
                    if (manager.findLastVisibleItemPosition() < (tickerAdapter.itemCount - 1)) {

                        logTickerRv.smoothSnapToPosition(
                            manager.findLastVisibleItemPosition() + 1)

                    } else {
                        requireActivity().runOnUiThread {
                            logTickerRv.smoothScrollToPosition(0)
                        }
                    }
                }
            }, 0, 5000
        )
    }

    override fun onClick(p0: View?) {
        when (hintStepCounts) {

            0 -> showcaseView.apply {
                    setShowcase(ViewTarget((activity as MainActivity).binding.fabsLayoutView),
                        false)
                    setContentTitle("")
                    setContentText(getString(R.string.hint_post_button))
                    (activity as MainActivity).onMainFabClick()
                }

            1 -> showcaseView.apply {
                (activity as MainActivity).onMainFabClick()

                setShowcase(ViewTarget(binding.showcaseCenterLine), false)
                setContentText(getString(R.string.hint_browse_button))
            }

            2 -> showcaseView.apply {
                setShowcase(ViewTarget(binding.buttonCurrentHeros), false)
                setContentText(getString(R.string.hint_hero_chart))
            }

            3 -> showcaseView.apply {
                setShowcase(ViewTarget((activity as MainActivity).binding.notification), false)
                setContentText(getString(R.string.hint_notification_button))
            }

            4 -> showcaseView.apply {
                setTarget(NONE)
                setContentText(getString(R.string.hint_profile))
            }

            5 -> showcaseView.apply {
                setTarget(NONE)
                setContentTitle(getString(R.string.hint_goodbye_title))
                setContentText("")
                setButtonText(getString(R.string.hint_close_btn_text))
            }

            6 -> {
                showcaseView.hide()
                hideCoverView()
            }
        }
        hintStepCounts++

        if (hintStepCounts == 7) {
            hintStepCounts = 0
        }
    }

    private fun showHintCoverView() {
        (activity as MainActivity).binding.preventTouchCoverView.visibility = View.VISIBLE
    }

    private fun hideCoverView() {
        (activity as MainActivity).binding.preventTouchCoverView.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        refreshHomePage = true
    }
}
