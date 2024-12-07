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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Prevent content from appearing in Recent Apps/Multitasking View
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )



        // Ensure status bar color is set correctly across all API levels
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


        setContent {
            println("Setting content...")
            CalcYouLaterTheme {
                when (currentScreen.value) {
                    "Calculator" -> {
                        println("Navigating to CalculatorScreen")
                        CalculatorScreen(
                            onAddFile = {
                                println("Launching file picker...")
                                pickFileLauncher.launch(arrayOf("image/*"))
                            },
                            files = files.value,
                            onImageClick = { println("Image clicked in CalculatorScreen") },
                            onSwitchToVault = {
                                println("Switch to Vault initiated. isFirstTimeUser: ${isFirstTimeUser.value}")
                                if (isFirstTimeUser.value) {
                                    println("Navigating to PasswordSetup")
                                    currentScreen.value = "PasswordSetup" // Navigate to PasswordSetup
                                } else {
                                    println("Navigating to PasswordScreen")
                                    currentScreen.value = "PasswordScreen" // Navigate to PasswordScreen
                                }
                            },
                            onPasswordSetup = {
                                println("Navigating to PasswordSetup from CalculatorScreen")
                                currentScreen.value = "PasswordSetup"
                            },
                            onPiButtonTapped = {
                                println("Navigating to PasswordScreen from CalculatorScreen")
                                currentScreen.value = "PasswordScreen"
                            },
                            isFirstTime = isFirstTimeUser.value
                        )
                    }
                    "PasswordSetup" -> {
                        println("Navigating to PasswordSetupScreen")
                        PasswordSetupScreen(
                            onPasswordSet = {
                                println("Password successfully set. Updating isFirstTimeUser...")
                                setFirstTimeUserFlag(false) // Persist the value
                                isFirstTimeUser.value = false // Update the state
                                currentScreen.value = "PasswordScreen" // Navigate to Password Screen
                            }
                        )
                    }
                    "Vault" -> {
                        println("Navigating to VaultScreen")
                        VaultScreen(
                            files = files.value,
                            onAddFile = {
                                println("Launching file picker from VaultScreen...")
                                pickFileLauncher.launch(arrayOf("image/*"))
                            },
                            onImageClick = { file ->
                                println("Image clicked in Vault: $file")
                                val index = files.value.indexOf(file)
                                if (index != -1) {
                                    println("Navigating to FullScreenImageViewer for file index: $index")
                                    selectedFileIndex.value = index
                                    currentScreen.value = "FullScreenImageViewer"
                                } else {
                                    println("File not found in the list")
                                }
                            },
                            onBack = {
                                println("Navigating back to CalculatorScreen from Vault")
                                currentScreen.value = "Calculator"
                            },
                            onDeleteFromVault = { file ->
                                println("Deleting file from Vault: $file")
                                deleteFromVault(file)
                            },
                            onMoveOutOfVault = { file ->
                                println("Moving file out of Vault: $file")
                                moveOutOfVault(file)
                            }
                        )
                    }
                    "PasswordScreen" -> {
                        println("Navigating to PasswordScreen")
                        PasswordScreen(
                            onPasswordCorrect = {
                                println("Password correct. Navigating to Vault")
                                currentScreen.value = "Vault"
                            },
                            onPasswordIncorrect = {
                                println("Password incorrect. Navigating back to Calculator")
                                currentScreen.value = "Calculator"
                            }
                        )
                    }
                    "FullScreenImageViewer" -> {
                        println("Navigating to FullScreenImageViewer")
                        FullScreenImageViewer(
                            files = files.value,
                            selectedIndex = selectedFileIndex.value,
                            onClose = {
                                println("Closing FullScreenImageViewer and returning to Vault")
                                currentScreen.value = "Vault"
                            }
                        )
                    }
                    else -> {
                        println("Unknown screen: ${currentScreen.value}")
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (currentScreen.value == "Vault" || currentScreen.value == "FullScreenImageViewer") {
            window.setFlags(
                android.view.WindowManager.LayoutParams.FLAG_SECURE,
                android.view.WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        }
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

                try {
                    val deleted = DocumentsContract.deleteDocument(contentResolver, uri)

                } catch (e: Exception) {
                    println("Error deleting original file using DocumentsContract: ${e.message}")
                }
            } catch (e: Exception) {
                println("Failed to move file to private storage: ${e.message}")
            }
        } else {
            println("Failed to open input stream for URI: $uri")
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
                } else {
                    println("Failed to delete file: ${fileToDelete.path}")
                }
            } else {
                println("File not found in vault: ${fileToDelete.path}")
            }
        }
    }

    private fun moveOutOfVault(file: SecretFile) {
        println("moveOutOfVault called with file: ${file.actualFileName}")
        lifecycleScope.launch {
            val vaultDir = File(filesDir, "vault")
            val fileInVault = File(vaultDir, file.actualFileName)
            val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

            println("Vault directory: ${vaultDir.path}")
            println("Public directory: ${publicDir.path}")

            if (!publicDir.exists()) {
                publicDir.mkdirs()
                println("Public directory created: ${publicDir.path}")
            }

            val movedFile = File(publicDir, file.actualFileName)

            if (fileInVault.exists()) {
                try {
                    println("Attempting to copy file to public directory: ${movedFile.path}")
                    fileInVault.copyTo(movedFile, overwrite = true)
                    if (fileInVault.delete()) {
                        println("File deleted from vault after moving: ${fileInVault.path}")
                        val updatedFiles = files.value.filterNot { it.uri == file.uri }
                        files.value = updatedFiles
                        println("Updated files list after moving out of vault: ${files.value}")
                        dataStoreManager.saveFiles(updatedFiles.map { it.uri.toString() }.toSet())
                    } else {
                        println("Failed to delete original file after moving: ${fileInVault.path}")
                    }
                } catch (e: Exception) {
                    println("Error while moving file out of vault: ${e.message}")
                }
            } else {
                println("File not found in vault: ${fileInVault.path}")
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
    onPasswordCorrect: () -> Unit,
    onPasswordIncorrect: () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("CalcYouLaterPrefs", MODE_PRIVATE)
    val correctPassword = sharedPreferences.getString("userPassword", "") ?: ""


    var countdown by remember { mutableStateOf(15) }
    var password by remember { mutableStateOf("") }
    val focusRequesterList = List(4) { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Automatically request focus for the first field
    LaunchedEffect(Unit) {
        focusRequesterList[0].requestFocus()
    }

    // Countdown logic
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

                            if (password == correctPassword) {
                                onPasswordCorrect()
                            } else {
                                onPasswordIncorrect()
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







@Composable
fun CalculatorScreen(
    onAddFile: () -> Unit,
    files: List<SecretFile>,
    onImageClick: (SecretFile) -> Unit,
    onSwitchToVault: () -> Unit, // Added parameter for switching screens
    onPiButtonTapped: () -> Unit,
    onPasswordSetup: () -> Unit,
    isFirstTime: Boolean
) {
    var displayText by remember { mutableStateOf("0") }
    var piTapCount by remember { mutableIntStateOf(0) }

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
        // Calculator Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    color = Color(0xFF1F1F1F),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = displayText,
                color = Color.White,
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.padding(end = 8.dp)
            )
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

                    displayText = when (buttonValue) {
                        "C" -> {

                            "0"
                        }
                        "=" -> {

                            evaluateExpression(displayText)
                        }
                        else -> {
                            val updatedText = if (displayText == "0") buttonValue else displayText + buttonValue

                            updatedText
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
            .replace("%", "/100") // Handle percentage as division by 100
            .replace("−", "-") // Ensure minus sign compatibility

        val tokens = sanitizedExpression.split(Regex("(?<=[+\\-*/])|(?=[+\\-*/])")).toMutableList()

        // Handle multiplication and division first
        while (tokens.contains("*") || tokens.contains("/")) {
            val index = tokens.indexOfFirst { it == "*" || it == "/" }
            val left = tokens[index - 1].toBigDecimal()
            val right = tokens[index + 1].toBigDecimal()

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
        var result = tokens[0].toBigDecimal()
        var i = 1
        while (i < tokens.size) {
            val operator = tokens[i]
            val value = tokens[i + 1].toBigDecimal()
            result = when (operator) {
                "+" -> result.add(value)
                "-" -> result.subtract(value)
                else -> throw IllegalArgumentException("Unexpected operator: $operator")
            }
            i += 2
        }

        result.stripTrailingZeros().toPlainString() // Final result as a clean string
    } catch (e: Exception) {
        "Error"
    }
}

