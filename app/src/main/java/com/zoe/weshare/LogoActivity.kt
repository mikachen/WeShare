package com.zoe.weshare

import android.animation.Animator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.zoe.weshare.databinding.ActivityLogoBinding

class LogoActivity : AppCompatActivity() {
    var animationStatus: Boolean = false

    lateinit var binding: ActivityLogoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLogoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lottieMain.repeatCount = 5
        binding.lottieMain.playAnimation()
        binding.lottieMain.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
                animationStatus = true
            }

            override fun onAnimationEnd(animation: Animator?) {
                finish()
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
    }

    public override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0)
    }

    fun leave(view: View) {
        finish()
    }
}