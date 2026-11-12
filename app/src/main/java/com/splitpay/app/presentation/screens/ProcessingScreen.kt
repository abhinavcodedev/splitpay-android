package com.splitpay.app.presentation.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.splitpay.app.data.model.ChunkStatus
import com.splitpay.app.data.model.PaymentChunk
import com.splitpay.app.presentation.viewmodel.SplitPayUiState
import com.splitpay.app.presentation.viewmodel.SplitPayViewModel
import com.splitpay.app.ui.theme.DarkButton
import com.splitpay.app.ui.theme.EmeraldGreen
import com.splitpay.app.ui.theme.MintBg
import com.splitpay.app.ui.theme.MintBorder
import com.splitpay.app.ui.theme.Slate100
import com.splitpay.app.ui.theme.Slate200
import com.splitpay.app.ui.theme.Slate300
import com.splitpay.app.ui.theme.Slate400
import com.splitpay.app.ui.theme.Slate50
import com.splitpay.app.ui.theme.Slate600
import com.splitpay.app.ui.theme.Slate700
import com.splitpay.app.ui.theme.Slate800
import com.splitpay.app.ui.theme.Slate900
import com.splitpay.app.ui.theme.SplitPayTheme
import com.splitpay.app.util.CurrencyFormatter

@Composable
fun ProcessingScreen(
    viewModel: SplitPayViewModel,
    onNavigateToPayMerchant: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    ProcessingContent(
        uiState = uiState,
        onSplitAnotherAmountClicked = {
            viewModel.resetFlow()
            onNavigateToPayMerchant()
        },
        modifier = modifier
    )
}

@Composable
fun ProcessingContent(
    uiState: SplitPayUiState,
    onSplitAnotherAmountClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalAmount = if (uiState.calculatedChunks.isNotEmpty()) {
        uiState.calculatedChunks.sumOf { it.amount }
    } else {
        uiState.parsedAmount
    }
    val totalCount = uiState.calculatedChunks.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // 1. Dark Upper Header with Radar & Headline (~35% visual balance)
        ProcessingHeaderRadar(
            totalAmount = totalAmount,
            chunkCount = totalCount,
            isCompleted = uiState.isPaymentCompleted,
            modifier = Modifier.fillMaxWidth()
        )

        // 2. Lower White Payment Sheet (Rounded Top Overlay)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Scrollable Transaction Steps List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        items = uiState.calculatedChunks,
                        key = { it.index }
                    ) { chunk ->
                        LiveChunkStepRow(
                            chunk = chunk,
                            totalCount = totalCount
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Panel: Notice Card OR Completion Banner + CTA
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = Slate100
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (uiState.isPaymentCompleted) {
                        // Completion Banner Card
                        CompletionBannerCard()

                        Spacer(modifier = Modifier.height(12.dp))

                        // Split Another Amount Button
                        Button(
                            onClick = onSplitAnotherAmountClicked,
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
                                text = "Split Another Amount",
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    } else {
                        // Processing Notice Card
                        ProcessingNoticeCard()
                    }
                }
            }
        }
    }
}

