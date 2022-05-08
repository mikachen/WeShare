package com.zoe.weshare

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import com.zoe.weshare.data.OperationLog
import com.zoe.weshare.databinding.ActivityMainBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.util.CurrentFragmentType
import com.zoe.weshare.util.Logger
import com.zoe.weshare.util.UserManager

class MainActivity : AppCompatActivity() {

    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.fab_rotate_open
        )
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.fab_rotate_close
        )
    }
    private val fromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.sub_fab_slide_from_bottom
        )
    }
    private val toBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.sub_fab_slide_to_bottom
        )
    }

    var notificationPageOpen: Boolean = false
    private var isFabExpend: Boolean = false
    val viewModel by viewModels<MainViewModel> { getVmFactory() }

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UserManager.init(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // lottie animation
        loginAnimate()


        // observe current fragment change, only for show info
        viewModel.currentFragmentType.observe(this)
        {
            Logger.i("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
            Logger.i("[${viewModel.currentFragmentType.value}]")
            Logger.i("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")

            showBottom()
            binding.apply {
                toolbarLogoImage.visibility = View.INVISIBLE
                topAppbar.visibility = View.VISIBLE
                layoutToolbarSubtitle.visibility = View.VISIBLE
                toolbarFragmentTitleText.text = it.value

                when (it) {
                    // 顯示副標題+倒退鍵
                    CurrentFragmentType.SEARCHLOCATION -> {
                        hideBottom()
                    }

                    // 大主頁
                    CurrentFragmentType.HOME -> {
                        toolbar.navigationIcon = null
                        toolbarLogoImage.visibility = View.VISIBLE
                        layoutToolbarSubtitle.visibility = View.INVISIBLE
                    }

                    CurrentFragmentType.MAP -> { binding.fabsLayoutView.visibility = View.GONE }
                    CurrentFragmentType.ROOMLIST -> {}
                    CurrentFragmentType.PROFILE -> topAppbar.visibility = View.GONE

                    CurrentFragmentType.CHATROOM -> {
                        hideBottom()
                        topAppbar.visibility = View.GONE
                    }
                    CurrentFragmentType.GIFTDETAIL -> { hideBottom() }
                    CurrentFragmentType.EVENTDETAIL -> { hideBottom() }
                    CurrentFragmentType.POSTGIFT -> { hideBottom() }
                    CurrentFragmentType.POSTEVENT -> { hideBottom() }
                    CurrentFragmentType.EDITPROFILE -> { hideBottom() }
                    CurrentFragmentType.NOTIFICATION -> { binding.fabsLayoutView.visibility = View.GONE }

                    CurrentFragmentType.LOGIN -> {
                        topAppbar.visibility = View.GONE
                        bottomAppBar.visibility = View.GONE
                        binding.fabsLayoutView.visibility = View.GONE
                    }

                    else -> {
                        topAppbar.visibility = View.VISIBLE
                    }
                }
            }
        }

        viewModel.reObserveNotification.observe(this) {
            it?.let {
                viewModel.liveNotifications.observe(this){ notifications->
                    notifications?.let {
                        updateBadge(notifications)
                    }
                }
            }
        }

        // view setup
        setupNavController()
        setupBottomNav()
        setupToolbar()
        setupFab()
    }

    private fun updateBadge(list: List<OperationLog>) {
        val count = list.filter { !it.read }.size

        if (count == 0) {
            binding.layoutBadge.visibility = View.INVISIBLE
        }else{
            binding.layoutBadge.visibility = View.VISIBLE
            binding.badgeCount.text = count.toString()
        }
    }

    private fun hideBottom() {
        binding.fabsLayoutView.visibility = View.GONE
        binding.bottomAppBar.performHide()
    }

    private fun showBottom() {
        binding.fabsLayoutView.visibility = View.VISIBLE
        binding.bottomAppBar.visibility = View.VISIBLE
        binding.bottomAppBar.performShow()
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
                R.id.roomListFragment -> CurrentFragmentType.ROOMLIST
                R.id.chatRoomFragment -> CurrentFragmentType.CHATROOM
                R.id.profileFragment -> CurrentFragmentType.PROFILE
                R.id.postEventFragment -> CurrentFragmentType.POSTEVENT
                R.id.postGiftFragment -> CurrentFragmentType.POSTGIFT
                R.id.eventDetailFragment -> CurrentFragmentType.EVENTDETAIL
                R.id.giftDetailFragment -> CurrentFragmentType.GIFTDETAIL
                R.id.searchLocationFragment -> CurrentFragmentType.SEARCHLOCATION
                R.id.giftManageFragment -> CurrentFragmentType.GIFTMANAGE
                R.id.notificationFragment -> CurrentFragmentType.NOTIFICATION
                R.id.loginFragment -> CurrentFragmentType.LOGIN
                R.id.editInfoFragment -> CurrentFragmentType.EDITPROFILE
                R.id.giftsAllFragment -> CurrentFragmentType.GIFTSALL
                R.id.eventsAllFragment -> CurrentFragmentType.EVENTSALL

                else -> viewModel.currentFragmentType.value
            }
            if(isFabExpend) {
                onMainFabClick()
            }
        }
    }

    private fun setupToolbar(){
        binding.notification.setOnClickListener {
            onNotificationClick()
        }

        binding.toolbarArrowBack.setOnClickListener {
            findNavController(R.id.nav_host_fragment).navigateUp()
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

                    findNavController(R.id.nav_host_fragment).navigate(NavGraphDirections.navigateToRoomlistFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.navigation_profile -> {
                    if (!UserManager.isLoggedIn) {
                        findNavController(R.id.nav_host_fragment).navigate(NavGraphDirections.actionGlobalLoginFragment())
                    } else {
                        findNavController(R.id.nav_host_fragment).navigate(NavGraphDirections.actionGlobalProfileFragment(
                            UserManager.weShareUser))
                    }
                    return@setOnItemSelectedListener true
                }
            }
            false
        }
    }

    private fun setupFab() {
        binding.fabMain.setOnClickListener {
            onMainFabClick()
        }

        binding.layoutFabEvent.setOnClickListener {
            findNavController(R.id.nav_host_fragment)
                .navigate(NavGraphDirections.navigateToPostEventFragment())
        }

        binding.layoutFabGift.setOnClickListener {
            findNavController(R.id.nav_host_fragment)
                .navigate(NavGraphDirections.navigateToPostGiftFragment())
        }
    }

    private fun onMainFabClick() {
        setVisibility(isFabExpend)
        setAnimation(isFabExpend)

        isFabExpend = !isFabExpend
    }

    private fun onNotificationClick(){
        if (!notificationPageOpen){
            findNavController(R.id.nav_host_fragment)
                .navigate(NavGraphDirections.actionGlobalNotificationFragment())
        }else{
            findNavController(R.id.nav_host_fragment).navigateUp()
        }
        notificationPageOpen = !notificationPageOpen
    }

    private fun setAnimation(isFabExpend: Boolean) {

        if (!isFabExpend) {
            binding.layoutFabGift.startAnimation(fromBottom)
            binding.layoutFabEvent.startAnimation(fromBottom)
            binding.fabMain.startAnimation(rotateOpen)
        } else {
            binding.layoutFabGift.startAnimation(toBottom)
            binding.layoutFabEvent.startAnimation(toBottom)
            binding.fabMain.startAnimation(rotateClose)
        }
    }

    private fun setVisibility(isFabExpend: Boolean) {
        if (!isFabExpend) {
            binding.layoutFabGift.visibility = View.VISIBLE
            binding.layoutFabEvent.visibility = View.VISIBLE
        }else {
            binding.layoutFabGift.visibility = View.GONE
            binding.layoutFabEvent.visibility = View.GONE
        }
    }
}
