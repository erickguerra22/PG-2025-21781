package com.eguerra.ciudadanodigital.ui.activity

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoadingViewModel : ViewModel() {

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun showLoadingDialog() {
        _isLoading.value = true
    }

    fun hideLoadingDialog() {
        _isLoading.value = false
    }
}