package com.zoe.weshare.detail.askgift

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zoe.weshare.R
import com.zoe.weshare.data.Author
import com.zoe.weshare.databinding.FragmentAskForGiftBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.util.Util.author

class AskForGiftFragment : BottomSheetDialogFragment() {

    lateinit var binding: FragmentAskForGiftBinding
    val viewModel by viewModels<AskForGiftViewModel> { getVmFactory(author) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val docId = AskForGiftFragmentArgs.fromBundle(requireArguments()).documentId

        binding = FragmentAskForGiftBinding.inflate(inflater, container, false)

        viewModel.newRequest.observe(viewLifecycleOwner) {
            if (it != null) {
                viewModel.askForGift(docId, it)
                viewModel.onNavigateBackToGiftDetail()
            } else {
                findNavController().navigateUp()
            }
        }

        setUpBtn()
        return binding.root
    }

    // expanded all dialog view when keyboard pop up
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        if (dialog is BottomSheetDialog) {
            dialog.behavior.skipCollapsed = true
            dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }

    private fun setUpBtn() {
        binding.buttonSubmit.setOnClickListener {
            // TODO 判斷是否登入

            val message = binding.editLeaveComment.text

            if (message != null) {
                if (message.isNotEmpty()) {
                    viewModel.onSendNewRequest(message.toString())
                    message.clear()
                } else {
                    Toast.makeText(requireContext(), "請填寫留言", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.buttonCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 鍵盤彈出後，畫面拉長上推畫面 android:windowSoftInputMode = adjustResize
        setStyle(STYLE_NORMAL, R.style.DialogStyle)
    }
}
