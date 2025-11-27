package com.eguerra.ciudadanodigital.ui.fragment.recovery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.eguerra.ciudadanodigital.databinding.FragmentSendRecoveryBinding
import com.eguerra.ciudadanodigital.ui.Status
import com.eguerra.ciudadanodigital.ui.fragment.login.LogInFragmentDirections
import com.eguerra.ciudadanodigital.ui.fragment.register.RegisterFragmentDirections
import com.eguerra.ciudadanodigital.ui.util.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SendRecoveryFragment : Fragment() {
    private lateinit var binding: FragmentSendRecoveryBinding
    private val recoveryViewModel: RecoveryViewModel by viewModels()
    private var isRestoring = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSendRecoveryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListeners()
        setObservers()
    }

    private fun setObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            recoveryViewModel.sendRecoveryStateFlow.collectLatest { result ->
                when (result) {
                    is Status.Loading -> {
                        binding.apply {
                            sendRecoveryFragmentRecoveryButton.visibility = View.INVISIBLE
                            sendRecoveryFragmentProgress.visibility = View.VISIBLE
                        }
                    }

                    is Status.Success -> {
                        showToast(result.value, requireContext(), true)
                        val action = SendRecoveryFragmentDirections.actionSendRecoveryFragmentToVerifyCodeFragment(
                            binding.sendRecoveryFragmentEmailEditText.text.toString()
                        )
                        requireView().findNavController().navigate(action)

                        recoveryViewModel.resetSendRecoveryState()
                    }

                    is Status.Error -> {
                        showToast(result.error, requireContext())
                        binding.apply {
                            sendRecoveryFragmentRecoveryButton.visibility = View.VISIBLE
                            sendRecoveryFragmentProgress.visibility = View.GONE
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    private fun setListeners() {
        binding.apply {
            sendRecoveryFragmentRecoveryButton.setOnClickListener {
                val email = sendRecoveryFragmentEmailTextInput.editText?.text?.toString()?.trim()

                if (email.isNullOrBlank()) {
                    showToast(
                        "Debes ingresar el correo electrónico para recuperar contraseña",
                        requireContext(),
                        true
                    )
                    return@setOnClickListener
                }

                recoveryViewModel.sendRecovery(email)


            }
        }
    }
}