package com.fitscan.app.ui.screens.camera

import android.Manifest
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.fitscan.app.domain.model.PoseLandmark
import com.fitscan.app.ml.PoseDetector
import com.fitscan.app.ui.components.PoseOverlayCanvas
import com.fitscan.app.ui.theme.CharcoalDark
import com.fitscan.app.ui.theme.ErrorColor
import com.fitscan.app.ui.theme.OnSurfaceDark
import com.fitscan.app.ui.theme.SoftGreen
import com.fitscan.app.ui.theme.SurfaceContainerDark
import com.fitscan.app.ui.theme.WarmGold
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    poseDetector: PoseDetector,
    onNavigateToResult: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val uiState by viewModel.uiState.collectAsState()
    val landmarks by viewModel.detectedLandmarks.collectAsState()

    // Permissions feedback
    LaunchedEffect(permissionState.status) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CharcoalDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (permissionState.status.isGranted) {
                CameraPreviewAndOverlay(
                    poseDetector = poseDetector,
                    landmarks = landmarks,
                    onPoseDetected = { list, bitmap ->
                        viewModel.onPoseDetected(list, bitmap)
                    }
                )
            } else {
                PermissionDeniedOverlay(
                    onRequestPermission = { permissionState.launchPermissionRequest() }
                )
            }

            // Top Status Instruction Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .background(SurfaceContainerDark.copy(alpha = 0.8f), RoundedCornerShape(999.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = WarmGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "STAND 1.5M AWAY · FULL BODY VISIBLE",
                        color = OnSurfaceDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Bottom Controller Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                BottomSheetController(
                    uiState = uiState,
                    poseDetected = landmarks.isNotEmpty(),
                    hasLandmarks = landmarks.isNotEmpty(),
                    onLockAndAnalyze = { height ->
                        viewModel.lockAndAnalyze(height)
                    },
                    onCompleteNav = { resultId ->
                        viewModel.resetState()
                        onNavigateToResult(resultId)
                    },
                    onReset = {
                        viewModel.resetState()
                    }
                )
            }
        }
    }
}

@Composable
fun CameraPreviewAndOverlay(
    poseDetector: PoseDetector,
    landmarks: List<PoseLandmark>,
    onPoseDetected: (List<PoseLandmark>, Bitmap) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val previewView = remember { PreviewView(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()

            imageAnalysis.setAnalyzer(
                cameraExecutor,
                PoseAnalyzer(poseDetector) { detectedLandmarks, bitmap ->
                    onPoseDetected(detectedLandmarks, bitmap)
                }
            )

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA, // Use rear camera by default
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                Log.e("CameraScreen", "UseCase binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
        
        // Skeletal Overlay Drawing
        PoseOverlayCanvas(
            landmarks = landmarks,
            shoulderWidthCm = 44f,
            waistCircCm = 80f
        )
    }
}

@Composable
fun PermissionDeniedOverlay(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalDark)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.headlineSmall,
            color = OnSurfaceDark
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "We need access to the camera to safely scan your body and suggest clothes sizes.",
            color = OnSurfaceDark.copy(alpha = 0.6f),
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = WarmGold)
        ) {
            Text("Grant Permission", color = CharcoalDark)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetController(
    uiState: CameraUiState,
    poseDetected: Boolean,
    hasLandmarks: Boolean,
    onLockAndAnalyze: (Float) -> Unit,
    onCompleteNav: (Int) -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = SurfaceContainerDark.copy(alpha = 0.95f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Handle line indicator
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(4.dp)
                .background(OnSurfaceDark.copy(alpha = 0.2f), RoundedCornerShape(999.dp))
        )

        when (uiState) {
            is CameraUiState.Idle -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(WarmGold, CircleShape)
                    )
                    Text(
                        text = "Scanning...",
                        style = MaterialTheme.typography.headlineSmall,
                        color = OnSurfaceDark
                    )
                }
                Text(
                    text = if (hasLandmarks) "Pose detected! Ready for computer vision analysis." else "Keep still for optimal AI measurement accuracy.",
                    color = OnSurfaceDark.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Gold lock and analyze button (full-width, 56dp)
                Button(
                    onClick = {
                        // Using default height 175cm as input field is removed
                        onLockAndAnalyze(175.0f)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("lock_and_analyze_button"),
                    enabled = poseDetected,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WarmGold,
                        contentColor = CharcoalDark,
                        disabledContainerColor = WarmGold.copy(alpha = 0.3f),
                        disabledContentColor = CharcoalDark.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lock & Analyze",
                            style = MaterialTheme.typography.headlineSmall,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            is CameraUiState.Detecting -> {
                Text(
                    text = "Detecting Pose...",
                    style = MaterialTheme.typography.headlineSmall,
                    color = OnSurfaceDark
                )
                CircularProgressIndicator(color = WarmGold)
            }

            is CameraUiState.Measuring -> {
                Text(
                    text = "Measuring body dimensions...",
                    style = MaterialTheme.typography.headlineSmall,
                    color = OnSurfaceDark
                )
                CircularProgressIndicator(color = WarmGold)
            }

            is CameraUiState.Complete -> {
                LaunchedEffect(uiState) {
                    onCompleteNav(uiState.result.id)
                }
                
                Text(
                    text = "✓ Analysis complete!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SoftGreen
                )
                CircularProgressIndicator(color = SoftGreen)
            }

            is CameraUiState.Error -> {
                Text(
                    text = "Error: ${uiState.message}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = ErrorColor
                )
                Button(
                    onClick = onReset,
                    colors = ButtonDefaults.buttonColors(containerColor = WarmGold)
                ) {
                    Text("Retry", color = CharcoalDark)
                }
            }
        }
    }
}
