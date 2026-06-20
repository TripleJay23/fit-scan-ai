package com.fitscan.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitscan.app.ui.theme.OnSurfaceDark
import com.fitscan.app.ui.theme.SurfaceDark

@Composable
fun MeasurementChip(
    label: String,
    value: String,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) OnSurfaceDark else SurfaceDark
    val textColor = if (isSelected) SurfaceDark else OnSurfaceDark
    val borderAlpha = if (isSelected) 0f else 0.1f

    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(999.dp))
            .then(
                if (borderAlpha > 0f) {
                    Modifier.border(1.dp, OnSurfaceDark.copy(alpha = borderAlpha), RoundedCornerShape(999.dp))
                } else Modifier
            )
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = "$label $value",
            color = textColor,
            fontSize = 12.sp,
            letterSpacing = 0.5.sp
        )
    }
}
