package com.signaltekno.qrcodescanner.screen

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import android.provider.SyncStateContract.Helpers.insert
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.signaltekno.qrcodescanner.CameraPreview
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import androidx.core.content.ContextCompat.startActivity

import android.content.Intent
import android.provider.Settings
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import com.signaltekno.qrcodescanner.navigation.Screen


@ExperimentalPermissionsApi
@Composable
fun GenerateScreen(navController: NavHostController) {
    var barcodeText by remember { mutableStateOf("") }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var doNotShowRationale by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
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
                    Column(modifier= Modifier
                        .fillMaxSize()
                        .padding(15.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Column(modifier = Modifier.padding(6.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text="akses Kamera and Penyimpanan dibutuhkan agar apl ini tetap berjalan. Untuk melanjutkan silahkan berikan hak akses.", textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(42.dp))
                            Row(modifier=Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                Button(onClick = { permissionsState.launchMultiplePermissionRequest() }, modifier = Modifier.width(150.dp)) {
                                    Text("Ok!")
                                }
                                Spacer(Modifier.width(8.dp))
                                Button(onClick = { doNotShowRationale = true }, modifier = Modifier.width(150.dp)) {
                                    Text("Nope")
                                }
                            }
                        }
                    }
                }
            },
            permissionsNotAvailableContent = {
                Column(modifier= Modifier
                    .fillMaxSize()
                    .padding(15.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Column(modifier = Modifier, verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
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
        ){
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(15.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedTextField(value = barcodeText, onValueChange = {barcodeText = it})
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = {
                    if(barcodeText.isNotEmpty()) {
                        bitmap = generateBarCode(barcodeText)
                    }
                }, modifier = Modifier.padding(4.dp)) {
                    Text(text = "Generate Barcode")
                }
                Spacer(modifier = Modifier.height(10.dp))
                bitmap?.asImageBitmap()?.let {
                    Image(bitmap = it, contentDescription = "", modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                bitmap?.let {
                    Button(onClick = {
                        savePhotoToExternalStorage(UUID.randomUUID().toString(), it, context)
                    }) {
                        Text(text = "Simpan Gambar")
                    }
                }
            }
        }
    })

}

fun generateBarCode(text: String): Bitmap {
    val width = 720
    val height = 540
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val codeWriter = MultiFormatWriter()
    try {
        val bitMatrix = codeWriter.encode(
            text,
            BarcodeFormat.CODE_128,
            width,
            height
        )
        for (x in 0 until width) {
            for (y in 0 until height) {
                val color = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                bitmap.setPixel(x, y, color)
            }
        }
    } catch (e: WriterException) {
        Log.d("TAG", "generateBarCode: ${e.message}")
    }
    return bitmap
}


fun savePhotoToExternalStorage(name : String, bmp : Bitmap?, context: Context) : Boolean{

    val imageCollection : Uri = if (sdkCheck()){
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    }else{
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    val contentValues = ContentValues().apply {

        put(MediaStore.Images.Media.DISPLAY_NAME,"$name.png")
        put(MediaStore.Images.Media.MIME_TYPE,"image/png")
        if (bmp != null){
            put(MediaStore.Images.Media.WIDTH,bmp.width)
            put(MediaStore.Images.Media.HEIGHT,bmp.height)
        }

    }

    return try{

        context.contentResolver.insert(imageCollection,contentValues)?.also {

            context.contentResolver.openOutputStream(it).use { outputStream ->

                if (bmp != null){

                    if(!bmp.compress(Bitmap.CompressFormat.PNG,95,outputStream)){

                        throw IOException("Failed to save Bitmap")
                    }
                }
            }

        } ?: throw IOException("Failed to create Media Store entry")
        true
    }catch (e: IOException){

        e.printStackTrace()
        false
    }

}