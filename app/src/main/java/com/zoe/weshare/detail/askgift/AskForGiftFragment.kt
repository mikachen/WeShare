package com.zoe.weshare.detail.askgift

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zoe.weshare.MainActivity
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.R
import com.zoe.weshare.data.GiftPost
import com.zoe.weshare.databinding.FragmentAskForGiftBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.ext.hideKeyboard
import com.zoe.weshare.ext.sendNotificationToTarget
import com.zoe.weshare.util.UserManager.weShareUser

class AskForGiftFragment : BottomSheetDialogFragment() {

    lateinit var binding: FragmentAskForGiftBinding
    lateinit var selectedGift: GiftPost

    val viewModel by viewModels<AskForGiftViewModel> { getVmFactory(weShareUser) }

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
                sendNotificationToTarget(selectedGift.author!!.uid, it)

                findNavController().navigate(
                    NavGraphDirections.actionGlobalGiftDetailFragment(selectedGift)
                )

                viewModel.backToDetailComplete()
            }
        }

        setupBtn()
        return binding.root
    }

    private fun setupBtn() {

        binding.buttonSubmit.setOnClickListener {

            val message = binding.editLeaveComment.text.toString()

            if (message.isNotEmpty()) {
                viewModel.onSendNewRequest(message, selectedGift)
                binding.editLeaveComment.text?.clear()
            } else {
                Toast.makeText(requireContext(), "請填寫留言", Toast.LENGTH_SHORT).show()
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

            //Disable Cancel on Touch Outside & on Back press
            dialog.setCanceledOnTouchOutside(false)

            //Disable from dragging
            dialog.behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
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
