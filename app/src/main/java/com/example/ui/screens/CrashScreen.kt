package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrashScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.crashState.collectAsState()
    val balance by viewModel.tokenBalance.collectAsState()

    var betInput by remember { mutableStateOf("100") }

    // Sync input defaults if game starts
    LaunchedEffect(state.isActive) {
        if (state.isActive) {
            betInput = state.betAmount.toString()
        }
    }

    // High history mock list for realistic visual display
    val previousCrashes = remember { listOf(1.45, 11.23, 1.05, 3.82, 2.15) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Crash Original", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Lobby",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CasinoDarkBg,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = CasinoDarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Balance Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = CasinoCardBg),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, CasinoSecondary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Previous history badges
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "History:",
                            color = DarkTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        previousCrashes.forEach { mult ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (mult >= 2.0) CasinoPrimary.copy(alpha = 0.15f) else CasinoError.copy(alpha = 0.15f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (mult >= 2.0) CasinoPrimary else CasinoError,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${String.format("%.2f", mult)}x",
                                    color = if (mult >= 2.0) CasinoPrimary else CasinoError,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Right: Balance
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Tokens",
                            tint = CasinoAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%,d", balance),
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Graph Flight Canvas Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CasinoCardBg)
                    .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background grid lines drawing
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Draw grid lines
                    val gridLinesCount = 5
                    for (i in 1 until gridLinesCount) {
                        val yPos = h * (i.toFloat() / gridLinesCount)
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.1f),
                            start = Offset(0f, yPos),
                            end = Offset(w, yPos),
                            strokeWidth = 1f
                        )
                        val xPos = w * (i.toFloat() / gridLinesCount)
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.1f),
                            start = Offset(xPos, 0f),
                            end = Offset(xPos, h),
                            strokeWidth = 1f
                        )
                    }
                }

                // Smooth exponential line drawing & Rocket flying animation
                if (state.isActive || state.isCrashed) {
                    val progressValue = min(1f, (state.currentMultiplier.toFloat() - 1f) / 10f)

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        val startX = 40f
                        val startY = h - 40f

                        val endX = startX + (w - startX - 60f) * progressValue
                        val endY = startY - (startY - 60f) * (progressValue * progressValue) // Quadratic curved ascent

                        // Curve Path drawing
                        val path = Path().apply {
                            moveTo(startX, startY)
                            // Draw curve using quadratic bezier
                            quadraticTo(
                                (startX + endX) / 1.5f,
                                startY,
                                endX,
                                endY
                            )
                        }

                        // Glow color mapping
                        val glowBrush = Brush.horizontalGradient(
                            colors = listOf(CasinoSecondary.copy(alpha = 0.3f), CasinoSecondary)
                        )

                        drawPath(
                            path = path,
                            brush = glowBrush,
                            style = Stroke(width = 6f)
                        )
                    }

                    // Floating Rocket Emoji or Explosion at current endpoint
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Estimate rocket percentage layout position on screen
                        val progressValue = kotlin.math.min(1.0, (state.currentMultiplier - 1.0) / 10.0)
                        val xPercent = progressValue
                        val yPercent = progressValue * progressValue

                        // Center indicator for current multiplier
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${String.format("%.2f", state.currentMultiplier)}x",
                                color = if (state.isCrashed) CasinoError else CasinoSecondary,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Black
                            )
                            if (state.isCrashed) {
                                Text(
                                    text = "CRASHED",
                                    color = CasinoError,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                )
                            } else {
                                Text(
                                    text = "LIVE MULTIPLIER",
                                    color = DarkTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        // Flying object
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp, vertical = 24.dp)
                        ) {
                            val alignX = -1f + (xPercent.toFloat() * 2f)
                            val alignY = 1f - (yPercent.toFloat() * 2f)

                            Box(
                                modifier = Modifier
                                    .align(androidx.compose.ui.BiasAlignment(alignX, alignY))
                                    .size(36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (state.isCrashed) "💥" else "🚀",
                                    fontSize = 28.sp
                                )
                            }
                        }
                    }
                } else {
                    // Empty or Idle screen before play
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🚀",
                            fontSize = 64.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Launch rocket & cash out before it explodes!",
                            color = DarkTextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Controls & Status
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Game Status Indicator
                Card(
                    colors = CardDefaults.cardColors(containerColor = CasinoCardBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.statusMessage,
                        color = if (state.isCrashed) CasinoError else if (state.isCashedOut) CasinoPrimary else Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }

                // Bet Amount Input (Only when inactive)
                OutlinedTextField(
                    value = betInput,
                    onValueChange = { if (!state.isActive) betInput = it.filter { char -> char.isDigit() } },
                    label = { Text("Bet Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CasinoSecondary,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                        focusedLabelColor = CasinoSecondary,
                        unfocusedLabelColor = DarkTextSecondary,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bet_input"),
                    enabled = !state.isActive,
                    maxLines = 1
                )

                // Large action button
                if (!state.isActive) {
                    Button(
                        onClick = {
                            val betVal = betInput.toLongOrNull() ?: 100L
                            viewModel.startCrash(betVal)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CasinoSecondary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("start_crash_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "LAUNCH ROCKET",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            letterSpacing = 1.sp
                        )
                    }
                } else {
                    // Running cashout button showing real-time earnings
                    val realTimeWinnings = (state.betAmount * state.currentMultiplier).toLong()

                    Button(
                        onClick = { viewModel.cashOutCrash() },
                        colors = ButtonDefaults.buttonColors(containerColor = CasinoAccent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("cashout_crash_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "CASH OUT ($realTimeWinnings tokens)",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}
