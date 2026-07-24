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

import com.logisparktech.parkingmanagementsystem.core.printer.PrinterManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@HiltViewModel
class ExitViewModel @Inject constructor(
    private val getActiveTicketUseCase: GetActiveTicketUseCase,
    private val getTicketByIdUseCase: com.logisparktech.parkingmanagementsystem.domain.use_case.GetTicketByIdUseCase,
    private val closeTicketUseCase: CloseTicketUseCase,
    private val rateRepository: com.logisparktech.parkingmanagementsystem.domain.repository.RateRepository,
    private val calculateAmountUseCase: com.logisparktech.parkingmanagementsystem.domain.use_case.CalculateAmountUseCase,
    private val getDurationUseCase: com.logisparktech.parkingmanagementsystem.domain.use_case.GetDurationUseCase,
    private val printerManager: PrinterManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExitUiState>(ExitUiState.Idle)
    val uiState: StateFlow<ExitUiState> = _uiState.asStateFlow()

    fun searchTicket(query: String) {
        viewModelScope.launch {
            _uiState.value = ExitUiState.Loading

            val foundTickets = mutableListOf<TicketEntity>()
            val ticketById = getTicketByIdUseCase(query)
            if (ticketById != null) {
                foundTickets.add(ticketById)
            } else {
                foundTickets.addAll(getActiveTicketUseCase(query))
            }

            if (foundTickets.isNotEmpty()) {
                val foundDataList = foundTickets.map { ticket ->
                    val rate = rateRepository.getRateById(ticket.rateId)
                    val vehicleType = rate?.vehicleType ?: "Standard"
                    val pricePerHour = rate?.pricePerHour ?: 0.0
                    val halfHourCost = rate?.halfHourCost ?: 0.0
                    val exceedingMin = rate?.exceedingMin ?: 0
                    val isActive30Min = rate?.active30Min ?: false

                    val now = System.currentTimeMillis()
                    val estimatedAmount = calculateAmountUseCase(
                        entryTime = ticket.entryTime,
                        exitTime = now,
                        rate = pricePerHour,
                        halfHourCost = halfHourCost,
                        exceedingLimitMin = exceedingMin,
                        is30MinActivation = isActive30Min
                    )

                    val durationStr = getDurationUseCase(ticket.entryTime, now, exceedingMin)

                    TicketFoundData(
                        ticket = ticket,
                        vehicleType = vehicleType,
                        duration = durationStr,
                        estimatedAmount = estimatedAmount,
                        exitTime = now
                    )
                }

                // Check if any found ticket is already closed (though query filter should prevent this)
                if (foundTickets.any { it.isClosed }) {
                    _uiState.value = ExitUiState.Error("One or more matching tickets are already closed")
                } else {
                    _uiState.value = ExitUiState.Found(foundDataList)
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
        val currentState = _uiState.value
        val exitTime = if (currentState is ExitUiState.Found) {
            currentState.tickets.find { it.ticket.ticketId == ticketId }?.exitTime ?: System.currentTimeMillis()
        } else {
            System.currentTimeMillis()
        }

        viewModelScope.launch {
            _uiState.value = ExitUiState.Loading
            val result = closeTicketUseCase(ticketId, exitTime)
            result.onSuccess { closedTicket ->
                val nepalTimeZone = TimeZone.getTimeZone("Asia/Kathmandu")
                val dateSdf = SimpleDateFormat("yyyy MMM dd", Locale.US).apply {
                    timeZone = nepalTimeZone
                }
                val timeSdf = SimpleDateFormat("hh:mm:ss a", Locale.US).apply {
                    timeZone = nepalTimeZone
                }
                
                val entryTimeLong = closedTicket.entryTime
                val exitTimeLong = closedTicket.exitTime ?: exitTime
                val durationStr = getDurationUseCase(entryTimeLong, exitTimeLong)

                printerManager.printReceipt(
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
    }

    sealed class ExitUiState {
        object Idle : ExitUiState()
        object Loading : ExitUiState()
        data class Found(
            val tickets: List<TicketFoundData>
        ) : ExitUiState()
        object Success : ExitUiState()
        data class Error(val message: String) : ExitUiState()
    }

    data class TicketFoundData(
        val ticket: TicketEntity,
        val vehicleType: String,
        val duration: String,
        val estimatedAmount: Double,
        val exitTime: Long
    )
}
