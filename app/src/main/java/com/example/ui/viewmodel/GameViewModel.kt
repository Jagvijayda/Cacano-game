package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.UserStats
import com.example.data.repository.UserStatsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.exp
import kotlin.math.max

enum class GlobalOverlay {
    NONE,
    BANKRUPT,
    MILLIONAIRE
}

// ==========================================
// Mines Game State
// ==========================================
data class MinesTile(
    val index: Int,
    val isMine: Boolean,
    val isRevealed: Boolean
)

data class MinesState(
    val betAmount: Long = 100L,
    val minesCount: Int = 3,
    val tiles: List<MinesTile> = emptyList(),
    val isActive: Boolean = false,
    val revealedSafeCount: Int = 0,
    val currentMultiplier: Double = 1.0,
    val isGameOver: Boolean = false,
    val isCashedOut: Boolean = false,
    val hasHitMine: Boolean = false,
    val winnings: Long = 0L,
    val statusMessage: String = "Enter bet & select number of mines to start."
)

// ==========================================
// Crash Game State
// ==========================================
data class CrashState(
    val betAmount: Long = 100L,
    val isActive: Boolean = false,
    val currentMultiplier: Double = 1.0,
    val crashPoint: Double = 0.0,
    val isCrashed: Boolean = false,
    val isCashedOut: Boolean = false,
    val winnings: Long = 0L,
    val statusMessage: String = "Place your bet and start the countdown!"
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = UserStatsRepository(database.userStatsDao())

    // Unified Token Economy States
    private val _tokenBalance = MutableStateFlow(5000L)
    val tokenBalance: StateFlow<Long> = _tokenBalance.asStateFlow()

    private val _userStats = MutableStateFlow(UserStats())
    val userStats: StateFlow<UserStats> = _userStats.asStateFlow()

    private val _globalOverlay = MutableStateFlow(GlobalOverlay.NONE)
    val globalOverlay: StateFlow<GlobalOverlay> = _globalOverlay.asStateFlow()

    // Game-specific states
    private val _minesState = MutableStateFlow(MinesState())
    val minesState: StateFlow<MinesState> = _minesState.asStateFlow()

    private val _crashState = MutableStateFlow(CrashState())
    val crashState: StateFlow<CrashState> = _crashState.asStateFlow()

    // Crash ticking coroutine job
    private var crashJob: Job? = null
    private val random = Random()

    init {
        // Load initial balance and stats from database
        viewModelScope.launch {
            repository.userStatsFlow.collect { stats ->
                if (stats != null) {
                    _userStats.value = stats
                    _tokenBalance.value = stats.tokenBalance
                    checkGlobalOverlay(stats.tokenBalance)
                } else {
                    // Create default user stats
                    val defaultStats = UserStats()
                    repository.updateStats(defaultStats)
                    _userStats.value = defaultStats
                    _tokenBalance.value = defaultStats.tokenBalance
                }
            }
        }
    }

    // ==========================================
    // Unified Token Economy Logic
    // ==========================================
    private fun checkGlobalOverlay(balance: Long) {
        when {
            balance >= 1000000L -> {
                _globalOverlay.value = GlobalOverlay.MILLIONAIRE
            }
            balance < 20 -> {
                _globalOverlay.value = GlobalOverlay.BANKRUPT
            }
            else -> {
                _globalOverlay.value = GlobalOverlay.NONE
            }
        }
    }

    fun resetEconomy() {
        viewModelScope.launch {
            val freshStats = UserStats(
                id = 1,
                tokenBalance = 5000L,
                totalSpins = 0,
                totalWins = 0,
                totalLosses = 0,
                highestBalance = 5000L
            )
            repository.updateStats(freshStats)
            _globalOverlay.value = GlobalOverlay.NONE
            // Reset game states
            _minesState.value = MinesState()
            _crashState.value = CrashState()
            crashJob?.cancel()
        }
    }

    private fun adjustBalance(amountChange: Long, isWin: Boolean) {
        viewModelScope.launch {
            val currentStats = repository.getStats()
            val newBalance = max(0, currentStats.tokenBalance + amountChange)
            val newSpins = currentStats.totalSpins + 1
            val newWins = if (isWin) currentStats.totalWins + 1 else currentStats.totalWins
            val newLosses = if (!isWin && amountChange < 0) currentStats.totalLosses + 1 else currentStats.totalLosses
            val maxBal = max(currentStats.highestBalance, newBalance)

            val updatedStats = currentStats.copy(
                tokenBalance = newBalance,
                totalSpins = newSpins,
                totalWins = newWins,
                totalLosses = newLosses,
                highestBalance = maxBal
            )
            repository.updateStats(updatedStats)
            _tokenBalance.value = newBalance
            checkGlobalOverlay(newBalance)
        }
    }

    // ==========================================
    // MINES GAME LOGIC
    // ==========================================
    fun startMines(bet: Long, mines: Int) {
        if (_minesState.value.isActive) return
        if (bet < 10) {
            _minesState.update { it.copy(statusMessage = "Minimum bet is 10 tokens!") }
            return
        }
        if (bet > _tokenBalance.value) {
            _minesState.update { it.copy(statusMessage = "Insufficient balance!") }
            return
        }
        if (mines < 1 || mines > 24) {
            _minesState.update { it.copy(statusMessage = "Mines must be between 1 and 24!") }
            return
        }

        // Deduct bet and initialize grid
        adjustBalance(-bet, isWin = false)

        val shuffledIndices = (0 until 25).shuffled()
        val mineIndices = shuffledIndices.take(mines).toSet()

        val initialTiles = (0 until 25).map { idx ->
            MinesTile(index = idx, isMine = mineIndices.contains(idx), isRevealed = false)
        }

        _minesState.value = MinesState(
            betAmount = bet,
            minesCount = mines,
            tiles = initialTiles,
            isActive = true,
            revealedSafeCount = 0,
            currentMultiplier = 1.0,
            isGameOver = false,
            isCashedOut = false,
            hasHitMine = false,
            winnings = 0L,
            statusMessage = "Mines active! Tap tile to reveal."
        )
    }

    fun revealMinesTile(index: Int) {
        val current = _minesState.value
        if (!current.isActive || current.isGameOver || current.isCashedOut) return

        val tile = current.tiles.getOrNull(index) ?: return
        if (tile.isRevealed) return

        // Update tile to revealed
        val updatedTiles = current.tiles.map { t ->
            if (t.index == index) t.copy(isRevealed = true) else t
        }

        if (tile.isMine) {
            // Hit Mine! Game over. Reveal all mines.
            val finalTiles = updatedTiles.map { t ->
                if (t.isMine) t.copy(isRevealed = true) else t
            }
            _minesState.value = current.copy(
                tiles = finalTiles,
                isActive = false,
                isGameOver = true,
                hasHitMine = true,
                currentMultiplier = 0.0,
                statusMessage = "BOOM! You hit a mine. Bet of ${current.betAmount} lost."
            )
            // Save state loss (already deducted)
        } else {
            // Safe tile!
            val newSafeCount = current.revealedSafeCount + 1
            val newMult = getMinesMultiplier(current.minesCount, newSafeCount)
            val potentialWinnings = (current.betAmount * newMult).toLong()

            val maxSafeTiles = 25 - current.minesCount
            if (newSafeCount == maxSafeTiles) {
                // All safe tiles revealed! Auto cashout.
                _minesState.value = current.copy(
                    tiles = updatedTiles,
                    isActive = false,
                    isGameOver = true,
                    isCashedOut = true,
                    revealedSafeCount = newSafeCount,
                    currentMultiplier = newMult,
                    winnings = potentialWinnings,
                    statusMessage = "Perfect! All safe tiles revealed. Payout: $potentialWinnings"
                )
                adjustBalance(potentialWinnings, isWin = true)
            } else {
                _minesState.value = current.copy(
                    tiles = updatedTiles,
                    revealedSafeCount = newSafeCount,
                    currentMultiplier = newMult,
                    winnings = potentialWinnings,
                    statusMessage = "Safe! Next payout: ${(current.betAmount * getMinesMultiplier(current.minesCount, newSafeCount + 1)).toLong()} tokens."
                )
            }
        }
    }

    fun cashOutMines() {
        val current = _minesState.value
        if (!current.isActive || current.isGameOver || current.isCashedOut || current.revealedSafeCount == 0) return

        val finalWinnings = (current.betAmount * current.currentMultiplier).toLong()

        // Reveal remaining mines as a helpful visual feedback
        val finalTiles = current.tiles.map { t ->
            if (t.isMine) t.copy(isRevealed = true) else t
        }

        _minesState.value = current.copy(
            tiles = finalTiles,
            isActive = false,
            isGameOver = true,
            isCashedOut = true,
            winnings = finalWinnings,
            statusMessage = "Cashed out at ${String.format("%.2f", current.currentMultiplier)}x! Won $finalWinnings tokens."
        )
        adjustBalance(finalWinnings, isWin = true)
    }

    private fun getMinesMultiplier(mines: Int, revealedSafe: Int): Double {
        if (revealedSafe == 0) return 1.0
        if (revealedSafe > 25 - mines) return 0.0
        var mult = 1.0
        for (i in 0 until revealedSafe) {
            mult *= (25.0 - i) / (25.0 - mines - i)
        }
        return mult * 0.98 // 2% House Edge
    }

    // ==========================================
    // CRASH GAME LOGIC
    // ==========================================
    fun startCrash(bet: Long) {
        if (_crashState.value.isActive) return
        if (bet < 10) {
            _crashState.update { it.copy(statusMessage = "Minimum bet is 10 tokens!") }
            return
        }
        if (bet > _tokenBalance.value) {
            _crashState.update { it.copy(statusMessage = "Insufficient balance!") }
            return
        }

        adjustBalance(-bet, isWin = false)

        // Generate crash point
        // 3% instant crash
        val targetPoint = if (random.nextDouble() < 0.03) {
            1.00
        } else {
            val u = random.nextDouble()
            max(1.01, 0.99 / (1.0 - u))
        }

        _crashState.value = CrashState(
            betAmount = bet,
            isActive = true,
            currentMultiplier = 1.00,
            crashPoint = targetPoint,
            isCrashed = false,
            isCashedOut = false,
            winnings = 0L,
            statusMessage = "Rocket launched! Ticking..."
        )

        // Start countdown/ticker
        crashJob?.cancel()
        crashJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            var currentMult = 1.00

            while (currentMult < targetPoint) {
                delay(30)
                val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0
                // Smooth exponential multiplier increase
                currentMult = exp(0.12 * elapsedSeconds)

                if (currentMult >= targetPoint) {
                    currentMult = targetPoint
                    break
                }

                _crashState.update {
                    it.copy(
                        currentMultiplier = currentMult,
                        statusMessage = "Multiplier: ${String.format("%.2f", currentMult)}x..."
                    )
                }
            }

            // CRASHED!
            _crashState.update {
                it.copy(
                    isActive = false,
                    isCrashed = true,
                    currentMultiplier = targetPoint,
                    statusMessage = "CRASHED at ${String.format("%.2f", targetPoint)}x! Bet of ${it.betAmount} tokens lost."
                )
            }
        }
    }

    fun cashOutCrash() {
        val current = _crashState.value
        if (!current.isActive || current.isCrashed || current.isCashedOut) return

        crashJob?.cancel() // Stop the crash timer

        val payoutMult = current.currentMultiplier
        val wonAmount = (current.betAmount * payoutMult).toLong()

        _crashState.value = current.copy(
            isActive = false,
            isCashedOut = true,
            winnings = wonAmount,
            statusMessage = "Successfully cashed out at ${String.format("%.2f", payoutMult)}x! Won $wonAmount tokens!"
        )
        adjustBalance(wonAmount, isWin = true)
    }

    override fun onCleared() {
        super.onCleared()
        crashJob?.cancel()
    }
}
