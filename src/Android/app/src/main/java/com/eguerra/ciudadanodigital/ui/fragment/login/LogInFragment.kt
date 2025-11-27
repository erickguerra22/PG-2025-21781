package com.eguerra.ciudadanodigital.ui.fragment.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.eguerra.ciudadanodigital.databinding.FragmentLogInBinding
import com.eguerra.ciudadanodigital.ui.activity.MainActivity
import com.eguerra.ciudadanodigital.ui.util.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LogInFragment : Fragment() {
    private lateinit var binding: FragmentLogInBinding
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentLogInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListeners()
        setObservers()
    }

    private fun setObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            loginViewModel.loginStateFlow.collectLatest { result ->
                when (result) {
                    is LoginStatus.Loading -> {
                        binding.apply {
                            logInFragmentLogInButton.visibility = View.INVISIBLE
                            logInFragmentProgress.visibility = View.VISIBLE
                        }
                    }

                    is LoginStatus.Logged -> {
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    }

                    is LoginStatus.Error -> {
                        showToast(result.error, requireContext())
                        binding.apply {
                            logInFragmentLogInButton.visibility = View.VISIBLE
                            logInFragmentProgress.visibility = View.GONE
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    private fun setListeners() {
        binding.apply {
            logInFragmentLogInButton.setOnClickListener {
                performLogin()
            }
            logInFragmentRegisterLink.setOnClickListener {
                val action = LogInFragmentDirections.actionLogInFragmentToRegisterFragment()
                requireView().findNavController().navigate(action)
            }
            logInFragmentTextRecoverPassword.setOnClickListener {
                val action = LogInFragmentDirections.actionLogInFragmentToSendRecoveryFragment()
                requireView().findNavController().navigate(action)
            }
        }
    }

    private fun performLogin() {
        val user = binding.logInFragmentEmailTextInput.editText!!.text.toString().trim()
        val password = binding.logInFragmentPasswordTextInput.editText!!.text.toString()

        loginViewModel.login(email = user, password = password)

    }
}