package com.splitpay.app.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.splitpay.app.data.model.MerchantInfo
import com.splitpay.app.presentation.viewmodel.SplitPayUiState
import com.splitpay.app.ui.theme.SplitPayTheme
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.splitpay.app.presentation.viewmodel.SplitPayViewModel
import com.splitpay.app.ui.theme.BlueLink
import com.splitpay.app.ui.theme.CardBorder
import com.splitpay.app.ui.theme.Emerald500
import com.splitpay.app.ui.theme.EmeraldGreen
import com.splitpay.app.ui.theme.MintBg
import com.splitpay.app.ui.theme.MintBorder
import com.splitpay.app.ui.theme.Slate100
import com.splitpay.app.ui.theme.Slate200
import com.splitpay.app.ui.theme.Slate400
import com.splitpay.app.ui.theme.Slate50
import com.splitpay.app.ui.theme.Slate500
import com.splitpay.app.ui.theme.Slate600
import com.splitpay.app.ui.theme.Slate700
import com.splitpay.app.ui.theme.Slate900

@Composable
fun PayMerchantScreen(
    viewModel: SplitPayViewModel,
    onNavigateToSplitPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    PayMerchantContent(
        uiState = uiState,
        onAmountChanged = { viewModel.onAmountChanged(it) },
        onSmartPayToggled = { viewModel.onSmartPayToggled(it) },
        onContinueClicked = {
            viewModel.calculateSplit()
            onNavigateToSplitPreview()
        },
        modifier = modifier
    )
}

@Composable
fun PayMerchantContent(
    uiState: SplitPayUiState,
    onAmountChanged: (String) -> Unit,
    onSmartPayToggled: (Boolean) -> Unit,
    onContinueClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            PayMerchantTopAppBar(
                onBackClicked = {},
                onHelpClicked = {}
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                color = Color.White
            ) {
                Button(
                    onClick = onContinueClicked,
                    enabled = uiState.parsedAmount > 0L,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Slate900,
                        contentColor = Color.White,
                        disabledContainerColor = Slate200,
                        disabledContentColor = Slate400
                    )
                ) {
                    Text(
                        text = "Continue",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Merchant Profile Section
            MerchantProfileHeader(merchantInfo = uiState.merchantInfo)

            // Amount Input Card Section
            AmountInputCard(
                rawAmountInput = uiState.rawAmountInput,
                errorMessage = uiState.errorMessage,
                onAmountChanged = onAmountChanged
            )

            // Smart Pay Feature Card Section
            SmartPayToggleCard(
                isEnabled = uiState.isSmartPayEnabled,
                onToggleChanged = onSmartPayToggled
            )

            // Information / Disclaimer Callout Section
            InfoDisclaimerCard()

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PayMerchantTopAppBar(
    onBackClicked: () -> Unit,
    onHelpClicked: () -> Unit
) {
    Row(
        modifier = Modifier
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
                tint = Slate900
            )
        }
        Text(
            text = "Pay Merchant",
            style = TextStyle(
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Slate900
            )
        )
        IconButton(onClick = onHelpClicked) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                contentDescription = "Help and info",
                tint = Slate900
            )
        }
    }
}

@Composable
fun MerchantProfileHeader(
    merchantInfo: MerchantInfo
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Store Avatar Icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Slate100, shape = RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Storefront,
                contentDescription = "Store icon",
                tint = Slate700,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = merchantInfo.name,
                style = TextStyle(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "UPI ID: ${merchantInfo.vpa}",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = Slate500
                    )
                )
                if (merchantInfo.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified Merchant",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AmountInputCard(
    rawAmountInput: String,
    errorMessage: String?,
    onAmountChanged: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero Amount Display & BasicTextField
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "₹",
                    style = TextStyle(
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
                BasicTextField(
                    value = rawAmountInput,
                    onValueChange = onAmountChanged,
                    textStyle = TextStyle(
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        textAlign = TextAlign.Start
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    cursorBrush = SolidColor(BlueLink),
                    modifier = Modifier
                        .widthIn(min = 28.dp)
                        .width(IntrinsicSize.Min),
                    decorationBox = { innerTextField ->
                        if (rawAmountInput.isEmpty()) {
                            Text(
                                text = "0",
                                style = TextStyle(
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate400,
                                    textAlign = TextAlign.Start
                                )
                            )
                        }
                        innerTextField()
                    }
                )
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add Note Button
            Surface(
                shape = RoundedCornerShape(99.dp),
                color = Slate100,
                modifier = Modifier.clickable { }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit note",
                        tint = Slate700,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add a note (optional)",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate700
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SmartPayToggleCard(
    isEnabled: Boolean,
    onToggleChanged: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MintBg,
        border = BorderStroke(1.dp, MintBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Emerald500, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Lightning icon",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Smart Pay",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggleChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Emerald500,
                        uncheckedThumbColor = Slate400,
                        uncheckedTrackColor = Slate200
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Automatically split this payment into multiple UPI transactions under ₹2,000 to avoid merchant charges.",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Slate600,
                    lineHeight = 18.sp
                )
            )
        }
    }
}

@Composable
fun InfoDisclaimerCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Slate50,
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "Info icon",
                tint = Slate400,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "For payments above ₹2,000, merchants may be charged an MDR. You won't be charged, but this helps reduce costs for the merchant.",
                    style = TextStyle(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Normal,
                        color = Slate600,
                        lineHeight = 17.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { }
                ) {
                    Text(
                        text = "Learn more →",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BlueLink
                        )
                    )
                }
            }
        }
    }
}

// ==========================================
// PREVIEWS
// ==========================================

@Preview(
    name = "Pay Merchant Screen",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=393dp,height=852dp,dpi=440"
)
@Composable
private fun PayMerchantScreenPreview() {
    SplitPayTheme {
        PayMerchantContent(
            uiState = SplitPayUiState(
                merchantInfo = MerchantInfo(
                    name = "The Coffee House",
                    vpa = "coffeehouse@upi",
                    isVerified = true
                ),
                rawAmountInput = "10000",
                parsedAmount = 10_000L,
                isSmartPayEnabled = true
            ),
            onAmountChanged = {},
            onSmartPayToggled = {},
            onContinueClicked = {}
        )
    }
}
