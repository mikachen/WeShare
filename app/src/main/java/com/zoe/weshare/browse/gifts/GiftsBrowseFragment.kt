package com.zoe.weshare.browse.gifts

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
import com.zoe.weshare.databinding.FragmentGiftsBrowseBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.ext.hideKeyboard

class GiftsBrowseFragment : Fragment() {

    private lateinit var binding: FragmentGiftsBrowseBinding
    private lateinit var giftsBrowseAdapter: GiftsBrowseAdapter

    private var onNavigateBack: Boolean = false
    private val viewModel by viewModels<GiftsBrowseViewModel> { getVmFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentGiftsBrowseBinding.inflate(inflater, container, false)

        viewModel.gifts.observe(viewLifecycleOwner) {
            onNavigateBack = false
            giftsBrowseAdapter.modifyList(it)
        }

        viewModel.navigateToSelectedGift.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(NavGraphDirections.actionGlobalGiftDetailFragment(it))
                viewModel.onNavigateGiftDetailsComplete()

                binding.giftsSearchview.apply {
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

        giftsBrowseAdapter = GiftsBrowseAdapter { viewModel.onNavigateGiftDetails(it) }

        binding.recyclerview.run {
            adapter = giftsBrowseAdapter

            layoutManager = GridLayoutManager(requireContext(), 2)

            setOnTouchListener { view, event ->

                binding.giftsSearchview.isIconified = true

                view.hideKeyboard()
                false
            }
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
                    if (!onNavigateBack) {
                        giftsBrowseAdapter.filter(newText, viewModel)
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
