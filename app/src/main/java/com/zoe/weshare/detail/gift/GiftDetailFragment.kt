package com.zoe.weshare.detail.gift

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.BounceInterpolator
import android.view.animation.ScaleAnimation
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
import com.zoe.weshare.util.UserManager.weShareUser
import com.zoe.weshare.util.Util

class GiftDetailFragment : Fragment() {

    private lateinit var binding: FragmentGiftDetailBinding
    private lateinit var adapter: GiftsCommentsAdapter

    val viewModel by viewModels<GiftDetailViewModel> { getVmFactory(weShareUser) }

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
            setupLikeBtn(it)
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

        viewModel.targetUser.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(NavGraphDirections.actionGlobalProfileFragment(it))
                viewModel.navigateToProfileComplete()
            }
        }

        return binding.root
    }

    private fun setupView(gift: GiftPost) {
        binding.apply {
            bindImage(this.images, gift.image)

            textGiftTitle.text = gift.title

            textProfileName.text = gift.author?.name

            bindImage(this.imageProfileAvatar, gift.author?.image)

            textPostedLocation.text = gift.location?.locationName

            textCreatedTime.text = getString(R.string.posted_time,
                gift.createdTime.toDisplayFormat()
            )

            textGiftSort.text = gift.sort

            textGiftCondition.text = gift.condition

            textLikedNumber.text =
                getString(R.string.number_who_liked, gift.whoLiked.size)

            textGiftDescription.text = gift.description

            when (gift.status) {
                GiftStatusType.OPENING.code -> {
                    binding.textStatus.text = GiftStatusType.OPENING.tag
                    binding.textStatus.setBackgroundResource(R.color.app_work_light_green)
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

    private fun setupBtn(gift: GiftPost) {
        binding.lottieBtnChatMe.setOnClickListener {
            viewModel.searchOnPrivateRoom(weShareUser!!)
        }

        when (true) {
            // author he/herself hide the button
            (gift.author!!.uid == weShareUser!!.uid) -> {
                binding.lottieBtnChatMe.visibility = View.GONE
                binding.layoutAskForGift.visibility = View.GONE
            }

            // gift status CLOSE or ABANDONED hide the button
            (gift.status == GiftStatusType.CLOSED.code) -> {
                binding.layoutAskForGift.visibility = View.GONE
            }
            (gift.status == GiftStatusType.ABANDONED.code) -> {
                binding.layoutAskForGift.visibility = View.GONE
            }
            else -> {
                binding.buttonAskForGift.setOnClickListener {
                    findNavController().navigate(
                        GiftDetailFragmentDirections
                            .actionGiftDetailFragmentToAskForGiftFragment(gift)
                    )
                }
            }
        }
        binding.imageProfileAvatar.setOnClickListener {
            findNavController().navigate(NavGraphDirections.actionGlobalProfileFragment(gift.author))
        }
    }

    private fun checkIfUserRequested(comments: List<Comment>) {
        binding.buttonAskForGift.apply {
            when (comments.none { it.uid == weShareUser!!.uid }) {
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
        val scaleAnimation = ScaleAnimation(
            0.7f,
            1.0f,
            0.7f,
            1.0f,
            Animation.RELATIVE_TO_SELF,
            0.7f,
            Animation.RELATIVE_TO_SELF,
            0.7f
        )
        scaleAnimation.duration = 500
        val bounceInterpolator = BounceInterpolator()
        scaleAnimation.interpolator = bounceInterpolator

        binding.buttonPressLike.setOnClickListener {

            it.startAnimation(scaleAnimation)

            viewModel.onPostLikePressed(selectedGift.id)
            playCreditScene()
        }

        binding.buttonAdditionHeart1.setOnClickListener {
            playCreditScene()
        }

        binding.buttonAdditionHeart2.setOnClickListener {
            playCreditScene()
        }
    }

    private fun playCreditScene() {
        if (binding.buttonAdditionHeart2.isChecked &&
            binding.buttonAdditionHeart1.isChecked &&
            binding.buttonPressLike.isChecked
        ) {
            findNavController().navigate(
                GiftDetailFragmentDirections.actionGiftDetailFragmentToCreditFragment()
            )
        }
    }
}
