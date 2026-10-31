package com.splitpay.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitpay.app.data.model.ChunkStatus
import com.splitpay.app.domain.usecase.CalculateSplitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SplitPayViewModel @Inject constructor(
    private val calculateSplitUseCase: CalculateSplitUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplitPayUiState())
    val uiState: StateFlow<SplitPayUiState> = _uiState.asStateFlow()

    private var simulationJob: Job? = null

    init {
        // Initial calculation for default amount (₹10,000)
        calculateSplit()
    }

    fun onAmountChanged(value: String) {
        val cleanValue = value.filter { it.isDigit() }.take(10)
        val parsed = cleanValue.toLongOrNull() ?: 0L
        _uiState.update {
            it.copy(
                rawAmountInput = cleanValue,
                parsedAmount = parsed,
                errorMessage = null
            )
        }
    }

    fun onSmartPayToggled(enabled: Boolean) {
        _uiState.update {
            it.copy(isSmartPayEnabled = enabled)
        }
    }

    fun calculateSplit() {
        val amount = _uiState.value.parsedAmount
        if (amount <= 0L) {
            _uiState.update {
                it.copy(
                    calculatedChunks = emptyList(),
                    errorMessage = "Amount must be greater than zero"
                )
            }
            return
        }

        try {
            val chunks = calculateSplitUseCase(amount)
            _uiState.update {
                it.copy(
                    calculatedChunks = chunks,
                    isProcessingActive = false,
                    isPaymentCompleted = false,
                    currentProcessingIndex = -1,
                    errorMessage = null
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    calculatedChunks = emptyList(),
                    errorMessage = e.message
                )
            }
        }
    }

    fun startPaymentSimulation(delayMillis: Long = 750L) {
        // Prevent duplicate simulation jobs if already active or already completed
        if (_uiState.value.isProcessingActive || _uiState.value.isPaymentCompleted) {
            return
        }

        // Validate chunk list is non-empty
        if (_uiState.value.calculatedChunks.isEmpty()) {
            calculateSplit()
        }
        val currentChunks = _uiState.value.calculatedChunks
        if (currentChunks.isEmpty()) return

        // Reset chunks: first chunk is immediately PROCESSING, subsequent chunks are PENDING
        val resetChunks = currentChunks.mapIndexed { index, chunk ->
            if (index == 0) {
                chunk.copy(status = ChunkStatus.PROCESSING, timestamp = null)
            } else {
                chunk.copy(status = ChunkStatus.PENDING, timestamp = null)
            }
        }

        _uiState.update {
            it.copy(
                calculatedChunks = resetChunks,
                isProcessingActive = true,
                isPaymentCompleted = false,
                currentProcessingIndex = 0
            )
        }

        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            val chunks = resetChunks.toMutableList()

            for (i in chunks.indices) {
                // Ensure current chunk is marked PROCESSING
                chunks[i] = chunks[i].copy(status = ChunkStatus.PROCESSING, timestamp = null)
                _uiState.update {
                    it.copy(
                        calculatedChunks = chunks.toList(),
                        currentProcessingIndex = i
                    )
                }

                // Short simulated delay (750ms default)
                delay(delayMillis)

                // Mark current chunk SUCCESS with completion timestamp
                chunks[i] = chunks[i].copy(
                    status = ChunkStatus.SUCCESS,
                    timestamp = getCurrentTimeFormatted()
                )

                // If next chunk exists, mark it PROCESSING immediately for clean sequential transition
                if (i + 1 < chunks.size) {
                    chunks[i + 1] = chunks[i + 1].copy(status = ChunkStatus.PROCESSING, timestamp = null)
                }

                _uiState.update {
                    it.copy(
                        calculatedChunks = chunks.toList(),
                        currentProcessingIndex = if (i + 1 < chunks.size) i + 1 else i
                    )
                }
            }

            // All chunks completed
            _uiState.update {
                it.copy(
                    isProcessingActive = false,
                    isPaymentCompleted = true,
                    currentProcessingIndex = chunks.size - 1
                )
            }
        }
    }

    fun resetFlow() {
        simulationJob?.cancel()
        _uiState.update {
            it.copy(
                rawAmountInput = "10000",
                parsedAmount = 10_000L,
                isProcessingActive = false,
                isPaymentCompleted = false,
                currentProcessingIndex = -1,
                errorMessage = null
            )
        }
        calculateSplit()
    }

    fun getCurrentTimeFormatted(): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date())
    }
}
