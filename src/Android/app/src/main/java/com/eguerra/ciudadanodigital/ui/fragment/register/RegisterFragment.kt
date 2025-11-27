package com.eguerra.ciudadanodigital.ui.fragment.register

import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.eguerra.ciudadanodigital.data.local.countries
import com.eguerra.ciudadanodigital.databinding.FragmentRegisterBinding
import com.eguerra.ciudadanodigital.helpers.DATE_FORMAT
import com.eguerra.ciudadanodigital.ui.activity.MainActivity
import com.eguerra.ciudadanodigital.ui.util.showToast
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    private val registerViewModel: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initEvents()
        setListeners()
        setObservers()
    }

    private fun initEvents() {
        initDateField()
        setDropLists()
    }

    private fun setDropLists() {
        val phoneCodes = countries.sortedBy { country -> country.phoneCode }.map { country ->
            "+${country.phoneCode} ${country.flag}"
        }

        val adapterPhoneCodes = ArrayAdapter(
            requireContext(),
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            phoneCodes
        )
        binding.registerFragmentPhonecodeEditText.setAdapter(adapterPhoneCodes)

        val defaultCountry = countries.find { it.abbreviation.equals("GT", ignoreCase = true) }
        if (defaultCountry != null) {
            val defaultValue = "+${defaultCountry.phoneCode} ${defaultCountry.flag}"
            binding.registerFragmentPhonecodeEditText.setText(defaultValue, false)
            binding.registerFragmentPhonecodeEditText.tag = "+${defaultCountry}.phoneCode"
        }
    }

    private fun validateData(): Boolean {
        val email = binding.registerFragmentEmailTextInput.editText?.text?.toString()?.trim()
        val name = binding.registerFragmentNameTextInput.editText?.text?.toString()?.trim()
        val lastname = binding.registerFragmentLastnameTextInput.editText?.text?.toString()?.trim()
        val birthdate =
            binding.registerFragmentBirthdateTextInput.editText?.text?.toString()?.trim()
        val selectedPhoneCode =
            binding.registerFragmentPhonecodeTextInput.editText?.text?.toString()?.trim()
        val phoneCode = countries.find { country ->
            selectedPhoneCode?.contains("+${country.phoneCode} ${country.flag}") == true
        }?.phoneCode
        val phoneNumber = binding.registerFragmentPhoneTextInput.editText?.text?.toString()?.trim()
        val password = binding.registerFragmentPasswordTextInput.editText?.text?.toString()?.trim()
        val confirmPassword =
            binding.registerFragmentConfirmPasswordTextInput.editText?.text?.toString()?.trim()

        if (email.isNullOrEmpty() || name.isNullOrEmpty() || lastname.isNullOrEmpty() || birthdate.isNullOrEmpty() || phoneCode.isNullOrEmpty() || phoneNumber.isNullOrEmpty() || password.isNullOrEmpty() || confirmPassword.isNullOrEmpty()) {
            showToast("Por favor completa todos los campos", requireContext())
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.registerFragmentEmailTextInput.error = "Correo electrónico inválido"
            return false
        } else {
            binding.registerFragmentEmailTextInput.error = null
        }

        if (!phoneNumber.matches(Regex("^[0-9]+$"))) {
            binding.registerFragmentPhoneTextInput.error = "Número de teléfono inválido"
            return false
        } else {
            binding.registerFragmentPhoneTextInput.error = null
        }

        if (!password.matches(Regex("^.{8,}$"))) {
            binding.registerFragmentPasswordTextInput.error =
                "La contraseña debe tener al menos 8 caracteres"
            return false
        } else {
            binding.registerFragmentPasswordTextInput.error = null
        }

        if (password != confirmPassword) {
            binding.registerFragmentConfirmPasswordTextInput.error = "Las contraseñas no coinciden"
            return false
        } else {
            binding.registerFragmentConfirmPasswordTextInput.error = null
        }

        return true
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val today = calendar.timeInMillis

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Selecciona tu fecha de nacimiento").setSelection(today)
            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR).build()

        datePicker.addOnPositiveButtonClickListener { selectedDate ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.timeInMillis = selectedDate

            val day = selectedCalendar.get(Calendar.DAY_OF_MONTH) + 1
            val month = selectedCalendar.get(Calendar.MONTH) + 1
            val year = selectedCalendar.get(Calendar.YEAR)

            val formattedDate = String.format(Locale.getDefault(), DATE_FORMAT, day, month, year)
            binding.registerFragmentBirthdateEditText.setText(formattedDate)
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun setListeners() {
        binding.apply {
            registerFragmentPhonecodeEditText.setOnItemClickListener { adapterView, _, position, _ ->
                registerFragmentPhonecodeTextInput.isErrorEnabled = false
                val selected = adapterView.getItemAtPosition(position).toString()
                registerFragmentPhonecodeEditText.setText(selected, false)
            }
            registerFragmentBirthdateTextInput.setEndIconOnClickListener {
                showDatePicker()
            }
            registerFragmentBirthdateTextInput.setOnClickListener {
                showDatePicker()
            }
            registerFragmentRegisterLink.setOnClickListener {
                val action = RegisterFragmentDirections.actionRegisterFragmentToLogInFragment()
                requireView().findNavController().navigate(action)
            }

            registerFragmentRegisterButton.setOnClickListener {
                if (!validateData()) return@setOnClickListener
                val selectedPhoneCode =
                    binding.registerFragmentPhonecodeTextInput.editText!!.text.toString().trim()

                println(
                    "PHONECODE: +${
                        countries.find { country ->
                            selectedPhoneCode.contains("+${country.phoneCode} ${country.flag}")
                        }!!.phoneCode
                    }"
                )

                registerViewModel.register(
                    binding.registerFragmentEmailTextInput.editText!!.text.toString().trim(),
                    binding.registerFragmentNameTextInput.editText!!.text.toString().trim(),
                    binding.registerFragmentLastnameTextInput.editText!!.text.toString().trim(),
                    binding.registerFragmentBirthdateTextInput.editText!!.text.toString().trim(),
                    "+${
                        countries.find { country ->
                            selectedPhoneCode.contains("+${country.phoneCode} ${country.flag}")
                        }!!.phoneCode
                    }",
                    binding.registerFragmentPhoneTextInput.editText!!.text.toString().trim(),
                    binding.registerFragmentPasswordTextInput.editText!!.text.toString().trim(),
                )
            }

            // Inputs
            registerFragmentEmailEditText.doOnTextChanged { _, _, _, _ ->
                registerFragmentEmailTextInput.isErrorEnabled = false
            }

            registerFragmentNameEditText.doOnTextChanged { _, _, _, _ ->
                registerFragmentNameTextInput.isErrorEnabled = false
            }

            registerFragmentLastnameEditText.doOnTextChanged { _, _, _, _ ->
                registerFragmentLastnameTextInput.isErrorEnabled = false
            }

            registerFragmentBirthdateEditText.doOnTextChanged { _, _, _, _ ->
                registerFragmentBirthdateTextInput.isErrorEnabled = false
            }

            registerFragmentEmailEditText.doOnTextChanged { _, _, _, _ ->
                registerFragmentEmailTextInput.isErrorEnabled = false
            }

            registerFragmentPhonecodeEditText.doOnTextChanged { _, _, _, _ ->
                registerFragmentPhonecodeTextInput.isErrorEnabled = false
            }

            registerFragmentPhoneEditText.doOnTextChanged { _, _, _, _ ->
                registerFragmentPhoneTextInput.isErrorEnabled = false
            }

            registerFragmentPasswordEditText.doOnTextChanged { _, _, _, _ ->
                registerFragmentPasswordTextInput.isErrorEnabled = false
            }

            registerFragmentConfirmPasswordEditText.doOnTextChanged { _, _, _, _ ->
                registerFragmentConfirmPasswordTextInput.isErrorEnabled = false
            }
        }
    }

    private fun initDateField() {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentYear = calendar.get(Calendar.YEAR)

        val dateText =
            String.format(Locale.getDefault(), DATE_FORMAT, currentDay, currentMonth, currentYear)
        binding.registerFragmentBirthdateEditText.setText(dateText)
    }

    private fun setObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            registerViewModel.registerStateFlow.collectLatest { result ->
                when (result) {
                    is RegisterStatus.Loading -> {
                        binding.apply {
                            registerFragmentRegisterButton.visibility = View.INVISIBLE
                            registerFragmentProgress.visibility = View.VISIBLE
                        }
                    }

                    is RegisterStatus.Logged -> {
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    }

                    is RegisterStatus.Error -> {
                        showToast(result.error, requireContext())
                        binding.apply {
                            registerFragmentRegisterButton.visibility = View.VISIBLE
                            registerFragmentProgress.visibility = View.GONE
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}