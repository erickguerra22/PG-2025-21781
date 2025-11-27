package com.eguerra.ciudadanodigital.ui.fragment.recovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eguerra.ciudadanodigital.data.Resource
import com.eguerra.ciudadanodigital.data.repository.AuthRepository
import com.eguerra.ciudadanodigital.ui.Status
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecoveryViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _sendRecoveryStateFlow: MutableStateFlow<Status<String>> =
        MutableStateFlow(Status.Default())
    val sendRecoveryStateFlow: StateFlow<Status<String>> = _sendRecoveryStateFlow

    fun sendRecovery(email: String) {
        _sendRecoveryStateFlow.value = Status.Loading()

        viewModelScope.launch {
            when (val result = repository.sendRecovery(email = email)) {
                is Resource.Success -> {
                    _sendRecoveryStateFlow.value = Status.Success(result.data)
                }

                else -> {
                    _sendRecoveryStateFlow.value =
                        Status.Error(result.code ?: 500, result.message ?: "Ocurrió un error.")
                }
            }
        }
    }

    private val _verifyCodeStateFlow: MutableStateFlow<Status<Pair<Boolean, String>>> =
        MutableStateFlow(Status.Default())
    val verifyCodeStateFlow: StateFlow<Status<Pair<Boolean, String>>> = _verifyCodeStateFlow

    fun verifyCode(email: String, code: Int) {
        _verifyCodeStateFlow.value = Status.Loading()

        viewModelScope.launch {
            when (val result = repository.verifyCode(email = email, code = code)) {
                is Resource.Success -> {
                    _verifyCodeStateFlow.value = Status.Success(result.data)
                }

                else -> {
                    _verifyCodeStateFlow.value =
                        Status.Error(result.code ?: 500, result.message ?: "Ocurrió un error.")
                }
            }
        }
    }

    private val _resetPasswordStateFlow: MutableStateFlow<Status<Pair<Boolean, String>>> =
        MutableStateFlow(Status.Default())
    val resetPasswordStateFlow: StateFlow<Status<Pair<Boolean, String>>> = _resetPasswordStateFlow

    fun resetPassword(password: String) {
        _resetPasswordStateFlow.value = Status.Loading()

        viewModelScope.launch {
            when (val result = repository.recoverPassword(newPassword = password)) {
                is Resource.Success -> {
                    _resetPasswordStateFlow.value = Status.Success(result.data)
                }

                else -> {
                    _resetPasswordStateFlow.value =
                        Status.Error(result.code ?: 500,result.message ?: "Ocurrió un error.")
                }
            }
        }
    }

    fun resetSendRecoveryState() {
        _sendRecoveryStateFlow.value = Status.Default()
    }

    fun resetVerifyCodeState() {
        _verifyCodeStateFlow.value = Status.Default()
    }
}