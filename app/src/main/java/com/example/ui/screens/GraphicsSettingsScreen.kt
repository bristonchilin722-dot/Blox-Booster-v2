package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CyberCard
import com.example.ui.components.StatusBadge
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
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.PotatoGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.BloxBoosterViewModel
import com.example.util.ResolutionManager
import kotlin.math.roundToInt

@Composable
fun GraphicsSettingsScreen(
    viewModel: BloxBoosterViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val visualConfig by viewModel.visualConfig.collectAsState()
    val resolutionState by viewModel.resolutionState.collectAsState()
    val deviceSpec by viewModel.deviceSpec.collectAsState()

    var saturation by remember(visualConfig.saturation) { mutableFloatStateOf(visualConfig.saturation) }
    var contrast by remember(visualConfig.contrast) { mutableFloatStateOf(visualConfig.contrast) }
    var brightness by remember(visualConfig.brightness) { mutableFloatStateOf(visualConfig.brightness) }
    var gamma by remember(visualConfig.gammaLift) { mutableFloatStateOf(visualConfig.gammaLift) }

    var previewSplitPos by remember { mutableFloatStateOf(0.5f) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Screen Header
        item {
            Column {
                Text(
                    text = "GRAPHICS & VISUAL OPTIMIZER",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
                Text(
                    text = "Fine-tune lightweight visual compensation & resolution downscaling",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // 1. Interactive Before/After Visual Enhancement Preview
        item {
            CyberCard(
                borderColor = NeonPurple,
                modifier = Modifier.testTag("visual_preview_card")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "LIVE VISUAL COMPARISON PREVIEW",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonPurpleLight
                        )
                        Text(
                            text = "Drag slider to inspect",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Split-screen simulated game scene canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val splitX = size.width * previewSplitPos

                            // LEFT SIDE: Dull standard potato graphics (washed out, low saturation, flat)
                            val dullSky = Color(0xFF6B7280)
                            val dullTerrain = Color(0xFF4B5563)
                            val dullObject = Color(0xFF9CA3AF)

                            drawRect(
                                brush = Brush.verticalGradient(listOf(Color(0xFF374151), dullSky)),
                                size = androidx.compose.ui.geometry.Size(splitX, size.height)
                            )
                            // Draw dull geometric Roblox-like blocks
                            val pathLeft = Path().apply {
                                moveTo(20f, size.height - 20f)
                                lineTo(splitX * 0.4f, size.height * 0.4f)
                                lineTo(splitX * 0.8f, size.height - 10f)
                                close()
                            }
                            drawPath(pathLeft, dullTerrain)
                            drawRect(
                                color = dullObject,
                                topLeft = Offset(40f, size.height * 0.45f),
                                size = androidx.compose.ui.geometry.Size(40f, 40f)
                            )

                            // RIGHT SIDE: Blox Potato Visual Mode (Vibrant, high dynamic contrast, crisp shadows)
                            val satFactor = (saturation / 1.35f).coerceIn(0.8f, 1.6f)
                            val vibrantSky = Color(
                                (0x0F * satFactor).toInt().coerceIn(0, 255),
                                (0x17 * satFactor).toInt().coerceIn(0, 255),
                                (0x4A * satFactor).toInt().coerceIn(0, 255)
                            )
                            val vibrantTerrain = Color(
                                (0x7C * satFactor).toInt().coerceIn(0, 255),
                                (0x3A * satFactor).toInt().coerceIn(0, 255),
                                (0xED * satFactor).toInt().coerceIn(0, 255)
                            )
                            val vibrantObject = Color(
                                0,
                                (0xEE * satFactor).toInt().coerceIn(0, 255),
                                (0xFF * satFactor).toInt().coerceIn(0, 255)
                            )

                            drawRect(
                                brush = Brush.verticalGradient(listOf(Color(0xFF0F0A24), vibrantSky)),
                                topLeft = Offset(splitX, 0f),
                                size = androidx.compose.ui.geometry.Size(size.width - splitX, size.height)
                            )
                            val pathRight = Path().apply {
                                moveTo(splitX, size.height * 0.55f)
                                lineTo(size.width * 0.7f, size.height * 0.35f)
                                lineTo(size.width - 20f, size.height - 10f)
                                lineTo(splitX, size.height - 10f)
                                close()
                            }
                            drawPath(pathRight, vibrantTerrain)
                            drawRect(
                                color = vibrantObject,
                                topLeft = Offset(splitX + 30f, size.height * 0.40f),
                                size = androidx.compose.ui.geometry.Size(46f, 46f)
                            )

                            // Split divider line
                            drawLine(
                                color = Color.White,
                                start = Offset(splitX, 0f),
                                end = Offset(splitX, size.height),
                                strokeWidth = 3.dp.toPx()
                            )
                        }

                        // Labels
                        Text(
                            text = "Standard Potato (Dull)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )

                        Text(
                            text = "Blox Potato Visual (Vivid HDR)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonPurpleLight,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = previewSplitPos,
                        onValueChange = { previewSplitPos = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = NeonPurple,
                            activeTrackColor = NeonPurple,
                            inactiveTrackColor = DarkSurface
                        )
                    )
                }
            }
        }

        // 2. Visual Mode Sliders (Saturation, Contrast, Gamma)
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
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Tuning",
                                tint = PotatoGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "VISUAL ENHANCEMENT CONTROLS",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Button(
                            onClick = {
                                saturation = 1.40f
                                contrast = 1.28f
                                brightness = 1.05f
                                gamma = 1.10f
                                viewModel.updateSliders(1.40f, 1.28f, 1.05f, 1.10f)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Reset", fontSize = 10.sp, color = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Saturation Boost Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Color Vibrance Boost", fontSize = 12.sp, color = TextPrimary)
                            Text(
                                text = "+${((saturation - 1.0f) * 100).roundToInt()}% (${(saturation * 10).roundToInt() / 10f}x)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan
                            )
                        }
                        Slider(
                            value = saturation,
                            onValueChange = {
                                saturation = it
                                viewModel.updateSliders(saturation, contrast, brightness, gamma)
                            },
                            valueRange = 1.0f..1.80f,
                            colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan)
                        )
                    }

                    // Contrast Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Dynamic Ambient Contrast", fontSize = 12.sp, color = TextPrimary)
                            Text(
                                text = "+${((contrast - 1.0f) * 100).roundToInt()}% (${(contrast * 10).roundToInt() / 10f}x)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonPurpleLight
                            )
                        }
                        Slider(
                            value = contrast,
                            onValueChange = {
                                contrast = it
                                viewModel.updateSliders(saturation, contrast, brightness, gamma)
                            },
                            valueRange = 1.0f..1.50f,
                            colors = SliderDefaults.colors(thumbColor = NeonPurple, activeTrackColor = NeonPurple)
                        )
                    }

                    // Gamma / Shadow Lift Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Shadow Equalizer (PvP Visibility)", fontSize = 12.sp, color = TextPrimary)
                            Text(
                                text = "+${((gamma - 1.0f) * 100).roundToInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PotatoGold
                            )
                        }
                        Slider(
                            value = gamma,
                            onValueChange = {
                                gamma = it
                                viewModel.updateSliders(saturation, contrast, brightness, gamma)
                            },
                            valueRange = 1.0f..1.40f,
                            colors = SliderDefaults.colors(thumbColor = PotatoGold, activeTrackColor = PotatoGold)
                        )
                    }

                    // Micro-Contrast Edge Clarity
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Simulated Edge Anti-Aliasing", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = "Enhances polygon contours without rendering MSAA passes", fontSize = 10.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = visualConfig.edgeClarity,
                            onCheckedChange = { viewModel.togglePotatoVisualMode() },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = NeonPurple)
                        )
                    }
                }
            }
        }

        // 3. Resolution Optimizer Pro with Exact ADB Commands
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
                                imageVector = Icons.Default.Tv,
                                contentDescription = "Resolution Optimizer",
                                tint = CyberGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "RESOLUTION OPTIMIZER PRO",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        StatusBadge(
                            text = if (resolutionState.isScaled) "SCALED" else "NATIVE",
                            isActive = resolutionState.isScaled,
                            activeColor = PotatoGold,
                            inactiveColor = CyberCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Reducing the OS framebuffer resolution relieves budget GPUs (Poco C71 Mali-G52, Helio G36) by cutting millions of fragment pixels per frame while preserving UI touch alignment.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val presets = ResolutionManager.getPresets(
                        deviceSpec.screenWidth,
                        deviceSpec.screenHeight,
                        deviceSpec.screenDpi
                    )

                    presets.forEach { preset ->
                        val isSelected = resolutionState.activePresetName == preset.name
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(
                                    1.dp,
                                    if (isSelected) NeonPurple else DarkCardBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.applyResolutionPreset(preset) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) NeonPurpleDark.copy(alpha = 0.35f) else DarkSurface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = preset.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) NeonPurpleLight else TextPrimary
                                        )
                                        if (preset.isRecommendedForLowEnd) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(CyberGreen.copy(alpha = 0.2f))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text("POCO / LOW-END REC", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = CyberGreen)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${preset.width}x${preset.height} @ ${preset.densityDpi} DPI • ${preset.gpuWorkloadPercent}% GPU Fillrate",
                                        fontSize = 11.sp,
                                        color = if (isSelected) CyberCyan else TextSecondary
                                    )
                                    Text(
                                        text = preset.description,
                                        fontSize = 10.sp,
                                        color = TextMuted,
                                        lineHeight = 14.sp
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = NeonPurpleLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ADB Command Box
                    val currentPreset = presets.find { it.name == resolutionState.activePresetName } ?: presets[2]
                    val cmd = ResolutionManager.generateAdbCommand(
                        currentPreset.width,
                        currentPreset.height,
                        currentPreset.densityDpi
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurface)
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ADB / Shizuku Command Generator:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Blox Booster ADB Command", cmd)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Command copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("COPY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = cmd,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = NeonPurpleLight
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Reset Resolution Button
                    OutlinedButton(
                        onClick = { viewModel.resetResolution() },
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = "Reset",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset to Device Native Resolution", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        }

        // 4. In-Game Roblox Recommended Settings Guide
        item {
            CyberCard(borderColor = DarkCardBorder) {
                Column {
                    Text(
                        text = "ROBLOX IN-GAME OPTIMIZATION BEST PRACTICES",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. In Roblox Settings, switch Graphics Mode to 'Manual' and set slider to 1 or 2. Blox Booster's Potato Visual Mode restores the color and contrast loss!\n" +
                                "2. Disable 'Camera Shake' to eliminate micro-stutters during combat abilities.\n" +
                                "3. Lower Roblox master volume to 50% on lower-end devices to relieve the audio thread from heavy sound mixing.\n" +
                                "4. In Android Developer Options, set 'Window Animation Scale' to 0.5x for faster UI navigation.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
