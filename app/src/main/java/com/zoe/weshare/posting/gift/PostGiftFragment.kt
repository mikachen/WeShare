package com.zoe.weshare.posting.gift

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.zoe.weshare.data.Author
import com.zoe.weshare.databinding.FragmentPostGiftBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.posting.event.PostEventViewModel


class PostGiftFragment : Fragment() {
    val author = Author(
        name = "Mock",
        userId = "Mock1234"
    )

    private lateinit var binding: FragmentPostGiftBinding
    private val viewModel by viewModels<PostGiftViewModel> { getVmFactory(author) }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentPostGiftBinding.inflate(inflater, container, false)

        viewModel.readyToPost.observe(viewLifecycleOwner) {
            if (it) {
                viewModel.gift.value?.let { gift -> viewModel.newPost(gift) }
            }else{
                Toast.makeText(activity,"請檢查資料", Toast.LENGTH_SHORT).show()
            }
        }


        setupNextBtn()

        return binding.root
    }

    private fun setupNextBtn() {
        binding.nextButton.setOnClickListener {
            viewModel.updateTitle(collectData())
        }
    }

    private fun collectData(): String {
        val title = binding.editTitle.text.toString()

        return title
    }
}