package com.eguerra.ciudadanodigital.ui.fragment.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eguerra.ciudadanodigital.data.Resource
import com.eguerra.ciudadanodigital.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RegisterStatus {
    data object Default : RegisterStatus()
    data object Loading : RegisterStatus()
    data object Logged : RegisterStatus()
    class Error(val error: String) : RegisterStatus()
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _registerStateFlow: MutableStateFlow<RegisterStatus> =
        MutableStateFlow(RegisterStatus.Default)
    val registerStateFlow: StateFlow<RegisterStatus> = _registerStateFlow

    fun register(
        email: String,
        name: String,
        lastname: String,
        birthdate: String,
        phoneCode: String,
        phoneNumber: String,
        password: String
    ) {
        _registerStateFlow.value = RegisterStatus.Loading

        viewModelScope.launch {
            when (val result = repository.register(
                email = email,
                name = name,
                lastname = lastname,
                phoneCode = phoneCode,
                phoneNumber = phoneNumber,
                password = password,
                birthdate = birthdate
            )) {
                is Resource.Success -> {
                    _registerStateFlow.value = RegisterStatus.Logged
                }

                else -> {
                    _registerStateFlow.value =
                        RegisterStatus.Error(result.message ?: "Ocurrió un error al registrar al usuario.")
                }
            }
        }
    }
}