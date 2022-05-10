package com.zoe.weshare.search.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.databinding.FragmentEventsBrowseBinding
import com.zoe.weshare.ext.getVmFactory

class EventsBrowseFragment : Fragment() {

    lateinit var binding: FragmentEventsBrowseBinding
    lateinit var adapter: EventsBrowseAdapter
    lateinit var manager: GridLayoutManager
    lateinit var recyclerView: RecyclerView

    val viewModel by viewModels<EventsBrowseViewModel> { getVmFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentEventsBrowseBinding.inflate(inflater, container, false)

        viewModel.events.observe(viewLifecycleOwner) {
            adapter.modifyList(it)
        }

        viewModel.navigateToSelectedEvent.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(NavGraphDirections.actionGlobalEventDetailFragment(it))
                viewModel.onNavigateEventDetailsComplete()

                binding.eventsSearchview.apply {
                    isIconified = true
                    setQuery("", false)
                }
            }
        }

        viewModel.onSearchEmpty.observe(viewLifecycleOwner) {

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

    fun setupView() {

        adapter = EventsBrowseAdapter(
            EventsBrowseAdapter.EventsAllOnClickListener { selectedEvent ->
                viewModel.onNavigateEventDetails(selectedEvent)
            }
        )

        manager = GridLayoutManager(requireContext(), 2)
        recyclerView = binding.recyclerview

        recyclerView.adapter = adapter
        recyclerView.layoutManager = manager

        binding.eventsSearchview.setOnClickListener {
            binding.eventsSearchview.isIconified = false
        }

        binding.eventsSearchview.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText, viewModel)

                return true
            }
        })
    }
}
