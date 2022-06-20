package com.zoe.weshare.browse.events

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.databinding.FragmentEventsBrowseBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.ext.hideKeyboard


class EventsBrowseFragment : Fragment() {

    private lateinit var binding: FragmentEventsBrowseBinding
    private lateinit var eventsBrowseAdapter: EventsBrowseAdapter

    private var onNavigateBack: Boolean = false
    private val viewModel by viewModels<EventsBrowseViewModel> { getVmFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentEventsBrowseBinding.inflate(inflater, container, false)

        viewModel.events.observe(viewLifecycleOwner) {
            onNavigateBack = false
            eventsBrowseAdapter.modifyList(it)
        }

        viewModel.navigateToSelectedEvent.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(NavGraphDirections.actionGlobalEventDetailFragment(it))
                viewModel.onNavigateEventDetailsComplete()

                binding.eventsSearchview.apply {
                    // close search view
                    isIconified = true

                    // clear search view text
                    setQuery("", false)
                }
            }
        }

        viewModel.emptyQuery.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    binding.hintNoItem.visibility = View.VISIBLE
                } else {
                    binding.hintNoItem.visibility = View.GONE
                }
            }
        }

        setupView()
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setupView() {

        eventsBrowseAdapter = EventsBrowseAdapter{ viewModel.onNavigateEventDetails(it)}

        binding.recyclerview.run{
            adapter = eventsBrowseAdapter

            layoutManager = GridLayoutManager(requireContext(), 2)

            setOnTouchListener { view, event ->
                binding.eventsSearchview.isIconified = true

                view.hideKeyboard()
                false
            }
        }

        binding.eventsSearchview.setOnClickListener {
            binding.eventsSearchview.isIconified = false
        }

        binding.eventsSearchview.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if (!onNavigateBack) {
                        eventsBrowseAdapter.filter(newText, viewModel)
                    }
                }
                return true
            }
        })
    }

    /** To prevent the searchView got triggered by query text */
    override fun onStop() {
        super.onStop()
        onNavigateBack = true
    }
}
