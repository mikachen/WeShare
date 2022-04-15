package com.zoe.weshare.detail.gift

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zoe.weshare.R
import com.zoe.weshare.data.Author
import com.zoe.weshare.data.Comment
import com.zoe.weshare.databinding.FragmentAskForGiftBinding
import com.zoe.weshare.ext.getVmFactory


class AskForGiftFragment : BottomSheetDialogFragment() {

    val author = Author(
        name = "Kelyie Chen",
        uid = "kelly0808",
        image = "https://hips.hearstapps.com/hmg-prod.s3.amazonaws.com/images/hbz-grace-kelly-1950s-gettyimages-517423148-1569860702.jpg"
    )

    lateinit var binding: FragmentAskForGiftBinding
    val viewModel by viewModels<AskForGiftViewModel> { getVmFactory(author) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val docId = AskForGiftFragmentArgs.fromBundle(requireArguments()).documentId

        binding = FragmentAskForGiftBinding.inflate(inflater, container, false)


        viewModel.comment.observe(viewLifecycleOwner) {
            viewModel.askForGift(docId, it)
        }

        setUpBtn()
        return binding.root
    }

    // expanded all dialog view when keyboard pop up
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        if (dialog is BottomSheetDialog) {
            dialog.behavior.skipCollapsed =true
            dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }

    private fun setUpBtn() {
        binding.buttonSubmit.setOnClickListener {

            val message = binding.editLeaveComment.text.toString()

            if (message.isNotEmpty()) {
                viewModel._comment.value = Comment(
                    uid = author.uid,
                    content = message
                )
            } else {
                Toast.makeText(requireContext(), "請填寫留言", Toast.LENGTH_SHORT).show()
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