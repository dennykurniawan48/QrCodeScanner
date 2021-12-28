package com.signaltekno.qrcodescanner.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.signaltekno.qrcodescanner.screen.GenerateScreen
import com.signaltekno.qrcodescanner.screen.HomeScreen
import com.signaltekno.qrcodescanner.screen.ScanScreen

@ExperimentalPermissionsApi
@Composable
fun SetupNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.Scan.route) {
            ScanScreen(navController = navController)
        }
        composable(Screen.Create.route) {
            GenerateScreen(navController = navController)
        }
    }
}