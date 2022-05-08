package com.zoe.weshare.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.zoe.weshare.MainActivity
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.R
import com.zoe.weshare.data.UserInfo
import com.zoe.weshare.databinding.FragmentLoginBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.util.Logger
import com.zoe.weshare.util.UserManager
import com.zoe.weshare.util.Util


class LoginFragment : Fragment() {

    companion object {
        const val RC_SIGN_IN = 100
    }

    lateinit var binding: FragmentLoginBinding

    val viewModel by viewModels<LoginViewModel> { getVmFactory() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {


        binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.buttonSignin.setOnClickListener { signIn() }


        viewModel.loginSuccess.observe(viewLifecycleOwner) {

            (activity as MainActivity).viewModel.getLiveNotificationResult()

            findNavController().navigate(NavGraphDirections.navigateToHomeFragment())
        }


        mockUser()
        return binding.root
    }


    val clientId = Util.getString(R.string.server_client_id)

    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestServerAuthCode(clientId)
            .requestIdToken(clientId)
            .requestEmail()
            .build()

        val mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        // signOut allow user to choose different account to login
        mGoogleSignInClient.signOut()

        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)

                account?.let {
                    val user = UserInfo(
                        name = it.displayName ?: "",
                        image = it.photoUrl.toString(),
                        uid = it.id ?: ""
                    )
                    viewModel.checkIfMemberExist(user)
                }


                Toast.makeText(requireContext(), "登入成功", Toast.LENGTH_SHORT).show()
            } catch (e: ApiException) {
                Logger.i("signInResult:failed code=" + e.statusCode)
                Toast.makeText(requireContext(), "登入失敗", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "登入失敗", Toast.LENGTH_SHORT).show()
        }
    }

    fun mockUser() {

        binding.apply {

            A.setOnClickListener {
                val user =  UserInfo(
                    name = "Johnny",
                    image = "https://img.tagsis.com/202204/96045.jpg",
                    uid = "12344408Johnny62583"
                )
                Log.d("setOnClickListener","a")
                viewModel.checkIfMemberExist(user)
            }
            B.setOnClickListener {
               val user = UserInfo(
                    name = "迅姐",
                    image = "https://www.laoziliao.net/fs/img/3e/3e1885f8708c208ab875033e3e5e3e8f.webp",
                    uid = " 12343j0000ZhouXun111"
                )
                Log.d("setOnClickListener","b")
                viewModel.checkIfMemberExist(user)
            }

            C.setOnClickListener {
                val user = UserInfo(
                    name = "小傑",
                    image = "https://images.chinatimes.com/newsphoto/2021-05-25/656/20210525003814.jpg",
                    uid = "100000Chiang2123212"
                )
                viewModel.checkIfMemberExist(user)
            }
            D.setOnClickListener {
               val user = UserInfo(
                    name = "艾瑪",
                    image = "https://image.knowing.asia/c4f9ba3d-78f0-4c0e-828a-58eb5ede41ea/70eed7080b3bc4791fe83d798c08a210.png",
                    uid = "98666Emma1ji332583"
                )
                viewModel.checkIfMemberExist(user)
            }
        }
    }

}