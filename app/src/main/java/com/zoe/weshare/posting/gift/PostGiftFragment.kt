package com.zoe.weshare.posting.gift

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.zoe.weshare.R
import com.zoe.weshare.data.Author
import com.zoe.weshare.data.GiftPost
import com.zoe.weshare.databinding.FragmentPostGiftBinding
import com.zoe.weshare.ext.getVmFactory

class PostGiftFragment : Fragment() {
    val author = Author(
        name = "Zoe Lo",
        uid = "zoe1018",
        image = "https://www.computerhope.com/jargon/a/android.png"
    )

    private lateinit var binding: FragmentPostGiftBinding
    val viewModel by viewModels<PostGiftViewModel> { getVmFactory(author) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentPostGiftBinding.inflate(inflater, container, false)

        viewModel.gift.observe(viewLifecycleOwner) {
            findNavController().navigate(PostGiftFragmentDirections.actionPostGiftFragmentToSearchLocationFragment(
                newGift = it,
                newEvent = null))
            Log.d("giftObs","$it")
        }

        setupNextBtn()
        setupDropdownMenu()

        return binding.root
    }

    private fun setupNextBtn() {
        binding.nextButton.setOnClickListener {
            dataCollecting()
        }
    }

    private fun dataCollecting() {

        val title = binding.editTitle.text.toString()
        val sort = binding.dropdownMenuSort.text.toString()
        val condition = binding.dropdownMenuCondition.text.toString()
        val description = binding.editDescription.text.toString()

        when(true){
            title.isEmpty() -> Toast.makeText(requireContext(), "title.isEmpty", Toast.LENGTH_SHORT).show()
            sort.isEmpty() -> Toast.makeText(requireContext(), "sort.isEmpty", Toast.LENGTH_SHORT).show()
            condition.isEmpty() -> Toast.makeText(requireContext(), "condition.isEmpty", Toast.LENGTH_SHORT).show()
            description.isEmpty() -> Toast.makeText(requireContext(), "description.isEmpty", Toast.LENGTH_SHORT).show()

            else -> viewModel._gift.value = GiftPost(
                author = author,
                title = title,
                sort = sort,
                condition = condition,
                description = description
            )
        }
    }

    private fun setupDropdownMenu() {
        val sortsString = resources.getStringArray(R.array.gift_item_sort)
        val conditionString = resources.getStringArray(R.array.gift_item_condition)


        val sortAdapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_list_item_1, sortsString)
        binding.dropdownMenuSort.setAdapter(sortAdapter)

        val conditionAdapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_list_item_1, conditionString)
        binding.dropdownMenuCondition.setAdapter(conditionAdapter)

    }
}
