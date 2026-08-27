package com.example.data

import kotlinx.coroutines.flow.Flow

class GigRepository(private val gigDao: GigDao) {
    val allGigs: Flow<List<GigEntity>> = gigDao.getAllGigs()
    val exploreGigs: Flow<List<GigEntity>> = gigDao.getExploreGigs()
    val flaggedGigs: Flow<List<GigEntity>> = gigDao.getFlaggedGigs()
    val pendingGigs: Flow<List<GigEntity>> = gigDao.getPendingGigs()
    val savedOrAppliedGigs: Flow<List<GigEntity>> = gigDao.getSavedOrAppliedGigs()

    val activeCount: Flow<Int> = gigDao.getActiveCount()
    val pendingCount: Flow<Int> = gigDao.getPendingCount()
    val flaggedCount: Flow<Int> = gigDao.getFlaggedCount()

    fun getGigById(id: Long): Flow<GigEntity?> = gigDao.getGigById(id)

    suspend fun insertGig(gig: GigEntity): Long = gigDao.insertGig(gig)

    suspend fun updateGig(gig: GigEntity) = gigDao.updateGig(gig)

    suspend fun deleteGig(id: Long) = gigDao.deleteGigById(id)

    suspend fun flagGig(id: Long, reason: String) = gigDao.flagGig(id, reason)

    suspend fun clearFlags(id: Long) = gigDao.clearFlags(id)

    suspend fun setStatus(id: Long, status: String) = gigDao.setStatus(id, status)

    suspend fun toggleSaved(id: Long, current: Boolean) = gigDao.setSaved(id, !current)

    suspend fun setApplied(id: Long, applied: Boolean) = gigDao.setApplied(id, applied)
}
