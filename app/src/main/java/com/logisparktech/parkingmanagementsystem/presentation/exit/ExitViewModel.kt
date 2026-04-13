package com.logisparktech.parkingmanagementsystem.presentation.exit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logisparktech.parkingmanagementsystem.data.local.entities.TicketEntity
import com.logisparktech.parkingmanagementsystem.domain.use_case.CloseTicketUseCase
import com.logisparktech.parkingmanagementsystem.domain.use_case.GetActiveTicketUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.logisparktech.parkingmanagementsystem.data.local.preferences.PreferenceManager
import com.logisparktech.parkingmanagementsystem.core.printer.PrinterManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltViewModel
class ExitViewModel @Inject constructor(
    private val getActiveTicketUseCase: GetActiveTicketUseCase,
    private val getTicketByIdUseCase: com.logisparktech.parkingmanagementsystem.domain.use_case.GetTicketByIdUseCase,
    private val closeTicketUseCase: CloseTicketUseCase,
    private val rateRepository: com.logisparktech.parkingmanagementsystem.domain.repository.RateRepository,
    private val calculateAmountUseCase: com.logisparktech.parkingmanagementsystem.domain.use_case.CalculateAmountUseCase,
    private val getDurationUseCase: com.logisparktech.parkingmanagementsystem.domain.use_case.GetDurationUseCase,
    private val printerManager: PrinterManager,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _ticket = MutableStateFlow<TicketEntity?>(null)
    val ticket: StateFlow<TicketEntity?> = _ticket.asStateFlow()

    private val _uiState = MutableStateFlow<ExitUiState>(ExitUiState.Idle)
    val uiState: StateFlow<ExitUiState> = _uiState.asStateFlow()

    fun searchTicket(query: String) {
        viewModelScope.launch {
            _uiState.value = ExitUiState.Loading
            
            val ticket = getTicketByIdUseCase(query) ?: getActiveTicketUseCase(query)
            
            if (ticket != null) {
                if (ticket.isClosed) {
                    _uiState.value = ExitUiState.Error("This ticket is already closed")
                } else {
                    _ticket.value = ticket
                    val rate = rateRepository.getRateById(ticket.rateId)
                    val vehicleType = rate?.vehicleType ?: "Standard"
                    val pricePerHour = rate?.pricePerHour ?: 0.0
                    val halfHourCost = rate?.halfHourCost ?: 0.0
                    val exceedingMin = rate?.exceedingMin ?: 0
                    val isActive30Min = rate?.active30Min ?: false
                    
                    val now = System.currentTimeMillis()
                    val estimatedAmount = calculateAmountUseCase(
                        entryTime = ticket.entryTime,
                        exitTime =  now,
                        rate = pricePerHour,
                        halfHourCost = halfHourCost,
                        exceedingLimitMin = exceedingMin,
                        is30MinActivation = isActive30Min
                    )
                    
                    val durationStr = getDurationUseCase(ticket.entryTime, now, exceedingMin)

                    _uiState.value = ExitUiState.Found(
                        ticket = ticket,
                        vehicleType = vehicleType,
                        duration = durationStr,
                        estimatedAmount = estimatedAmount
                    )
                }
            } else {
                _uiState.value = ExitUiState.Error("No active ticket found for: $query")
            }
        }
    }

    fun searchTicketById(ticketId: String) {
        searchTicket(ticketId)
    }

    fun closeTicket(ticketId: String) {
        viewModelScope.launch {
            _uiState.value = ExitUiState.Loading
            val exitTime = System.currentTimeMillis()
            val result = closeTicketUseCase(ticketId, exitTime)
            result.onSuccess { closedTicket ->
                val dateSdf = SimpleDateFormat("yyyy MMM dd", Locale.US)
                val timeSdf = SimpleDateFormat("hh:mm:ss a", Locale.US)
                
                val entryTimeLong = closedTicket.entryTime
                val exitTimeLong = closedTicket.exitTime ?: exitTime
                val durationStr = getDurationUseCase(entryTimeLong, exitTimeLong)

                printerManager.printReceipt(
                    branchName = preferenceManager.getBranch(),
                    vehicleNumber = closedTicket.vehicleNumber,
                    entryDate = dateSdf.format(Date(entryTimeLong)),
                    entryTime = timeSdf.format(Date(entryTimeLong)),
                    exitDate = dateSdf.format(Date(exitTimeLong)),
                    exitTime = timeSdf.format(Date(exitTimeLong)),
                    amount = closedTicket.amount,
                    ticketId = closedTicket.ticketId,
                    duration = durationStr
                )
                _uiState.value = ExitUiState.Success
            }.onFailure {
                _uiState.value = ExitUiState.Error(it.message ?: "Failed to close ticket")
            }
        }
    }

    fun resetState() {
        _uiState.value = ExitUiState.Idle
        _ticket.value = null
    }

    sealed class ExitUiState {
        object Idle : ExitUiState()
        object Loading : ExitUiState()
        data class Found(
            val ticket: TicketEntity,
            val vehicleType: String,
            val duration: String,
            val estimatedAmount: Double
        ) : ExitUiState()
        object Success : ExitUiState()
        data class Error(val message: String) : ExitUiState()
    }
}
