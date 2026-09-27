package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.CyberCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.ToggleFeatureCard
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberPink
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleDark
import com.example.ui.theme.NeonPurpleGlow
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.PotatoGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.BloxBoosterViewModel
import com.example.util.ResolutionManager
import com.example.util.ShizukuServiceStatus

@Composable
fun HomeScreen(
    viewModel: BloxBoosterViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfiles: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val visualConfig by viewModel.visualConfig.collectAsState()
    val fpsMetrics by viewModel.fpsMetrics.collectAsState()
    val deviceSpec by viewModel.deviceSpec.collectAsState()
    val resolutionState by viewModel.resolutionState.collectAsState()
    val shizukuState by viewModel.shizukuState.collectAsState()
    val isRobloxInstalled by viewModel.isRobloxInstalled.collectAsState()
    val robloxVersion by viewModel.robloxVersion.collectAsState()
    val isOverlayActive by viewModel.isOverlayActive.collectAsState()
    val actionFeedback by viewModel.actionFeedback.collectAsState()

    var showOverlayPermDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // 1. Top Brand Header with Balaclava Logo
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.5.dp, NeonPurple, RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_blox_icon),
                            contentDescription = "Blox Booster Balaclava Logo",
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "BLOX BOOSTER",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "POTATO GRAPHICS & VISUAL OPTIMIZER",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = NeonPurpleLight
                        )
                    }
                }

                StatusBadge(
                    text = when (shizukuState.status) {
                        ShizukuServiceStatus.AUTHORIZED -> "SHIZUKU READY"
                        ShizukuServiceStatus.PERMISSION_REQUIRED -> "AUTH NEEDED"
                        ShizukuServiceStatus.SERVICE_STOPPED -> "SHIZUKU OFF"
                        ShizukuServiceStatus.NOT_INSTALLED -> "ADB ONLY"
                    },
                    isActive = shizukuState.status == ShizukuServiceStatus.AUTHORIZED,
                    activeColor = CyberGreen,
                    inactiveColor = when (shizukuState.status) {
                        ShizukuServiceStatus.PERMISSION_REQUIRED -> CyberAmber
                        else -> TextMuted
                    }
                )
            }
        }

        // Live Action Feedback Banner
        actionFeedback?.let { feedback ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SYSTEM FEEDBACK",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonPurpleLight
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = feedback,
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = TextSecondary,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { viewModel.clearFeedback() }
                        )
                    }
                }
            }
        }

        // Auto-revert countdown safety alert (if awaiting confirmation)
        if (resolutionState.isAwaitingConfirmation) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF331500)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, CyberAmber)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = "Safety Alert", tint = CyberAmber, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Safety Revert: ${resolutionState.revertTimerSeconds}s",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberAmber
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "New resolution applied. Auto-reverting if not confirmed.",
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.confirmResolution() },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color.Black),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("KEEP RESOLUTION", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { viewModel.resetResolution() },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurface, contentColor = TextPrimary),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("REVERT NOW", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // 2. Main Hero Card with Balaclava Art & Big Launch Roblox Button
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_banner_card")
                    .border(1.5.dp, NeonPurple, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF241544),
                                    DarkSurfaceElevated,
                                    DarkSurface
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                val statusText = when {
                                    visualConfig.potatoModeActive -> "POTATO MODE: ACTIVE"
                                    visualConfig.performanceModeActive -> "PERFORMANCE MODE: READY"
                                    else -> "OPTIMIZATION ENGINE: STANDBY"
                                }
                                val statusColor = when {
                                    visualConfig.potatoModeActive -> PotatoGold
                                    visualConfig.performanceModeActive -> CyberCyan
                                    else -> TextSecondary
                                }

                                Text(
                                    text = statusText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = statusColor,
                                    letterSpacing = 0.5.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = if (visualConfig.potatoModeActive)
                                        "Potato Visuals & Fillrate Relief"
                                    else
                                        "Optimize Roblox Framerates",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = if (isRobloxInstalled)
                                        "Roblox client ready ${robloxVersion?.let { "• v$it" } ?: ""}"
                                    else
                                        "Roblox client not detected (tap to get)",
                                    fontSize = 12.sp,
                                    color = if (isRobloxInstalled) CyberGreen else CyberAmber
                                )
                            }

                            // Balaclava Mascot Emblem
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(2.dp, NeonPurple, RoundedCornerShape(16.dp))
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_blox_icon),
                                    contentDescription = "Blox Balaclava Graphic",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Large Launch Roblox Action Button
                        Button(
                            onClick = { viewModel.launchRoblox() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("launch_roblox_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonPurple,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Launch",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isRobloxInstalled) "LAUNCH ROBLOX WITH OPTIMIZATIONS" else "INSTALL / LAUNCH ROBLOX",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        // 3. Official Shizuku Integration Card
        item {
            CyberCard(
                borderColor = when (shizukuState.status) {
                    ShizukuServiceStatus.AUTHORIZED -> CyberGreen
                    ShizukuServiceStatus.PERMISSION_REQUIRED -> CyberAmber
                    else -> DarkCardBorder
                }
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Shizuku",
                                tint = when (shizukuState.status) {
                                    ShizukuServiceStatus.AUTHORIZED -> CyberGreen
                                    ShizukuServiceStatus.PERMISSION_REQUIRED -> CyberAmber
                                    else -> CyberCyan
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "OFFICIAL SHIZUKU INTEGRATION",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        StatusBadge(
                            text = when (shizukuState.status) {
                                ShizukuServiceStatus.AUTHORIZED -> "AUTHORIZED"
                                ShizukuServiceStatus.PERMISSION_REQUIRED -> "NEEDS PERMISSION"
                                ShizukuServiceStatus.SERVICE_STOPPED -> "SERVICE OFF"
                                ShizukuServiceStatus.NOT_INSTALLED -> "NOT INSTALLED"
                            },
                            isActive = shizukuState.status == ShizukuServiceStatus.AUTHORIZED,
                            activeColor = CyberGreen,
                            inactiveColor = when (shizukuState.status) {
                                ShizukuServiceStatus.PERMISSION_REQUIRED -> CyberAmber
                                else -> TextMuted
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = when (shizukuState.status) {
                            ShizukuServiceStatus.AUTHORIZED ->
                                "Shizuku v${shizukuState.shizukuVersion} active. OS display scaling commands (`wm size/density`) execute with genuine elevated permissions."
                            ShizukuServiceStatus.PERMISSION_REQUIRED ->
                                "Shizuku service is running, but Blox Booster needs authorization to scale resolutions."
                            ShizukuServiceStatus.SERVICE_STOPPED ->
                                "Shizuku app is installed, but the service has not been started. Start it via Wireless Debugging."
                            ShizukuServiceStatus.NOT_INSTALLED ->
                                "Shizuku allows 1-tap resolution scaling without a PC connection each reboot. You can also run commands via ADB."
                        },
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (shizukuState.status == ShizukuServiceStatus.PERMISSION_REQUIRED) {
                            Button(
                                onClick = { viewModel.requestShizukuPermission() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyberAmber, contentColor = Color.Black)
                            ) {
                                Text("GRANT PERMISSION", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        Button(
                            onClick = { viewModel.openShizukuApp() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = TextPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple)
                        ) {
                            Text(
                                text = if (shizukuState.isInstalled) "OPEN SHIZUKU" else "GET SHIZUKU",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = { viewModel.refreshState() },
                            modifier = Modifier.size(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // 4. POTATO MODE (Star Feature)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("potato_mode_card")
                    .border(1.5.dp, if (visualConfig.potatoModeActive) PotatoGold else DarkCardBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (visualConfig.potatoModeActive) Color(0xFF1E1700) else DarkSurface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ColorLens,
                                contentDescription = "Potato Mode",
                                tint = PotatoGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "POTATO MODE",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (visualConfig.potatoModeActive) "ACTIVE & OPTIMIZING" else "TAP TO ACTIVATE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (visualConfig.potatoModeActive) PotatoGold else TextMuted
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.togglePotatoVisualMode() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (visualConfig.potatoModeActive) PotatoGold else NeonPurple,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = if (visualConfig.potatoModeActive) "DISABLE" else "ENABLE",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "How Potato Mode Optimizes Roblox:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "• APPLIED: Cuts GPU pixel shader workload by up to 55-75% via OS resolution scaling (720p/540p), clears memory caches, and lifts shadow gamma (+15%) so enemies/items remain visible when in-game graphics are lowered.\n" +
                                "• UNAVAILABLE IN-APP: Roblox game engine polygon count and draw distance cannot be injected externally; set Graphics Mode to Manual (level 1-2) inside Roblox.",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // 5. Core Performance Toggles
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "ADDITIONAL SYSTEM ENHANCEMENTS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = TextSecondary
                )

                // ⚡ PERFORMANCE MODE
                ToggleFeatureCard(
                    title = "Performance Mode",
                    subtitle = "Prioritizes smooth frametimes, touch latency mitigation, and frees low-priority system RAM caches.",
                    icon = Icons.Default.Bolt,
                    iconColor = CyberCyan,
                    isChecked = visualConfig.performanceModeActive,
                    onCheckedChange = { viewModel.togglePerformanceMode(it) },
                    testTag = "performance_mode_toggle"
                )

                // 🎨 VISUAL ENHANCEMENT MODE
                ToggleFeatureCard(
                    title = "Visual Enhancement Mode",
                    subtitle = "Rich color vibrance, deep shadows & ambient clarity with zero GPU rendering overhead.",
                    icon = Icons.Default.Visibility,
                    iconColor = NeonPurple,
                    isChecked = visualConfig.visualModeActive,
                    onCheckedChange = { viewModel.toggleVisualMode(it) },
                    testTag = "visual_mode_toggle"
                )
            }
        }

        // 6. Real-Time FPS & Hardware Performance Monitor (Transparent & Honest)
        item {
            CyberCard(
                borderColor = DarkCardBorder,
                modifier = Modifier.testTag("fps_performance_monitor_card")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "FPS",
                                tint = CyberGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DISPLAY PACING & HARDWARE MONITOR",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        StatusBadge(
                            text = "${fpsMetrics.currentFps} FPS",
                            isActive = true,
                            activeColor = if (fpsMetrics.currentFps >= 50) CyberGreen else CyberAmber
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Display Frame Rate", fontSize = 11.sp, color = TextMuted)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${fpsMetrics.currentFps}",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                                Text(
                                    text = " / ${deviceSpec.refreshRateHz} Hz",
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "1% Lows (Pacing)", fontSize = 11.sp, color = TextMuted)
                            Text(
                                text = "${fpsMetrics.onePercentLowFps} FPS",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (fpsMetrics.onePercentLowFps >= 45) CyberGreen else CyberAmber
                            )
                            Text(
                                text = "${fpsMetrics.frameStabilityPercent}% Stable",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // RAM Usage Bar
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val usedRam = (deviceSpec.totalRamMb - deviceSpec.availableRamMb).coerceAtLeast(0)
                            Text(
                                text = "RAM: ${usedRam}MB / ${deviceSpec.totalRamMb}MB",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "${deviceSpec.ramUsagePercent}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (deviceSpec.ramUsagePercent > 80) CyberAmber else CyberGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { deviceSpec.ramUsagePercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (deviceSpec.ramUsagePercent > 80) CyberAmber else NeonPurple,
                            trackColor = DarkSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Floating Overlay HUD Toggle Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "In-Game Floating HUD",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Display real FPS & Potato toggle over Roblox",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Button(
                            onClick = {
                                if (Settings.canDrawOverlays(context)) {
                                    viewModel.toggleOverlayService()
                                } else {
                                    showOverlayPermDialog = true
                                }
                            },
                            modifier = Modifier.testTag("toggle_floating_hud_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isOverlayActive) CyberGreen else DarkSurfaceElevated,
                                contentColor = if (isOverlayActive) Color.Black else TextPrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isOverlayActive) CyberGreen else NeonPurple)
                        ) {
                            Text(
                                text = if (isOverlayActive) "HUD ACTIVE" else "ENABLE HUD",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Honest Measurement Disclosure Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurface.copy(alpha = 0.7f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Verified",
                            tint = CyberGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Choreographer display sync timing • No simulated metrics",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // 7. Quick Resolution Optimizer
        item {
            CyberCard(
                borderColor = DarkCardBorder,
                modifier = Modifier.testTag("quick_resolution_card")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = "Resolution",
                                tint = NeonPurpleLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "RESOLUTION SCALING (GPU RELIEF)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = "Settings ->",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonPurpleLight,
                            modifier = Modifier.clickable { onNavigateToSettings() }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Current: ${resolutionState.currentWidth}x${resolutionState.currentHeight} (${resolutionState.activePresetName})",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val presets = ResolutionManager.getPresets(
                        deviceSpec.screenWidth,
                        deviceSpec.screenHeight,
                        deviceSpec.screenDpi
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.forEach { preset ->
                            val isSelected = resolutionState.activePresetName == preset.name
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        1.dp,
                                        if (isSelected) NeonPurple else DarkCardBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .background(if (isSelected) NeonPurpleDark.copy(alpha = 0.5f) else DarkSurface)
                                    .clickable { viewModel.applyResolutionPreset(preset) }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = when {
                                            preset.scaleFactor <= 0.55f -> "540p"
                                            preset.scaleFactor <= 0.70f -> "720p"
                                            preset.scaleFactor <= 0.85f -> "900p"
                                            else -> "Native"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) NeonPurpleLight else TextPrimary
                                    )
                                    Text(
                                        text = "${preset.gpuWorkloadPercent}% GPU",
                                        fontSize = 9.sp,
                                        color = if (preset.gpuWorkloadPercent <= 50) CyberGreen else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 8. Restore Native Settings Button (Requirement 5)
        item {
            OutlinedButton(
                onClick = { viewModel.restoreAllSettings() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("restore_native_settings_button"),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = "Restore Defaults",
                    tint = NeonPurpleLight,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "RESTORE ALL NATIVE SETTINGS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonPurpleLight
                )
            }
        }

        // 9. Roblox Client Settings & Technical Investigation Info (Requirement 2)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Roblox Client Info",
                            tint = CyberCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Roblox Android & ClientAppSettings Investigation",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• ClientAppSettings.json / FFlags: Unlike Windows Bloxstrap, Roblox's native Android client does NOT load ClientAppSettings from external storage. Android's SELinux policy blocks third-party apps from injecting into private package data without root.\n" +
                                "• Account Safety: Third-party APK injectors risk account bans. Blox Booster never touches Roblox binaries.\n" +
                                "• Best Practice: Use Blox Booster's Shizuku resolution downscaling + Potato color grading, then set Roblox in-game graphics to Manual (level 1–3).",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // 10. Device Hardware Specs (Android 15 verified)
        item {
            CyberCard(borderColor = DarkCardBorder) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = "Specs",
                                tint = CyberPink,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DEVICE HARDWARE SPECS",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = TextSecondary,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { viewModel.refreshState() }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Device Model", fontSize = 11.sp, color = TextMuted)
                            Text(text = deviceSpec.deviceName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Android OS", fontSize = 11.sp, color = TextMuted)
                            Text(text = "${deviceSpec.androidVersion} (API ${deviceSpec.apiLevel})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "CPU & Cores", fontSize = 11.sp, color = TextMuted)
                            Text(text = "${deviceSpec.cpuCores} Cores (${deviceSpec.cpuArch})", fontSize = 12.sp, color = TextPrimary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Display Refresh", fontSize = 11.sp, color = TextMuted)
                            Text(text = "${deviceSpec.refreshRateHz} Hz (${deviceSpec.screenWidth}x${deviceSpec.screenHeight})", fontSize = 12.sp, color = CyberCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Thermal State", fontSize = 11.sp, color = TextMuted)
                            Text(text = deviceSpec.thermalStatus, fontSize = 12.sp, color = CyberGreen)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Battery Level", fontSize = 11.sp, color = TextMuted)
                            Text(text = "${deviceSpec.batteryPercent}% ${if (deviceSpec.isCharging) "(Charging)" else ""}", fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    // Permission explanation dialog for Overlay Window
    if (showOverlayPermDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showOverlayPermDialog = false },
            containerColor = DarkSurfaceElevated,
            title = {
                Text(
                    text = "Display Over Other Apps Permission",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Why Blox Booster requests this permission:",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. To draw the floating real-time FPS & Stability HUD over Roblox during gameplay.\n" +
                                "2. To apply the lightweight, zero-GPU Potato Visual color enhancement matrix so Roblox textures look vivid and clear.\n\n" +
                                "Blox Booster does NOT read touch gestures inside Roblox or modify Roblox memory.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showOverlayPermDialog = false
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple, contentColor = Color.Black)
                ) {
                    Text("Grant in Settings", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showOverlayPermDialog = false },
                    border = androidx.compose.foundation.BorderStroke(1.dp, TextMuted)
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
