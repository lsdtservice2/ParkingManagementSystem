package com.logisparktech.parkingmanagementsystem.presentation.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logisparktech.parkingmanagementsystem.data.local.entities.RateEntity
import com.logisparktech.parkingmanagementsystem.data.local.entities.TicketEntity
import com.logisparktech.parkingmanagementsystem.data.local.preferences.PreferenceManager
import com.logisparktech.parkingmanagementsystem.domain.use_case.CreateTicketUseCase
import com.logisparktech.parkingmanagementsystem.domain.use_case.GetRatesUseCase
import com.logisparktech.parkingmanagementsystem.domain.use_case.SyncRatesUseCase
import com.logisparktech.parkingmanagementsystem.domain.use_case.GetUnsyncedCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.logisparktech.parkingmanagementsystem.core.printer.PrinterManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

@HiltViewModel
class EntryViewModel @Inject constructor(
    private val createTicketUseCase: CreateTicketUseCase,
    private val getUnsyncedCountUseCase: GetUnsyncedCountUseCase, // Add this use case
    private val getRatesUseCase: GetRatesUseCase,
    private val syncRatesUseCase: SyncRatesUseCase,
    private val preferenceManager: PreferenceManager,
    private val printerManager: PrinterManager
) : ViewModel() {

    private val _rates = MutableStateFlow<List<RateEntity>>(emptyList())
    val rates: StateFlow<List<RateEntity>> = _rates.asStateFlow()

    private val _uiState = MutableStateFlow<EntryUiState>(EntryUiState.Idle)
    val uiState: StateFlow<EntryUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _unsyncedCount = MutableStateFlow(0)
    val unsyncedCount: StateFlow<Int> = _unsyncedCount.asStateFlow()

    init {
        syncAndLoadRates()
        updateUnsyncedCount()
    }

    private fun updateUnsyncedCount() {
        viewModelScope.launch {
            _unsyncedCount.value = getUnsyncedCountUseCase()
        }
    }

    private fun syncAndLoadRates() {
        viewModelScope.launch {
            _uiState.value = EntryUiState.Loading

            // Load from local DB first to show data quickly
            val localRates = getRatesUseCase()
            _rates.value = localRates

            // If local data is empty, try to sync from remote
            if (localRates.isEmpty()) {
                val result = syncRatesUseCase("MAIN")
                if (result.isSuccess) {
                    _rates.value = getRatesUseCase()
                }
            }

            _uiState.value = EntryUiState.Idle
        }
    }

    fun refreshRates() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _rates.value = getRatesUseCase()
            _isRefreshing.value = false
        }
    }

    //    fun createTicket(vehicleNumber: String, rateId: String) {
////        android.util.Log.d("EntryViewModel", "createTicket called with: $vehicleNumber, $rateId")
//        viewModelScope.launch {
//            _uiState.value = EntryUiState.Loading
//
//            val unsyncedCount = getUnsyncedCountUseCase()
//            if (unsyncedCount >= 1000) {
//                _uiState.value =
//                    EntryUiState.Error("Sync limit reached (1000 tickets). Please sync data from Tickets screen.")
//                return@launch
//            }
//
//
//            val ticketId = preferenceManager.generateTicketCode()
//            val ticket = TicketEntity(
//                ticketId = ticketId,
//                vehicleNumber = vehicleNumber,
//                entryTime = System.currentTimeMillis(),
//                exitTime = null,
//                rateId = rateId,
//                isClosed = false,
//                amount = 0.0,
//                isSynced = false
//            )
//            try {
//                createTicketUseCase(ticket)
//
//                // Print Ticket
//                val selectedRate = _rates.value.find { it.rateId == rateId }
//                val dateSdf = SimpleDateFormat("yyyy MMM dd", Locale.US)
//                val timeSdf = SimpleDateFormat("hh:mm:ss a", Locale.US)
//                val entryDate = dateSdf.format(Date(ticket.entryTime))
//                val entryTime = timeSdf.format(Date(ticket.entryTime))
//
//                printerManager.printTicket(
//                    branchName = preferenceManager.getBranch(),
//                    ticketId = ticketId,
//                    vehicleNumber = vehicleNumber,
//                    vehicleType = selectedRate?.vehicleType ?: "Unknown",
//                    entryDate = entryDate,
//                    entryTime = entryTime,
//                    qrCodeContent = ticketId
//                )
//                updateUnsyncedCount()
//                _uiState.value = EntryUiState.Success
//            } catch (e: Exception) {
//                _uiState.value = EntryUiState.Error(e.message ?: "Failed to create ticket")
//            }
//        }
//    }
    fun createTicket(vehicleNumber: String, rateId: String) {
        if (vehicleNumber.isBlank()) {
            _uiState.value = EntryUiState.Error("Please enter vehicle number")
            return
        }

        viewModelScope.launch {
            if (_uiState.value is EntryUiState.Loading) return@launch
            _uiState.value = EntryUiState.Loading

            try {
                // 1. Validate Rate exists before doing anything
                val selectedRate = _rates.value.find { it.rateId == rateId }
                    ?: throw Exception("Invalid Vehicle Type selected. Please refresh rates.")

                val unsyncedCount = getUnsyncedCountUseCase()
                if (unsyncedCount >= 1000) {
                    _uiState.value = EntryUiState.Error("Sync limit reached. Please sync data first.")
                    return@launch
                }

                val currentTime = System.currentTimeMillis()
                val ticketId = preferenceManager.generateTicketCode(selectedRate.vehicleType)

                val ticket = TicketEntity(
                    ticketId = ticketId,
                    uuid = UUID.randomUUID().toString(),
                    vehicleNumber = vehicleNumber.trim().uppercase(Locale.US),
                    entryTime = currentTime,
                    exitTime = null,
                    rateId = rateId,
                    isClosed = false,
                    amount = 0.0,
                    isSynced = false
                )

                // 2. Perform DB Insertion
                createTicketUseCase(ticket)

                // 3. Attempt Printing
                try {
                    val nepalTimeZone = TimeZone.getTimeZone("Asia/Kathmandu")
                    val dateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = nepalTimeZone }
                    val timeSdf = SimpleDateFormat("hh:mm:ss a", Locale.US).apply { timeZone = nepalTimeZone }

                    printerManager.printTicket(
                        ticketId = ticketId,
                        vehicleNumber = ticket.vehicleNumber,
                        vehicleType = selectedRate.vehicleType,
                        entryDate = dateSdf.format(Date(currentTime)),
                        entryTime = timeSdf.format(Date(currentTime)),
                        rate = selectedRate.pricePerHour,
                        qrCodeContent = ticketId
                    )
                } catch (printError: Exception) {
                    // IMPORTANT: Ticket is saved, but print failed.
                    // Inform user so they don't try to recreate it.
                    _uiState.value = EntryUiState.Error("Ticket saved (#$ticketId) but Printer Failed: ${printError.message}")
                    updateUnsyncedCount()
                    return@launch
                }

                updateUnsyncedCount()
                _uiState.value = EntryUiState.Success(ticketId)

            } catch (e: Exception) {
                _uiState.value = EntryUiState.Error(e.message ?: "Failed to create ticket")
            }
        }
    }
    fun resetState() {
        _uiState.value = EntryUiState.Idle
    }

    sealed class EntryUiState {
        data object Idle : EntryUiState()
        data object Loading : EntryUiState()
        data class Success(val ticketId: String) : EntryUiState()
        data class Error(val message: String) : EntryUiState()
    }
}
