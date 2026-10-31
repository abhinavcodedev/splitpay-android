package com.splitpay.app.presentation.viewmodel

import com.splitpay.app.data.model.MerchantInfo
import com.splitpay.app.data.model.PaymentChunk

data class SplitPayUiState(
    val merchantInfo: MerchantInfo = MerchantInfo(),
    val rawAmountInput: String = "10000",
    val parsedAmount: Long = 10_000L,
    val maxChunkLimit: Long = 1_999L,
    val isSmartPayEnabled: Boolean = true,
    val calculatedChunks: List<PaymentChunk> = emptyList(),
    val currentProcessingIndex: Int = -1,
    val isProcessingActive: Boolean = false,
    val isPaymentCompleted: Boolean = false,
    val errorMessage: String? = null
)
