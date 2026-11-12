package com.splitpay.app.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.splitpay.app.data.model.ChunkStatus
import com.splitpay.app.data.model.PaymentChunk
import com.splitpay.app.presentation.viewmodel.SplitPayUiState
import com.splitpay.app.presentation.viewmodel.SplitPayViewModel
import com.splitpay.app.ui.theme.DarkButton
import com.splitpay.app.ui.theme.ScreenBackground
import com.splitpay.app.ui.theme.Slate100
import com.splitpay.app.ui.theme.Slate200
import com.splitpay.app.ui.theme.Slate400
import com.splitpay.app.ui.theme.Slate50
import com.splitpay.app.ui.theme.Slate500
import com.splitpay.app.ui.theme.Slate600
import com.splitpay.app.ui.theme.Slate700
import com.splitpay.app.ui.theme.Slate800
import com.splitpay.app.ui.theme.Slate900
import com.splitpay.app.ui.theme.SplitPayTheme
import com.splitpay.app.util.CurrencyFormatter

@Composable
fun SplitPreviewScreen(
    viewModel: SplitPayViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToProcessing: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    SplitPreviewContent(
        uiState = uiState,
        onBackClicked = onNavigateBack,
        onHelpClicked = {},
        onConfirmAndPayClicked = {
            viewModel.startPaymentSimulation()
            onNavigateToProcessing()
        },
        onEditSplitClicked = onNavigateBack,
        modifier = modifier
    )
}

@Composable
fun SplitPreviewContent(
    uiState: SplitPayUiState,
    onBackClicked: () -> Unit,
    onHelpClicked: () -> Unit,
    onConfirmAndPayClicked: () -> Unit,
    onEditSplitClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalAmount = if (uiState.calculatedChunks.isNotEmpty()) {
        uiState.calculatedChunks.sumOf { it.amount }
    } else {
        uiState.parsedAmount
    }
    val paymentCount = uiState.calculatedChunks.size

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ScreenBackground,
        topBar = {
            SplitPreviewTopAppBar(
                onBackClicked = onBackClicked,
                onHelpClicked = onHelpClicked
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. Heading Section
            SplitSummaryHeader(
                amount = totalAmount,
                count = paymentCount
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Chunks Breakdown Card
            SplitListCard(chunks = uiState.calculatedChunks)

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Order Summary Details Block
            OrderSummaryCard(
                totalAmount = totalAmount,
                paymentCount = paymentCount
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 4. Security / UPI Guarantee Note
            SecurityDisclaimerNote()

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Action Buttons (Confirm & Pay + Edit Split)
            Button(
                onClick = onConfirmAndPayClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkButton,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = "Confirm & Pay",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onEditSplitClicked)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Edit Split",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate700
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun SplitPreviewTopAppBar(
    onBackClicked: () -> Unit,
    onHelpClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onBackClicked) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Go back",
                tint = Slate800
            )
        }
        Text(
            text = "Smart Pay",
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Slate900,
                letterSpacing = (-0.2).sp
            )
        )
        IconButton(onClick = onHelpClicked) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "Help and info",
                tint = Slate800
            )
        }
    }
}

@Composable
fun SplitSummaryHeader(
    amount: Long,
    count: Int,
    modifier: Modifier = Modifier
) {
    val paymentText = if (count == 1) "1 payment" else "$count payments"
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "We'll split ${CurrencyFormatter.formatInr(amount)}\ninto $paymentText",
            style = TextStyle(
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                textAlign = TextAlign.Center,
                lineHeight = 30.sp,
                letterSpacing = (-0.5).sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "All transactions will be below ₹2,000.",
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Slate500,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
fun SplitListCard(
    chunks: List<PaymentChunk>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Slate100),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            chunks.forEachIndexed { index, chunk ->
                ChunkRowItem(chunk = chunk)
                if (index < chunks.lastIndex) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = Slate50
                    )
                }
            }
        }
    }
}

@Composable
fun ChunkRowItem(
    chunk: PaymentChunk,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Slate100, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = chunk.index.toString(),
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate600
                )
            )
        }
        Text(
            text = CurrencyFormatter.formatInr(chunk.amount),
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Slate800,
                letterSpacing = (-0.2).sp
            )
        )
    }
}

@Composable
fun OrderSummaryCard(
    totalAmount: Long,
    paymentCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total amount",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Slate500
                )
            )
            Text(
                text = CurrencyFormatter.formatInr(totalAmount),
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate800
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Number of payments",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Slate500
                )
            )
            Text(
                text = paymentCount.toString(),
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate800
                )
            )
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = Slate200.copy(alpha = 0.7f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "You pay",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate700
                )
            )
            Text(
                text = CurrencyFormatter.formatInr(totalAmount),
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            )
        }
    }
}

@Composable
fun SecurityDisclaimerNote(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = "Secure lock icon",
            tint = Slate400,
            modifier = Modifier
                .size(14.dp)
                .padding(top = 1.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "All payments will be made securely via UPI. This usually takes a few seconds.",
            style = TextStyle(
                fontSize = 11.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.Normal,
                color = Slate400,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        )
    }
}

// ==========================================
// PREVIEWS
// ==========================================

private val sampleSplitPreviewUiState = SplitPayUiState(
    rawAmountInput = "10000",
    parsedAmount = 10_000L,
    calculatedChunks = listOf(
        PaymentChunk(index = 1, amount = 1_999L, status = ChunkStatus.PENDING),
        PaymentChunk(index = 2, amount = 1_999L, status = ChunkStatus.PENDING),
        PaymentChunk(index = 3, amount = 1_999L, status = ChunkStatus.PENDING),
        PaymentChunk(index = 4, amount = 1_999L, status = ChunkStatus.PENDING),
        PaymentChunk(index = 5, amount = 1_999L, status = ChunkStatus.PENDING),
        PaymentChunk(index = 6, amount = 5L, status = ChunkStatus.PENDING)
    )
)

@Preview(
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=393dp,height=852dp,dpi=440"
)
@Composable
private fun SplitPreviewScreenPreview() {
    SplitPayTheme {
        SplitPreviewContent(
            uiState = sampleSplitPreviewUiState,
            onBackClicked = {},
            onHelpClicked = {},
            onConfirmAndPayClicked = {},
            onEditSplitClicked = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChunkRowItemPreview() {
    SplitPayTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ChunkRowItem(
                chunk = PaymentChunk(index = 1, amount = 1_999L, status = ChunkStatus.PENDING)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SplitListCardPreview() {
    SplitPayTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            SplitListCard(
                chunks = sampleSplitPreviewUiState.calculatedChunks
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderSummaryCardPreview() {
    SplitPayTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            OrderSummaryCard(
                totalAmount = 10_000L,
                paymentCount = 6
            )
        }
    }
}
