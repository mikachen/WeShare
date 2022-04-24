package com.zoe.weshare.detail.gift

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.BounceInterpolator
import android.view.animation.ScaleAnimation
import android.widget.CompoundButton
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
import com.zoe.weshare.util.GiftStatusType
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
            binding.buttonPressLike.isChecked = it
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

        setupLikeBtn(selectedGift)
        setupBtn(selectedGift)
        return binding.root
    }

    private fun setupView(selectedGift: GiftPost) {
        binding.apply {
            bindImage(this.images, selectedGift.image)

            textGiftTitle.text = selectedGift.title

            textProfileName.text = selectedGift.author?.name

            bindImage(this.imageProfileAvatar, selectedGift.author?.image)

            textPostedLocation.text = selectedGift.location?.locationName

            textCreatedTime.text = getString(R.string.posted_time,
                selectedGift.createdTime.toDisplayFormat())

            textSort.text = selectedGift.sort

            textLikedNumber.text =
                getString(R.string.number_who_liked, selectedGift.whoLiked.size)

            textGiftDescription.text = selectedGift.description

            when (selectedGift.status) {
                GiftStatusType.OPENING.code -> {
                    binding.textStatus.text = GiftStatusType.OPENING.tag
                    binding.textStatus.setBackgroundResource(R.color.message_sender_green)
                }
                GiftStatusType.CLOSED.code -> {
                    binding.imageLogoStatus.visibility = View.VISIBLE
                    binding.imageLogoStatus.setBackgroundResource(R.drawable.status_close_title_logo)
                    binding.textStatus.text = GiftStatusType.CLOSED.tag
                    binding.textStatus.setBackgroundResource(R.color.app_work_orange3)
                }
                GiftStatusType.ABANDONED.code -> {
                    binding.textStatus.text = GiftStatusType.ABANDONED.tag
                    binding.textStatus.setBackgroundResource(R.color.app_work_light_grey)
                }
            }
        }
    }

    private fun setupBtn(selectedGift: GiftPost) {
        binding.buttonSendPmToAuthor.setOnClickListener {
            viewModel.searchOnPrivateRoom(currentUser)
        }




        // author he/herself hide the button
        if (selectedGift.author!!.uid == currentUser.uid) {
            binding.buttonSendPmToAuthor.visibility = View.GONE
            binding.layoutAskForGift.visibility = View.GONE
        } else {
            binding.buttonAskForGift.setOnClickListener {
                findNavController().navigate(
                    GiftDetailFragmentDirections
                        .actionGiftDetailFragmentToAskForGiftFragment(selectedGift)
                )
            }
        }
    }

    private fun checkIfUserRequested(comments: List<Comment>) {
        binding.buttonAskForGift.apply {
            when (comments.none { it.uid == currentUser.uid }) {
                true -> {
                    isEnabled = true
                    text = Util.getString(R.string.request_gift)
                }
                false -> {
                    isEnabled = false
                    text = Util.getString(R.string.already_requested_gift)
                }
            }
        }
    }

    private fun setupLikeBtn(selectedGift: GiftPost) {
        val scaleAnimation = ScaleAnimation(0.7f,
            1.0f,
            0.7f,
            1.0f,
            Animation.RELATIVE_TO_SELF,
            0.7f,
            Animation.RELATIVE_TO_SELF,
            0.7f)
        scaleAnimation.duration = 500
        val bounceInterpolator = BounceInterpolator()
        scaleAnimation.interpolator = bounceInterpolator


        binding.buttonPressLike.setOnClickListener {
            it.startAnimation(scaleAnimation)

            viewModel.onPostLikePressed(selectedGift.id)
        }
    }
}
