package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1,
    val tokenBalance: Long = 5000L,
    val totalSpins: Int = 0,
    val totalWins: Int = 0,
    val totalLosses: Int = 0,
    val highestBalance: Long = 5000L
)
