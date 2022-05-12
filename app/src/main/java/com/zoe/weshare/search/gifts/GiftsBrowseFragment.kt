package com.zoe.weshare.search.gifts

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
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
import com.zoe.weshare.ext.hideKeyboard


class GiftsBrowseFragment : Fragment() {

    private lateinit var binding: FragmentGiftsBrowseBinding
    private lateinit var adapter: GiftsBrowseAdapter
    private lateinit var manager: GridLayoutManager
    private lateinit var recyclerView: RecyclerView

    private var onNavigateBack: Boolean = false

    private val viewModel by viewModels<GiftsBrowseViewModel> { getVmFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentGiftsBrowseBinding.inflate(inflater, container, false)

        viewModel.gifts.observe(viewLifecycleOwner) {
            adapter.modifyList(it)

            onNavigateBack = false
        }

        viewModel.navigateToSelectedGift.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(NavGraphDirections.actionGlobalGiftDetailFragment(it))
                viewModel.onNavigateGiftDetailsComplete()

                binding.giftsSearchview.apply {
                    //close search view
                    isIconified = true

                    //clear search view text
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

    @SuppressLint("ClickableViewAccessibility")
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


        recyclerView.setOnTouchListener { view, event ->

            binding.giftsSearchview.isIconified = true
            view.hideKeyboard()
            false
        }

        binding.giftsSearchview.setOnClickListener {
            binding.giftsSearchview.isIconified = false
        }

        binding.giftsSearchview.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    Log.d("onQueryTextChange", "$newText")
                    if (!onNavigateBack) {
                        adapter.filter(newText, viewModel)
                    }
                }

                return true
            }
        })
    }

    override fun onStop() {
        super.onStop()

        onNavigateBack = true
    }
}
