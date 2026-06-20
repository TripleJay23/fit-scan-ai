package com.fitscan.app.ui.screens.upload

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fitscan.app.ui.theme.CharcoalDark
import com.fitscan.app.ui.theme.OffWhite
import com.fitscan.app.ui.theme.OnSurfaceDark
import com.fitscan.app.ui.theme.SurfaceContainerDark
import com.fitscan.app.ui.theme.WarmGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    viewModel: UploadViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToResult: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var heightCmInput by remember { mutableStateOf("175") }
    
    // Reference Object Dropdown
    val referenceOptions = listOf("None", "A4 Paper", "Credit Card")
    var dropdownExpanded by remember { mutableStateOf(false) }
    var selectedReference by remember { mutableStateOf(referenceOptions[0]) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    // Direct result flow completed handle
    LaunchedEffect(uiState) {
        val state = uiState
        if (state is UploadUiState.Complete) {
            viewModel.resetState()
            onNavigateToResult(state.result.id)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CharcoalDark,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = OnSurfaceDark
                    )
                }
                Text(
                    text = "Upload Photo",
                    style = MaterialTheme.typography.headlineSmall,
                    color = WarmGold
                )
                Spacer(modifier = Modifier.width(48.dp)) // balance spacer
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (uiState) {
                is UploadUiState.Processing -> {
                    // Processing Sim Loading
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = WarmGold, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Analyzing Photo...",
                            style = MaterialTheme.typography.headlineSmall,
                            color = OnSurfaceDark,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Extracting posture landmarks via MediaPipe and measuring sizes...",
                            color = OnSurfaceDark.copy(alpha = 0.5f),
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                else -> {
                    // Off-white card container for the upload area
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(OffWhite, RoundedCornerShape(16.dp))
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (imageUri == null) {
                            // Target content: "Dashed border upload box"
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(OffWhite.copy(alpha = 0.3f))
                                    .border(
                                        width = 2.dp,
                                        color = WarmGold.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(12.dp) // dashed placeholder simulation
                                    )
                                    .clickable { galleryLauncher.launch("image/*") }
                                    .testTag("upload_dashed_box"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PhotoLibrary,
                                        contentDescription = null,
                                        tint = CharcoalDark.copy(alpha = 0.6f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Tap to choose a full-body photo",
                                        color = CharcoalDark,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        } else {
                            // Selected Image Preview bounding
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(2.dp, WarmGold, RoundedCornerShape(12.dp))
                                    .clickable { galleryLauncher.launch("image/*") }
                            ) {
                                AsyncImage(
                                    model = imageUri,
                                    contentDescription = "Selected Scan Profile",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .background(CharcoalDark.copy(alpha = 0.8f), RoundedCornerShape(topStart = 8.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Change Photo", color = OnSurfaceDark, fontSize = 11.sp)
                                }
                            }
                        }

                        // Height field with gold focus ring
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "YOUR HEIGHT (CM)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalDark.copy(alpha = 0.7f),
                                letterSpacing = 1.sp
                            )
                            OutlinedTextField(
                                value = heightCmInput,
                                onValueChange = { heightCmInput = it.filter { c -> c.isDigit() } },
                                placeholder = { Text("e.g. 175", color = CharcoalDark.copy(alpha = 0.4f)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("upload_height_field"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = CharcoalDark,
                                    unfocusedTextColor = CharcoalDark,
                                    focusedBorderColor = WarmGold,
                                    unfocusedBorderColor = CharcoalDark.copy(alpha = 0.2f),
                                    focusedContainerColor = OffWhite,
                                    unfocusedContainerColor = OffWhite
                                )
                            )
                        }

                        // Reference object dropdown
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "REFERENCE OBJECT IN PHOTO?",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalDark.copy(alpha = 0.7f),
                                letterSpacing = 1.sp
                            )
                            
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = selectedReference,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = CharcoalDark
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { dropdownExpanded = true },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = CharcoalDark,
                                        unfocusedTextColor = CharcoalDark,
                                        focusedBorderColor = WarmGold,
                                        unfocusedBorderColor = CharcoalDark.copy(alpha = 0.2f),
                                        focusedContainerColor = OffWhite,
                                        unfocusedContainerColor = OffWhite
                                    )
                                )

                                DropdownMenu(
                                    expanded = dropdownExpanded,
                                    onDismissRequest = { dropdownExpanded = false },
                                    modifier = Modifier.fillMaxWidth(0.8f).background(OffWhite)
                                ) {
                                    referenceOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option, color = CharcoalDark) },
                                            onClick = {
                                                selectedReference = option
                                                dropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Trigger Analyze Button
                        Button(
                            onClick = {
                                val loadedUri = imageUri
                                if (loadedUri != null) {
                                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                        ImageDecoder.decodeBitmap(
                                            ImageDecoder.createSource(context.contentResolver, loadedUri)
                                        ) { decoder, _, _ ->
                                            decoder.isMutableRequired = true
                                        }
                                    } else {
                                        @Suppress("DEPRECATION")
                                        MediaStore.Images.Media.getBitmap(context.contentResolver, loadedUri)
                                    }
                                    val heightCm = heightCmInput.toFloatOrNull() ?: 175f
                                    viewModel.analyzeSelectedPhoto(bitmap, heightCm)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("upload_submit_button"),
                            enabled = imageUri != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WarmGold,
                                contentColor = CharcoalDark,
                                disabledContainerColor = WarmGold.copy(alpha = 0.3f),
                                disabledContentColor = CharcoalDark.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Analyze Photo",
                                style = MaterialTheme.typography.headlineSmall,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Integrity / Privacy note
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = null,
                            tint = OnSurfaceDark.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Your photo is analyzed on-device only",
                            color = OnSurfaceDark.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
