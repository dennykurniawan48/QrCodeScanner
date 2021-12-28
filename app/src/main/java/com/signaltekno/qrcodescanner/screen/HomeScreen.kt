package com.signaltekno.qrcodescanner.screen

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.signaltekno.qrcodescanner.navigation.Screen

@ExperimentalPermissionsApi
@Composable
fun HomeScreen(navController: NavHostController) {
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current

    Scaffold(scaffoldState = scaffoldState, topBar = {
        TopAppBar(
            title = {
                Text(text = "Home")
            },
            navigationIcon = {
                IconButton(onClick = {
                    (context as Activity).finish()
                }) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Back")
                }
            }
        )
    }, content = {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

            Button(onClick = {
                navController.navigate(Screen.Scan.route)
            }, modifier = Modifier.fillMaxWidth(0.6f)) {
                Text(text = "Scan Barcode")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = {
                navController.navigate(Screen.Create.route)
            }, modifier = Modifier.fillMaxWidth(0.6f)) {
                Text(text = "Generate Barcode")
            }
    }
    })
}