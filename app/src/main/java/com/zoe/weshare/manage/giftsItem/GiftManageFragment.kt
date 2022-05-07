package com.zoe.weshare.manage.giftsItem

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.R
import com.zoe.weshare.data.GiftPost
import com.zoe.weshare.databinding.FragmentGiftManageBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.network.LoadApiStatus
import com.zoe.weshare.util.UserManager.weShareUser

class GiftManageFragment : Fragment() {


    private lateinit var binding: FragmentGiftManageBinding
    lateinit var adapter: GiftItemsAdapter
    lateinit var manager: LinearLayoutManager

    var currentTabPosition = 0

    private val viewModel by viewModels<GiftManageViewModel> { getVmFactory(weShareUser) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentGiftManageBinding.inflate(inflater, container, false)

        viewModel.onSearchGiftsDetail(currentTabPosition)


        viewModel.giftsDisplay.observe(viewLifecycleOwner) {
            adapter.submitList(it)

            if(it.isEmpty()){
                binding.hintNoNews.visibility = View.VISIBLE
            }else{
                binding.hintNoNews.visibility = View.INVISIBLE
            }
        }

        viewModel.onAlterMsgShowing.observe(viewLifecycleOwner) {
            onAlertAbandon(it)
        }

        viewModel.abandonStatus.observe(viewLifecycleOwner) {
            if (it == LoadApiStatus.DONE) {
                Toast.makeText(requireContext(), "下架成功", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.onCommentsShowing.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(NavGraphDirections.actionGlobalDistributeFragment(it))
                viewModel.showCommentsComplete()
            }
        }

        viewModel.saveLogComplete.observe(viewLifecycleOwner){
            viewModel.refreshFilterView(currentTabPosition)
        }

        setupView()
        return binding.root
    }

    private fun onAlertAbandon(gift: GiftPost) {
        val builder = AlertDialog.Builder(requireActivity())

        builder.apply {
            setTitle(getString(R.string.abandoned_title, gift.title))
            setMessage(getString(R.string.abandoned_message))
            setPositiveButton(getString(R.string.abandoned_yes)) { dialog, id ->
                viewModel.abandonGift(gift)

                adapter.viewBinderHelper.closeLayout(gift.id)
                dialog.cancel()
            }

            setNegativeButton(getString(R.string.abandoned_no)) { dialog, id ->
                dialog.cancel()
            }
        }

        val alter: AlertDialog = builder.create()
        alter.show()
    }

    fun setupView(){
        adapter = GiftItemsAdapter(
            viewModel,
            GiftItemsAdapter.OnClickListener {
                findNavController().navigate(NavGraphDirections.actionGlobalGiftDetailFragment(it))
            }
        )

        manager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL, false
        )

        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = manager

        binding.notificationTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentTabPosition = tab.position

                viewModel.filteringGift(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
}
