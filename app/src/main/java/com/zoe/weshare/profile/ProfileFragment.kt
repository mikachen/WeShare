package com.zoe.weshare.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.zoe.weshare.MainActivity
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.R
import com.zoe.weshare.data.OperationLog
import com.zoe.weshare.data.UserInfo
import com.zoe.weshare.data.UserProfile
import com.zoe.weshare.databinding.FragmentProfileBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.util.LogType
import com.zoe.weshare.util.UserManager
import com.zoe.weshare.util.UserManager.weShareUser

class ProfileFragment : Fragment() {

    private lateinit var targetUser: UserInfo
    private lateinit var binding: FragmentProfileBinding
    private var isFollowingTarget = false

    private val viewModel: ProfileViewModel by viewModels { getVmFactory(targetUser) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        targetUser = ProfileFragmentArgs.fromBundle(requireArguments()).targetUser!!

        binding = FragmentProfileBinding.inflate(inflater, container, false)

        viewModel.user.observe(viewLifecycleOwner) {
            setupView(it)
            setupSocialButtons()
        }

        viewModel.userLog.observe(viewLifecycleOwner) {
            setupBoardCountView(it)
            viewModel.calculateContribution(it)
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

        viewModel.onUpdateContribution.observe(viewLifecycleOwner){
            it?.let {
                viewModel.updateContribution(it)
            }
        }

        mockUser()
        mockUser2()

        return binding.root
    }

    private fun setupBoardCountView(logs: List<OperationLog>) {
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

    private fun setupView(user: UserProfile) {
        isFollowingTarget = user.follower.contains(weShareUser!!.uid)

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
        }
    }

    private fun setupSocialButtons() {

        binding.buttonFollow.setOnCheckedChangeListener { btn, checked ->
            btn.isChecked = checked
        }

        if (targetUser.uid != weShareUser!!.uid) {

            //target user profile
            binding.layoutSocialButton.visibility = View.VISIBLE
            binding.buttonFollow.isChecked = isFollowingTarget

            if (isFollowingTarget) {
                binding.buttonFollow.setOnCheckedChangeListener { btn, checked ->
                    btn.isChecked = !checked
                }
            }
            binding.buttonFollow.setOnClickListener {
                followBtnClick()
            }

            binding.buttonMessage.setOnClickListener {
                viewModel.getUserAllRooms(weShareUser!!)
            }

        } else {

            //user self profile
            binding.buttonSettings.visibility = View.VISIBLE
            binding.buttonSettings.setOnClickListener {
                showPopupMenu(it,0)
            }
        }
    }

    private fun followBtnClick() {
        if (isFollowingTarget) {
                showPopupMenu(binding.buttonFollow, 1)

        } else {
            viewModel.updateTargetFollower(targetUser.uid)
            viewModel.updateUserFollowing(targetUser.uid)
        }
    }


    private fun showPopupMenu(view: View, condition: Int) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.profile_popup_menu, popupMenu.menu)

        when(condition){
            0->  popupMenu.menu.removeItem(R.id.action_cancel_following)

            1-> {
                popupMenu.menu.removeItem(R.id.edit_user_info)
                popupMenu.menu.removeItem(R.id.action_gifts_manage)
                popupMenu.menu.removeItem(R.id.action_events_manage)
                popupMenu.menu.removeItem(R.id.action_log_out)
            }
        }

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.edit_user_info -> findNavController().navigate(
                    ProfileFragmentDirections
                        .actionProfileFragmentToEditInfoFragment(viewModel.user.value!!)
                )

                R.id.action_gifts_manage -> findNavController().navigate(
                    ProfileFragmentDirections.actionProfileFragmentToGiftManageFragment()
                )

                R.id.action_events_manage -> findNavController().navigate(
                    ProfileFragmentDirections.actionProfileFragmentToEventManageFragment()
                )

                R.id.action_cancel_following -> {
                    viewModel.cancelTargetFollower(targetUser.uid)
                    viewModel.cancelUserFollowing(targetUser.uid)
                }

                R.id.action_log_out -> findNavController().navigate(
                    NavGraphDirections.actionGlobalLoginFragment(true))
            }
            false
        }
        popupMenu.show()
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
    }

    fun mockUser() {

        binding.apply {

            ken.setOnClickListener {
                weShareUser = UserInfo(
                    name = "Roy Chiu",
                    image = "https://images2.gamme.com.tw/news2/2014/94/31/p6CWnp6ckqKW.jpg",
                    uid = "kenku037362583"
                )
            }
            amy.setOnClickListener {
                weShareUser = UserInfo(
                    name = "莎夏·布勞斯",
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
                    name = "Ann Hsu",
                    image = "https://truth.bahamut.com.tw/s01/201309/f7d2d1613cbcd827ac28c1353bc54693.JPG",
                    uid = "manddy1ji332583"
                )
            }
        }
    }
    fun mockUser2() {

        binding.apply {

            A.setOnClickListener {
                UserManager.weShareUser = UserInfo(
                    name = "Johnny",
                    image = "https://img.tagsis.com/202204/96045.jpg",
                    uid = "12344408Johnny62583"
                )
            }
            B.setOnClickListener {
                UserManager.weShareUser = UserInfo(
                    name = "迅姐",
                    image = "https://www.laoziliao.net/fs/img/3e/3e1885f8708c208ab875033e3e5e3e8f.webp",
                    uid = " 12343j0000ZhouXun111"
                )
            }

            C.setOnClickListener {
                UserManager.weShareUser = UserInfo(
                    name = "小傑",
                    image = "https://images.chinatimes.com/newsphoto/2021-05-25/656/20210525003814.jpg",
                    uid = "100000Chiang2123212"
                )
            }
            D.setOnClickListener {
                UserManager.weShareUser = UserInfo(
                    name = "艾瑪",
                    image = "https://image.knowing.asia/c4f9ba3d-78f0-4c0e-828a-58eb5ede41ea/70eed7080b3bc4791fe83d798c08a210.png",
                    uid = "98666Emma1ji332583"
                )
            }
        }
    }

}
