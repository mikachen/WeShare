package com.zoe.weshare.detail.gift

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.R
import com.zoe.weshare.data.Comment
import com.zoe.weshare.data.GiftPost
import com.zoe.weshare.databinding.FragmentGiftDetailBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.ext.toDisplayFormat
import com.zoe.weshare.util.UserManager.userLora
import com.zoe.weshare.util.Util
import com.zoe.weshare.util.Util.getColor

class GiftDetailFragment : Fragment() {

    private lateinit var binding: FragmentGiftDetailBinding
    private lateinit var adapter: GiftsCommentsAdapter
    private val currentUser = userLora

    val viewModel by viewModels<GiftDetailViewModel> { getVmFactory(currentUser) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentGiftDetailBinding.inflate(inflater, container, false)

        val selectedGift = GiftDetailFragmentArgs.fromBundle(requireArguments()).selectedGift

        viewModel.onGiftDisplay(selectedGift)
        viewModel.getAskForGiftComments(selectedGift.id)

        adapter = GiftsCommentsAdapter(viewModel)
        binding.commentsRecyclerView.adapter = adapter

        viewModel.selectedGiftDisplay.observe(viewLifecycleOwner) {
            setupView(it)
            setupBtn(it)
        }

        viewModel.onCommentLikePressed.observe(viewLifecycleOwner) {
            adapter.notifyItemChanged(it)
        }

        viewModel.comments.observe(viewLifecycleOwner) {
            adapter.submitList(it)

            if (it != null) {
                checkIfUserRequested(it)
            }

            if (it != null) {
                binding.textRegistrantsNumber.text =
                    getString(R.string.gift_registrants_number, it.size)
            }

            // make sure it only run one time
            if (viewModel.onProfileSearchComplete.value == null) {
                viewModel.searchUsersProfile(it)
            }
        }

        // drawing the user avatar image and nickName after searching user's profile docs
        viewModel.onProfileSearchComplete.observe(viewLifecycleOwner) {
            if (it == 0) {
                adapter.notifyDataSetChanged()
            }
        }

        viewModel.currentLikedNumber.observe(viewLifecycleOwner) {
            binding.textLikedNumber.text = resources.getString(R.string.number_who_liked, it)
        }

        viewModel.isUserPressedLike.observe(viewLifecycleOwner) {
            if (it) {
                binding.buttonPressLike.text = "已點讚 <3"
                binding.buttonPressLike.backgroundTintList =
                    ColorStateList.valueOf(getColor(R.color.yellowTestColor))
            } else {
                binding.buttonPressLike.text = "感恩讚"
                binding.buttonPressLike.backgroundTintList =
                    ColorStateList.valueOf(getColor(R.color.lightBlueTestColor))
            }
        }

        viewModel.userChatRooms.observe(viewLifecycleOwner) {
            it?.let {
                if (it.isNotEmpty()) {
                    viewModel.checkIfPrivateRoomExist(it)
                } else {
                    viewModel.onNewRoomPrepare()
                }
            }
        }

        viewModel.navigateToFormerRoom.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(NavGraphDirections.actionGlobalChatRoomFragment(it))
                viewModel.navigateToRoomComplete()
            }
        }

        viewModel.navigateToNewRoom.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(NavGraphDirections.actionGlobalChatRoomFragment(it))
                viewModel.navigateToRoomComplete()
            }
        }

        setupBtn(selectedGift)
        return binding.root
    }

    private fun setupView(selectedGift: GiftPost) {
        binding.apply {
            bindImage(this.images, selectedGift.image)
            textGiftTitle.text = selectedGift.title
            textProfileName.text = selectedGift.author?.name
            bindImage(this.imageProfileAvatar, selectedGift.author?.image)
            textPostedLocation.text = getString(R.string.gift_post_location_name,
                selectedGift.location?.locationName)
            textCreatedTime.text = getString(R.string.posted_time,
                selectedGift.createdTime.toDisplayFormat())
            textSort.text = getString(R.string.gift_post_sort, selectedGift.sort)
            textQuantity.text =
                getString(R.string.gift_post_quantity, selectedGift.quantity)
            textLikedNumber.text =
                getString(R.string.number_who_liked, selectedGift.whoLiked?.size)
            textDescription.text = selectedGift.description
        }
    }

    private fun setupBtn(selectedGift: GiftPost) {

        binding.buttonPressLike.setOnClickListener {
            viewModel.onPostLikePressed(selectedGift.id)
        }
        binding.buttonSendPmToAuthor.setOnClickListener {
            viewModel.searchOnPrivateRoom(currentUser)
        }


        // author he/herself hide the button
        if (selectedGift.author!!.uid == currentUser.uid) {
            binding.buttonSendPmToAuthor.visibility = View.GONE
            binding.layoutAskforgiftButtons.visibility = View.GONE
        } else {
            binding.buttonAskForGift.setOnClickListener {
                findNavController().navigate(
                    GiftDetailFragmentDirections
                        .actionGiftDetailFragmentToAskForGiftFragment(selectedGift.id)
                )
            }

        }
    }

    private fun checkIfUserRequested(comments: List<Comment>){
        binding.buttonAskForGift.isEnabled = comments.none { it.uid == currentUser.uid }
        binding.buttonAskForGift.text = Util.getString(R.string.already_requested_gift)
    }
}
