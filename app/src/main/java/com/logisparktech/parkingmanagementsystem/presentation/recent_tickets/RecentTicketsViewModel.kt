package com.logisparktech.parkingmanagementsystem.presentation.recent_tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logisparktech.parkingmanagementsystem.data.local.entities.TicketEntity
import com.logisparktech.parkingmanagementsystem.domain.use_case.GetAllTicketsUseCase
import com.logisparktech.parkingmanagementsystem.domain.use_case.SyncParkingSalesUseCase
import com.logisparktech.parkingmanagementsystem.core.printer.PrinterManager
import com.logisparktech.parkingmanagementsystem.domain.repository.RateRepository
import com.logisparktech.parkingmanagementsystem.data.local.preferences.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class RecentTicketsViewModel @Inject constructor(
    private val getAllTicketsUseCase: GetAllTicketsUseCase,
    private val syncParkingSalesUseCase: SyncParkingSalesUseCase,
    private val printerManager: PrinterManager,
    private val rateRepository: RateRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _allTickets = MutableStateFlow<List<TicketEntity>>(emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _tickets = MutableStateFlow<List<TicketEntity>>(emptyList())
    val tickets: StateFlow<List<TicketEntity>> = _tickets.asStateFlow()

    private val _rateTypeMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val rateTypeMap: StateFlow<Map<String, String>> = _rateTypeMap.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _syncEvent = MutableSharedFlow<SyncResult>()
    val syncEvent = _syncEvent.asSharedFlow()

    init {
        loadTickets()
        loadRates()
        // Keep filtered list in sync whenever raw data or query changes
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(_allTickets, _searchQuery) { all, query ->
                if (query.isBlank()) all
                else all.filter { ticket ->
                    ticket.vehicleNumber.contains(query, ignoreCase = true) ||
                    ticket.ticketId.contains(query, ignoreCase = true)
                }
            }.collect { filtered ->
                _tickets.value = filtered
            }
        }
    }

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }


    private fun loadTickets() {
        viewModelScope.launch {
            _isLoading.value = true
            _allTickets.value = getAllTicketsUseCase().sortedByDescending { it.entryTime }
            _isLoading.value = false
        }
    }
    private fun loadRates() {
        viewModelScope.launch {
            val rates = rateRepository.getAllRates()
            _rateTypeMap.value = rates.associate { it.rateId to it.vehicleType }
        }
    }

    fun refreshTickets() {
        loadTickets()
        loadRates()
    }

    fun syncTickets() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = syncParkingSalesUseCase()
            if (result.isSuccess) {
                _syncEvent.emit(SyncResult.Success)
                loadTickets()
            } else {
                _syncEvent.emit(SyncResult.Error(result.exceptionOrNull()?.message ?: "Sync failed"))
            }
            _isLoading.value = false
        }
    }

    fun reprintTicket(ticket: TicketEntity) {
        viewModelScope.launch {
            val nepalTimeZone = TimeZone.getTimeZone("Asia/Kathmandu")
            val dateSdf = SimpleDateFormat("dd-MM-yyyy", Locale.US).apply {
                timeZone = nepalTimeZone
            }
            val timeSdf = SimpleDateFormat("hh:mm a",Locale.US).apply {
                timeZone = nepalTimeZone
            }

            val entryDate = dateSdf.format(Date(ticket.entryTime))
            val entryTime = timeSdf.format(Date(ticket.entryTime))

            val rate = rateRepository.getRateById(ticket.rateId)
            val vehicleType = rate?.vehicleType ?: "Unknown"

            if (ticket.isClosed && ticket.exitTime != null) {
                val exitDate = dateSdf.format(Date(ticket.exitTime))
                val exitTime = timeSdf.format(Date(ticket.exitTime))

                printerManager.printReceipt(
                    vehicleNumber = ticket.vehicleNumber,
                    entryDate = entryDate,
                    entryTime = entryTime,
                    exitDate = exitDate,
                    exitTime = exitTime,
                    amount = ticket.amount,
                    ticketId = ticket.ticketId,
                    duration = null // Duration can be calculated if needed
                )
            } else {
                val qrJson = JSONObject().apply {
                    put("ticketNumber", ticket.ticketId)
                    put("tokenNumber", ticket.ticketId) // Fallback
                    put("vehicleNumber", ticket.vehicleNumber)
                    put("vehicleType", vehicleType)
                    put("checkInTime", "$entryDate $entryTime")
                    put("location", "MAIN")
                    put("rate", JSONObject().apply {
                        put("pricePerHour", rate?.pricePerHour ?: 0.0)
                        put("halfHourCost", rate?.halfHourCost ?: 0.0)
                        put("exceedingMin", rate?.exceedingMin ?: 0)
                        put("active30Min", rate?.active30Min ?: false)
                    })
                    put("operator", preferenceManager.getName())
                    put("uniqueTicketId", ticket.uuid)
                    put("metadata", JSONObject().apply {
                        put("isSynced", ticket.isSynced)
                    })
                }

                printerManager.printTicket(
                    ticketId = ticket.ticketId,
                    vehicleNumber = ticket.vehicleNumber,
                    vehicleType = vehicleType,
                    entryDate = entryDate,
                    entryTime = entryTime,
                    qrCodeContent = qrJson.toString()
                )
            }
        }
    }
}

sealed class SyncResult {
    object Success : SyncResult()
    data class Error(val message: String) : SyncResult()
}
