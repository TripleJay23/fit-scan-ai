package com.fitscan.app.ui.screens.history

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitscan.app.domain.model.ScanResult
import com.fitscan.app.ui.screens.home.FitScanBottomNav
import com.fitscan.app.ui.theme.CharcoalDark
import com.fitscan.app.ui.theme.OnSurfaceDark
import com.fitscan.app.ui.theme.SurfaceContainerDark
import com.fitscan.app.ui.theme.WarmGold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToResult: (Int) -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scans by viewModel.allScans.collectAsState()

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
                    text = "Scan History",
                    style = MaterialTheme.typography.headlineSmall,
                    color = WarmGold
                )
                if (scans.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearAllHistory() }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Clear all",
                            tint = Color.Red.copy(alpha = 0.8f)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }
        },
        bottomBar = {
            FitScanBottomNav(
                currentRoute = "history",
                onHomeClick = onNavigateBack,
                onScanClick = onNavigateToCamera,
                onHistoryClick = {},
                onProfileClick = onNavigateToProfile
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (scans.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Straighten,
                        contentDescription = null,
                        tint = OnSurfaceDark.copy(alpha = 0.2f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Scans Registered Yet",
                        color = OnSurfaceDark.copy(alpha = 0.4f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Your completed on-device scans will appear here.",
                        color = OnSurfaceDark.copy(alpha = 0.3f),
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .testTag("history_list"),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Group scans by date helper
                    val groupedScans = groupScansByDate(scans)
                    
                    groupedScans.forEach { (dateGroup, scanList) ->
                        item {
                            Text(
                                text = dateGroup.uppercase(),
                                fontSize = 11.sp,
                                letterSpacing = 1.sp,
                                color = OnSurfaceDark.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                            )
                        }
                        
                        items(scanList) { scan ->
                            HistoryScanItem(
                                scanResult = scan,
                                onClick = { onNavigateToResult(scan.id) },
                                onDelete = { viewModel.deleteScan(scan.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryScanItem(
    scanResult: ScanResult,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateString = remember(scanResult.timestamp) {
        val sdf = SimpleDateFormat("hh:mm AM", Locale.getDefault())
        sdf.format(Date(scanResult.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceContainerDark
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(OnSurfaceDark.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Straighten,
                        contentDescription = null,
                        tint = WarmGold.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    val labelText = "Height: ${scanResult.heightCm.toInt()}cm"
                    Text(
                        text = labelText,
                        color = OnSurfaceDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = dateString,
                        color = OnSurfaceDark.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .background(WarmGold.copy(alpha = 0.15f), RoundedCornerShape(999.dp))
                        .border(1.dp, WarmGold.copy(alpha = 0.3f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${scanResult.recommendedSize} · EU ${50}", // standardize display
                        color = WarmGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Delete",
                        tint = OnSurfaceDark.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun groupScansByDate(scans: List<ScanResult>): Map<String, List<ScanResult>> {
    val todaySdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val todayString = todaySdf.format(Date())

    return scans.groupBy { scan ->
        val groupSdf = todaySdf.format(Date(scan.timestamp))
        if (groupSdf == todayString) {
            "Today"
        } else {
            val formatHeader = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            formatHeader.format(Date(scan.timestamp))
        }
    }
}
