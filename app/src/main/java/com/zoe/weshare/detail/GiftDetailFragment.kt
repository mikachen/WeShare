package com.zoe.weshare.detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zoe.weshare.databinding.FragmentGiftDetailBinding

class GiftDetailFragment : Fragment() {

    private lateinit var binding: FragmentGiftDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding =  FragmentGiftDetailBinding.inflate(inflater, container, false)


        return binding.root
    }

}