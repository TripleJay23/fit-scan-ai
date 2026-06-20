package com.fitscan.app.ui.screens.home

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image as ImageIcon
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitscan.app.domain.model.ScanResult
import com.fitscan.app.ui.theme.OffWhite
import com.fitscan.app.ui.theme.OnSurfaceDark
import com.fitscan.app.ui.theme.SurfaceContainerDark
import com.fitscan.app.ui.theme.WarmGold

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToCamera: () -> Unit,
    onNavigateToUpload: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scans by viewModel.allScans.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            FitScanBottomNav(
                currentRoute = "home",
                onHomeClick = {},
                onScanClick = onNavigateToCamera,
                onHistoryClick = onNavigateToHistory,
                onProfileClick = onNavigateToProfile
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Header with luxury profile avatar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(1.dp, WarmGold.copy(alpha = 0.2f), CircleShape)
                            .background(SurfaceContainerDark)
                    ) {
                        // Profile Avatar Mock
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = WarmGold,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "FitScan",
                            style = MaterialTheme.typography.labelLarge,
                            color = OnSurfaceDark.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Hello, Alex",
                            style = MaterialTheme.typography.headlineSmall,
                            color = WarmGold
                        )
                    }
                }
                
                IconButton(
                    onClick = {},
                    modifier = Modifier.clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notifications",
                        tint = OnSurfaceDark
                    )
                }
            }

            // 2. Greeting Promo
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                    text = "Ready for your",
                    style = MaterialTheme.typography.displayMedium,
                    color = OnSurfaceDark
                )
                Text(
                    text = "perfect fit?",
                    style = MaterialTheme.typography.displayMedium,
                    color = WarmGold
                )
            }

            // 3. Bento Grid - Action Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Real-Time Scan Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainerDark)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .clickable { onNavigateToCamera() }
                        .testTag("real_time_scan_card")
                ) {
                    // Decorative ambient gold glow on card drawing
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                drawCircle(
                                    color = WarmGold.copy(alpha = 0.05f),
                                    radius = size.minDimension / 1.5f,
                                    center = Offset(size.width, size.height)
                                )
                            }
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Gold circle icon container
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(WarmGold)
                                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PhotoCamera,
                                contentDescription = null,
                                tint = SurfaceContainerDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Text(
                            text = "Real-Time\nScan",
                            style = MaterialTheme.typography.headlineSmall,
                            color = OnSurfaceDark,
                            lineHeight = 28.sp
                        )
                    }
                }

                // Upload Photo Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainerDark)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .clickable { onNavigateToUpload() }
                        .testTag("upload_photo_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(OnSurfaceDark.copy(alpha = 0.08f))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ImageIcon,
                                contentDescription = null,
                                tint = OnSurfaceDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Text(
                            text = "Upload\nPhoto",
                            style = MaterialTheme.typography.headlineSmall,
                            color = OnSurfaceDark,
                            lineHeight = 28.sp
                        )
                    }
                }
            }

            // 4. Trust Strip - "On-Device AI · No Photos Stored · Works Offline"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainerDark.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    tint = WarmGold.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "On-Device AI • No Photos Stored • Works Offline",
                    color = OnSurfaceDark.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    letterSpacing = 0.2.sp
                )
            }

            // 5. Last Scan Card (if exists in Room DB)
            val lastScan = scans.firstOrNull()
            if (lastScan != null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "My Last Scan",
                            style = MaterialTheme.typography.headlineSmall,
                            color = OnSurfaceDark
                        )
                        Text(
                            text = "View All",
                            color = WarmGold,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clickable { onNavigateToHistory() }
                                .padding(4.dp)
                        )
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp)),
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
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(OnSurfaceDark.copy(alpha = 0.05f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Checkroom,
                                        contentDescription = null,
                                        tint = OnSurfaceDark.copy(alpha = 0.6f),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "Size Recommendation",
                                        color = OnSurfaceDark.copy(alpha = 0.6f),
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "T-Shirt / Jacket",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = OnSurfaceDark,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                            
                            Box(
                                modifier = Modifier
                                    .background(WarmGold, RoundedCornerShape(999.dp))
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${lastScan.recommendedSize} / ${lastScan.clothingSizes.firstOrNull()?.euSize ?: ""}",
                                    color = SurfaceContainerDark,
                                    fontSize = 12.sp,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FitScanBottomNav(
    currentRoute: String,
    onHomeClick: () -> Unit,
    onScanClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        containerColor = SurfaceContainerDark.copy(alpha = 0.95f),
        tonalElevation = 8.dp,
        modifier = Modifier.clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = onHomeClick,
            icon = {
                Icon(
                    imageVector = if (currentRoute == "home") Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home"
                )
            },
            label = { Text("Home", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SurfaceContainerDark,
                selectedTextColor = WarmGold,
                indicatorColor = WarmGold,
                unselectedIconColor = OnSurfaceDark.copy(alpha = 0.6f),
                unselectedTextColor = OnSurfaceDark.copy(alpha = 0.6f)
            )
        )

        NavigationBarItem(
            selected = currentRoute == "scan",
            onClick = onScanClick,
            icon = {
                Icon(
                    imageVector = Icons.Filled.PhotoCamera,
                    contentDescription = "Scan"
                )
            },
            label = { Text("Scan", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SurfaceContainerDark,
                selectedTextColor = WarmGold,
                indicatorColor = WarmGold,
                unselectedIconColor = OnSurfaceDark.copy(alpha = 0.6f),
                unselectedTextColor = OnSurfaceDark.copy(alpha = 0.6f)
            )
        )

        NavigationBarItem(
            selected = currentRoute == "history",
            onClick = onHistoryClick,
            icon = {
                Icon(
                    imageVector = if (currentRoute == "history") Icons.Filled.History else Icons.Outlined.History,
                    contentDescription = "History"
                )
            },
            label = { Text("History", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SurfaceContainerDark,
                selectedTextColor = WarmGold,
                indicatorColor = WarmGold,
                unselectedIconColor = OnSurfaceDark.copy(alpha = 0.6f),
                unselectedTextColor = OnSurfaceDark.copy(alpha = 0.6f)
            )
        )

        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = onProfileClick,
            icon = {
                Icon(
                    imageVector = if (currentRoute == "profile") Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SurfaceContainerDark,
                selectedTextColor = WarmGold,
                indicatorColor = WarmGold,
                unselectedIconColor = OnSurfaceDark.copy(alpha = 0.6f),
                unselectedTextColor = OnSurfaceDark.copy(alpha = 0.6f)
            )
        )
    }
}
