package com.example.data.repository

import com.example.data.database.UserStatsDao
import com.example.data.model.UserStats
import kotlinx.coroutines.flow.Flow

class UserStatsRepository(private val userStatsDao: UserStatsDao) {
    val userStatsFlow: Flow<UserStats?> = userStatsDao.getUserStatsFlow()

    suspend fun getStats(): UserStats {
        return userStatsDao.getUserStats() ?: UserStats()
    }

    suspend fun updateStats(stats: UserStats) {
        userStatsDao.insertOrUpdate(stats)
    }
}
