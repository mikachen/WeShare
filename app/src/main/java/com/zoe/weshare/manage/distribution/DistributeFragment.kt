package com.zoe.weshare.manage.distribution

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zoe.weshare.R
import com.zoe.weshare.databinding.FragmentDistributeBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.util.UserManager.userZoe

class DistributeFragment : BottomSheetDialogFragment() {

    val currentUser = userZoe

    lateinit var binding: FragmentDistributeBinding
    val viewModel by viewModels<DistributeViewModel> { getVmFactory(currentUser) }

    lateinit var layoutList: LinearLayout


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentDistributeBinding.inflate(inflater, container, false)

        val selectedGift = DistributeFragmentArgs.fromBundle(requireArguments()).selectedGift
        viewModel.getAskForGiftComments(selectedGift.id)

        val adapter = DistributeAdapter(viewModel)
        val manager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.VERTICAL, false)

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


        return binding.root
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogBg)
    }

//    override fun onStart() {
//        super.onStart()
//        //拿到系统的 bottom_sheet
//        val view: ConstraintLayout = binding.sheetLayout
//        //设置view高度
//        view.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
//        //获取behavior
//        val behavior = BottomSheetBehavior.from(view)
//        //设置弹出高度
//        behavior.peekHeight = 3000
//        //设置展开状态
//        behavior.state = BottomSheetBehavior.STATE_EXPANDED
//    }

    // expanded all dialog view when keyboard pop up
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)


        dialog.setOnShowListener {

            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { it ->
                val behaviour = BottomSheetBehavior.from(it)
                setupFullHeight(it)
                behaviour.isFitToContents = false
                behaviour.expandedOffset = 300
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
