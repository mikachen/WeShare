package com.zoe.weshare.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.zoe.weshare.data.UserInfo
import com.zoe.weshare.databinding.FragmentUsersBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.profile.userself.SelfViewModel

//TODO 這邊應要顯示 瀏覽其他人的profile :userArg 外部傳入
class UsersFragment : Fragment() {


    lateinit var binding: FragmentUsersBinding

    lateinit var userArg: UserInfo


    val viewModel: SelfViewModel by viewModels { getVmFactory(userArg) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentUsersBinding.inflate(inflater, container, false)

        return binding.root
    }
}