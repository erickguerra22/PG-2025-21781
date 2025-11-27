package com.eguerra.ciudadanodigital.ui.fragment.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eguerra.ciudadanodigital.data.Resource
import com.eguerra.ciudadanodigital.data.local.entity.UserModel
import com.eguerra.ciudadanodigital.data.repository.UserRepository
import com.eguerra.ciudadanodigital.ui.Status
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _updateStateFlow: MutableStateFlow<Status<Pair<String, UserModel>>> =
        MutableStateFlow(Status.Default())
    val updateStateFlow: StateFlow<Status<Pair<String, UserModel>>> = _updateStateFlow

    fun updateUser(
        email: String,
        name: String,
        lastname: String,
        birthdate: String,
        phoneCode: String,
        phoneNumber: String,
    ) {
        _updateStateFlow.value = Status.Loading()

        viewModelScope.launch {
            when (val result = repository.updateUser(
                email = email,
                name = name,
                lastname = lastname,
                phoneCode = phoneCode,
                phoneNumber = phoneNumber,
                birthdate = birthdate
            )) {
                is Resource.Success -> {
                    _updateStateFlow.value = Status.Success(
                        Pair(
                            result.message ?: "Usuario actualizado correctamente", result.data
                        )
                    )
                }

                else -> {
                    _updateStateFlow.value = Status.Error(
                        result.code ?: 500,
                        result.message ?: "Ocurrió un error al registrar al usuario."
                    )
                }
            }
        }
    }
}