package com.zoe.weshare.posting.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.zoe.weshare.R
import com.zoe.weshare.data.EventPost
import com.zoe.weshare.databinding.FragmentPostEventBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.util.UserManager.author

class PostEventFragment : Fragment() {

    private lateinit var binding: FragmentPostEventBinding
    val viewModel by viewModels<PostEventViewModel> { getVmFactory(author) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentPostEventBinding.inflate(inflater, container, false)

        viewModel.event.observe(viewLifecycleOwner) {
            findNavController().navigate(
                PostEventFragmentDirections.actionPostEventFragmentToSearchLocationFragment(
                    newEvent = it,
                    newGift = null
                )
            )
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
        val volunteerNeeds = binding.editVolunteer.text.toString()
        val description = binding.editDescription.text.toString()

        when (true) {
            title.isEmpty() -> Toast.makeText(requireContext(), "title.isEmpty", Toast.LENGTH_SHORT)
                .show()
            sort.isEmpty() -> Toast.makeText(requireContext(), "sort.isEmpty", Toast.LENGTH_SHORT)
                .show()
            volunteerNeeds.isEmpty() -> Toast.makeText(requireContext(), "sort.isEmpty", Toast.LENGTH_SHORT)
                .show()
            description.isEmpty() -> Toast.makeText(
                requireContext(),
                "description.isEmpty",
                Toast.LENGTH_SHORT
            ).show()

            else -> viewModel._event.value = EventPost(
                author = author,
                title = title,
                sort = sort,
                volunteerNeeds = volunteerNeeds.toInt(),
                description = description
            )
        }
    }

    private fun setupDropdownMenu() {
        val sortsString = resources.getStringArray(R.array.event_type_sort)

        val sortAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1, sortsString
        )

        binding.dropdownMenuSort.setAdapter(sortAdapter)
    }
}
