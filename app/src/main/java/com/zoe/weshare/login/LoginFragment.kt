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
            (activity as MainActivity).viewModel.getLiveNotificationResult(it.uid)

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
                Log.i("givemepass", "signInResult:failed code=" + e.statusCode)
                Toast.makeText(requireContext(), "登入失敗", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "登入失敗", Toast.LENGTH_SHORT).show()
        }
    }

    fun mockUser() {

        binding.apply {

            ken.setOnClickListener {
                viewModel.checkIfMemberExist(UserInfo(
                    name = "Ken",
                    image = "https://images2.gamme.com.tw/news2/2014/94/31/p6CWnp6ckqKW.jpg",
                    uid = "kenku037362583"
                ))
            }
            amy.setOnClickListener { viewModel.checkIfMemberExist(UserInfo(
                name = "Amy",
                image = "https://1.bp.blogspot.com/-wXhIWjtUkrc/XxzD1uRhQHI/AAAAAAAAhbc/3sL6IPSuG-gEJeg8Qy5sdLBRDurPCNpbwCLcBGAsYHQ/s640/Shingeki%2Bno%2BKyojin%2B-%2BOAD%2B03%2B%2528DVD%2B1024x576%2BAVC%2BAAC%2529.mp4_20200710_000330.072.jpg",
                uid = " ko3jMaAmy03731283111"
            ))}
            lora.setOnClickListener {viewModel.checkIfMemberExist(UserInfo(
                name = "蘿拉卡芙特",
                image = "https://images2.gamme.com.tw/news2/2016/26/12/q52SpaablqCbqA.jpeg",
                uid = "123ijijloraefe2212"
            )) }
            mandy.setOnClickListener { viewModel.checkIfMemberExist(UserInfo(
                name = "Mandy",
                image = "https://truth.bahamut.com.tw/s01/201309/f7d2d1613cbcd827ac28c1353bc54693.JPG",
                uid = "manddy1ji332583"
            ))}

        }
    }
}