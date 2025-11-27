package com.eguerra.ciudadanodigital.ui.fragment.profile

import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.addCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.eguerra.ciudadanodigital.R
import com.eguerra.ciudadanodigital.data.local.countries
import com.eguerra.ciudadanodigital.data.local.entity.UserModel
import com.eguerra.ciudadanodigital.databinding.FragmentProfileBinding
import com.eguerra.ciudadanodigital.helpers.DATE_FORMAT
import com.eguerra.ciudadanodigital.ui.Status
import com.eguerra.ciudadanodigital.ui.activity.LoadingViewModel
import com.eguerra.ciudadanodigital.ui.activity.MainActivity
import com.eguerra.ciudadanodigital.ui.activity.UserSessionStatus
import com.eguerra.ciudadanodigital.ui.activity.UserViewModel
import com.eguerra.ciudadanodigital.ui.util.getColorFromSeed
import com.eguerra.ciudadanodigital.ui.util.showConfirmationDialog
import com.eguerra.ciudadanodigital.ui.util.showToast
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.threeten.bp.ZoneId
import java.util.Locale

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val profileViewModel: ProfileViewModel by viewModels()
    private val mainUserViewModel: UserViewModel by activityViewModels()
    private val loadingViewModel: LoadingViewModel by activityViewModels()
    private lateinit var user: UserModel
    private var isEditing: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initEvents()
        setListeners()
        setObservers()
    }

    private fun showDatePicker() {
        val birthdateMillis =
            user.birthdate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Selecciona tu fecha de nacimiento").setSelection(birthdateMillis)
            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR).build()

        datePicker.addOnPositiveButtonClickListener { selectedDate ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.timeInMillis = selectedDate

            val day = selectedCalendar.get(Calendar.DAY_OF_MONTH) + 1
            val month = selectedCalendar.get(Calendar.MONTH) + 1
            val year = selectedCalendar.get(Calendar.YEAR)

            val formattedDate = String.format(Locale.getDefault(), DATE_FORMAT, day, month, year)
            binding.profileFragmentBirthdateEditText.setText(formattedDate)
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun validateData(): Boolean {
        val email = binding.profileFragmentEmailTextInput.editText?.text?.toString()?.trim()
        val name = binding.profileFragmentNameTextInput.editText?.text?.toString()?.trim()
        val lastname = binding.profileFragmentLastnameTextInput.editText?.text?.toString()?.trim()
        val birthdate = binding.profileFragmentBirthdateTextInput.editText?.text?.toString()?.trim()
        val selectedPhoneCode =
            binding.profileFragmentPhonecodeTextInput.editText?.text?.toString()?.trim()
        val phoneCode = countries.find { country ->
            selectedPhoneCode?.contains("+${country.phoneCode} ${country.flag}") == true
        }?.phoneCode
        val phoneNumber = binding.profileFragmentPhoneTextInput.editText?.text?.toString()?.trim()

        if (email.isNullOrEmpty() || name.isNullOrEmpty() || lastname.isNullOrEmpty() || birthdate.isNullOrEmpty() || phoneCode.isNullOrEmpty() || phoneNumber.isNullOrEmpty()) {
            showToast("Por favor completa todos los campos", requireContext())
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.profileFragmentEmailTextInput.error = "Correo electrónico inválido"
            return false
        } else {
            binding.profileFragmentEmailTextInput.error = null
        }

        if (!phoneNumber.matches(Regex("^[0-9]+$"))) {
            binding.profileFragmentPhoneTextInput.error = "Número de teléfono inválido"
            return false
        } else {
            binding.profileFragmentPhoneTextInput.error = null
        }

        return true
    }

    private fun setListeners() {
        binding.apply {
            profileFragmentBirthdateTextInput.setEndIconOnClickListener {
                if (!isEditing) return@setEndIconOnClickListener
                showDatePicker()
            }
            profileFragmentPhonecodeTextInput.setEndIconOnClickListener {
                if (!isEditing) return@setEndIconOnClickListener
            }
            profileFragmentReturnImageButton.setOnClickListener {
                if (isEditing) {
                    showConfirmationDialog(
                        title = "Cancelar edición",
                        description = "¿Está seguro de cancelar los cambios?",
                        context = requireContext()
                    ) { confirm ->
                        if (confirm) {
                            isEditing = false
                            binding.profileFragmentProfileButton.text =
                                getString(R.string.edit_label)
                            setUpUserData()
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                    }
                } else {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }

            profileFragmentPhonecodeEditText.setOnItemClickListener { adapterView, _, position, _ ->
                profileFragmentPhonecodeTextInput.isErrorEnabled = false
                val selected = adapterView.getItemAtPosition(position).toString()
                profileFragmentPhonecodeEditText.setText(selected, false)
            }
            profileFragmentBirthdateTextInput.setOnClickListener {
                showDatePicker()
            }

            profileFragmentProfileButton.setOnClickListener {
                if (!isEditing) {
                    isEditing = true
                    profileFragmentPhonecodeEditText.isEnabled = true
                    profileFragmentPhoneEditText.isEnabled = true
                    profileFragmentEmailEditText.isEnabled = true
                    profileFragmentNameEditText.isEnabled = true
                    profileFragmentLastnameEditText.isEnabled = true
                    profileFragmentBirthdateEditText.isEnabled = true
                    profileFragmentProfileButton.text = getString(R.string.save_label)
                    return@setOnClickListener
                }
                if (!validateData()) return@setOnClickListener
                val selectedPhoneCode =
                    binding.profileFragmentPhonecodeTextInput.editText!!.text.toString().trim()

                profileViewModel.updateUser(
                    binding.profileFragmentEmailTextInput.editText!!.text.toString().trim(),
                    binding.profileFragmentNameTextInput.editText!!.text.toString().trim(),
                    binding.profileFragmentLastnameTextInput.editText!!.text.toString().trim(),
                    binding.profileFragmentBirthdateTextInput.editText!!.text.toString().trim(),
                    "+${
                        countries.find { country ->
                            selectedPhoneCode.contains("+${country.phoneCode} ${country.flag}")
                        }!!.phoneCode
                    }",
                    binding.profileFragmentPhoneTextInput.editText!!.text.toString().trim(),
                )
            }

            profileFragmentEmailEditText.doOnTextChanged { _, _, _, _ ->
                profileFragmentEmailTextInput.isErrorEnabled = false
            }

            profileFragmentNameEditText.doOnTextChanged { _, _, _, _ ->
                profileFragmentNameTextInput.isErrorEnabled = false
            }

            profileFragmentLastnameEditText.doOnTextChanged { _, _, _, _ ->
                profileFragmentLastnameTextInput.isErrorEnabled = false
            }

            profileFragmentBirthdateEditText.doOnTextChanged { _, _, _, _ ->
                profileFragmentBirthdateTextInput.isErrorEnabled = false
            }

            profileFragmentEmailEditText.doOnTextChanged { _, _, _, _ ->
                profileFragmentEmailTextInput.isErrorEnabled = false
            }

            profileFragmentPhonecodeEditText.doOnTextChanged { _, _, _, _ ->
                profileFragmentPhonecodeTextInput.isErrorEnabled = false
            }

            profileFragmentPhoneEditText.doOnTextChanged { _, _, _, _ ->
                profileFragmentPhoneTextInput.isErrorEnabled = false
            }
        }
    }

    private fun setObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainUserViewModel.userDataStateFlow.collectLatest { state ->
                    when (state) {
                        is UserSessionStatus.Default -> {
                            loadingViewModel.showLoadingDialog()
                        }

                        is UserSessionStatus.Logged -> {
                            println("USUARIO: ${state.data}")
                            user = state.data
                            setDropLists()
                            setUpUserData(state.data)
                            loadingViewModel.hideLoadingDialog()
                        }

                        is UserSessionStatus.NotLogged -> {
                            loadingViewModel.hideLoadingDialog()
                            (requireActivity() as MainActivity).handleLogoutAction("No se pudo obtener la información del usuario")
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileViewModel.updateStateFlow.collectLatest { state ->
                    when (state) {
                        is Status.Loading -> {
                            loadingViewModel.showLoadingDialog()
                        }

                        is Status.Success -> {
                            user = state.value.second
                            setDropLists()
                            setUpUserData(state.value.second)
                            binding.profileFragmentProfileButton.text = getString(R.string.edit_label)
                            isEditing = false
                            loadingViewModel.hideLoadingDialog()
                        }

                        is Status.Error -> {
                            showToast(state.error, requireContext())
                            loadingViewModel.hideLoadingDialog()
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun setUpUserData(user: UserModel = this.user) {
        binding.apply {
            profileFragmentEmailEditText.setText(user.email)
            profileFragmentNameEditText.setText(user.names)
            profileFragmentLastnameEditText.setText(user.lastnames)

            val birthdate = user.birthdate
            val dateText = String.format(
                Locale.getDefault(),
                DATE_FORMAT,
                birthdate.dayOfMonth,
                birthdate.monthValue,
                birthdate.year
            )
            profileFragmentBirthdateEditText.setText(dateText)

            val defaultCountry = countries.find { "+${it.phoneCode}" == user.phoneCode }
            if (defaultCountry != null) {
                val defaultValue = "+${defaultCountry.phoneCode} ${defaultCountry.flag}"
                profileFragmentPhonecodeEditText.setText(defaultValue, false)
            }

            profileFragmentPhoneEditText.setText(user.phoneNumber)

            val avatar = profileFragmentUserInitial
            val initial = user.names.trim().firstOrNull()?.uppercase() ?: "?"
            val color = getColorFromSeed(seed = user.userId.toString())
            val title = profileFragmentTitleProfile

            avatar.text = initial
            avatar.background.setTint(color)
            title.text =
                getString(R.string.username, user.names.split(" ")[0], user.lastnames.split(" ")[0])
        }
    }

    private fun initEvents() {
        mainUserViewModel.getUserData(false)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (isEditing) {
                showConfirmationDialog(
                    "Cancelar edición", "¿Deseas salir sin guardar los cambios?", requireContext()
                ) { confirm ->
                    if (confirm) {
                        isEditing = false
                        setUpUserData()
                        binding.profileFragmentProfileButton.text = getString(R.string.edit_label)
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            } else {
                isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

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
        binding.profileFragmentPhonecodeEditText.setAdapter(adapterPhoneCodes)

        val defaultCountry = countries.find { "+${it.phoneCode}" == user.phoneCode }
        if (defaultCountry != null) {
            val defaultValue = "+${defaultCountry.phoneCode} ${defaultCountry.flag}"
            binding.profileFragmentPhonecodeEditText.setText(defaultValue, false)
            binding.profileFragmentPhonecodeEditText.tag = "+${defaultCountry}.phoneCode"
        }
    }
}