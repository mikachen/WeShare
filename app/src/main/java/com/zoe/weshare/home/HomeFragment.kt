package com.zoe.weshare.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.NavGraphDirections

import com.zoe.weshare.databinding.FragmentHomeBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.map.CardGalleryAdapter
import com.zoe.weshare.map.GalleryDecoration
import com.zoe.weshare.map.MapViewModel

//TODO TEMP

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CardGalleryAdapter

    val viewModel by viewModels<MapViewModel> { getVmFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentHomeBinding.inflate(inflater, container, false)


        viewModel.gifts.observe(viewLifecycleOwner) {
            viewModel.onCardPrepare(gifts = it, events = null)
        }

        viewModel.events.observe(viewLifecycleOwner) {
            viewModel.onCardPrepare(gifts = null, events = it)
        }

        viewModel.cards.observe(viewLifecycleOwner) {
            if (viewModel.isEventCardsComplete && viewModel.isGiftCardsComplete) {
                adapter.submitCards(it.shuffled())
            }
        }

        viewModel.navigateToSelectedGift.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(
                    NavGraphDirections.actionGlobalGiftDetailFragment(it))

                viewModel.displayCardDetailsComplete()
            }
        }

        viewModel.navigateToSelectedEvent.observe(viewLifecycleOwner) {
            it?.let {
                findNavController()
                    .navigate(NavGraphDirections.actionGlobalEventDetailFragment(it))
                viewModel.displayCardDetailsComplete()
            }
        }

        setupCardGallery()


        return binding.root
    }

    private fun setupCardGallery() {

        recyclerView = binding.recyclerview

        adapter = CardGalleryAdapter(
            CardGalleryAdapter.CardOnClickListener { selectedCard ->
                viewModel.displayCardDetails(selectedCard)
            }
        )

        val manager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.HORIZONTAL, false)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = manager

        val linearSnapHelper = LinearSnapHelper().apply {
            attachToRecyclerView(recyclerView)
        }

        val marginDecoration = GalleryDecoration()
        recyclerView.addItemDecoration(marginDecoration)


        recyclerView.setOnScrollChangeListener { _, _, _, _, _ ->
            viewModel.onGalleryScrollChange(
                recyclerView.layoutManager, linearSnapHelper
            )
        }
    }

}

