package com.splitpay.app.domain.usecase

import com.splitpay.app.data.model.ChunkStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalculateSplitUseCaseTest {

    private lateinit var useCase: CalculateSplitUseCase

    @Before
    fun setUp() {
        useCase = CalculateSplitUseCase()
    }

    @Test
    fun `calculateSplit for 1500 returns single chunk of 1500`() {
        val result = useCase(1500L)

        assertEquals(1, result.size)
        assertEquals(1500L, result[0].amount)
        assertEquals(1, result[0].index)
        assertEquals(ChunkStatus.PENDING, result[0].status)
        assertEquals(1500L, result.sumOf { it.amount })
        assertTrue(result.all { it.amount <= 1999L })
    }

    @Test
    fun `calculateSplit for 1999 returns single chunk of 1999`() {
        val result = useCase(1999L)

        assertEquals(1, result.size)
        assertEquals(1999L, result[0].amount)
        assertEquals(1, result[0].index)
        assertEquals(1999L, result.sumOf { it.amount })
        assertTrue(result.all { it.amount <= 1999L })
    }

    @Test
    fun `calculateSplit for 2000 returns 1999 and 1`() {
        val result = useCase(2000L)

        assertEquals(2, result.size)
        assertEquals(1999L, result[0].amount)
        assertEquals(1L, result[1].amount)
        assertEquals(2000L, result.sumOf { it.amount })
        assertTrue(result.all { it.amount <= 1999L })
    }

    @Test
    fun `calculateSplit for 2001 returns 1999 and 2`() {
        val result = useCase(2001L)

        assertEquals(2, result.size)
        assertEquals(1999L, result[0].amount)
        assertEquals(2L, result[1].amount)
        assertEquals(2001L, result.sumOf { it.amount })
        assertTrue(result.all { it.amount <= 1999L })
    }

    @Test
    fun `calculateSplit for 4000 returns 1999 1999 and 2`() {
        val result = useCase(4000L)

        assertEquals(3, result.size)
        assertEquals(1999L, result[0].amount)
        assertEquals(1999L, result[1].amount)
        assertEquals(2L, result[2].amount)
        assertEquals(4000L, result.sumOf { it.amount })
        assertTrue(result.all { it.amount <= 1999L })
    }

    @Test
    fun `calculateSplit for 10000 returns five 1999 chunks and one 5 chunk`() {
        val result = useCase(10000L)

        assertEquals(6, result.size)
        assertEquals(1999L, result[0].amount)
        assertEquals(1999L, result[1].amount)
        assertEquals(1999L, result[2].amount)
        assertEquals(1999L, result[3].amount)
        assertEquals(1999L, result[4].amount)
        assertEquals(5L, result[5].amount)
        assertEquals(10000L, result.sumOf { it.amount })
        assertTrue(result.all { it.amount <= 1999L })
    }

    @Test(expected = IllegalArgumentException::class)
    fun `calculateSplit for zero throws IllegalArgumentException`() {
        useCase(0L)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `calculateSplit for negative amount throws IllegalArgumentException`() {
        useCase(-5000L)
    }
}
