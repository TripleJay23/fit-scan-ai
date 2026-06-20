package com.fitscan.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitscan.app.ui.theme.OnSurfaceDark
import com.fitscan.app.ui.theme.SurfaceContainerDark
import com.fitscan.app.ui.theme.WarmGold

@Composable
fun SizeResultCard(
    recommendedSize: String,
    euSize: String,
    usSize: String,
    ukSize: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceContainerDark, RoundedCornerShape(16.dp))
            .border(1.dp, OnSurfaceDark.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "RECOMMENDED SIZE",
            fontSize = 12.sp,
            letterSpacing = 1.2.sp,
            color = OnSurfaceDark.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = recommendedSize,
            fontSize = 80.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = WarmGold,
            lineHeight = 80.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier
                .background(SurfaceContainerDark.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
                .border(1.dp, OnSurfaceDark.copy(alpha = 0.05f), RoundedCornerShape(999.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = euSize, fontSize = 12.sp, color = OnSurfaceDark)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "•", fontSize = 12.sp, color = OnSurfaceDark.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = usSize, fontSize = 12.sp, color = OnSurfaceDark)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "•", fontSize = 12.sp, color = OnSurfaceDark.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = ukSize, fontSize = 12.sp, color = OnSurfaceDark)
        }
    }
}
