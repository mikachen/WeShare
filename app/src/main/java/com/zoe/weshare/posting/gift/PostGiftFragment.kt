package com.zoe.weshare.posting.gift

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.zoe.weshare.data.Author
import com.zoe.weshare.databinding.FragmentPostGiftBinding
import com.zoe.weshare.ext.getVmFactory

class PostGiftFragment : Fragment() {
    val author = Author(
        name = "Mock",
        userId = "Mock1234"
    )

    private lateinit var binding: FragmentPostGiftBinding
    private val viewModel by viewModels<PostGiftViewModel> { getVmFactory(author) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentPostGiftBinding.inflate(inflater, container, false)

        setupNextBtn()

        return binding.root
    }

    private fun setupNextBtn() {
        binding.nextButton.setOnClickListener {
            findNavController().navigate(PostGiftFragmentDirections.actionPostGiftFragmentToSearchLocationFragment())
        }
    }
}
