package com.eguerra.ciudadanodigital.ui.fragment.recovery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.eguerra.ciudadanodigital.databinding.FragmentResetPasswordBinding
import com.eguerra.ciudadanodigital.ui.Status
import com.eguerra.ciudadanodigital.ui.util.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ResetPasswordFragment : Fragment() {
    private lateinit var binding: FragmentResetPasswordBinding
    private val recoveryViewModel: RecoveryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListeners()
        setObservers()
    }

    private fun setObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            recoveryViewModel.resetPasswordStateFlow.collectLatest { result ->
                when (result) {
                    is Status.Loading -> {
                        binding.apply {
                            resetPasswordFragmentSetPasswordButton.visibility = View.INVISIBLE
                            resetPasswordFragmentProgress.visibility = View.VISIBLE
                        }
                    }

                    is Status.Success -> {
                        showToast(result.value.second, requireContext(), true)
                        val action =
                            ResetPasswordFragmentDirections.actionResetPasswordFragmentToLogInFragment()
                        requireView().findNavController().navigate(action)
                    }

                    is Status.Error -> {
                        showToast(result.error, requireContext())
                        binding.apply {
                            resetPasswordFragmentSetPasswordButton.visibility = View.VISIBLE
                            resetPasswordFragmentProgress.visibility = View.GONE
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    private fun validateData(): Boolean {
        val password =
            binding.resetPasswordFragmentPasswordTextInput.editText?.text?.toString()?.trim()
        val confirmPassword =
            binding.resetPasswordFragmentConfirmPasswordTextInput.editText?.text?.toString()?.trim()

        if (password.isNullOrEmpty() || confirmPassword.isNullOrEmpty()) {
            showToast("Por favor completa todos los campos", requireContext())
            return false
        }

        if (!password.matches(Regex("^.{8,}$"))) {
            binding.resetPasswordFragmentPasswordTextInput.error =
                "La contraseña debe tener al menos 8 caracteres"
            return false
        } else {
            binding.resetPasswordFragmentPasswordTextInput.error = null
        }

        if (password != confirmPassword) {
            binding.resetPasswordFragmentConfirmPasswordTextInput.error =
                "Las contraseñas no coinciden"
            return false
        } else {
            binding.resetPasswordFragmentConfirmPasswordTextInput.error = null
        }
        return true
    }

    private fun setListeners() {
        binding.apply {
            resetPasswordFragmentSetPasswordButton.setOnClickListener {
                if (!validateData()) return@setOnClickListener
                val newPassword =
                    binding.resetPasswordFragmentPasswordEditText.text.toString().trim()

                recoveryViewModel.resetPassword(newPassword)
            }
        }
    }
}