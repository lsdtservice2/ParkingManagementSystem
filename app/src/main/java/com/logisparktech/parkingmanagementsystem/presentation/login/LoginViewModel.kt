package com.logisparktech.parkingmanagementsystem.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logisparktech.parkingmanagementsystem.data.local.preferences.PreferenceManager
import com.logisparktech.parkingmanagementsystem.data.remote.dto.LoginResponse
import com.logisparktech.parkingmanagementsystem.domain.use_case.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val syncRatesUseCase: com.logisparktech.parkingmanagementsystem.domain.use_case.SyncRatesUseCase,
    private val preferenceManager: PreferenceManager,
    private val ticketRepository: com.logisparktech.parkingmanagementsystem.domain.repository.TicketRepository,
    private val rateRepository: com.logisparktech.parkingmanagementsystem.domain.repository.RateRepository
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _state.value = LoginState.Loading

            // Cleanup old data before login
            ticketRepository.deleteClosedAndSyncedTickets()
            rateRepository.clearAllRates()

            val result = loginUseCase(username, password)
            result.onSuccess { response ->
                val userData = response.data
                if (userData != null) {
                    // Save user data to preferences
                    preferenceManager.saveUserData(
                        name = userData.name,
                        contact = userData.contact,
                        branch = userData.branch,
                        code = userData.code
                    )
                    
                    // Auto sync rates after first login
                    syncRatesUseCase(userData.branch)

                    _state.value = LoginState.Success(response)
                } else {
                    _state.value = LoginState.Error("User data not found")
                }
            }.onFailure {
                _state.value = LoginState.Error(it.message ?: "Unknown error")
            }
        }
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val response: LoginResponse) : LoginState()
        data class Error(val message: String) : LoginState()
    }
}
