package com.eguerra.ciudadanodigital.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eguerra.ciudadanodigital.data.Resource
import com.eguerra.ciudadanodigital.data.local.entity.UserModel
import com.eguerra.ciudadanodigital.data.repository.AuthRepository
import com.eguerra.ciudadanodigital.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UserSessionStatus {
    class Logged(val data: UserModel) : UserSessionStatus()
    class NotLogged(val error: String?) : UserSessionStatus()
    data object Default : UserSessionStatus()
}

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _userDataStateFlow: MutableStateFlow<UserSessionStatus> = MutableStateFlow(
        UserSessionStatus.Default
    )
    val userDataStateFlow: StateFlow<UserSessionStatus> = _userDataStateFlow

    fun getUserData(remote: Boolean) {

        _userDataStateFlow.value = UserSessionStatus.Default

        viewModelScope.launch {
            when (val result = userRepository.getLoggedUserData(remote)) {
                is Resource.Success -> {
                    _userDataStateFlow.value = UserSessionStatus.Logged(result.data)
                }

                else -> {
                    _userDataStateFlow.value = UserSessionStatus.NotLogged(result.message)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _userDataStateFlow.value = UserSessionStatus.NotLogged(null)
        }
    }

}