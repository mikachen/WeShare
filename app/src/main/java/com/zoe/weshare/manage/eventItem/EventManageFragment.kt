package com.zoe.weshare.manage.eventItem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.zoe.weshare.databinding.FragmentEventManageBinding

class EventManageFragment : Fragment() {

    lateinit var binding: FragmentEventManageBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentEventManageBinding.inflate(inflater, container, false)




        return binding.root
    }
}