package com.zoe.weshare.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.zoe.weshare.databinding.FragmentEventsAllBinding

class EventsAllFragment : Fragment() {
    lateinit var binding: FragmentEventsAllBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentEventsAllBinding.inflate(inflater, container, false)


        return binding.root
    }
}