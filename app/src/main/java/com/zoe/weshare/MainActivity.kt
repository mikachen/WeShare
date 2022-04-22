package com.zoe.weshare

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import com.zoe.weshare.databinding.ActivityMainBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.util.CurrentFragmentType
import com.zoe.weshare.util.FabBehavior
import com.zoe.weshare.util.Logger
import com.zoe.weshare.util.UserManager

class MainActivity : AppCompatActivity() {
// TODO 處理user點開fab main卻沒有實際點擊按鈕的情況，應該要關閉fab, 從刊登頁面返回也會壞掉

    private var subFabsExpanded: Boolean = false
    val viewModel by viewModels<MainViewModel> { getVmFactory() }

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UserManager.init(this)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.inflateMenu(R.menu.toolbar_menu)

        // lottie animation
        loginAnimate()

        // view setup
        setupNavController()
        setUpNavigateUpIcon()
        setupBottomNav()
        setupFab()
//        setUpFabBehavior()

        // observe current fragment change, only for show info
        viewModel.currentFragmentType.observe(
            this
        ) {
            Logger.i("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
            Logger.i("[${viewModel.currentFragmentType.value}]")
            Logger.i("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")

            binding.apply {
                toolbarLogoImage.visibility = View.INVISIBLE
                layoutToolbarSubtitle.visibility = View.VISIBLE
                toolbarFragmentTitleText.text = it.value


            when (it) {
                // 完全隱藏上方
                CurrentFragmentType.PROFILE -> topAppbar.visibility = View.GONE

                CurrentFragmentType.CHATROOM -> {
                    hideBottom()
                }

                //顯示副標題+倒退鍵
                CurrentFragmentType.SEARCHLOCATION -> toolbarFragmentTitleText.text = it.value

                //大主頁
                CurrentFragmentType.HOME -> {
                    toolbar.navigationIcon = null
                    toolbarLogoImage.visibility = View.VISIBLE
                    layoutToolbarSubtitle.visibility = View.INVISIBLE
                }

                CurrentFragmentType.PROFILE -> topAppbar.visibility = View.GONE


                CurrentFragmentType.POSTGIFT -> hideBottom()
                CurrentFragmentType.POSTEVENT -> hideBottom()
                CurrentFragmentType.MAP -> hideBottom()


                else -> {
                    topAppbar.visibility = View.VISIBLE
                    showBottom()
                }
            }
            }
        }
    }

    private fun setUpFabBehavior(){
        val params = binding.bottomAppBar.layoutParams as CoordinatorLayout.LayoutParams
        params.behavior = object : FabBehavior() {
            override fun onSlideDown() {
//                scan_button.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_top))
                binding.fabMain.hide()
            }

            override fun onSlideUp() {
                binding.fabMain.show()

//                scan_button.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_add))
            }
        }
    }



    private fun hideBottom() {
        binding.fabMain.hide()
        binding.bottomAppBar.performHide()
    }

    private fun showBottom() {
        binding.fabMain.show()
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

                else -> viewModel.currentFragmentType.value
            }
        }
    }

    private fun setUpNavigateUpIcon() {
        binding.toolbarArrowBackIcon.setOnClickListener {
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

                    findNavController(R.id.nav_host_fragment).navigate(NavGraphDirections.navigateToProfileFragment())
                    return@setOnItemSelectedListener true
                }
            }
            false
        }
    }

    private fun setupFab() {
        // default close
        closeSubMenusFab()

        binding.fabMain.setOnClickListener {
            if (subFabsExpanded) {
                closeSubMenusFab()
            } else {
                openSubMenusFab()
            }
            rotateFabMain()
        }

        binding.subfabPostEvent.setOnClickListener {
            findNavController(R.id.nav_host_fragment)
                .navigate(NavGraphDirections.navigateToPostEventFragment())

            closeSubMenusFab()
            rotateFabMain()
        }

        binding.subfabPostGift.setOnClickListener {
            findNavController(R.id.nav_host_fragment)
                .navigate(NavGraphDirections.navigateToPostGiftFragment())

            closeSubMenusFab()
            rotateFabMain()
        }
    }

    private fun closeSubMenusFab() {
        binding.subFabBackgroundView.visibility = View.GONE
        binding.constraintView.visibility = View.GONE

        binding.subfabPostEvent.hide()
        binding.subfabPostGift.hide()
        binding.subfabPostFavoriate.hide()

        subFabsExpanded = false
    }

    private fun openSubMenusFab() {
        /** view */
        binding.constraintView.visibility = View.VISIBLE
        binding.subFabBackgroundView.visibility = View.VISIBLE

        val animation =
            AnimationUtils.loadAnimation(
                applicationContext,
                R.anim.subfab_favorite_show
            )
        binding.subFabBackgroundView.startAnimation(animation)
        binding.subFabBackgroundView.animation.setAnimationListener(
            object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) = Unit

                override fun onAnimationEnd(p0: Animation?) {}

                override fun onAnimationRepeat(p0: Animation?) = Unit
            })

        binding.subfabPostEvent.show()
        binding.subfabPostGift.show()
        binding.subfabPostFavoriate.show()
//        binding.subfabPostEvent.startAnimation(
//            AnimationUtils.loadAnimation(
//                applicationContext,
//                R.anim.subfab_event_show
//            )
//        )
//        binding.subfabPostGift.startAnimation(
//            AnimationUtils.loadAnimation(
//                applicationContext,
//                R.anim.subfab_gift_show
//            )
//        )
//        binding.subfabPostFavoriate.startAnimation(
//            AnimationUtils.loadAnimation(
//                applicationContext,
//                R.anim.subfab_favorite_show
//            )
//        )

        subFabsExpanded = true
    }

    private fun rotateFabMain() {
        // 點選+後，旋轉變成x
        val fromDegree: Float
        val toDegree: Float

        if (subFabsExpanded) {
            // 旋轉由0度到-45度。狀態(+) -> 狀態(x)
            fromDegree = 0.0f
            toDegree = -45.0f
        } else {
            // 旋轉由-45度到0度。狀態(x) -> 狀態(+)
            fromDegree = -45.0f
            toDegree = 0.0f
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
