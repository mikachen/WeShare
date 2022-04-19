package com.zoe.weshare.detail.commenting

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
import com.zoe.weshare.databinding.FragmentCommentDialogBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.util.Util.author

class CommentDialogFragment : BottomSheetDialogFragment() {

    lateinit var binding: FragmentCommentDialogBinding
    val viewModel by viewModels<CommentViewModel> { getVmFactory(author) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val docId = CommentDialogFragmentArgs.fromBundle(requireArguments()).documentId

        binding = FragmentCommentDialogBinding.inflate(inflater, container, false)

        viewModel.newComment.observe(viewLifecycleOwner) {
            if (it != null) {
                viewModel.sendComment(docId, it)
                viewModel.onNavigateBackToEventDetail()
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
        binding.buttonSendComment.setOnClickListener {
            // TODO 判斷是否登入

            val message = binding.editLeaveComment.text

            if (message != null) {
                if (message.isNotEmpty()) {
                    viewModel.onSendNewComment(message.toString())
                    message.clear()
                } else {
                    Toast.makeText(requireContext(), "請填寫留言", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 鍵盤彈出後，畫面拉長上推畫面 android:windowSoftInputMode = adjustResize
        setStyle(STYLE_NORMAL, R.style.DialogStyle)
    }
}
