package com.zoe.weshare

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.badge.BadgeDrawable
import com.zoe.weshare.data.OperationLog
import com.zoe.weshare.databinding.ActivityMainBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.ext.hideNavigationBar
import com.zoe.weshare.ext.showNavigationBar
import com.zoe.weshare.ext.showToast
import com.zoe.weshare.network.LoadApiStatus
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

    private var isFabExpend: Boolean = false
    val viewModel by viewModels<MainViewModel> { getVmFactory() }

    private lateinit var chatRoomBadge: BadgeDrawable
    lateinit var binding: ActivityMainBinding
    private lateinit var progressBar: ProgressBar
    private lateinit var animation: ObjectAnimator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UserManager.init(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // lottie animation
        showLoginAnimate()

        if (didUserSignInBefore()) {
            val uid = GoogleSignIn.getLastSignedInAccount(applicationContext)?.id ?: ""
            viewModel.getUserLoginProfile(uid)
        } else {
            requireLogin()
        }

        viewModel.loginStatus.observe(this) {
            it?.let {
                when (it) {
                    LoadApiStatus.LOADING -> {

                        binding.preventTouchCoverView.apply {
                            setOnClickListener { }
                            visibility = View.VISIBLE
                        }
                        this.showToast(getString(R.string.toast_login_process))
                    }

                    LoadApiStatus.DONE -> {
                        binding.preventTouchCoverView.visibility = View.GONE
                        this.showToast(getString(R.string.toast_login_success))
                    }

                    LoadApiStatus.ERROR -> {
                        requireLogin()
                    }
                }
            }
        }

        // observe current fragment change, only for show info
        viewModel.currentFragmentType.observe(this) {
            Logger.i("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
            Logger.i("[${viewModel.currentFragmentType.value}]")
            Logger.i("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")

            binding.apply {
                toolbarLogoImage.visibility = View.INVISIBLE
                topAppbar.visibility = View.VISIBLE
                layoutToolbarSubtitle.visibility = View.VISIBLE
                toolbarFragmentTitleText.text = it.value

                when (it) {

                    CurrentFragmentType.HOME -> {
                        binding.bottomNavView.menu.getItem(0).isChecked = true

                        showBottomBar()
                        toolbarLogoImage.visibility = View.VISIBLE
                        layoutToolbarSubtitle.visibility = View.INVISIBLE
                    }

                    CurrentFragmentType.MAP -> {
                        binding.bottomNavView.menu.getItem(1).isChecked = true

                        showBottomBar()
                        fabsLayoutView.visibility = View.INVISIBLE
                    }

                    CurrentFragmentType.ROOMLIST -> {
                        binding.bottomNavView.menu.getItem(2).isChecked = true

                        showBottomBar()
                        fabsLayoutView.visibility = View.INVISIBLE
                    }

                    CurrentFragmentType.PROFILE -> {
                        binding.bottomNavView.menu.getItem(3).isChecked = true

                        showBottomBar()
                        fabsLayoutView.visibility = View.INVISIBLE
                        topAppbar.visibility = View.GONE
                    }

                    CurrentFragmentType.GIFTDETAIL,
                    CurrentFragmentType.EVENTDETAIL,
                    CurrentFragmentType.POSTGIFT,
                    CurrentFragmentType.POSTEVENT,
                    CurrentFragmentType.EDITPROFILE,
                    CurrentFragmentType.SEARCHLOCATION,
                    CurrentFragmentType.CHATROOM -> {
                        hideBottomBar()
                    }

                    CurrentFragmentType.GIFTSBROWSE,
                    CurrentFragmentType.EVENTSBROWSE,
                    CurrentFragmentType.NOTIFICATION,
                    CurrentFragmentType.EVENTMANAGE,
                    CurrentFragmentType.GIFTMANAGE -> {
                        showBottomBar()
                        binding.fabsLayoutView.visibility = View.INVISIBLE
                    }


                    CurrentFragmentType.HERORANK -> {
                        topAppbar.visibility = View.INVISIBLE
                        fabsLayoutView.visibility = View.INVISIBLE
                    }

                    CurrentFragmentType.LOGIN -> {
                        topAppbar.visibility = View.GONE
                        bottomAppBar.visibility = View.GONE
                        fabsLayoutView.visibility = View.GONE
                    }

                    else -> {
                        topAppbar.visibility = View.VISIBLE
                    }
                }
            }
        }

        viewModel.reObserveNotification.observe(this) {
            it?.let {
                viewModel.liveNotifications.observe(this) { notifications ->
                    notifications?.let {
                        updateNotificationBadge(notifications)
                    }
                }
            }
        }

        viewModel.reObserveChatRoom.observe(this) {
            it?.let {
                viewModel.liveChatRooms.observe(this) { chatRooms ->
                    chatRooms?.let {
                        viewModel.getUnreadRoom(chatRooms)
                    }
                }
            }
        }

        viewModel.roomBadgeCount.observe(this) {
            it?.let {
                updateUnReadMsgBadge(it)
            }
        }

        // view setup
        setupNavController()
        setupBottomNav()
        setupToolbar()
        setupFab()
        setupProgressBar()
    }

    fun requireLogin() {
        findNavController(R.id.nav_host_fragment)
            .navigate(NavGraphDirections.actionGlobalLoginFragment())
    }

    fun didUserSignInBefore(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(applicationContext) != null
    }

    private fun updateUnReadMsgBadge(count: Int) {

        if (count > 0) {
            chatRoomBadge.number = count
            chatRoomBadge.isVisible = true
        } else {
            chatRoomBadge.isVisible = false
            chatRoomBadge.clearNumber()
        }
    }

    private fun updateNotificationBadge(list: List<OperationLog>) {
        val count = list.filter { !it.read }.size

        if (count == 0) {
            binding.layoutNotificationBadge.visibility = View.INVISIBLE
        } else {
            binding.layoutNotificationBadge.visibility = View.VISIBLE
            binding.badgeCount.text = count.toString()
        }
    }

    private fun hideBottomBar() {
        binding.fabsLayoutView.visibility = View.INVISIBLE
        binding.bottomAppBar.performHide()
    }

    private fun showBottomBar() {
        binding.fabsLayoutView.visibility = View.VISIBLE
        binding.bottomAppBar.visibility = View.VISIBLE
        binding.bottomAppBar.performShow()
    }

    private fun showLoginAnimate() {
        startActivity(
            Intent(this, LogoActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            }
        )
    }

    private fun setupNavController() {
        findNavController(R.id.nav_host_fragment).addOnDestinationChangedListener { navController: NavController, _: NavDestination, _: Bundle? ->

            if (isFabExpend) {
                onMainFabClick()
            }

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
                R.id.eventManageFragment -> CurrentFragmentType.EVENTMANAGE
                R.id.notificationFragment -> CurrentFragmentType.NOTIFICATION
                R.id.loginFragment -> CurrentFragmentType.LOGIN
                R.id.editInfoFragment -> CurrentFragmentType.EDITPROFILE
                R.id.giftsBrowseFragment -> CurrentFragmentType.GIFTSBROWSE
                R.id.eventsBrowseFragment -> CurrentFragmentType.EVENTSBROWSE
                R.id.eventCheckInFragment -> CurrentFragmentType.EVENTCHECKIN
                R.id.heroRankFragment -> CurrentFragmentType.HERORANK

                else -> viewModel.currentFragmentType.value
            }
        }
    }

    private fun setupToolbar() {
        binding.notification.setOnClickListener {
            findNavController(R.id.nav_host_fragment)
                .navigate(NavGraphDirections.actionGlobalNotificationFragment())
        }

        binding.toolbarArrowBack.setOnClickListener {
            findNavController(R.id.nav_host_fragment).navigateUp()
        }

    }

    private fun setupProgressBar() {
        progressBar = binding.progressBar
        progressBar.max = 100 * 100
    }

    private fun setupBottomNav() {
        binding.bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    findNavController(R.id.nav_host_fragment).navigate(
                        NavGraphDirections.navigateToHomeFragment()
                    )
                    return@setOnItemSelectedListener true
                }
                R.id.navigation_map -> {

                    findNavController(R.id.nav_host_fragment).navigate(
                        NavGraphDirections.navigateToMapFragment()
                    )
                    return@setOnItemSelectedListener true
                }
                R.id.navigation_messages -> {

                    findNavController(R.id.nav_host_fragment).navigate(
                        NavGraphDirections.navigateToRoomlistFragment()
                    )

                    return@setOnItemSelectedListener true
                }
                R.id.navigation_profile -> {
                    findNavController(R.id.nav_host_fragment).navigate(
                        NavGraphDirections.actionGlobalProfileFragment(
                            UserManager.weShareUser
                        )
                    )
                    return@setOnItemSelectedListener true
                }
            }
            false
        }

        chatRoomBadge = binding.bottomNavView.getOrCreateBadge(R.id.navigation_messages)
    }

    private fun setupFab() {
        binding.fabMain.setOnClickListener {
            onMainFabClick()
        }

        binding.layoutFabEvent.setOnClickListener {
            onMainFabClick()
            findNavController(R.id.nav_host_fragment)
                .navigate(NavGraphDirections.navigateToPostEventFragment())
        }

        binding.layoutFabGift.setOnClickListener {
            onMainFabClick()
            findNavController(R.id.nav_host_fragment)
                .navigate(NavGraphDirections.navigateToPostGiftFragment())
        }
    }

    fun onMainFabClick() {
        setVisibility(isFabExpend)
        setAnimation(isFabExpend)

        isFabExpend = !isFabExpend
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
        } else {
            binding.layoutFabGift.visibility = View.INVISIBLE
            binding.layoutFabEvent.visibility = View.INVISIBLE
        }
    }

    fun showProgressBar() {

        binding.layoutMainProgressBar.visibility = View.VISIBLE
        binding.imageUploadingHint.visibility = View.VISIBLE

        hideNavigationBar()
    }

    fun hideProgressBar() {
        binding.layoutMainProgressBar.visibility = View.INVISIBLE
        binding.imageUploadingHint.visibility = View.INVISIBLE

        progressBarLoading(0) // reset
        showNavigationBar()
    }

    fun progressBarLoading(progress: Int) {

        animation = ObjectAnimator.ofInt(
            progressBar,
            "progress",
            progressBar.progress,
            progress * 100
        )
        animation.duration = 300
        animation.interpolator = DecelerateInterpolator()
        animation.start()
    }
}
