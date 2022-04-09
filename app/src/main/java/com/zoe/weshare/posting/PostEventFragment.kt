package com.zoe.weshare.posting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zoe.weshare.databinding.FragmentPostEventBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class PostEventFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        val binding = FragmentPostEventBinding.inflate(inflater, container, false)
        return binding.root

    }
}