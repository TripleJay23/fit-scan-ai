package com.fitscan.app.ui.screens.result

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitscan.app.domain.model.ClothingSize
import com.fitscan.app.ui.components.ConfidenceBadge
import com.fitscan.app.ui.components.MeasurementChip
import com.fitscan.app.ui.components.SizeResultCard
import com.fitscan.app.ui.theme.CharcoalDark
import com.fitscan.app.ui.theme.OnSurfaceDark
import com.fitscan.app.ui.theme.SurfaceContainerDark
import com.fitscan.app.ui.theme.WarmGold

@Composable
fun ResultScreen(
    scanId: Int,
    viewModel: ResultViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(scanId) {
        viewModel.loadScanResult(scanId)
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
                    text = "FitScan",
                    style = MaterialTheme.typography.headlineSmall,
                    color = WarmGold
                )
                Spacer(modifier = Modifier.width(48.dp))
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is ResultUiState.Loading -> {
                    CircularProgressIndicator(color = WarmGold)
                }
                is ResultUiState.Success -> {
                    ResultDetails(
                        scanResult = state.scanResult,
                        onNavigateBack = onNavigateBack
                    )
                }
                is ResultUiState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "Error: ${state.message}", color = Color.Red)
                        Button(
                            onClick = { viewModel.loadScanResult(scanId) },
                            colors = ButtonDefaults.buttonColors(containerColor = WarmGold)
                        ) {
                            Text("Retry", color = CharcoalDark)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultDetails(
    scanResult: com.fitscan.app.domain.model.ScanResult,
    onNavigateBack: () -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val categories = listOf("T-Shirt", "Shirt", "Trousers", "Jacket")
    
    // Animate items on launch
    var animateStart by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animateStart = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Confidence Badge -> Outline "87% ACCURATE" at top
        ConfidenceBadge(score = scanResult.confidenceScore)

        // "AI: MediaPipe Pose - On-Device" small chip
        Box(
            modifier = Modifier
                .background(OnSurfaceDark.copy(alpha = 0.05f), RoundedCornerShape(999.dp))
                .border(1.dp, OnSurfaceDark.copy(alpha = 0.1f), RoundedCornerShape(999.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "AI: MediaPipe Pose - On-Device",
                color = OnSurfaceDark.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        // Section header
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Your Measurements",
                style = MaterialTheme.typography.headlineSmall,
                color = OnSurfaceDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = scanResult.measurements.calibrationMethod,
                color = OnSurfaceDark.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
        }

        // Horizontal scrollable measurement chips with staggered reveal simulation
        LazyRow(
            modifier = Modifier.fillMaxWidth().testTag("measurements_lazy_row"),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val measurements = scanResult.measurements
            item {
                AnimatedVisibility(
                    visible = animateStart,
                    enter = fadeIn(animationSpec = tween(300)) + slideInVertically(animationSpec = tween(300)) { 20 }
                ) {
                    MeasurementChip(label = "Shoulder", value = "${measurements.shoulderWidth.toInt()}cm", isSelected = true)
                }
            }
            item {
                AnimatedVisibility(
                    visible = animateStart,
                    enter = fadeIn(animationSpec = tween(400)) + slideInVertically(animationSpec = tween(400)) { 20 }
                ) {
                    MeasurementChip(label = "Chest", value = "${measurements.chestCirc.toInt()}cm")
                }
            }
            item {
                AnimatedVisibility(
                    visible = animateStart,
                    enter = fadeIn(animationSpec = tween(500)) + slideInVertically(animationSpec = tween(500)) { 20 }
                ) {
                    MeasurementChip(label = "Waist", value = "${measurements.waistCirc.toInt()}cm")
                }
            }
            item {
                AnimatedVisibility(
                    visible = animateStart,
                    enter = fadeIn(animationSpec = tween(600)) + slideInVertically(animationSpec = tween(600)) { 20 }
                ) {
                    MeasurementChip(label = "Hips", value = "${measurements.hipCirc.toInt()}cm")
                }
            }
            item {
                AnimatedVisibility(
                    visible = animateStart,
                    enter = fadeIn(animationSpec = tween(700)) + slideInVertically(animationSpec = tween(700)) { 20 }
                ) {
                    MeasurementChip(label = "Arm Length", value = "${measurements.armLength.toInt()}cm")
                }
            }
            item {
                AnimatedVisibility(
                    visible = animateStart,
                    enter = fadeIn(animationSpec = tween(800)) + slideInVertically(animationSpec = tween(800)) { 20 }
                ) {
                    MeasurementChip(label = "Torso", value = "${measurements.torsoHeight.toInt()}cm")
                }
            }
            item {
                AnimatedVisibility(
                    visible = animateStart,
                    enter = fadeIn(animationSpec = tween(900)) + slideInVertically(animationSpec = tween(900)) { 20 }
                ) {
                    MeasurementChip(label = "Inseam", value = "${measurements.inseam.toInt()}cm")
                }
            }
        }

        // Tab Row
        Column(modifier = Modifier.fillMaxWidth()) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = OnSurfaceDark,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = WarmGold,
                        height = 2.dp
                    )
                },
                divider = {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(OnSurfaceDark.copy(alpha = 0.1f)))
                }
            ) {
                categories.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) WarmGold else OnSurfaceDark.copy(alpha = 0.6f)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display active mapped clothing sizes
            val activeCategory = categories[selectedTabIndex]
            val activeSize = scanResult.clothingSizes.firstOrNull { it.category == activeCategory }
                ?: ClothingSize(activeCategory, "L", "EU 50", "US 40", "UK 40", "Standard fit.")

            SizeResultCard(
                recommendedSize = activeSize.sizeText,
                euSize = activeSize.euSize,
                usSize = activeSize.usSize,
                ukSize = activeSize.ukSize
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Fit Notes card with gold left border 4dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainerDark, RoundedCornerShape(12.dp))
                    .drawBehind {
                        // Left gold border 4dp
                        drawLine(
                            color = WarmGold,
                            start = Offset(0f, 0f),
                            end = Offset(0f, size.height),
                            strokeWidth = 4.dp.toPx()
                        )
                    }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = WarmGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Fit Notes",
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceDark,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = activeSize.fitNotes,
                            color = OnSurfaceDark.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        // Staggered Two Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onNavigateBack,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = WarmGold
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmGold),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag("save_measurements_button")
            ) {
                Text(
                    text = "Save",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Button(
                onClick = { /* Share Size trigger system */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarmGold,
                    contentColor = CharcoalDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag("share_size_button")
            ) {
                Text(
                    text = "Share Size",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
