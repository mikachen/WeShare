package com.zoe.weshare.report

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zoe.weshare.R
import com.zoe.weshare.databinding.DialogReportViolationBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.ext.hideKeyboard
import com.zoe.weshare.ext.showDropdownMenu
import com.zoe.weshare.ext.showToast
import com.zoe.weshare.util.UserManager.weShareUser
import java.util.*

class ReportViolationDialog : BottomSheetDialogFragment() {

    private lateinit var binding: DialogReportViolationBinding
    private val viewModel by viewModels<ReportViewModel> { getVmFactory(weShareUser) }
    private lateinit var sortAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = DialogReportViolationBinding.inflate(inflater, container, false)

        val targetUid = ReportViolationDialogArgs.fromBundle(requireArguments()).reportTarget

        viewModel.setTargetUid(targetUid)

        viewModel.reportSendComplete.observe(viewLifecycleOwner) {
            it?.let {
                activity.showToast(getString(R.string.toast_report_send_complete))
                binding.editLeaveReason.text?.clear()

                viewModel.reportComplete()
                this.dismiss()
            }
        }

        setupBtn()
        setupDropdownMenu()
        return binding.root
    }

    private fun setupBtn() {
        binding.buttonSubmit.setOnClickListener {
            dataCollecting()
            it.hideKeyboard()
        }

        binding.buttonCancel.setOnClickListener {
            findNavController().navigateUp()
            it.hideKeyboard()
        }
    }

    private fun dataCollecting() {
        val reason = binding.editLeaveReason.text.toString().trim()
        val sort = binding.dropdownMenuSort.text.toString().trim()

        val isCollectComplete = verifyData(reason,sort)

        if (isCollectComplete) {
            viewModel.onSendReport(reason,sort)
        }
    }

    private fun verifyData(reason: String, sort:String): Boolean {

        return if (reason.isEmpty()) {
            activity.showToast(getString(R.string.toast_leave_report_reason))
            false
        } else (if(sort.isEmpty()){
            activity.showToast(getString(R.string.toast_choose_report_flag))
            false
        } else  {
            true
        })
    }

    private fun setupDropdownMenu() {

        val sortsString = resources.getStringArray(R.array.violation_sort)

        sortAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1, sortsString
        )

        binding.dropdownMenuSort.apply {
            setAdapter(sortAdapter)

            setOnClickListener {
                this.hideKeyboard()
                this.showDropdownMenu(sortAdapter)
            }
        }


        sortAdapter.setNotifyOnChange(true)
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