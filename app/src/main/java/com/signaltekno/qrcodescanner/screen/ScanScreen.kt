package com.signaltekno.qrcodescanner.screen

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.*
import com.signaltekno.qrcodescanner.CameraPreview

@ExperimentalPermissionsApi
@Composable
fun ScanScreen(navController: NavHostController) {
    val context = LocalContext.current
    var result by rememberSaveable{ mutableStateOf("") }
    var doNotShowRationale by rememberSaveable { mutableStateOf(false) }

    val permissions = mutableListOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)

    if(!sdkCheck()){
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    val permissionsState = rememberMultiplePermissionsState(
        permissions = permissions
    )

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(
        key1 = lifecycleOwner,
        effect = {
            val observer = LifecycleEventObserver { _, event ->
                if(event == Lifecycle.Event.ON_START) {
                    permissionsState.launchMultiplePermissionRequest()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    )

    val scaffoldState = rememberScaffoldState()

    Scaffold(scaffoldState = scaffoldState, topBar = {
        TopAppBar(
            title = {
                Text(text = "Generate Barcode")
            },
            navigationIcon = {
                IconButton(onClick = {
                    navController.popBackStack()
                }) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
    }, content = {
        PermissionsRequired(
            multiplePermissionsState = permissionsState,
            permissionsNotGrantedContent = {
                if (doNotShowRationale) {
                    Text("Feature not available")
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(15.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier.padding(6.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "akses Kamera and Penyimpanan dibutuhkan agar apl ini tetap berjalan. Untuk melanjutkan silahkan berikan hak akses.",
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(42.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { permissionsState.launchMultiplePermissionRequest() },
                                    modifier = Modifier.width(150.dp)
                                ) {
                                    Text("Ok!")
                                }
                                Spacer(Modifier.width(8.dp))
                                Button(
                                    onClick = { doNotShowRationale = true },
                                    modifier = Modifier.width(150.dp)
                                ) {
                                    Text("Nope")
                                }
                            }
                        }
                    }
                }
            },
            permissionsNotAvailableContent = {
                Column(
                    modifier = Modifier.fillMaxSize().padding(15.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier,
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Permission ditolak, anda tidak bisa melanjutkan menggunakan aplikasi. Silahkan untuk membuka menu settings"
                        )
                        Spacer(modifier = Modifier.height(42.dp))
                        Button(onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            val uri = Uri.fromParts("package", context.packageName, null)
                            intent.data = uri
                            context.startActivity(intent)
                        }) {
                            Text("Open Settings")
                        }
                    }
                }
            }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                ) {
                    CameraPreview(context = context) { result = it }
                }
                Spacer(modifier = Modifier.height(15.dp))
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Hasil Scan Barcode: ",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = result,
                        style = MaterialTheme.typography.h5,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (result.isNotEmpty()) {
                        Button(onClick = {
                            val clipboard: ClipboardManager = context.getSystemService(
                                ComponentActivity.CLIPBOARD_SERVICE
                            ) as ClipboardManager
                            val clip = ClipData.newPlainText("barcode", result)
                            clipboard.setPrimaryClip(clip)
                        }) {
                            Text(text = "Salin Barcode")
                        }
                    }
                }
            }
        }
    })
}

fun sdkCheck() : Boolean{

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
        return true
    }

    return false

}