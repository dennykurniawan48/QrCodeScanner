package com.signaltekno.qrcodescanner.navigation

sealed class Screen(val route: String) {
    object Home: Screen("HOME")
    object Scan: Screen("SCAN")
    object Create: Screen("CREATE")
}