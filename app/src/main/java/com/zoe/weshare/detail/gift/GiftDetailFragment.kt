package com.zoe.weshare.detail.gift

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.zoe.weshare.R
import com.zoe.weshare.data.GiftPost
import com.zoe.weshare.databinding.FragmentGiftDetailBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.ext.toDisplayFormat


class GiftDetailFragment : Fragment() {

    private lateinit var binding: FragmentGiftDetailBinding
    private lateinit var adapter: GiftsCommentsAdapter

    val viewModel by viewModels<GiftDetailViewModel> { getVmFactory() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentGiftDetailBinding.inflate(inflater, container, false)

        val selectedGift = GiftDetailFragmentArgs.fromBundle(requireArguments()).selectedGift

        setUpDetailView(selectedGift)
        viewModel.getAskForGiftComments(selectedGift.id)

        adapter = GiftsCommentsAdapter(viewModel)
        binding.commentsRecyclerView.adapter = adapter

        viewModel.comments.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            viewModel.searchUsersProfile(it)
        }

        //drawing the user avatar image and nickName after searching user's profile docs
        viewModel.onProfileSearching.observe(viewLifecycleOwner) {
            if (it == 0) {
                adapter.notifyDataSetChanged()
            }
        }

        setUpBtn(selectedGift)

        return binding.root
    }


    private fun setUpDetailView(selectedGift: GiftPost) {
        binding.apply {
            bindImage(this.images, selectedGift.image)
            textGiftTitle.text = selectedGift.title
            textProfileName.text = selectedGift.author?.name
            bindImage(this.imageProfileAvatar, selectedGift.author?.image)
            textPostedLocation.text = resources.getString(R.string.gift_post_location_name,selectedGift.location?.locationName)
            textCreatedTime.text = resources.getString(R.string.posted_time,selectedGift.createdTime.toDisplayFormat())
            textSort.text = resources.getString(R.string.gift_post_sort,selectedGift.sort)
            textLikedNumber.text = resources.getString(R.string.number_who_liked,selectedGift.whoLiked?.size)
            textDescription.text = selectedGift.description
        }
    }

    private fun setUpBtn(selectedGift: GiftPost) {
        binding.buttonAskForGift.setOnClickListener {
            findNavController().navigate(GiftDetailFragmentDirections
                .actionGiftDetailFragmentToAskForGiftFragment(
                selectedGift.id))
        }
    }
}