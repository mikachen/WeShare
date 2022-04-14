package com.zoe.weshare.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.zoe.weshare.data.GiftPost
import com.zoe.weshare.databinding.FragmentGiftDetailBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.ext.toDisplayFormat


// TODO 引入viewModel處理user頭像 登記人數＋索取人的留言

class GiftDetailFragment : Fragment() {

    private lateinit var binding: FragmentGiftDetailBinding
    private lateinit var adapter: CommentsAdapter

    val viewModel by viewModels<GiftDetailViewModel> { getVmFactory() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentGiftDetailBinding.inflate(inflater, container, false)

        val selectedGift = GiftDetailFragmentArgs.fromBundle(requireArguments()).selectedGift
        setUpDetailContent(selectedGift)
        viewModel.getAllRequestComments(selectedGift.id)

        adapter = CommentsAdapter(viewModel)
        binding.commentsRecyclerView.adapter = adapter

        viewModel.comments.observe(viewLifecycleOwner) {
            viewModel.getUserList(it)
        }

        //want to make sure finish getting userInfo before start recyclerView adapter
        viewModel.onCommentsDisplay.observe(viewLifecycleOwner) {
            if(it == 0) {
                adapter.submitList(viewModel.comments.value)
            }
        }

        return binding.root
    }


    private fun setUpDetailContent(selectedGift: GiftPost) {

        bindImage(binding.images, selectedGift.image)

        binding.apply {
            textGiftTitle.text = selectedGift.title
            textProfileName.text = selectedGift.author?.name
            textPostedLocation.text = selectedGift.location?.locationName
            textCreatedTime.text = selectedGift.createdTime.toDisplayFormat()
            textSort.text = selectedGift.sort
            textDescription.text = selectedGift.description
        }
    }

    private fun setUpBtn() {}

}