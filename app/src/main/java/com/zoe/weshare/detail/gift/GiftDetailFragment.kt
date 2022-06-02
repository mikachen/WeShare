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
import com.zoe.weshare.detail.isUserLikedBefore
import com.zoe.weshare.detail.isUserRequestedBefore
import com.zoe.weshare.detail.isUserThePostAuthor
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.ext.showToast
import com.zoe.weshare.ext.toDisplayFormat
import com.zoe.weshare.util.GiftStatusType
import com.zoe.weshare.util.Logger
import com.zoe.weshare.util.UserManager.weShareUser
import com.zoe.weshare.util.Util

class GiftDetailFragment : Fragment() {

    private lateinit var binding: FragmentGiftDetailBinding
    private lateinit var adapter: RequestGiftAdapter

    private val viewModel by viewModels<GiftDetailViewModel> { getVmFactory(weShareUser) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGiftDetailBinding.inflate(inflater, container, false)

        val selectedGift = GiftDetailFragmentArgs.fromBundle(requireArguments()).selectedGift
        viewModel.onViewPrepare(selectedGift)

        adapter = RequestGiftAdapter(viewModel)
        binding.commentsRecyclerView.adapter = adapter

        viewModel.liveGiftDetailResult.observe(viewLifecycleOwner) {
            it?.let {
                viewModel.setGift(it)
                setupView(it)
                setupBtn(it)
                setupLikeBtn(it)
            }
        }

        viewModel.liveRequestComments.observe(viewLifecycleOwner) {
            viewModel.filterRequestComments()
        }

        viewModel.filteredComments.observe(viewLifecycleOwner) {
            adapter.submitList(it)

            viewModel.searchUsersProfile(it)
            setupRequestButton(it)
        }

        // drawing the user avatar image and nickName after searching user's profile docs
        viewModel.profileSearchComplete.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    adapter.notifyDataSetChanged()
                }
            }
        }

        viewModel.userChatRoomsResult.observe(viewLifecycleOwner) {
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
                findNavController().navigate(
                    NavGraphDirections.actionGlobalChatRoomFragment(it))

                viewModel.navigateToRoomComplete()
            }
        }

        viewModel.navigateToNewRoom.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(
                    NavGraphDirections.actionGlobalChatRoomFragment(it))

                viewModel.navigateToRoomComplete()
            }
        }

        viewModel.onTargetAvatarClicked.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(
                    GiftDetailFragmentDirections.actionGiftDetailToProfile(it))

                viewModel.navigateToProfileComplete()
            }
        }

        viewModel.navigateToRequest.observe(viewLifecycleOwner){
            it?.let {
                findNavController().navigate(
                    GiftDetailFragmentDirections.actionGiftDetailToRequestGift(it))
            }
        }

        viewModel.onReportUserViolation.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(
                    NavGraphDirections.actionGlobalReportViolationDialog(it))

                viewModel.navigateToReportComplete()
            }
        }

        viewModel.onBlockListUser.observe(viewLifecycleOwner) {
            it?.let {
                activity.showToast(getString(R.string.block_this_person_complete))
                viewModel.refreshCommentBoard()
            }
        }

        return binding.root
    }

    private fun setupView(gift: GiftPost) {
        binding.apply {
            bindImage(giftImage, gift.image)

            textGiftTitle.text = gift.title

            textProfileName.text = gift.author.name

            bindImage(imageAuthorAvatar, gift.author.image)

            textPostedLocation.text = gift.location.locationName

            textCreatedTime.text = getString(
                R.string.posted_time,
                gift.createdTime.toDisplayFormat()
            )

            textGiftSort.text = gift.sort

            textGiftCondition.text = gift.condition

            buttonPressLike.isChecked = isUserLikedBefore(gift.whoLiked)

            textLikedNumber.text =
                getString(R.string.number_who_liked, gift.whoLiked.size)

            textGiftDescription.text = gift.description

            when (gift.status) {
                GiftStatusType.OPENING.code -> {
                    textStatus.text = GiftStatusType.OPENING.tag
                    textStatus.setBackgroundResource(R.color.event_awaiting_tag)
                }
                GiftStatusType.CLOSED.code -> {
                    textStatus.text = GiftStatusType.CLOSED.tag
                    textStatus.setBackgroundResource(R.color.app_work_orange3)
                }
                GiftStatusType.ABANDONED.code -> {
                    textStatus.text = GiftStatusType.ABANDONED.tag
                    textStatus.setBackgroundResource(R.color.app_work_dark_grey)
                }
            }
        }
    }

    private fun setupBtn(gift: GiftPost) {

        binding.apply {

            imageAuthorAvatar.setOnClickListener {
                viewModel.onNavigateToTargetProfile(gift.author.uid)
            }

            if (isUserThePostAuthor(gift.author.uid)) {
                lottieBtnChatMe.visibility = View.GONE
                layoutAskForGift.visibility = View.GONE

            } else {
                lottieBtnChatMe.setOnClickListener {
                    viewModel.searchOnPrivateRoom(weShareUser)
                }
            }

            if (gift.status != GiftStatusType.OPENING.code) {
                layoutAskForGift.visibility = View.GONE
            }
        }
    }

    private fun setupRequestButton(comments: List<Comment>) {
        binding.textRegistrantsNumber.text =
            getString(R.string.gift_registrants_number, comments.size)

        binding.buttonRequestGift.apply {

            this.setOnClickListener {
                viewModel.onNavigateToRequestDialog()
            }

            if (!isUserRequestedBefore(comments)) {
                isEnabled = true
                text = Util.getString(R.string.request_gift)
            } else {
                isEnabled = false
                text = Util.getString(R.string.already_requested_gift)
            }
        }
}

    private fun setupLikeBtn(gift: GiftPost) {

        val fromScale = 0.7f
        val toScale = 1.0f
        val duration = 500L
        val bounceInterpolator = BounceInterpolator()

        val scaleAnimation = ScaleAnimation(
            fromScale, toScale, fromScale, toScale,
            Animation.RELATIVE_TO_SELF, fromScale,
            Animation.RELATIVE_TO_SELF, fromScale
        )

        scaleAnimation.duration = duration
        scaleAnimation.interpolator = bounceInterpolator

        binding.buttonPressLike.setOnClickListener {
            it.startAnimation(scaleAnimation)
            playCreditScene()

            viewModel.onPostLikePressed(gift)
        }

        binding.buttonAdditionHeart1.setOnClickListener {
            it.startAnimation(scaleAnimation)
            playCreditScene()
        }

        binding.buttonLike.setOnClickListener {
            it.startAnimation(scaleAnimation)
            playCreditScene()
        }
    }

    private fun playCreditScene() {
        if (binding.buttonLike.isChecked
            && binding.buttonAdditionHeart1.isChecked
            && binding.buttonPressLike.isChecked) {

            findNavController().navigate(
                NavGraphDirections.actionGlobalCreditFragment()
            )
        }
    }
}
