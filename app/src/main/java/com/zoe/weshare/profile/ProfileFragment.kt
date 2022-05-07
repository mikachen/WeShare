package com.zoe.weshare.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.R
import com.zoe.weshare.data.OperationLog
import com.zoe.weshare.data.UserInfo
import com.zoe.weshare.data.UserProfile
import com.zoe.weshare.databinding.FragmentProfileBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.util.LogType
import com.zoe.weshare.util.UserManager.weShareUser

class ProfileFragment : Fragment() {

    lateinit var targetUser: UserInfo
    lateinit var binding: FragmentProfileBinding

    val viewModel: ProfileViewModel by viewModels { getVmFactory(targetUser) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        targetUser = ProfileFragmentArgs.fromBundle(requireArguments()).targetUser!!

        binding = FragmentProfileBinding.inflate(inflater, container, false)

        viewModel.user.observe(viewLifecycleOwner) {
            setupView(it)
        }

        viewModel.userLog.observe(viewLifecycleOwner) {
            setupLogView(it)
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

        mockUser()
        setupBtn()
        return binding.root
    }

    private fun setupLogView(logs: List<OperationLog>) {
        if (logs.isNotEmpty()) {
            binding.apply {

                textGiftPostCount.text =
                    logs.filter { it.logType == LogType.POST_GIFT.value }.size.toString()

                textGiftSentCount.text =
                    logs.filter { it.logType == LogType.SEND_GIFT.value }.size.toString()

                textEventPostCount.text =
                    logs.filter { it.logType == LogType.POST_EVENT.value }.size.toString()

                textEventVolunteerCount.text =
                    logs.filter { it.logType == LogType.VOLUNTEER_EVENT.value }.size.toString()
            }
        }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.profile_popup_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.edit_user_info -> findNavController().navigate(ProfileFragmentDirections
                    .actionProfileFragmentToEditInfoFragment(viewModel.user.value!!))

                R.id.action_gifts_manage -> findNavController().navigate(NavGraphDirections
                    .actionGlobalGiftManageFragment())

            }
            false
        }
        popupMenu.show()
    }

    private fun setupBtn() {

        binding.buttonFollow.setOnClickListener {
            viewModel.updateTargetFollower(targetUser.uid)
            viewModel.updateUserFollowing(targetUser.uid)
        }

        binding.buttonMessage.setOnClickListener {
            viewModel.searchOnPrivateRoom(weShareUser!!)
        }

        if (targetUser.uid == weShareUser!!.uid) {
            binding.buttonSettings.visibility = View.VISIBLE

            binding.buttonSettings.setOnClickListener {
                showPopupMenu(it)
            }
        }
    }

    private fun setupView(user: UserProfile) {
        binding.apply {
            bindImage(imageProfileAvatar, user.image)
            textProfileName.text = user.name
            textFollowerNumber.text = (user.follower.size).toString()
            textFollowingNumber.text = (user.following.size).toString()
            textIntroMessage.text =
                when (user.introMsg.isBlank()) {
                    true -> getString(R.string.request_leave_intro_message)
                    false -> user.introMsg
                }

            if (targetUser.uid != weShareUser!!.uid) {
                layoutSocialButton.visibility = View.VISIBLE

                buttonFollow.isChecked = user.follower.contains(weShareUser!!.uid)
            }
        }
    }

    fun mockUser() {

        binding.apply {

            ken.setOnClickListener {
                weShareUser = UserInfo(
                    name = "Ken",
                    image = "https://images2.gamme.com.tw/news2/2014/94/31/p6CWnp6ckqKW.jpg",
                    uid = "kenku037362583"
                )
            }
            amy.setOnClickListener {
                weShareUser = UserInfo(
                    name = "Amy",
                    image = "https://1.bp.blogspot.com/-wXhIWjtUkrc/XxzD1uRhQHI/AAAAAAAAhbc/3sL6IPSuG-gEJeg8Qy5sdLBRDurPCNpbwCLcBGAsYHQ/s640/Shingeki%2Bno%2BKyojin%2B-%2BOAD%2B03%2B%2528DVD%2B1024x576%2BAVC%2BAAC%2529.mp4_20200710_000330.072.jpg",
                    uid = " ko3jMaAmy03731283111"
                )
            }

            lora.setOnClickListener {
                weShareUser = UserInfo(
                    name = "蘿拉卡芙特",
                    image = "https://images2.gamme.com.tw/news2/2016/26/12/q52SpaablqCbqA.jpeg",
                    uid = "123ijijloraefe2212"
                )
            }
            mandy.setOnClickListener {
                weShareUser = UserInfo(
                    name = "Mandy",
                    image = "https://truth.bahamut.com.tw/s01/201309/f7d2d1613cbcd827ac28c1353bc54693.JPG",
                    uid = "manddy1ji332583"
                )
            }
        }
    }
}