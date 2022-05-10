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
        binding.buttonCheckEvents.setOnClickListener {
            findNavController().navigate(NavGraphDirections.actionGlobalEventsBrowseFragment())
        }
        binding.buttonCheckGifts.setOnClickListener {
            findNavController().navigate(NavGraphDirections.actionGlobalGiftsBrowseFragment())
        }
        binding.buttonCurrentHeros.setOnClickListener {
        }
    }

    fun setShowCase() {

        val lps = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        val margin = ((resources.displayMetrics.density * 24) as Number).toInt()
        lps.setMargins(margin, margin, margin, margin * 10)

        showcaseView = ShowcaseView.Builder(requireActivity())
            .setTarget(NONE)
            .setStyle(R.style.CustomShowcaseTheme)
            .setContentTitle("歡迎進入WeShare，恭喜您已成功地完成共享的第一步！")
            .setContentText("讓我為您做個操作介紹吧:)")
            .setOnClickListener(this)
            .hideOnTouchOutside()
            .build()

        showcaseView.setButtonPosition(lps)
        showcaseView.setButtonText("下一步")
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

        // disable user interact
        logTickerRv.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                return true
            }
        })

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
            },
            0, 5000
        )
    }

    override fun onClick(p0: View?) {
        when (counter) {

            0 ->
                showcaseView.apply {
                    setShowcase(ViewTarget((activity as MainActivity).binding.fabMain), true)
                    setContentTitle("點擊+按鈕可以刊登贈品或刊登活動")
                    setContentText("")
                }

            1 -> showcaseView.apply {
                setShowcase(ViewTarget(binding.buttonCheckEvents), true)
                setContentTitle("點擊查看活動可以瀏覽當前最新的活動項目")
            }

            2 -> showcaseView.apply {
                setShowcase(ViewTarget(binding.buttonCheckGifts), true)
                setContentTitle("點擊查看贈品可以瀏覽當前最新的贈品項目")
            }
            3 -> showcaseView.apply {
                setShowcase(ViewTarget(binding.buttonCurrentHeros), true)
                setContentTitle("點擊英雄榜可以查看到用戶的貢獻排名")
            }
            4 -> showcaseView.apply {
                setShowcase(ViewTarget((activity as MainActivity).binding.notification), true)
                setContentTitle("點擊小鈴鐺來看看最新的通知")
                setButtonText("關閉")
            }
            5 -> {
                showcaseView.hide()
            }
        }
        counter++

        if (counter == 6) {
            counter = 0
        }
    }
}
