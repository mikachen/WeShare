package com.zoe.weshare.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import com.yuyakaido.android.cardstackview.*
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.databinding.FragmentHomeBinding
import com.zoe.weshare.ext.getVmFactory

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class HomeFragment : Fragment(), CardStackListener {

    private lateinit var cardStackView: CardStackView
    private lateinit var binding: FragmentHomeBinding
    private lateinit var manager: CardStackLayoutManager
    private lateinit var adapter: CardStackAdapter

    val viewModel by viewModels<HomeViewModel> { getVmFactory() }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupCardStackView()

        viewModel.gifts.observe(viewLifecycleOwner){
            viewModel.onCardPrepare(gifts = it , events = null)
        }

        viewModel.events.observe(viewLifecycleOwner){
            viewModel.onCardPrepare(gifts = null , events = it)
        }

        viewModel.cards.observe(viewLifecycleOwner){
            adapter.onListUpdate(it)
            adapter.notifyDataSetChanged()
        }

        viewModel.navigateToSelectedGift.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(NavGraphDirections.actionGlobalGiftDetailFragment(it))
                viewModel.displayCardDetailsComplete()
            }
        }

        viewModel.navigateToSelectedEvent.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(NavGraphDirections.actionGlobalEventDetailFragment(it))
                viewModel.displayCardDetailsComplete()
            }
        }

        return binding.root
    }

    private fun setupCardStackView() {
        cardStackView = binding.cardStackView
        adapter = CardStackAdapter(CardStackAdapter.StackViewOnClickListener { selectedCard ->
            viewModel.displayCardDetails(selectedCard)
        })
        manager = CardStackLayoutManager(requireContext(), this)
        manager.setStackFrom(StackFrom.Top)
        manager.setVisibleCount(3)
        manager.setTranslationInterval(10.0f)
        manager.setScaleInterval(0.85f)
        manager.setSwipeThreshold(0.3f)
        manager.setMaxDegree(20.0f)
        manager.setDirections(Direction.HORIZONTAL)
        manager.setCanScrollHorizontal(true)
        manager.setCanScrollVertical(true)
        manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
        manager.setOverlayInterpolator(LinearInterpolator())
        cardStackView.layoutManager = manager
        cardStackView.adapter = adapter
        cardStackView.itemAnimator.apply {
            if (this is DefaultItemAnimator) {
                supportsChangeAnimations = false
            }
        }
    }

    override fun onCardDragging(direction: Direction, ratio: Float) {
        Log.d("CardStackView", "onCardDragging: d = ${direction.name}, r = $ratio")
    }

    override fun onCardSwiped(direction: Direction) {
        Log.d("CardStackView", "onCardSwiped: p = ${manager.topPosition}, d = $direction")
        if (manager.topPosition == adapter.itemCount - 5) {
//            paginate()
        }
    }

    override fun onCardRewound() {
        Log.d("CardStackView", "onCardRewound: ${manager.topPosition}")
    }

    override fun onCardCanceled() {
        Log.d("CardStackView", "onCardCanceled: ${manager.topPosition}")
    }

    override fun onCardAppeared(view: View, position: Int) {
//        val textView = view.findViewById<TextView>(R.id.item_name)
//        Log.d("CardStackView", "onCardAppeared: ($position) ${textView.text}")
    }

    override fun onCardDisappeared(view: View, position: Int) {
//        val textView = view.findViewById<TextView>(R.id.item_name)
//        Log.d("CardStackView", "onCardDisappeared: ($position) ${textView.text}")
    }

}
