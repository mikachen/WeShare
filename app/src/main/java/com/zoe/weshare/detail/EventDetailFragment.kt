package com.zoe.weshare.detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zoe.weshare.R
import com.zoe.weshare.databinding.FragmentEventDetailBinding

class EventDetailFragment : Fragment() {


    private lateinit var binding: FragmentEventDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding =  FragmentEventDetailBinding.inflate(inflater, container, false)


        return binding.root
    }

}