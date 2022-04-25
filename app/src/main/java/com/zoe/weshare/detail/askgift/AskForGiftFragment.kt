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
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.R
import com.zoe.weshare.data.GiftPost
import com.zoe.weshare.databinding.FragmentAskForGiftBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.network.LoadApiStatus
import com.zoe.weshare.util.UserManager

class AskForGiftFragment : BottomSheetDialogFragment() {

    lateinit var binding: FragmentAskForGiftBinding
    private val currentUser = UserManager.userLora
    lateinit var selectedGift: GiftPost


    val viewModel by viewModels<AskForGiftViewModel> { getVmFactory(currentUser) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        selectedGift = AskForGiftFragmentArgs.fromBundle(requireArguments()).selectedGift
        binding = FragmentAskForGiftBinding.inflate(inflater, container, false)


        viewModel.newRequestComment.observe(viewLifecycleOwner){
            viewModel.askForGiftRequest(selectedGift, it)
        }
       viewModel.saveLogComplete.observe(viewLifecycleOwner){
           if(it == LoadApiStatus.DONE) {
               findNavController().navigate(
                   NavGraphDirections.actionGlobalGiftDetailFragment(selectedGift))
               Toast.makeText(requireContext(),"save log success",Toast.LENGTH_SHORT).show()
           }
       }

        setupBtn()
        return binding.root
    }


    private fun setupBtn() {
        binding.buttonSubmit.setOnClickListener {
            // TODO 判斷是否登入

            val message = binding.editLeaveComment.text.toString()

            if (message.isNotEmpty()) {
                viewModel.onSendNewRequest(message , selectedGift)
                binding.editLeaveComment.text?.clear()
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

    // expanded all dialog view when keyboard pop up
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        if (dialog is BottomSheetDialog) {
            dialog.behavior.skipCollapsed = true
            dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }
}
