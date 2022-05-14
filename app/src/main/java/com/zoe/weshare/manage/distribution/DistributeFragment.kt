package com.zoe.weshare.manage.distribution

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.R
import com.zoe.weshare.data.GiftPost
import com.zoe.weshare.data.UserProfile
import com.zoe.weshare.databinding.FragmentDistributeBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.ext.sendNotificationToTarget
import com.zoe.weshare.ext.sendNotificationsToFollowers
import com.zoe.weshare.util.UserManager.weShareUser

class DistributeFragment : BottomSheetDialogFragment() {

    val viewModel by viewModels<DistributeViewModel> { getVmFactory(weShareUser) }

    lateinit var binding: FragmentDistributeBinding
    lateinit var layoutList: LinearLayout
    lateinit var selectedGift: GiftPost

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentDistributeBinding.inflate(inflater, container, false)

        selectedGift = DistributeFragmentArgs.fromBundle(requireArguments()).selectedGift
        viewModel.getAskForGiftComments(selectedGift)

        val adapter = DistributeAdapter(viewModel)
        val manager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL, false
        )

        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = manager

        viewModel.comments.observe(viewLifecycleOwner) {
            adapter.submitList(it)

            // make sure it only run at first time
            if (viewModel.onProfileSearchComplete.value == null) {
                viewModel.searchUsersProfile(it)
            }
        }

        viewModel.onProfileSearchComplete.observe(viewLifecycleOwner) {
            if (it == 0) {
                adapter.notifyDataSetChanged()
            }
        }

        viewModel.onConfirmMsgShowing.observe(viewLifecycleOwner) {
            onConfirmingOperation(it)
        }

        viewModel.targetUser.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(
                    DistributeFragmentDirections.actionDistributeFragmentToProfileFragment(it))
                viewModel.navigateToProfileComplete()
            }
        }

        viewModel.receiverNotification.observe(viewLifecycleOwner) {
            sendNotificationToTarget(it.operatorUid, it)
        }

        viewModel.saveLogComplete.observe(viewLifecycleOwner) {
            sendNotificationsToFollowers(it)

            Toast.makeText(requireContext(), "送出成功", Toast.LENGTH_SHORT).show()
//            findNavController().navigate(NavGraphDirections.actionGlobalGiftManageFragment())
        }

        setupBtn()
        return binding.root
    }

    private fun setupBtn() {
        binding.buttonCloseDialog.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun onConfirmingOperation(target: UserProfile?) {
        val builder = AlertDialog.Builder(requireActivity())

        builder.apply {
            setTitle(getString(R.string.send_gift_title, selectedGift.title, target!!.name))
            setMessage(getString(R.string.send_gift_message))
            setPositiveButton(getString(R.string.confirm_yes)) { dialog, _ ->
                viewModel.sendGift(selectedGift, target)
                dialog.cancel()
            }

            setNegativeButton(getString(R.string.confirm_no)) { dialog, _ ->
                dialog.cancel()
            }
        }

        builder.create().show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogStyle)
    }

    // expanded all dialog view when keyboard pop up
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setOnShowListener {

            val bottomSheetDialog = it as BottomSheetDialog

            val parentLayout = bottomSheetDialog
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

            parentLayout?.let { it ->
                val behaviour = BottomSheetBehavior.from(it)
                setupFullHeight(it)
                behaviour.isFitToContents = false
                behaviour.expandedOffset = 900
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        return dialog
    }

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }
}
