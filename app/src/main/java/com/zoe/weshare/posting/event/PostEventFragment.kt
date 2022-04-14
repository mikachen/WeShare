package com.zoe.weshare.posting.event

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.zoe.weshare.data.Author
import com.zoe.weshare.data.EventPost
import com.zoe.weshare.data.PostLocation
import com.zoe.weshare.databinding.FragmentPostEventBinding

class PostEventFragment : Fragment() {

    val author = Author(
        name = "Mock",
        userId = "Mock1234"
    )

    private lateinit var binding: FragmentPostEventBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentPostEventBinding.inflate(inflater, container, false)

        setupNextBtn()

        return binding.root
    }

    private fun setupNextBtn() {
        binding.nextButton.setOnClickListener {

            findNavController().navigate(PostEventFragmentDirections.actionPostEventFragmentToSearchLocationFragment(
                newEvent = EventPost(),
                newGift = null))
        }
    }
}
