package com.zoe.weshare.herorank

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.R
import com.zoe.weshare.data.UserInfo
import com.zoe.weshare.data.UserProfile
import com.zoe.weshare.databinding.FragmentHeroRankBinding
import com.zoe.weshare.ext.bindImage
import com.zoe.weshare.ext.getVmFactory


class HeroRankFragment : Fragment() {

    private lateinit var binding: FragmentHeroRankBinding
    private lateinit var adapter: HeroRankAdapter
    private lateinit var manager: LinearLayoutManager
    private lateinit var recyclerView: RecyclerView

    private val viewModel by viewModels<HeroRankViewModel> { getVmFactory() }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentHeroRankBinding.inflate(inflater, container, false)


        viewModel.ranking.observe(viewLifecycleOwner) {
            setupView(it)
            adapter.modifyList(it)
        }

        viewModel.selectedUser.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(
                    NavGraphDirections.actionGlobalProfileFragment(UserInfo(uid = it.uid)))
                viewModel.onNavigateComplete()
            }
        }


        return binding.root
    }

    fun setupView(users: List<UserProfile>) {

        adapter = HeroRankAdapter(
            HeroRankAdapter.HeroOnClickListener { selectedUser ->
                viewModel.onNavigateToUserProfile(selectedUser)
            }
        )

        manager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL, false
        )

        recyclerView = binding.heroRecyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = manager

        val firstPlace = users[0]
        val secondPlace = users[1]
        val thirdPlace = users[2]

        binding.apply {
            bindImage(imageUserImage1, firstPlace.image)
            textUserName1.text = firstPlace.name
            textContribution1.text =
                getString(R.string.hero_contribution, firstPlace.contribution?.totalContribution)

            bindImage(imageUserImage2, secondPlace.image)
            textUserName2.text = secondPlace.name
            textContribution2.text =
                getString(R.string.hero_contribution, secondPlace.contribution?.totalContribution)

            bindImage(imageUserImage3, thirdPlace.image)
            textUserName3.text = thirdPlace.name
            textContribution3.text =
                getString(R.string.hero_contribution, thirdPlace.contribution?.totalContribution)

            first.setOnClickListener {
                findNavController().navigate(
                    NavGraphDirections.actionGlobalProfileFragment(UserInfo(uid = firstPlace.uid)))
            }

            second.setOnClickListener {
                findNavController().navigate(
                    NavGraphDirections.actionGlobalProfileFragment(UserInfo(uid = secondPlace.uid)))
            }

            third.setOnClickListener {
                findNavController().navigate(
                    NavGraphDirections.actionGlobalProfileFragment(UserInfo(uid = thirdPlace.uid)))
            }
        }
        binding.toolbarArrowBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}