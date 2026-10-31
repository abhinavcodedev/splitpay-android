package com.splitpay.app.presentation.navigation

sealed class Screen(val route: String) {
    object PayMerchant : Screen("pay_merchant")
    object SplitPreview : Screen("split_preview")
    object Processing : Screen("processing")
}
