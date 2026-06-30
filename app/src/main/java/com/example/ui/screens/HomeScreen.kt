package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel

@Composable
fun HomeScreen(
    viewModel: GameViewModel,
    onNavigateToMines: () -> Unit,
    onNavigateToCrash: () -> Unit,
    modifier: Modifier = Modifier
) {
    val balance by viewModel.tokenBalance.collectAsState()
    val stats by viewModel.userStats.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CasinoDarkBg)
    ) {
        // 1. Status Bar / Header (Profile Header)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Profile Avatar with dynamic gradient
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF00FFA3), Color(0xFF00A3FF))
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🤵", fontSize = 20.sp)
                }

                // Profile Info
                Column {
                    Text(
                        text = "HIGH ROLLER",
                        color = DarkTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "CryptoKing_88",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Settings Button
            IconButton(
                onClick = { /* No-op / decorative */ },
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CasinoCardBg)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = DarkTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Scrollable content
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 2. Global Token Balance Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("balance_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.dp, CasinoPrimary.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF18181B), Color(0xFF09090B))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "AVAILABLE BALANCE",
                                        color = CasinoPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 2.sp
                                    )
                                    Row(
                                        verticalAlignment = Alignment.Bottom,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = String.format("%,d", balance),
                                            color = Color.White,
                                            fontSize = 36.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = (-1).sp
                                        )
                                        Text(
                                            text = "TOKENS",
                                            color = CasinoPrimary,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                    }
                                }

                                // Wallet icon box
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF27272A).copy(alpha = 0.5f))
                                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("💳", fontSize = 18.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Linear Progress Bar toward 1M goal
                            val progress = (balance.toFloat() / 1_000_000f).coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF27272A))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(progress)
                                        .background(CasinoPrimary)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Progress to 1M: ${String.format("%.2f", progress * 100)}%",
                                color = DarkTextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Stats Sub-row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CasinoCardBg),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("🎰", fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("TOTAL PLAYS", color = DarkTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("${stats.totalSpins}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CasinoCardBg),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("📈", fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("HIGHEST BALANCE", color = DarkTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(String.format("%,d", stats.highestBalance), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 3. Game Selection Bento Grid Header
            item {
                Text(
                    text = "STAKE ORIGINALS BENTO GRID",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Rows representing the Bento Grid of Games
            // Row 1: Mines (Large) & Crash (Large)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Mines Large Card
                    MinesBentoCard(
                        onClick = onNavigateToMines,
                        modifier = Modifier.weight(1f)
                    )

                    // Crash Large Card
                    CrashBentoCard(
                        onClick = onNavigateToCrash,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Row 2: Plinko & Dice (Teaser cards to prepare user instructions and retain high design fidelity)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TeaserBentoCard(
                        title = "Plinko",
                        subtitle = "Drop & Win",
                        emoji = "🔵",
                        accentColor = Color(0xFFFB923C), // Orange
                        modifier = Modifier.weight(1f)
                    )
                    TeaserBentoCard(
                        title = "Dice",
                        subtitle = "Predict Roll",
                        emoji = "🎲",
                        accentColor = Color(0xFF60A5FA), // Blue
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Row 3: Blackjack & More
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TeaserBentoCard(
                        title = "Blackjack",
                        subtitle = "Hit or Stand",
                        emoji = "🃏",
                        accentColor = Color(0xFFF87171), // Red
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(68.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CasinoDarkBg)
                            .border(BorderStroke(1.dp, Brush.linearGradient(listOf(Color.Gray.copy(alpha = 0.3f), Color.Transparent))), RoundedCornerShape(16.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("➕", fontSize = 16.sp)
                            Text(
                                text = "7 MORE",
                                color = DarkTextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 4. Bottom Navigation Bar representing UI layout
        Surface(
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(0.dp))
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    label = "Home",
                    emoji = "🏠",
                    isActive = true
                )
                BottomNavItem(
                    label = "Games",
                    emoji = "🎮",
                    isActive = false
                )
                BottomNavItem(
                    label = "Vault",
                    emoji = "🏦",
                    isActive = false
                )
                BottomNavItem(
                    label = "Rank",
                    emoji = "🎖️",
                    isActive = false
                )
            }
        }
    }
}

@Composable
fun MinesBentoCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable { onClick() }
            .testTag("mines_game_button"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CasinoCardBg),
        border = BorderStroke(1.dp, CasinoPrimary.copy(alpha = 0.15f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            // Icon overlay top-right
            Text(
                text = "💥",
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.TopEnd)
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Mines",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "25 Multiplier Slots",
                        color = DarkTextSecondary,
                        fontSize = 10.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .background(CasinoPrimary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "LIVE",
                        color = CasinoPrimary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CrashBentoCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable { onClick() }
            .testTag("crash_game_button"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CasinoCardBg),
        border = BorderStroke(1.dp, CasinoSecondary.copy(alpha = 0.15f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            // Icon overlay top-right
            Text(
                text = "📈",
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.TopEnd)
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Crash",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Real-time Multiplier",
                        color = DarkTextSecondary,
                        fontSize = 10.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .background(CasinoSecondary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "HOT",
                        color = CasinoSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TeaserBentoCard(
    title: String,
    subtitle: String,
    emoji: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(68.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CasinoCardBg),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(accentColor.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                    .border(1.dp, accentColor.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 18.sp)
            }
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = DarkTextSecondary,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
fun BottomNavItem(
    label: String,
    emoji: String,
    isActive: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.clickable { /* No-op decoration */ }
    ) {
        Text(
            text = emoji,
            fontSize = 20.sp,
            color = if (isActive) CasinoPrimary else DarkTextSecondary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label.uppercase(),
            color = if (isActive) CasinoPrimary else DarkTextSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}
