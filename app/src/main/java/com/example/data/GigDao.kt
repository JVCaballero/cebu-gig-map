package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GigDao {
    @Query("SELECT * FROM gigs ORDER BY postedTime DESC")
    fun getAllGigs(): Flow<List<GigEntity>>

    @Query("SELECT * FROM gigs WHERE status != 'HIDDEN' ORDER BY postedTime DESC")
    fun getExploreGigs(): Flow<List<GigEntity>>

    @Query("SELECT * FROM gigs WHERE flagCount > 0 ORDER BY flagCount DESC, postedTime DESC")
    fun getFlaggedGigs(): Flow<List<GigEntity>>

    @Query("SELECT * FROM gigs WHERE status = 'PENDING_REVIEW' ORDER BY postedTime DESC")
    fun getPendingGigs(): Flow<List<GigEntity>>

    @Query("SELECT * FROM gigs WHERE isSaved = 1 OR hasApplied = 1 ORDER BY postedTime DESC")
    fun getSavedOrAppliedGigs(): Flow<List<GigEntity>>

    @Query("SELECT * FROM gigs WHERE id = :id LIMIT 1")
    fun getGigById(id: Long): Flow<GigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGig(gig: GigEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(gigs: List<GigEntity>)

    @Update
    suspend fun updateGig(gig: GigEntity)

    @Query("DELETE FROM gigs WHERE id = :id")
    suspend fun deleteGigById(id: Long)

    @Query("UPDATE gigs SET flagCount = flagCount + 1, flagReason = :reason WHERE id = :id")
    suspend fun flagGig(id: Long, reason: String)

    @Query("UPDATE gigs SET flagCount = 0, flagReason = '' WHERE id = :id")
    suspend fun clearFlags(id: Long)

    @Query("UPDATE gigs SET status = :status WHERE id = :id")
    suspend fun setStatus(id: Long, status: String)

    @Query("UPDATE gigs SET isSaved = :isSaved WHERE id = :id")
    suspend fun setSaved(id: Long, isSaved: Boolean)

    @Query("UPDATE gigs SET hasApplied = :hasApplied WHERE id = :id")
    suspend fun setApplied(id: Long, hasApplied: Boolean)

    @Query("SELECT COUNT(*) FROM gigs WHERE status = 'ACTIVE'")
    fun getActiveCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM gigs WHERE status = 'PENDING_REVIEW'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM gigs WHERE flagCount > 0")
    fun getFlaggedCount(): Flow<Int>
}
