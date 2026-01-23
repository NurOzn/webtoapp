package com.melikenurozun.webtoapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melikenurozun.webtoapp.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    val isLoggedIn: Boolean
        get() = authRepository.isLoggedIn
    
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all fields")
            return
        }
        
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.login(email, password)
                .onSuccess { _authState.value = AuthState.Success }
                .onFailure { _authState.value = AuthState.Error(getErrorMessage(it)) }
        }
    }
    
    fun register(email: String, password: String, confirmPassword: String) {
        when {
            email.isBlank() || password.isBlank() -> {
                _authState.value = AuthState.Error("Please fill in all fields")
                return
            }
            password != confirmPassword -> {
                _authState.value = AuthState.Error("Passwords do not match")
                return
            }
            password.length < 6 -> {
                _authState.value = AuthState.Error("Password must be at least 6 characters")
                return
            }
        }
        
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.register(email, password)
                .onSuccess { _authState.value = AuthState.Success }
                .onFailure { _authState.value = AuthState.Error(getErrorMessage(it)) }
        }
    }
    
    fun logout() {
        authRepository.logout()
    }
    
    fun resetState() {
        _authState.value = AuthState.Idle
    }
    
    private fun getErrorMessage(throwable: Throwable): String {
        return when {
            throwable.message?.contains("email", ignoreCase = true) == true -> 
                "Invalid email format"
            throwable.message?.contains("password", ignoreCase = true) == true -> 
                "Incorrect password"
            throwable.message?.contains("no user", ignoreCase = true) == true -> 
                "No account found with this email"
            throwable.message?.contains("already", ignoreCase = true) == true -> 
                "This email is already registered"
            throwable.message?.contains("network", ignoreCase = true) == true -> 
                "Network error. Please check your connection"
            else -> throwable.message ?: "An unexpected error occurred"
        }
    }
}
