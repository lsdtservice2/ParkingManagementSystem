package com.logisparktech.parkingmanagementsystem.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logisparktech.parkingmanagementsystem.data.local.preferences.PreferenceManager
import com.logisparktech.parkingmanagementsystem.domain.repository.TicketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager,
    private val ticketRepository: TicketRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        _uiState.value = ProfileUiState(
            userName = preferenceManager.getName(),
            userContact = preferenceManager.getContact()
        )
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            // Optional: Cleanup old synced tickets on logout
            ticketRepository.deleteClosedAndSyncedTickets()
            // Clear all preferences (user data, login state, etc.)
            preferenceManager.clear()
            onLogoutSuccess()
        }
    }
}

data class ProfileUiState(
    val userName: String = "",
    val userContact: String = ""
)