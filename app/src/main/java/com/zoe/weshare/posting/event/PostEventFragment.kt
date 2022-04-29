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
import com.google.android.material.datepicker.MaterialDatePicker
import com.zoe.weshare.R
import com.zoe.weshare.databinding.FragmentPostEventBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.util.UserManager.weShareUser

class PostEventFragment : Fragment() {

    val currentUser = weShareUser

    private lateinit var binding: FragmentPostEventBinding
    val viewModel by viewModels<PostEventViewModel> { getVmFactory(currentUser) }

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

        viewModel.datePick.observe(viewLifecycleOwner) {
            binding.editDatePicker.setText(it)
        }

        setupNextBtn()
        setupDropdownMenu()
        setupDatePicker()

        return binding.root
    }

    private fun setupNextBtn() {
        binding.nextButton.setOnClickListener {
            onDataChecking()
        }
    }

    private fun onDataChecking() {

        val title = binding.editTitle.text.toString()
        val sort = binding.dropdownMenuSort.text.toString()
        val volunteerNeeds = binding.editVolunteer.text.toString()
        val description = binding.editDescription.text.toString()
        val time = binding.editDatePicker.text.toString()

        when (true) {
            title.isEmpty() -> Toast.makeText(
                requireContext(),
                getString(R.string.error_title_isEmpty),
                Toast.LENGTH_SHORT
            )
                .show()
            sort.isEmpty() -> Toast.makeText(
                requireContext(),
                getString(R.string.error_sort_isEmpty),
                Toast.LENGTH_SHORT
            )
                .show()
            volunteerNeeds.isEmpty() -> Toast.makeText(
                requireContext(),
                getString(R.string.error_volunteer_need),
                Toast.LENGTH_SHORT
            )
                .show()
            description.isEmpty() -> Toast.makeText(
                requireContext(),
                getString(R.string.error_description_isEmpty),
                Toast.LENGTH_SHORT
            ).show()

            time.isEmpty() -> Toast.makeText(
                requireContext(),
                getString(R.string.error_date_pick_isEmpty),
                Toast.LENGTH_SHORT
            ).show()

            else -> viewModel.onNavigateSearchLocation(title, sort, volunteerNeeds, description)
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

    private fun setupDatePicker() {

        val dateRangePicker =
            MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select dates")
                .build()

        dateRangePicker.addOnPositiveButtonClickListener { datePick ->

            val startDate = datePick.first
            val secondDate = datePick.second

            viewModel.onDatePickDisplay(startDate, secondDate)
        }

        binding.editDatePicker.setOnClickListener {
            dateRangePicker.show(requireActivity().supportFragmentManager, "date_range_picker")
        }
    }
}
