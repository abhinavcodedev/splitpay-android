package com.splitpay.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.splitpay.app.presentation.screens.PayMerchantScreen
import com.splitpay.app.presentation.screens.ProcessingScreen
import com.splitpay.app.presentation.screens.SplitPreviewScreen
import com.splitpay.app.presentation.viewmodel.SplitPayViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: SplitPayViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.PayMerchant.route
    ) {
        composable(Screen.PayMerchant.route) {
            PayMerchantScreen(
                viewModel = viewModel,
                onNavigateToSplitPreview = {
                    navController.navigate(Screen.SplitPreview.route)
                }
            )
        }
        composable(Screen.SplitPreview.route) {
            SplitPreviewScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToProcessing = {
                    navController.navigate(Screen.Processing.route)
                }
            )
        }
        composable(Screen.Processing.route) {
            ProcessingScreen(
                viewModel = viewModel,
                onNavigateToPayMerchant = {
                    navController.navigate(Screen.PayMerchant.route) {
                        popUpTo(Screen.PayMerchant.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
