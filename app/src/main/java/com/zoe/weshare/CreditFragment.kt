package com.zoe.weshare

import android.animation.Animator
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zoe.weshare.databinding.FragmentCreditBinding

class CreditFragment : BottomSheetDialogFragment() {

    var animationStatus: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val binding = FragmentCreditBinding.inflate(inflater,container,false)

        binding.lottieMain.playAnimation()
        binding.lottieMain.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
                animationStatus = true
            }

            override fun onAnimationEnd(animation: Animator?) {
                dismiss()
                binding.lottieMain.visibility = View.GONE
                animationStatus = false
            }

            override fun onAnimationCancel(animation: Animator?) {
                animationStatus = false
            }

            override fun onAnimationStart(animation: Animator?) {
                binding.lottieMain.visibility = View.VISIBLE
                animationStatus = true
            }
        })
        binding.lottieMain.setOnClickListener(
            this::leave
        )

        return binding.root

    }

    fun leave(view: View) {
        dismiss()
    }

    // expanded all dialog view when keyboard pop up
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setOnShowListener {

            val bottomSheetDialog = it as BottomSheetDialog

            val parentLayout = bottomSheetDialog
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

            parentLayout?.let { view ->
                val behaviour = BottomSheetBehavior.from(view)

                setupFullHeight(view)

                behaviour.isFitToContents = false
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogBg)
    }

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }
}



