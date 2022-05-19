package com.zoe.weshare.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.zoe.weshare.MainActivity
import com.zoe.weshare.NavGraphDirections
import com.zoe.weshare.R
import com.zoe.weshare.data.UserInfo
import com.zoe.weshare.databinding.FragmentLoginBinding
import com.zoe.weshare.ext.getVmFactory
import com.zoe.weshare.ext.showToast
import com.zoe.weshare.util.Logger
import com.zoe.weshare.util.UserManager.userConsentPolicy
import com.zoe.weshare.util.Util

class LoginFragment : Fragment() {

    companion object {
        const val RC_SIGN_IN = 100
    }

    private lateinit var binding: FragmentLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var gso: GoogleSignInOptions

    private var resetFastLogin: Boolean = false

    private val viewModel by viewModels<LoginViewModel> { getVmFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentLoginBinding.inflate(inflater, container, false)

        resetFastLogin = LoginFragmentArgs.fromBundle(requireArguments()).resetFastLogin

        viewModel.loginSuccess.observe(viewLifecycleOwner) {
            it?.let {
                (activity as MainActivity).viewModel.getLiveNotificationResult()
                (activity as MainActivity).viewModel.getLiveRoomResult()

                findNavController().navigate(NavGraphDirections.navigateToHomeFragment())
                viewModel.loginComplete()

                activity.showToast(getString(R.string.toast_login_success))
            }
        }

        setupGoogleClient()
        return binding.root
    }

    private fun setupGoogleClient() {
        val clientId = Util.getString(R.string.server_client_id)

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestServerAuthCode(clientId)
            .requestIdToken(clientId)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        if (userConsentPolicy) {
            binding.consentBox.isChecked = true
            binding.disableCover.visibility = View.GONE

            binding.buttonSignin.setOnClickListener {
                signIn()
            }
        } else {
            binding.consentPolicy2.setOnClickListener {
                findNavController().navigate(NavGraphDirections.actionGlobalPolicyTermFragment())
            }
        }

        // signOut allow user to choose different account to login
        if (resetFastLogin) {
            googleSignInClient.signOut()
        }
    }


    private fun signIn() {

        val signInIntent = googleSignInClient.signInIntent
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

            } catch (e: ApiException) {
                Logger.i("signInResult:failed code=" + e.statusCode)
            }
        } else {
            Logger.i("User press backKey and cancel login")
        }
    }
}
