package com.zoe.weshare

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.zoe.weshare.databinding.ActivityMainBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.util.CurrentFragmentType
import com.zoe.weshare.util.Logger

class MainActivity : AppCompatActivity() {

    val viewModel by viewModels<MainViewModel> { getVmFactory() }


    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //lottie animation
        loginAnimate()


        // observe current fragment change, only for show info
        viewModel.currentFragmentType.observe(
            this
        ) {
            binding.toolbarTitle.text = it.value
            Logger.i("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
            Logger.i("[${viewModel.currentFragmentType.value}]")
            Logger.i("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
        }

        setupNavController()
        setupBottomNav()
        setupFab()
    }

    private fun loginAnimate() {
        startActivity(
            Intent(this, LogoActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            }
        )
    }

    // 每導航新頁面重新assign currentFragmentType
    private fun setupNavController() {
        findNavController(R.id.nav_host_fragment).addOnDestinationChangedListener { navController: NavController, _: NavDestination, _: Bundle? ->
            viewModel.currentFragmentType.value = when (navController.currentDestination?.id) {
                R.id.homeFragment -> CurrentFragmentType.HOME
                R.id.mapFragment -> CurrentFragmentType.MAP
                R.id.messageFragment -> CurrentFragmentType.MESSAGE
                R.id.profileFragment -> CurrentFragmentType.PROFILE
                R.id.postEventFragment -> CurrentFragmentType.POSTEVENT
                R.id.postGiftFragment -> CurrentFragmentType.POSTGIFT


                else -> viewModel.currentFragmentType.value
            }
        }
    }

    private fun setupBottomNav() {
        binding.bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {

                    findNavController(R.id.nav_host_fragment).navigate(NavGraphDirections.navigateToHomeFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.navigation_map -> {

                    findNavController(R.id.nav_host_fragment).navigate(NavGraphDirections.navigateToMapFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.navigation_messages -> {

                    findNavController(R.id.nav_host_fragment).navigate(NavGraphDirections.navigateToMessageFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.navigation_profile -> {

                    findNavController(R.id.nav_host_fragment).navigate(NavGraphDirections.navigateToProfileFragment())
                    return@setOnItemSelectedListener true
                }
            }
            false
        }
    }

    private fun setupFab(){

        binding.subfabPostEvent.setOnClickListener {
            findNavController(R.id.nav_host_fragment).navigate(NavGraphDirections.navigateToPostEventFragment())
        }
        binding.subfabPostGift.setOnClickListener {
            findNavController(R.id.nav_host_fragment).navigate(NavGraphDirections.navigateToPostGiftFragment())
        }
    }
}