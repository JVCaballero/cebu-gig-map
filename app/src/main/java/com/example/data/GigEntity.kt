package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gigs")
data class GigEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // "Cover Band", "Marching Band", "Session", "Audition", "Collab"
    val dateText: String,
    val locationName: String,
    val posX: Float = 0.5f, // Normalized 0.0f..1.0f on map
    val posY: Float = 0.5f, // Normalized 0.0f..1.0f on map
    val payText: String = "Negotiable",
    val contactInfo: String = "contact@example.com",
    val description: String = "",
    val status: String = "ACTIVE", // "ACTIVE", "FILLED", "PENDING_REVIEW", "HIDDEN"
    val flagCount: Int = 0,
    val flagReason: String = "", // e.g. "Inappropriate Content", "Spam / Duplicate", "Wrong Category"
    val postedTime: Long = System.currentTimeMillis(),
    val isSaved: Boolean = false,
    val hasApplied: Boolean = false,
    val posterName: String = "Community Member"
)
