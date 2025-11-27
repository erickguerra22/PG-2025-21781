package com.eguerra.ciudadanodigital.ui.fragment.recovery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.eguerra.ciudadanodigital.databinding.FragmentVerifyCodeBinding
import com.eguerra.ciudadanodigital.ui.Status
import com.eguerra.ciudadanodigital.ui.util.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VerifyCodeFragment : Fragment() {
    private lateinit var binding: FragmentVerifyCodeBinding
    private val recoveryViewModel: RecoveryViewModel by viewModels()

    private val args: VerifyCodeFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentVerifyCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListeners()
        setObservers()
    }

    private fun setObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            recoveryViewModel.verifyCodeStateFlow.collectLatest { result ->
                when (result) {
                    is Status.Loading -> {
                        binding.apply {
                            verifyCodeFragmentVerifyCodeButton.visibility = View.INVISIBLE
                            verifyCodeFragmentProgress.visibility = View.VISIBLE
                        }
                    }

                    is Status.Success -> {
                        showToast(result.value.second, requireContext(), true)
                        if (result.value.first) {
                            val action =
                                VerifyCodeFragmentDirections.actionVerifyCodeFragmentToResetPasswordFragment()
                            requireView().findNavController().navigate(action)

                            // binding.verifyCodeFragmentSixthDigitEditText.setText("")
                            recoveryViewModel.resetVerifyCodeState()
                        }
                    }

                    is Status.Error -> {
                        showToast(result.error, requireContext())
                        binding.apply {
                            verifyCodeFragmentVerifyCodeButton.visibility = View.VISIBLE
                            verifyCodeFragmentProgress.visibility = View.GONE
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    private fun setListeners() {
        binding.apply {
            verifyCodeFragmentVerifyCodeButton.setOnClickListener {
                val code = buildString {
                    append(verifyCodeFragmentFirstDigitInputContainer.editText?.text)
                    append(verifyCodeFragmentSecondDigitInputContainer.editText?.text)
                    append(verifyCodeFragmentThirdDigitInputContainer.editText?.text)
                    append(verifyCodeFragmentFourthDigitInputContainer.editText?.text)
                    append(verifyCodeFragmentFifthDigitInputContainer.editText?.text)
                    append(verifyCodeFragmentSixthDigitInputContainer.editText?.text)
                }.trim()



                if (code.isBlank() || code.length < 6) {
                    showToast(
                        "Debes ingresar el código de verificación completo", requireContext(), true
                    )
                    return@setOnClickListener
                }

                recoveryViewModel.verifyCode(email = args.email, code = code.toInt())
            }

            val editTexts = listOf(
                verifyCodeFragmentFirstDigitEditText,
                verifyCodeFragmentSecondDigitEditText,
                verifyCodeFragmentThirdDigitEditText,
                verifyCodeFragmentFourthDigitEditText,
                verifyCodeFragmentFifthDigitEditText,
                verifyCodeFragmentSixthDigitEditText
            )

            editTexts.forEachIndexed { index, editText ->
                editText.doOnTextChanged { text, start, before, count ->
                    if (text?.length == 1) {
                        if (index < editTexts.size - 1) {
                            editTexts[index + 1].requestFocus()
                        } else {
                            editText.clearFocus()
                            verifyCodeFragmentVerifyCodeButton.performClick()
                        }
                    }
                }

                editText.setOnKeyListener { _, keyCode, event ->
                    if (keyCode == android.view.KeyEvent.KEYCODE_DEL &&
                        event.action == android.view.KeyEvent.ACTION_DOWN &&
                        editText.text.isNullOrEmpty()
                    ) {
                        if (index > 0) {
                            editTexts[index - 1].requestFocus()
                            editTexts[index - 1].text?.clear()
                        }
                    }
                    false
                }
            }
        }
    }
}