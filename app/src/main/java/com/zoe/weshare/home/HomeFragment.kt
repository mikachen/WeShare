package com.zoe.weshare.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import com.yuyakaido.android.cardstackview.*
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.databinding.FragmentCardSwipeBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.map.CardGalleryAdapter



class HomeFragment : Fragment(){

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