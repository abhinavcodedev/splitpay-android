package com.splitpay.app.data.model

data class PaymentChunk(
    val index: Int,
    val amount: Long,
    val status: ChunkStatus = ChunkStatus.PENDING,
    val timestamp: String? = null
)
