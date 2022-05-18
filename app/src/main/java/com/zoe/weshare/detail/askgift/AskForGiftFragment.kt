package com.zoe.weshare.detail.askgift

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zoe.weshare.R
import com.zoe.weshare.data.GiftPost
import com.zoe.weshare.databinding.FragmentAskForGiftBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.ext.hideKeyboard
import com.zoe.weshare.ext.sendNotificationToTarget
import com.zoe.weshare.ext.showToast
import com.zoe.weshare.util.UserManager.weShareUser

class AskForGiftFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentAskForGiftBinding
    private lateinit var selectedGift: GiftPost

    private val viewModel by viewModels<AskForGiftViewModel> { getVmFactory(weShareUser) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        selectedGift = AskForGiftFragmentArgs.fromBundle(requireArguments()).selectedGift
        binding = FragmentAskForGiftBinding.inflate(inflater, container, false)

        viewModel.newRequestComment.observe(viewLifecycleOwner) {
            viewModel.askForGiftRequest(selectedGift, it)
        }

        viewModel.saveLogComplete.observe(viewLifecycleOwner) {
            it?.let {
                binding.editLeaveComment.text?.clear()

                sendNotificationToTarget(selectedGift.author!!.uid, it)

                viewModel.requestGiftComplete()

                this.dismiss()
            }
        }

        setupBtn()
        return binding.root
    }

    private fun setupBtn() {

        binding.buttonSubmit.setOnClickListener {

            val message = binding.editLeaveComment.text.toString().trim()

            if (message.isNotEmpty()) {
                viewModel.onSendNewRequest(message, selectedGift)
            } else {
                activity.showToast(getString(R.string.pls_leave_askforgift_comment))
            }

            it.hideKeyboard()
        }

        binding.buttonCancel.setOnClickListener {
            findNavController().navigateUp()

            it.hideKeyboard()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NORMAL, R.style.DialogStyle)
    }

    // expanded all dialog view when keyboard pop up
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        if (dialog is BottomSheetDialog) {
            dialog.behavior.skipCollapsed = true
            dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

            // Disable Cancel on Touch Outside & on Back press
            dialog.setCanceledOnTouchOutside(false)

            // Disable from dragging
            dialog.behavior.addBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                }
            })
        }
        return dialog
    }
}
