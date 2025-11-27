package com.eguerra.ciudadanodigital.ui.fragment.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eguerra.ciudadanodigital.data.Resource
import com.eguerra.ciudadanodigital.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginStatus {
    data object Default : LoginStatus()
    data object Loading : LoginStatus()
    data object Logged : LoginStatus()
    class Error(val error: String) : LoginStatus()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _loginStateFlow: MutableStateFlow<LoginStatus> =
        MutableStateFlow(LoginStatus.Default)
    val loginStateFlow: StateFlow<LoginStatus> = _loginStateFlow

    fun login(email: String, password: String) {
        _loginStateFlow.value = LoginStatus.Loading

        if (email.trim() == "" || password.trim() == "") viewModelScope.launch {
            _loginStateFlow.value = LoginStatus.Error("Ingrese sus credenciales.")
        }
        else viewModelScope.launch {

            when (val result = repository.login(email = email, password)) {
                is Resource.Success -> {
                    _loginStateFlow.value = LoginStatus.Logged
                }

                else -> {
                    _loginStateFlow.value = LoginStatus.Error(result.message ?: "Ocurrió un error.")
                }
            }
        }
    }
}