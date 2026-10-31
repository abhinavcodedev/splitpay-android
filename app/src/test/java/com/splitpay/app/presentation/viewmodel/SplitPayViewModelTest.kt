package com.splitpay.app.presentation.viewmodel

import com.splitpay.app.data.model.ChunkStatus
import com.splitpay.app.domain.usecase.CalculateSplitUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplitPayViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var useCase: CalculateSplitUseCase
    private lateinit var viewModel: SplitPayViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        useCase = CalculateSplitUseCase()
        viewModel = SplitPayViewModel(useCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state defaults to 10000 with 6 pending chunks`() {
        val state = viewModel.uiState.value

        assertEquals(10_000L, state.parsedAmount)
        assertEquals(6, state.calculatedChunks.size)
        assertTrue(state.calculatedChunks.all { it.status == ChunkStatus.PENDING })
        assertTrue(state.calculatedChunks.all { it.timestamp == null })
        assertFalse(state.isProcessingActive)
        assertFalse(state.isPaymentCompleted)
    }

    @Test
    fun `empty chunk list or zero amount does not start simulation`() = runTest {
        viewModel.onAmountChanged("0")
        viewModel.calculateSplit()

        val stateBefore = viewModel.uiState.value
        assertEquals(0, stateBefore.calculatedChunks.size)

        viewModel.startPaymentSimulation(delayMillis = 100L)

        val stateAfter = viewModel.uiState.value
        assertFalse(stateAfter.isProcessingActive)
        assertFalse(stateAfter.isPaymentCompleted)
    }

    @Test
    fun `sequential simulation transitions each chunk from PENDING to PROCESSING to SUCCESS`() = runTest {
        val stepDelay = 200L
        viewModel.startPaymentSimulation(delayMillis = stepDelay)

        // Initial launch: chunk 0 is PROCESSING, others PENDING
        var state = viewModel.uiState.value
        assertTrue(state.isProcessingActive)
        assertFalse(state.isPaymentCompleted)
        assertEquals(ChunkStatus.PROCESSING, state.calculatedChunks[0].status)
        assertNull(state.calculatedChunks[0].timestamp)
        for (i in 1 until state.calculatedChunks.size) {
            assertEquals(ChunkStatus.PENDING, state.calculatedChunks[i].status)
            assertNull(state.calculatedChunks[i].timestamp)
        }

        // Advance past chunk 0 delay: chunk 0 becomes SUCCESS, chunk 1 becomes PROCESSING
        advanceTimeBy(stepDelay + 10L)
        state = viewModel.uiState.value
        assertEquals(ChunkStatus.SUCCESS, state.calculatedChunks[0].status)
        assertNotNull(state.calculatedChunks[0].timestamp)
        assertEquals(ChunkStatus.PROCESSING, state.calculatedChunks[1].status)
        assertNull(state.calculatedChunks[1].timestamp)

        // Only one chunk is PROCESSING at any time
        assertEquals(1, state.calculatedChunks.count { it.status == ChunkStatus.PROCESSING })

        // Complete remaining chunks
        advanceUntilIdle()
        state = viewModel.uiState.value

        assertFalse(state.isProcessingActive)
        assertTrue(state.isPaymentCompleted)
        assertEquals(6, state.calculatedChunks.size)
        assertTrue(state.calculatedChunks.all { it.status == ChunkStatus.SUCCESS })
        assertTrue(state.calculatedChunks.all { it.timestamp != null })
        assertEquals(10_000L, state.calculatedChunks.sumOf { it.amount })
    }

    @Test
    fun `duplicate taps on startPaymentSimulation are ignored during active processing`() = runTest {
        viewModel.startPaymentSimulation(delayMillis = 500L)
        assertTrue(viewModel.uiState.value.isProcessingActive)

        // Trigger duplicate tap
        viewModel.startPaymentSimulation(delayMillis = 500L)
        assertTrue(viewModel.uiState.value.isProcessingActive)

        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isPaymentCompleted)
    }

    @Test
    fun `resetFlow cancels simulation and resets state to default`() = runTest {
        viewModel.startPaymentSimulation(delayMillis = 500L)
        viewModel.resetFlow()

        val state = viewModel.uiState.value
        assertFalse(state.isProcessingActive)
        assertFalse(state.isPaymentCompleted)
        assertEquals(10_000L, state.parsedAmount)
        assertEquals(6, state.calculatedChunks.size)
        assertTrue(state.calculatedChunks.all { it.status == ChunkStatus.PENDING })
    }

    @Test
    fun `a second payment simulation can be successfully started and completed after resetFlow`() = runTest {
        viewModel.startPaymentSimulation(delayMillis = 100L)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isPaymentCompleted)

        viewModel.resetFlow()
        assertFalse(viewModel.uiState.value.isPaymentCompleted)
        assertFalse(viewModel.uiState.value.isProcessingActive)

        // Change amount for second run
        viewModel.onAmountChanged("2000")
        viewModel.calculateSplit()
        assertEquals(2, viewModel.uiState.value.calculatedChunks.size)

        viewModel.startPaymentSimulation(delayMillis = 100L)
        assertTrue(viewModel.uiState.value.isProcessingActive)

        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isPaymentCompleted)
        assertEquals(2, viewModel.uiState.value.calculatedChunks.size)
        assertTrue(viewModel.uiState.value.calculatedChunks.all { it.status == ChunkStatus.SUCCESS })
    }

    @Test
    fun `onAmountChanged filters out non-digit characters and safely handles large input`() {
        viewModel.onAmountChanged("abc₹1,500xyz")
        assertEquals("1500", viewModel.uiState.value.rawAmountInput)
        assertEquals(1500L, viewModel.uiState.value.parsedAmount)

        // Extremely large input capped at 10 digits safely
        viewModel.onAmountChanged("9999999999999999999999")
        assertEquals(10, viewModel.uiState.value.rawAmountInput.length)
        assertEquals(9_999_999_999L, viewModel.uiState.value.parsedAmount)
    }

    @Test
    fun `onSmartPayToggled toggles smart pay state`() {
        assertTrue(viewModel.uiState.value.isSmartPayEnabled)
        viewModel.onSmartPayToggled(false)
        assertFalse(viewModel.uiState.value.isSmartPayEnabled)
        viewModel.onSmartPayToggled(true)
        assertTrue(viewModel.uiState.value.isSmartPayEnabled)
    }
}
