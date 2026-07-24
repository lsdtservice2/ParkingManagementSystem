package com.logisparktech.parkingmanagementsystem.presentation.rate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logisparktech.parkingmanagementsystem.data.local.entities.RateEntity
import com.logisparktech.parkingmanagementsystem.domain.use_case.GetRatesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RateViewModel @Inject constructor(
    private val getRatesUseCase: GetRatesUseCase,
    private val syncRatesUseCase: com.logisparktech.parkingmanagementsystem.domain.use_case.SyncRatesUseCase
) : ViewModel() {

    private val _rates = MutableStateFlow<List<RateEntity>>(emptyList())
    val rates: StateFlow<List<RateEntity>> = _rates.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadRates()
    }

    private fun loadRates() {
        viewModelScope.launch {
            _isLoading.value = true
            _rates.value = getRatesUseCase()
            _isLoading.value = false
        }
    }

    fun syncRates() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = syncRatesUseCase("MAIN")
            if (result.isSuccess) {
                _rates.value = getRatesUseCase()
            }
            _isLoading.value = false
        }
    }
}
