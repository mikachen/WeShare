package com.zoe.weshare.search.gifts

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
import com.zoe.weshare.databinding.FragmentGiftsBrowseBinding
import com.zoe.weshare.ext.getVmFactory

class GiftsBrowseFragment : Fragment() {

    lateinit var binding: FragmentGiftsBrowseBinding
    lateinit var adapter: GiftsBrowseAdapter
    lateinit var manager: GridLayoutManager
    lateinit var recyclerView: RecyclerView

    val viewModel by viewModels<GiftsBrowseViewModel> { getVmFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentGiftsBrowseBinding.inflate(inflater, container, false)

        viewModel.gifts.observe(viewLifecycleOwner) {
            adapter.modifyList(it)
        }

        viewModel.navigateToSelectedGift.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(NavGraphDirections.actionGlobalGiftDetailFragment(it))
                viewModel.onNavigateGiftDetailsComplete()

                binding.giftsSearchview.apply {
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

        adapter = GiftsBrowseAdapter(
            GiftsBrowseAdapter.GiftsALLOnClickListener { selectedGift ->
                viewModel.onNavigateGiftDetails(selectedGift)
            }
        )

        manager = GridLayoutManager(requireContext(), 2)
        recyclerView = binding.recyclerview

        recyclerView.adapter = adapter
        recyclerView.layoutManager = manager

        binding.giftsSearchview.setOnClickListener {
            binding.giftsSearchview.isIconified = false
        }

        binding.giftsSearchview.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
