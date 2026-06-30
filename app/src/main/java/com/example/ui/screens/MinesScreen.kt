package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinesScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.minesState.collectAsState()
    val balance by viewModel.tokenBalance.collectAsState()

    var betInput by remember { mutableStateOf("100") }
    var minesInput by remember { mutableStateOf("3") }

    // Sync input defaults if game starts
    LaunchedEffect(state.isActive) {
        if (state.isActive) {
            betInput = state.betAmount.toString()
            minesInput = state.minesCount.toString()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Mines Original", fontWeight = FontWeight.Bold) },
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
            // Top: Balance indicator
            Card(
                colors = CardDefaults.cardColors(containerColor = CasinoCardBg),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, CasinoPrimary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Balance:",
                        color = DarkTextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
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
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Game Board (5x5 Grid)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CasinoCardBg)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (state.tiles.isEmpty()) {
                    // Empty grid placeholder prior to game launch
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "💣",
                            fontSize = 64.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Set bet and mines, then press Start Game",
                            color = DarkTextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.tiles) { tile ->
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            tile.isRevealed && tile.isMine -> CasinoError
                                            tile.isRevealed && !tile.isMine -> CasinoPrimary.copy(alpha = 0.2f)
                                            else -> CasinoDarkBg
                                        }
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = when {
                                            tile.isRevealed && tile.isMine -> CasinoError
                                            tile.isRevealed && !tile.isMine -> CasinoPrimary
                                            else -> Color.Gray.copy(alpha = 0.4f)
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable(enabled = state.isActive && !tile.isRevealed) {
                                        viewModel.revealMinesTile(tile.index)
                                    }
                                    .testTag("tile_${tile.index}"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (tile.isRevealed) {
                                    Text(
                                        text = if (tile.isMine) "💥" else "💎",
                                        fontSize = 22.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Control Inputs
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Status message
                Card(
                    colors = CardDefaults.cardColors(containerColor = CasinoCardBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.statusMessage,
                        color = if (state.hasHitMine) CasinoError else if (state.isCashedOut) CasinoPrimary else Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Bet Amount Input
                    OutlinedTextField(
                        value = betInput,
                        onValueChange = { if (!state.isActive) betInput = it.filter { char -> char.isDigit() } },
                        label = { Text("Bet Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CasinoPrimary,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            focusedLabelColor = CasinoPrimary,
                            unfocusedLabelColor = DarkTextSecondary,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("bet_input"),
                        enabled = !state.isActive,
                        maxLines = 1
                    )

                    // Number of Mines Input
                    OutlinedTextField(
                        value = minesInput,
                        onValueChange = { if (!state.isActive) minesInput = it.filter { char -> char.isDigit() } },
                        label = { Text("Mines (1-24)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CasinoPrimary,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            focusedLabelColor = CasinoPrimary,
                            unfocusedLabelColor = DarkTextSecondary,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("mines_count_input"),
                        enabled = !state.isActive,
                        maxLines = 1
                    )
                }

                // Current Multiplier indicators (only active when playing)
                if (state.isActive && state.revealedSafeCount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Current Multiplier:",
                            color = DarkTextSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${String.format("%.2f", state.currentMultiplier)}x",
                            color = CasinoPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Play / Cashout Action Button
                if (!state.isActive) {
                    Button(
                        onClick = {
                            val betVal = betInput.toLongOrNull() ?: 100L
                            val minesVal = minesInput.toIntOrNull() ?: 3
                            viewModel.startMines(betVal, minesVal)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CasinoPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("start_mines_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "START GAME",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            letterSpacing = 1.sp
                        )
                    }
                } else {
                    Button(
                        onClick = { viewModel.cashOutMines() },
                        colors = ButtonDefaults.buttonColors(containerColor = CasinoAccent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("cashout_mines_button"),
                        shape = RoundedCornerShape(12.dp),
                        enabled = state.revealedSafeCount > 0
                    ) {
                        Text(
                            text = "CASH OUT (${state.winnings} tokens)",
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