@Composable
fun ProcessingHeaderRadar(
    totalAmount: Long,
    chunkCount: Int,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarTransition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing)
        ),
        label = "RadarRotation"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Column(
        modifier = modifier
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Radar / Badge graphic container
        Box(
            modifier = Modifier
                .size(112.dp)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!isCompleted) {
                // Outer glowing animated radar track
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(pulseScale)
                ) {
                    val strokeWidth = 3.dp.toPx()
                    // Inactive track
                    drawCircle(
                        color = Color(0xFF1F2937),
                        radius = (size.minDimension - strokeWidth) / 2,
                        style = Stroke(width = strokeWidth)
                    )
                    // Active glowing arc
                    drawArc(
                        color = Color(0xFF22C55E),
                        startAngle = rotation,
                        sweepAngle = 140f,
                        useCenter = false,
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // Inner Core Badge
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .shadow(
                        elevation = if (isCompleted) 12.dp else 6.dp,
                        shape = CircleShape,
                        spotColor = Color(0x6610B981)
                    )
                    .background(Color(0xFF059669), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success check",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Routing payment",
                        tint = Color.White,
                        modifier = Modifier
                            .size(32.dp)
                            .rotate(-15f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Headline
        Text(
            text = if (isCompleted) "Payment Complete!" else "Processing Payments...",
            style = TextStyle(
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-0.3).sp
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Subtitle
        if (isCompleted) {
            Text(
                text = buildAnnotatedString {
                    append("Total ")
                    withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.SemiBold)) {
                        append(CurrencyFormatter.formatInr(totalAmount))
                    }
                    append(" successfully routed in $chunkCount splits")
                },
                style = TextStyle(
                    fontSize = 13.5.sp,
                    color = Slate400,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            Text(
                text = "Total ${CurrencyFormatter.formatInr(totalAmount)} being routed in $chunkCount splits",
                style = TextStyle(
                    fontSize = 13.5.sp,
                    color = Slate400,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun LiveChunkStepRow(
    chunk: PaymentChunk,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val isProcessing = chunk.status == ChunkStatus.PROCESSING
    val isSuccess = chunk.status == ChunkStatus.SUCCESS

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isProcessing) Slate50 else Color.Transparent,
        border = if (isProcessing) BorderStroke(1.dp, Slate100) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Status Icon & Description
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                when (chunk.status) {
                    ChunkStatus.SUCCESS -> {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFF059669), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed checkmark",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    ChunkStatus.PROCESSING -> {
                        Box(
                            modifier = Modifier.size(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = EmeraldGreen
                            )
                        }
                    }
                    ChunkStatus.PENDING -> {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .border(BorderStroke(1.5.dp, Slate300), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(Slate400, CircleShape)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Payment ${chunk.index} of $totalCount",
                        style = TextStyle(
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSuccess || isProcessing) Slate900 else Slate700,
                            lineHeight = 18.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when (chunk.status) {
                            ChunkStatus.SUCCESS -> "Completed • ${chunk.timestamp ?: "Just now"}"
                            ChunkStatus.PROCESSING -> "Processing..."
                            ChunkStatus.PENDING -> "Waiting in queue"
                        },
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = when (chunk.status) {
                                ChunkStatus.PROCESSING -> EmeraldGreen
                                else -> Slate400
                            }
                        )
                    )
                }
            }

            // Right: Amount and status chevron
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = CurrencyFormatter.formatInr(chunk.amount),
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSuccess || isProcessing) Slate900 else Slate700,
                        letterSpacing = (-0.2).sp
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (isSuccess || isProcessing) Slate400 else Slate300,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ProcessingNoticeCard(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Slate50,
        border = BorderStroke(1.dp, Slate200.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(BorderStroke(1.dp, Slate100), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = "Lock icon",
                    tint = Slate700,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Please keep this app open until all payments are completed.",
                style = TextStyle(
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate700,
                    lineHeight = 16.sp
                )
            )
        }
    }
}

@Composable
fun CompletionBannerCard(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MintBg,
        border = BorderStroke(1.dp, MintBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Simulation complete check",
                tint = EmeraldGreen,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Simulation complete — no real payment was made.",
                style = TextStyle(
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF065F46)
                )
            )
        }
    }
}

// ==========================================
// PREVIEWS
// ==========================================

private val sampleProcessingUiState = SplitPayUiState(
    rawAmountInput = "10000",
    parsedAmount = 10_000L,
    isProcessingActive = true,
    isPaymentCompleted = false,
    currentProcessingIndex = 1,
    calculatedChunks = listOf(
        PaymentChunk(index = 1, amount = 1_999L, status = ChunkStatus.SUCCESS, timestamp = "9:41 AM"),
        PaymentChunk(index = 2, amount = 1_999L, status = ChunkStatus.PROCESSING),
        PaymentChunk(index = 3, amount = 1_999L, status = ChunkStatus.PENDING),
        PaymentChunk(index = 4, amount = 1_999L, status = ChunkStatus.PENDING),
        PaymentChunk(index = 5, amount = 1_999L, status = ChunkStatus.PENDING),
        PaymentChunk(index = 6, amount = 5L, status = ChunkStatus.PENDING)
    )
)

private val sampleInitialProcessingUiState = SplitPayUiState(
    rawAmountInput = "10000",
    parsedAmount = 10_000L,
    isProcessingActive = true,
    isPaymentCompleted = false,
    currentProcessingIndex = 0,
    calculatedChunks = listOf(
        PaymentChunk(index = 1, amount = 1_999L, status = ChunkStatus.PENDING),
        PaymentChunk(index = 2, amount = 1_999L, status = ChunkStatus.PENDING),
        PaymentChunk(index = 3, amount = 1_999L, status = ChunkStatus.PENDING),
        PaymentChunk(index = 4, amount = 1_999L, status = ChunkStatus.PENDING),
        PaymentChunk(index = 5, amount = 1_999L, status = ChunkStatus.PENDING),
        PaymentChunk(index = 6, amount = 5L, status = ChunkStatus.PENDING)
    )
)

private val sampleCompletedUiState = SplitPayUiState(
    rawAmountInput = "10000",
    parsedAmount = 10_000L,
    isProcessingActive = false,
    isPaymentCompleted = true,
    currentProcessingIndex = 5,
    calculatedChunks = listOf(
        PaymentChunk(index = 1, amount = 1_999L, status = ChunkStatus.SUCCESS, timestamp = "9:41 AM"),
        PaymentChunk(index = 2, amount = 1_999L, status = ChunkStatus.SUCCESS, timestamp = "9:41 AM"),
        PaymentChunk(index = 3, amount = 1_999L, status = ChunkStatus.SUCCESS, timestamp = "9:41 AM"),
        PaymentChunk(index = 4, amount = 1_999L, status = ChunkStatus.SUCCESS, timestamp = "9:42 AM"),
        PaymentChunk(index = 5, amount = 1_999L, status = ChunkStatus.SUCCESS, timestamp = "9:42 AM"),
        PaymentChunk(index = 6, amount = 5L, status = ChunkStatus.SUCCESS, timestamp = "9:42 AM")
    )
)

/**
 * Preview A: Mid-flight Processing State (First SUCCESS, Second PROCESSING, remaining PENDING)
 */
@Preview(
    name = "Processing State",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=393dp,height=852dp,dpi=440"
)
@Composable
private fun ProcessingStatePreview() {
    SplitPayTheme {
        ProcessingContent(
            uiState = sampleProcessingUiState,
            onSplitAnotherAmountClicked = {}
        )
    }
}

/**
 * Preview B: Initial Processing State (All chunks PENDING)
 */
@Preview(
    name = "Initial Processing State",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=393dp,height=852dp,dpi=440"
)
@Composable
private fun InitialProcessingStatePreview() {
    SplitPayTheme {
        ProcessingContent(
            uiState = sampleInitialProcessingUiState,
            onSplitAnotherAmountClicked = {}
        )
    }
}

/**
 * Preview C: Completion State (All chunks SUCCESS, completion banner & CTA visible)
 */
@Preview(
    name = "Completion State",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=393dp,height=852dp,dpi=440"
)
@Composable
private fun CompletionStatePreview() {
    SplitPayTheme {
        ProcessingContent(
            uiState = sampleCompletedUiState,
            onSplitAnotherAmountClicked = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LiveChunkStepRowPreview() {
    SplitPayTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            LiveChunkStepRow(
                chunk = PaymentChunk(index = 1, amount = 1_999L, status = ChunkStatus.SUCCESS, timestamp = "9:41 AM"),
                totalCount = 6
            )
            LiveChunkStepRow(
                chunk = PaymentChunk(index = 2, amount = 1_999L, status = ChunkStatus.PROCESSING),
                totalCount = 6
            )
            LiveChunkStepRow(
                chunk = PaymentChunk(index = 3, amount = 1_999L, status = ChunkStatus.PENDING),
                totalCount = 6
            )
        }
    }
}
