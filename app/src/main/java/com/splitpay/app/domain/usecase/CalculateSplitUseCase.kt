package com.splitpay.app.domain.usecase

import com.splitpay.app.data.model.ChunkStatus
import com.splitpay.app.data.model.PaymentChunk
import javax.inject.Inject

class CalculateSplitUseCase @Inject constructor() {

    companion object {
        const val MAX_CHUNK_LIMIT: Long = 1_999L
    }

    /**
     * Splits an amount into chunks where every chunk is <= MAX_CHUNK_LIMIT (₹1,999).
     * Returns a list of PaymentChunk objects.
     * Throws IllegalArgumentException if amount <= 0.
     */
    operator fun invoke(amount: Long): List<PaymentChunk> {
        require(amount > 0L) { "Amount must be greater than zero" }

        if (amount <= MAX_CHUNK_LIMIT) {
            return listOf(
                PaymentChunk(
                    index = 1,
                    amount = amount,
                    status = ChunkStatus.PENDING
                )
            )
        }

        val chunks = mutableListOf<PaymentChunk>()
        var remainingAmount = amount
        var currentIndex = 1

        while (remainingAmount > 0L) {
            val chunkAmount = if (remainingAmount > MAX_CHUNK_LIMIT) MAX_CHUNK_LIMIT else remainingAmount
            chunks.add(
                PaymentChunk(
                    index = currentIndex,
                    amount = chunkAmount,
                    status = ChunkStatus.PENDING
                )
            )
            remainingAmount -= chunkAmount
            currentIndex++
        }

        return chunks
    }
}
