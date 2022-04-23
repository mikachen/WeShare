package com.zoe.weshare.manage.giftsItem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.R
import com.zoe.weshare.data.GiftPost
import com.zoe.weshare.databinding.FragmentGiftManageBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.util.UserManager.userZoe

class GiftManageFragment : Fragment() {

    var index = -1
    val currentUser = userZoe
    private lateinit var binding: FragmentGiftManageBinding

    private val viewModel by viewModels<GiftManageViewModel> { getVmFactory(currentUser) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentGiftManageBinding.inflate(inflater, container, false)

        //everytime when tabs position change, the index change
        index = requireArguments().getInt(INDEX_VALUE)

        val adapter = GiftItemsAdapter(viewModel, GiftItemsAdapter.OnClickListener {
            findNavController().navigate(NavGraphDirections.actionGlobalGiftDetailFragment(it))
        })
        val manager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.VERTICAL, false)

        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = manager


        viewModel.log.observe(viewLifecycleOwner) {
            it?.let {
                viewModel.onSearchGiftsDetail(it)
            }
        }

        viewModel.onDocSearch.observe(viewLifecycleOwner) {
            if (it == 0) {
                viewModel.filteringGift(index)
            }
        }

        viewModel.allGifts.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.onAlterMsgShowing.observe(viewLifecycleOwner) {
            abandonedEvent(it)
        }

        viewModel.abandonStatus.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), "下架成功", Toast.LENGTH_SHORT).show()
        }

        viewModel.onCommentsShowing.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(NavGraphDirections.actionGlobalDistributeFragment(it))
                viewModel.showCommentsComplete()
            }
        }




        return binding.root
    }

    private fun abandonedEvent(gift: GiftPost) {
        val builder = AlertDialog.Builder(requireActivity())

        builder.apply {
            setTitle(getString(R.string.abandoned_title, gift.title))
            setMessage(getString(R.string.abandoned_message))
            setPositiveButton(getString(R.string.abandoned_yes)) { dialog, id ->
                viewModel.abandonGift(gift)
                dialog.cancel()
            }

            setNegativeButton(getString(R.string.abandoned_no)) { dialog, id ->
                dialog.cancel()
            }
        }

        val alter: AlertDialog = builder.create()
        alter.show()
    }


    companion object {
        private const val INDEX_VALUE = "INDEX"

        fun getInstance(position: Int): Fragment {
            val childFragment = GiftManageFragment()

            val arg = Bundle()
            arg.putInt(INDEX_VALUE, position)
            childFragment.arguments = arg

            return childFragment
        }
    }
}