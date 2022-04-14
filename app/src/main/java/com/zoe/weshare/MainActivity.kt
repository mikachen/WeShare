package com.zoe.weshare

import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import com.zoe.weshare.databinding.ActivityMainBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.util.CurrentFragmentType
import com.zoe.weshare.util.Logger

class MainActivity : AppCompatActivity() {
// TODO 處理user點開fab main卻沒有實際點擊按鈕的情況，應該要關閉fab, 從刊登頁面返回也會壞掉

    var subFabsExpanded: Boolean = false
    val viewModel by viewModels<MainViewModel> { getVmFactory() }

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // lottie animation
        loginAnimate()

        // view setup
        setupNavController()
        setupBottomNav()
        setupFab()

        // observe current fragment change, only for show info
        viewModel.currentFragmentType.observe(
            this
        ) {
            binding.toolbarTitle.text = it.value

            Logger.i("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
            Logger.i("[${viewModel.currentFragmentType.value}]")
            Logger.i("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")

            if (it == CurrentFragmentType.POSTGIFT || it == CurrentFragmentType.POSTEVENT) {
                binding.fabMain.hide()
                binding.bottomAppBar.performHide()
            } else {
                binding.fabMain.show()
                binding.bottomAppBar.performShow()
            }
        }
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
                R.id.eventDetailFragment -> CurrentFragmentType.EVENTDETAIL
                R.id.giftDetailFragment -> CurrentFragmentType.GIFTDETAIL
                R.id.searchLocationFragment -> CurrentFragmentType.SEARCHLOCATION

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

    private fun setupFab() {
        closeSubMenusFab()

        binding.fabMain.setOnClickListener {

            if (subFabsExpanded) {
                closeSubMenusFab()
            } else {
                openSubMenusFab()
            }
        }

        binding.subfabPostEvent.setOnClickListener {
            findNavController(R.id.nav_host_fragment).navigate(NavGraphDirections.navigateToPostEventFragment())
            closeSubMenusFab()
            rotateFabMain()
        }
        binding.subfabPostGift.setOnClickListener {
            findNavController(R.id.nav_host_fragment).navigate(NavGraphDirections.navigateToPostGiftFragment())
            closeSubMenusFab()
            rotateFabMain()
        }
    }

    private fun closeSubMenusFab() {

        binding.subfabPostEvent.hide()
        binding.subfabPostGift.hide()
        binding.subfabPostFavoriate.hide()

        subFabsExpanded = false
    }

    private fun openSubMenusFab() {
        rotateFabMain()

        binding.subfabPostEvent.show()
        binding.subfabPostGift.show()
        binding.subfabPostFavoriate.show()
        binding.subfabPostEvent.startAnimation(
            AnimationUtils.loadAnimation(
                applicationContext,
                R.anim.subfab_event_show
            )
        )
        binding.subfabPostGift.startAnimation(
            AnimationUtils.loadAnimation(
                applicationContext,
                R.anim.subfab_gift_show
            )
        )
        binding.subfabPostFavoriate.startAnimation(
            AnimationUtils.loadAnimation(
                applicationContext,
                R.anim.subfab_favorite_show
            )
        )

        subFabsExpanded = true
    }

    private fun rotateFabMain() {
        // 點選+後，旋轉變成x
        val fromDegree: Float
        val toDegree: Float

        if (subFabsExpanded) {
            // 旋轉由-45度到0度。開啟狀態(x) -> 關閉狀態(+)
            fromDegree = -45.0f
            toDegree = 0.0f
        } else {
            // 旋轉由0度到-45度。開啟狀態(x) -> 關閉狀態(+)
            fromDegree = 0.0f
            toDegree = -45.0f
        }

        val animRotate = RotateAnimation(
            fromDegree, toDegree,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )

        animRotate.duration = 300
        animRotate.fillAfter = true

        binding.fabMain.startAnimation(animRotate)
    }
}
