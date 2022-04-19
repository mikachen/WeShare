package com.zoe.weshare.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.zoe.weshare.databinding.FragmentCardSwipeBinding
import com.zoe.weshare.ext.getVmFactory


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentCardSwipeBinding


    val viewModel by viewModels<HomeViewModel> { getVmFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentCardSwipeBinding.inflate(inflater, container, false)



        return binding.root
    }
}