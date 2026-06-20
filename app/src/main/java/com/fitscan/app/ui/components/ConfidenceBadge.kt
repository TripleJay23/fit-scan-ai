package com.fitscan.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitscan.app.ui.theme.MutedRed
import com.fitscan.app.ui.theme.SoftGreen
import com.fitscan.app.ui.theme.WarmGold

@Composable
fun ConfidenceBadge(
    score: Int,
    modifier: Modifier = Modifier
) {
    val color = when {
        score >= 80 -> SoftGreen
        score >= 60 -> WarmGold
        else -> MutedRed
    }

    Row(
        modifier = modifier
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.5f),
                shape = RoundedCornerShape(999.dp) // pill shape
            )
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = color,
            modifier = Modifier.padding(end = 6.dp)
        )
        Text(
            text = "$score% ACCURATE",
            color = color,
            fontSize = 12.sp,
            letterSpacing = 1.2.sp,
            modifier = Modifier
        )
    }
}
