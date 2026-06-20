package com.fitscan.app.ui.screens.profile

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitscan.app.ui.screens.home.FitScanBottomNav
import com.fitscan.app.ui.theme.CharcoalDark
import com.fitscan.app.ui.theme.ErrorColor
import com.fitscan.app.ui.theme.OnSurfaceDark
import com.fitscan.app.ui.theme.SurfaceContainerDark
import com.fitscan.app.ui.theme.WarmGold

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onClearAllScans: () -> Unit,
    modifier: Modifier = Modifier
) {
    var saveMeasurementsEnabled by remember { mutableStateOf(true) }
    var notificationEnabled by remember { mutableStateOf(false) }
    var selectedUnit by remember { mutableStateOf("cm") }
    var selectedStandard by remember { mutableStateOf("EU") }

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
                    text = "Profile Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    color = WarmGold
                )
                Spacer(modifier = Modifier.width(48.dp))
            }
        },
        bottomBar = {
            FitScanBottomNav(
                currentRoute = "profile",
                onHomeClick = onNavigateBack,
                onScanClick = onNavigateToCamera,
                onHistoryClick = onNavigateToHistory,
                onProfileClick = {}
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(2.dp, WarmGold.copy(alpha = 0.3f), CircleShape)
                        .background(SurfaceContainerDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Avatar picture symbol",
                        tint = WarmGold,
                        modifier = Modifier.fillMaxSize().padding(16.dp)
                    )
                }

                Text(
                    text = "Alex Reed",
                    style = MaterialTheme.typography.headlineSmall,
                    color = OnSurfaceDark
                )
                Text(
                    text = "alex.reed@example.com",
                    color = OnSurfaceDark.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            }

            // Group 1: Scanning & Preferences bento card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainerDark, RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Feature 1: Saved measurements toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Straighten, contentDescription = null, tint = WarmGold)
                        Text(text = "Saved Measurements", color = OnSurfaceDark, fontSize = 15.sp)
                    }
                    Switch(
                        checked = saveMeasurementsEnabled,
                        onCheckedChange = { saveMeasurementsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CharcoalDark,
                            checkedTrackColor = WarmGold,
                            uncheckedThumbColor = OnSurfaceDark.copy(alpha = 0.4f),
                            uncheckedTrackColor = OnSurfaceDark.copy(alpha = 0.1f)
                        )
                    )
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(OnSurfaceDark.copy(alpha = 0.05f)))

                // Feature 2: Unit Preference segmented controls CM/IN
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.SquareFoot, contentDescription = null, tint = WarmGold)
                        Text(text = "Unit Preference", color = OnSurfaceDark, fontSize = 15.sp)
                    }
                    Row(
                        modifier = Modifier
                            .background(OnSurfaceDark.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    if (selectedUnit == "cm") WarmGold else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { selectedUnit = "cm" }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "cm",
                                color = if (selectedUnit == "cm") CharcoalDark else OnSurfaceDark.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(
                                    if (selectedUnit == "in") WarmGold else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { selectedUnit = "in" }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "in",
                                color = if (selectedUnit == "in") CharcoalDark else OnSurfaceDark.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(OnSurfaceDark.copy(alpha = 0.05f)))

                // Feature 3: Size Standard segmented controls EU/US/UK
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Language, contentDescription = null, tint = WarmGold)
                        Text(text = "Size Standard", color = OnSurfaceDark, fontSize = 15.sp)
                    }
                    Row(
                        modifier = Modifier
                            .background(OnSurfaceDark.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("EU", "US", "UK").forEach { std ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (selectedStandard == std) WarmGold else Color.Transparent,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { selectedStandard = std }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = std,
                                    color = if (selectedStandard == std) CharcoalDark else OnSurfaceDark.copy(alpha = 0.6f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Group 2: Notifications Bento Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainerDark, RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Notifications, contentDescription = null, tint = WarmGold)
                    Text(text = "Notifications", color = OnSurfaceDark, fontSize = 15.sp)
                }
                Switch(
                    checked = notificationEnabled,
                    onCheckedChange = { notificationEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CharcoalDark,
                        checkedTrackColor = WarmGold,
                        uncheckedThumbColor = OnSurfaceDark.copy(alpha = 0.4f),
                        uncheckedTrackColor = OnSurfaceDark.copy(alpha = 0.1f)
                    )
                )
            }

            // Privacy Card First
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainerDark.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = OnSurfaceDark.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Privacy First:",
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceDark,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Your images are processed locally or securely encrypted. They are never stored permanently on our servers.",
                            color = OnSurfaceDark.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Delete My Data red outlined button with hover animation
            Button(
                onClick = onClearAllScans,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ErrorColor
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ErrorColor.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("delete_my_data_button")
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteForever,
                        contentDescription = null,
                        tint = ErrorColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delete My Data",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
