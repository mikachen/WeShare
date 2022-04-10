package com.zoe.weshare.posting.event

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.zoe.weshare.data.Author
import com.zoe.weshare.databinding.FragmentPostEventBinding
import com.zoe.weshare.databinding.FragmentPostGiftBinding
import com.zoe.weshare.ext.getVmFactory


class PostEventFragment : Fragment() {

    val author = Author(
        name = "Mock",
        userId = "Mock1234"
    )

    private lateinit var binding: FragmentPostEventBinding
    private val viewModel by viewModels<PostEventViewModel> { getVmFactory(author) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentPostEventBinding.inflate(inflater, container, false)


        viewModel.readyToPost.observe(viewLifecycleOwner) {
            if (it) {
                viewModel.event.value?.let { event -> viewModel.newPost(event) }
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
        val itemTitle = binding.editTitle.text.toString()

        return itemTitle
    }
}
