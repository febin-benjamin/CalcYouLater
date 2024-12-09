package com.calcyoulater.app

import android.content.Context.MODE_PRIVATE
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberAsyncImagePainter
import com.calcyoulater.app.ui.theme.CalcYouLaterTheme
import kotlinx.coroutines.launch
import java.io.File
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import java.math.BigDecimal
import java.math.RoundingMode
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import android.provider.OpenableColumns
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction


data class SecretFile(
    val uri: Uri,
    val name: String, // Full URI or any identifier for display purposes
    val actualFileName: String // Only the file name, e.g., "cat.10.jpg"
)


class MainActivity : AppCompatActivity() {

    private val dataStoreManager by lazy { DataStoreManager(applicationContext) }
    private val pickFileLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                println("File selected: $uri")
                onFilePicked(it)
            } ?: println("File selection cancelled")
        }

    private val files = mutableStateOf(listOf<SecretFile>())
    private val currentScreen = mutableStateOf("Calculator") // Default to Calculator screen
    private val selectedFileIndex = mutableStateOf(0) // Default to the first image
    private val isFirstTimeUser = mutableStateOf(true) // Assume the user is new by default

    // Define decoyFiles here
    private val decoyFiles = listOf(
        SecretFile(Uri.parse("android.resource://com.calcyoulater.app/drawable/dummy"), "Decoy Image 1", "dummy.png"),
        SecretFile(Uri.parse("android.resource://com.calcyoulater.app/drawable/dummy1"), "Decoy Image 2", "dummy1.png"),
        SecretFile(Uri.parse("android.resource://com.calcyoulater.app/drawable/dummy2"), "Decoy Image 3", "dummy2.png"),
        SecretFile(Uri.parse("android.resource://com.calcyoulater.app/drawable/dummy3"), "Decoy Image 4", "dummy3.png"),
        SecretFile(Uri.parse("android.resource://com.calcyoulater.app/drawable/dummy5"), "Decoy Image 5", "dummy5.png"),
        SecretFile(Uri.parse("android.resource://com.calcyoulater.app/drawable/dummy6"), "Decoy Image 6", "dummy6.png"),
        SecretFile(Uri.parse("android.resource://com.calcyoulater.app/drawable/dummy7"), "Decoy Image 7", "dummy7.png")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Globally apply FLAG_SECURE to prevent content from appearing in Recent Apps/Multitasking View
        applyFlagSecure()

        // Set up status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            WindowCompat.setDecorFitsSystemWindows(window, true)
            val insetsController = WindowInsetsControllerCompat(window, window.decorView)
            insetsController.isAppearanceLightStatusBars = true // Light text/icons on black background
            window.statusBarColor = android.graphics.Color.BLACK // Set to black
            println("Status bar color set to black")
        }

        // Load first-time user state
        val sharedPreferences = getSharedPreferences("CalcYouLaterPrefs", MODE_PRIVATE)
        isFirstTimeUser.value = sharedPreferences.getBoolean("isFirstTimeUser", true)

        // Load files from DataStore
        lifecycleScope.launch {
            dataStoreManager.getFiles().collect { fileUris ->
                files.value = fileUris.map { uri ->
                    val actualFileName = Uri.parse(uri).lastPathSegment ?: "unknown_file"
                    SecretFile(
                        uri = Uri.parse(uri),
                        name = uri,
                        actualFileName = actualFileName
                    )
                }
            }
        }

        val selectedFile = mutableStateOf<SecretFile?>(null)

        // Set content
        setContent {
            CalcYouLaterTheme {
                when (currentScreen.value) {
                    "Calculator" -> CalculatorScreen(
                        onAddFile = { pickFileLauncher.launch(arrayOf("image/*")) },
                        files = files.value,
                        onImageClick = { println("Image clicked in CalculatorScreen") },
                        onSwitchToVault = {
                            if (isFirstTimeUser.value) {
                                currentScreen.value = "PasswordSetup"
                            } else {
                                currentScreen.value = "PasswordScreen"
                            }
                        },
                        onPasswordSetup = { currentScreen.value = "PasswordSetup" },
                        onPiButtonTapped = { currentScreen.value = "PasswordScreen" },
                        isFirstTime = isFirstTimeUser.value
                    )
                    "PasswordSetup" -> PasswordSetupScreen(
                        onPasswordSet = {
                            setFirstTimeUserFlag(false)
                            isFirstTimeUser.value = false
                            currentScreen.value = "PasswordScreen"
                        }
                    )
                    "Vault" -> VaultScreen(
                        files = files.value,
                        onAddFile = { pickFileLauncher.launch(arrayOf("image/*")) },
                        onImageClick = { file ->
                            val index = files.value.indexOf(file)
                            if (index != -1) {
                                selectedFileIndex.value = index
                                currentScreen.value = "FullScreenImageViewer"
                            }
                        },
                        onBack = { currentScreen.value = "Calculator" },
                        onDeleteFromVault = { file -> deleteFromVault(file) },
                        onMoveOutOfVault = { file -> moveOutOfVault(file) }
                    )
                    "PasswordScreen" -> PasswordScreen(
                        onPasswordCorrect = { decoy ->
                            if (decoy) {
                                currentScreen.value = "DecoyVault"
                            } else {
                                currentScreen.value = "Vault"
                            }
                        },
                        onDecoyPassword = {
                            currentScreen.value = "DecoyVault"
                        },
                        onPasswordIncorrect = {
                            currentScreen.value = "Calculator"
                        }
                    )

                    "DecoyVault" -> DecoyVaultScreen(
                        decoyFiles = decoyFiles,
                        onAddFile = { println("Add File is disabled in Decoy Vault") },
                        onImageClick = { println("Image clicked in Decoy Vault") },
                        onBack = { currentScreen.value = "Calculator" },
                        onDeleteFromVault = { println("Delete operation not allowed in Decoy Vault") },
                        onMoveOutOfVault = { println("Move operation not allowed in Decoy Vault") }
                    )

                    "FullScreenImageViewer" -> FullScreenImageViewer(
                        files = files.value,
                        selectedIndex = selectedFileIndex.value,
                        onClose = { currentScreen.value = "Vault" }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Apply FLAG_SECURE globally to prevent multitasking view content exposure
        applyFlagSecure()

        // Check if the app is returning from the background
        if (currentScreen.value == "Vault" || currentScreen.value == "FullScreenImageViewer") {
            currentScreen.value = "PasswordScreen" // Force the user to re-enter the password
        } else {
            println("App resumed on non-secure screen: ${currentScreen.value}")
        }
    }


    // Applies FLAG_SECURE to ensure content is hidden in multitasking view
    private fun applyFlagSecure() {
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    private fun setFirstTimeUserFlag(isFirstTime: Boolean) {
        val sharedPreferences = getSharedPreferences("CalcYouLaterPrefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean("isFirstTimeUser", isFirstTime)
            apply()
        }
    }

    private fun onFilePicked(uri: Uri) {
        val fileName = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                cursor.getString(nameIndex)
            } else {
                null
            }
        } ?: "unknown_file_${System.currentTimeMillis()}"

        val inputStream = contentResolver.openInputStream(uri)
        if (inputStream != null) {
            val privateDir = filesDir
            val vaultDir = File(privateDir, "vault")
            if (!vaultDir.exists()) vaultDir.mkdir()

            val targetFile = File(vaultDir, fileName)
            try {
                inputStream.use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                val fileUri = Uri.fromFile(targetFile)

                lifecycleScope.launch {
                    val updatedFiles = files.value + SecretFile(
                        uri = fileUri,
                        name = fileUri.toString(),
                        actualFileName = fileName
                    )
                    files.value = updatedFiles
                    dataStoreManager.saveFiles(updatedFiles.map { it.uri.toString() }.toSet())
                }

                DocumentsContract.deleteDocument(contentResolver, uri)
            } catch (e: Exception) {
                println("Error: ${e.message}")
            }
        }
    }

    private fun deleteFromVault(file: SecretFile) {
        lifecycleScope.launch {
            val vaultDir = File(filesDir, "vault")
            val fileToDelete = File(vaultDir, file.actualFileName)
            if (fileToDelete.exists()) {
                if (fileToDelete.delete()) {
                    val updatedFiles = files.value.filterNot { it.uri == file.uri }
                    files.value = updatedFiles
                    dataStoreManager.saveFiles(updatedFiles.map { it.uri.toString() }.toSet())
                }
            }
        }
    }

    private fun moveOutOfVault(file: SecretFile) {
        lifecycleScope.launch {
            val vaultDir = File(filesDir, "vault")
            val fileInVault = File(vaultDir, file.actualFileName)
            val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            if (!publicDir.exists()) publicDir.mkdirs()
            val movedFile = File(publicDir, file.actualFileName)

            if (fileInVault.exists()) {
                try {
                    fileInVault.copyTo(movedFile, overwrite = true)
                    if (fileInVault.delete()) {
                        val updatedFiles = files.value.filterNot { it.uri == file.uri }
                        files.value = updatedFiles
                        dataStoreManager.saveFiles(updatedFiles.map { it.uri.toString() }.toSet())
                    }
                } catch (e: Exception) {
                    println("Error moving file: ${e.message}")
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DecoyVaultScreen(
    decoyFiles: List<SecretFile>, // Decoy files specific to this screen
    onAddFile: () -> Unit,
    onImageClick: (SecretFile) -> Unit,
    onBack: () -> Unit,
    onDeleteFromVault: (SecretFile) -> Unit,
    onMoveOutOfVault: (SecretFile) -> Unit
) {

    var selectedFile by remember { mutableStateOf<SecretFile?>(null) }

    selectedFile?.let { file ->

        AlertDialog(
            onDismissRequest = {
                selectedFile = null
            },
            title = {
                Text(
                    text = "Manage File",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "What would you like to do with this file?",
                    color = Color.DarkGray,
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteFromVault(file)
                        selectedFile = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800),
                        contentColor = Color.Black
                    )
                ) {
                    Text("Delete from Vault")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onMoveOutOfVault(file)
                        selectedFile = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800),
                        contentColor = Color.Black
                    )
                ) {
                    Text("Move Out of Vault")
                }
            },
            containerColor = Color(0xFFF5F5F5),
            tonalElevation = 4.dp,
            shape = RoundedCornerShape(12.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2E2E2E))
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    onBack()
                },
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
            ) {
                Text("Back", color = Color.Black)
            }
            Button(
                onClick = {
                    onAddFile()
                },
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
            ) {
                Text("Add File", color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (decoyFiles.isEmpty()) {
            Text(
                "Vault is empty. Add your secret files here.",
                fontSize = 18.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {

            LazyVerticalGrid(
                columns = GridCells.Adaptive(100.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(decoyFiles) { file ->

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                            .combinedClickable(
                                onClick = {
                                    onImageClick(file)
                                },
                                onLongClick = {
                                    selectedFile = file
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(file.uri),
                            contentDescription = file.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(
                                    Color.Black.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                                )
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = file.actualFileName.take(15),
                                color = Color.White,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordSetupScreen(onPasswordSet: (String) -> Unit) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var step by remember { mutableStateOf(1) } // 1: Set password, 2: Confirm password
    val focusRequesterList = List(4) { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("CalcYouLaterPrefs", MODE_PRIVATE)

    LaunchedEffect(Unit) {
        focusRequesterList[0].requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2E2E2E))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (step == 1) "Set a 4-digit PIN" else "Re-enter your 4-digit PIN",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(4) { index ->
                TextField(
                    value = if (step == 1) password.getOrNull(index)?.toString() ?: "" else confirmPassword.getOrNull(index)?.toString() ?: "",
                    onValueChange = { input ->
                        if (input.length == 1 && if (step == 1) password.length <= index else confirmPassword.length <= index) {
                            if (step == 1) {
                                password += input
                                if (index < 3) focusRequesterList[index + 1].requestFocus()
                                else focusManager.clearFocus()
                            } else {
                                confirmPassword += input
                                if (index < 3) focusRequesterList[index + 1].requestFocus()
                                else focusManager.clearFocus()
                            }
                        }
                    },
                    modifier = Modifier
                        .size(80.dp)
                        .padding(8.dp)
                        .focusRequester(focusRequesterList[index]),
                    textStyle = TextStyle(
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color(0xFF444444),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (step == 1) {
                    if (password.length == 4) {
                        step = 2
                        errorMessage = null
                    } else {
                        errorMessage = "PIN must be 4 digits"
                        password = ""
                    }
                } else {
                    if (password == confirmPassword) {
                        // Save the password in SharedPreferences
                        with(sharedPreferences.edit()) {
                            putString("userPassword", password)
                            apply()
                        }
                        onPasswordSet(password)
                    } else {
                        errorMessage = "PINs do not match"
                        confirmPassword = "" // Clear the confirm input
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF9800),
                contentColor = Color.White
            ),
            modifier = Modifier.width(200.dp)
        ) {
            Text(if (step == 1) "Next" else "Set PIN")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordScreen(
    onPasswordCorrect: (Boolean) -> Unit, // Accept a Boolean parameter
    onDecoyPassword: () -> Unit,
    onPasswordIncorrect: () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("CalcYouLaterPrefs", MODE_PRIVATE)
    val correctPassword = sharedPreferences.getString("userPassword", "") ?: ""
    val decoyPassword = "4836" // Reserved password for the decoy vault

    var countdown by remember { mutableStateOf(15) }
    var password by remember { mutableStateOf("") }
    val focusRequesterList = List(4) { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusRequesterList[0].requestFocus()
    }

    LaunchedEffect(Unit) {
        while (countdown > 0) {
            kotlinx.coroutines.delay(1000L)
            countdown--
        }
        if (countdown == 0) {
            onPasswordIncorrect() // Timeout logic
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2E2E2E)) // Match the calculator screen background
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Enter 4-digit PIN",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Time left: $countdown seconds",
            fontSize = 20.sp,
            color = Color.Red,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(4) { index ->
                TextField(
                    value = password.getOrNull(index)?.toString() ?: "",
                    onValueChange = { input ->
                        if (input.length == 1 && password.length <= index) {
                            password += input
                            if (index < 3) {
                                focusRequesterList[index + 1].requestFocus()
                            } else {
                                focusManager.clearFocus()
                            }
                        }

                        if (password.length == 4) {
                            when (password) {
                                correctPassword -> {
                                    onPasswordCorrect(false) // Navigate to actual vault
                                }

                                decoyPassword -> {
                                    onPasswordCorrect(true) // Navigate to decoy vault
                                }

                                else -> {
                                    password = "" // Reset on incorrect password
                                    onPasswordIncorrect()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .size(80.dp)
                        .padding(8.dp)
                        .focusRequester(focusRequesterList[index]),
                    textStyle = TextStyle(
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color(0xFF444444),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    onAddFile: () -> Unit,
    files: List<SecretFile>,
    onImageClick: (SecretFile) -> Unit,
    onSwitchToVault: () -> Unit,
    onPiButtonTapped: () -> Unit,
    onPasswordSetup: () -> Unit,
    isFirstTime: Boolean
) {
    var displayText by remember { mutableStateOf("0") }
    var realTimeAnswer by remember { mutableStateOf("") }
    var lastValidAnswer by remember { mutableStateOf("") }
    var piTapCount by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()

    // Function to evaluate real-time answer
    fun calculateRealTimeAnswer(input: String): String {
        return try {
            // Check if the input ends with an operator
            if (input.endsWith("+") || input.endsWith("-") || input.endsWith("×") || input.endsWith("÷")) {
                lastValidAnswer // Return the last valid answer for incomplete expressions
            } else {
                val sanitizedInput = input
                    .replace("×", "*")
                    .replace("÷", "/")
                    .replace("%", "/100")

                val answer = evaluateExpression(sanitizedInput) // Use the sanitized input
                lastValidAnswer = answer // Update the last valid answer
                answer
            }
        } catch (e: Exception) {
            lastValidAnswer // Retain the last valid answer in case of any errors
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF3E3E3E), // Top Gradient Color
                        Color(0xFF1B1B1B)  // Bottom Gradient Color
                    )
                )
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Editable Display Box with Backspace, Real-Time Answer, and Vertical Stacking
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp) // Increased height for real-time answer
                .background(
                    color = Color(0xFF1F1F1F),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 48.dp) // Leave space for the backspace icon
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.End
            ) {
                // Stacked Input Display
                displayText.chunked(17).forEach { line ->
                    Text(
                        text = line,
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.End
                        ),
                        modifier = Modifier.padding(bottom = 4.dp) // Gap between lines
                    )
                }

                // Real-Time Answer Display
                if (realTimeAnswer.isNotEmpty()) {
                    Text(
                        text = realTimeAnswer,
                        style = TextStyle(
                            color = Color.White.copy(alpha = 0.5f), // Low-opacity text
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Light,
                            textAlign = TextAlign.End
                        ),
                        modifier = Modifier.padding(top = 8.dp) // Add some spacing
                    )
                }
            }

            // Backspace Button
            IconButton(
                onClick = {
                    if (displayText.isNotEmpty() && displayText != "0") {
                        displayText = displayText.dropLast(1)
                        if (displayText.isEmpty()) displayText = "0"
                    }
                    realTimeAnswer = calculateRealTimeAnswer(displayText)
                },
                enabled = displayText.isNotEmpty() && displayText != "0",
                modifier = Modifier.align(Alignment.TopEnd) // Position in the top-right corner
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_backspace), // Use custom backspace drawable
                    contentDescription = "Backspace",
                    tint = if (displayText.isNotEmpty() && displayText != "0") Color.White else Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Buttons Grid
        ButtonGrid(
            onButtonClick = { buttonValue ->
                if (buttonValue == "π") {
                    piTapCount++

                    if (piTapCount == 6) {
                        piTapCount = 0
                        if (isFirstTime) {
                            onPasswordSetup()
                        } else {
                            onPiButtonTapped()
                        }
                    }
                } else {
                    piTapCount = 0

                    if (buttonValue == "=") {
                        // Set the final answer and clear the real-time answer
                        displayText = realTimeAnswer
                        realTimeAnswer = ""
                    } else {
                        displayText = when (buttonValue) {
                            "C" -> {
                                lastValidAnswer = ""  // Clear the last valid answer
                                realTimeAnswer = ""  // Clear the real-time answer
                                "0"                   // Reset display text to "0"
                            }
                            else -> {
                                val updatedText =
                                    if (displayText == "0") buttonValue else displayText + buttonValue
                                realTimeAnswer = calculateRealTimeAnswer(updatedText)
                                updatedText
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun ButtonGrid(onButtonClick: (String) -> Unit) {
    val buttons = listOf(
        listOf("C", "±", "%", "÷", "√"),
        listOf("7", "8", "9", "×", "^"),
        listOf("4", "5", "6", "-", "π"),
        listOf("1", "2", "3", "+", "e"),
        listOf("0", ".", "(", ")", "=")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        buttons.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp), // Fixed height for uniformity
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { label ->
                    Box(
                        modifier = Modifier
                            .weight(1f) // Ensures equal distribution across the row
                            .aspectRatio(1f) // Makes buttons circular
                            .background(
                                color = if (label in listOf("C", "±", "%", "÷", "×", "-", "+", "=")) {
                                    Color(0xFFFF9800) // Accent color for operators
                                } else {
                                    Color(0xFF2E2E2E) // Default button background
                                },
                                shape = RoundedCornerShape(50) // Makes the button circular
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null // Disables ripple effect
                            ) { onButtonClick(label) }
                            .padding(6.dp), // Adds a gap between buttons
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}









@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VaultScreen(
    files: List<SecretFile>,
    onAddFile: () -> Unit,
    onImageClick: (SecretFile) -> Unit,
    onBack: () -> Unit,
    onDeleteFromVault: (SecretFile) -> Unit,
    onMoveOutOfVault: (SecretFile) -> Unit
) {

    var selectedFile by remember { mutableStateOf<SecretFile?>(null) }

    selectedFile?.let { file ->

        AlertDialog(
            onDismissRequest = {

                selectedFile = null
            },
            title = {
                Text(
                    text = "Manage File",
                    color = Color.Black, // Title remains black as per your requirement
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "What would you like to do with this file?",
                    color = Color.DarkGray, // Closer to black for the description text
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {

                        onDeleteFromVault(file)
                        selectedFile = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800), // Match button color to app theme
                        contentColor = Color.Black
                    )
                ) {
                    Text("Delete from Vault")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {

                        onMoveOutOfVault(file)
                        selectedFile = null
                    }, colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800), // Match button color to app theme
                        contentColor = Color.Black
                    )
                ) {
                    Text("Move Out of Vault")
                }
            }, containerColor = Color(0xFFF5F5F5), // Dialog background color
            tonalElevation = 4.dp, // Add elevation to match the theme's depth
            shape = RoundedCornerShape(12.dp) // Rounded corners for consistency
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2E2E2E)) // Updated background color
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {

                    onBack()
                },
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)) // Updated color
            ) {
                Text("Back", color = Color.Black)
            }
            Button(
                onClick = {

                    onAddFile()
                },
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)) // Updated color
            ) {
                Text("Add File", color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (files.isEmpty()) {

            Text(
                "Vault is empty. Add your secret files here.",
                fontSize = 18.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {

            LazyVerticalGrid(
                columns = GridCells.Adaptive(100.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(files) { file ->

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                            .combinedClickable(
                                onClick = {

                                    onImageClick(file)
                                },
                                onLongClick = {

                                    selectedFile = file
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(file.uri),
                            contentDescription = file.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(
                                    Color.Black.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                                )
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = file.actualFileName.take(15), // Show a truncated name if too long
                                color = Color.White,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}




@Composable
fun VaultFileItem(
    file: SecretFile,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Thumbnail
        Image(
            painter = rememberAsyncImagePainter(file.uri),
            contentDescription = file.name,
            modifier = Modifier
                .size(100.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        // File Name (Extracted from the full path)
        Text(
            text = file.name.substringAfterLast("/"),
            fontSize = 14.sp,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun FullScreenImageViewer(
    files: List<SecretFile>,
    selectedIndex: Int,
    onClose: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = selectedIndex)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        HorizontalPager(
            count = files.size,
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val file = files[page]
            val painter = rememberAsyncImagePainter(model = file.uri)

            // Zoom State Variables
            var scale by remember { mutableStateOf(1f) }
            var offsetX by remember { mutableStateOf(0f) }
            var offsetY by remember { mutableStateOf(0f) }

            // Gesture Detector for Pinch-to-Zoom
            val gestureModifier = Modifier.pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 5f) // Limit zoom scale between 1x and 5x
                    offsetX += pan.x
                    offsetY += pan.y
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
                    .then(gestureModifier),
                contentAlignment = Alignment.Center
            ) {
                if (painter.state is coil.compose.AsyncImagePainter.State.Error) {
                    Text(
                        text = "Image not found",
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    Image(
                        painter = painter,
                        contentDescription = file.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Close button
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(48.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.6f), // Translucent black
                    shape = CircleShape
                )
                .clickable { onClose() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close, // Use a modern close icon
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(24.dp) // Adjust size for sleekness
            )
        }
    }
}





@Composable
fun CalculatorDisplay(displayText: String) {
    Text(
        text = displayText,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        fontSize = 48.sp
    )
}


fun evaluateExpression(expression: String): String {
    return try {
        // Replace symbols with proper operators for evaluation
        val sanitizedExpression = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-") // Ensure minus sign compatibility

        val tokens = sanitizedExpression.split(Regex("(?<=[+\\-*/])|(?=[+\\-*/])")).toMutableList()

        // Handle percentages explicitly by processing each token
        for (i in tokens.indices) {
            if (tokens[i].endsWith("%")) {
                val number = tokens[i].removeSuffix("%").toBigDecimalOrNull()
                if (number != null) {
                    tokens[i] = number.divide(BigDecimal(100), 10, RoundingMode.HALF_UP).toPlainString()
                } else {
                    return "Error" // Invalid percentage input
                }
            }
        }

        // Process multiplication and division first
        while (tokens.contains("*") || tokens.contains("/")) {
            val index = tokens.indexOfFirst { it == "*" || it == "/" }
            if (index <= 0 || index >= tokens.size - 1) {
                return "Error" // Invalid operation
            }

            val left = tokens[index - 1].toBigDecimalOrNull()
            val right = tokens[index + 1].toBigDecimalOrNull()
            if (left == null || right == null) {
                return "Error"
            }

            val result = when (tokens[index]) {
                "*" -> left.multiply(right)
                "/" -> if (right != BigDecimal.ZERO) left.divide(right, 10, RoundingMode.HALF_UP) else return "Error"
                else -> throw IllegalArgumentException("Unexpected operator: ${tokens[index]}")
            }

            tokens[index - 1] = result.stripTrailingZeros().toPlainString()
            tokens.removeAt(index) // Remove operator
            tokens.removeAt(index) // Remove right operand
        }

        // Then handle addition and subtraction
        var result = tokens[0].toBigDecimalOrNull() ?: return "Error"
        var i = 1
        while (i < tokens.size) {
            val operator = tokens[i]
            val value = tokens.getOrNull(i + 1)?.toBigDecimalOrNull() ?: return "Error"
            result = when (operator) {
                "+" -> result.add(value)
                "-" -> result.subtract(value)
                else -> return "Error"
            }
            i += 2
        }

        result.stripTrailingZeros().toPlainString() // Final result as a clean string
    } catch (e: Exception) {
        "Error"
    }
}



