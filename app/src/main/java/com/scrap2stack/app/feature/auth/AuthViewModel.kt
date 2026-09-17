 package com.scrap2stack.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.login(email, password)
                .onSuccess {
                    _authState.value = AuthState.Success("Login successful")
                }
                .onFailure { e ->
                    _authState.value = AuthState.Error(e.message ?: "Login failed")
                }
        }
    }

    fun register(name: String, username: String, email: String, password: String) {
        if (name.isBlank() || username.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("All fields are required")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.register(name, username, email, password)
                .onSuccess {
                    _authState.value = AuthState.Success("Registration successful. Please check your email for verification.")
                }
                .onFailure { e ->
                    _authState.value = AuthState.Error(e.message ?: "Registration failed")
                }
        }
    }

    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            _authState.value = AuthState.Error("Email is required")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.forgotPassword(email)
                .onSuccess {
                    _authState.value = AuthState.Success("Password reset email sent")
                }
                .onFailure { e ->
                    _authState.value = AuthState.Error(e.message ?: "Failed to send reset email")
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _authState.value = AuthState.Idle
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    suspend fun isUserLoggedIn(): Boolean {
        return repository.isUserLoggedIn()
    }
    
    suspend fun setOnboardingCompleted(completed: Boolean) {
        repository.setOnboardingCompleted(completed)
    }

    fun isOnboardingCompleted(): Flow<Boolean> = repository.isOnboardingCompleted()
}
