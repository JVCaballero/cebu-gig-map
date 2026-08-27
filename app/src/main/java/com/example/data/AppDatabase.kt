package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [GigEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gigDao(): GigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cebu_gig_database"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.gigDao())
                }
            }
        }

        private suspend fun populateInitialData(dao: GigDao) {
            val initialGigs = listOf(
                GigEntity(
                    title = "Sinulog Festival Stage",
                    category = "Cover Band",
                    dateText = "Jan 15, 2024 • 6:00 PM",
                    locationName = "Fuente Osmeña Circle",
                    posX = 0.43f,
                    posY = 0.44f,
                    payText = "₱5,000 / set",
                    contactInfo = "sinulogstage@cebucreative.ph",
                    description = "High-energy cover band needed for 2-hour festival stage slot at Fuente Osmeña. Full backline, monitors, and stage lighting provided. Setlist should feature local Vispop and OPM classics.",
                    status = "ACTIVE",
                    posterName = "Sinulog Music Council",
                    postedTime = System.currentTimeMillis() - 1000 * 3600 * 2
                ),
                GigEntity(
                    title = "Barangay Fiesta Parade",
                    category = "Marching Band",
                    dateText = "Feb 2, 2024 • 8:00 AM",
                    locationName = "Guadalupe",
                    posX = 0.38f,
                    posY = 0.38f,
                    payText = "₱15,000 / group",
                    contactInfo = "fiesta@guadalupe.gov.ph",
                    description = "Marching band needed for annual Guadalupe fiesta morning parade route (3km). Water stations and snacks provided for the entire band. Energetic cadence preferred.",
                    status = "ACTIVE",
                    posterName = "Guadalupe Youth Council",
                    postedTime = System.currentTimeMillis() - 1000 * 3600 * 5
                ),
                GigEntity(
                    title = "Urgent: Session Bassist",
                    category = "Session",
                    dateText = "Tonight • 9:00 PM",
                    locationName = "IT Park, Lahug",
                    posX = 0.52f,
                    posY = 0.28f,
                    payText = "₱4,500 / night",
                    contactInfo = "funkcollective@groove.ph",
                    description = "Urgent sub bassist required for indie funk/soul set tonight at IT Park open-air lounge. Charts and audio demos provided immediately upon confirmation.",
                    status = "ACTIVE",
                    posterName = "The Groove Collective",
                    postedTime = System.currentTimeMillis() - 1000 * 3600 * 1
                ),
                GigEntity(
                    title = "Indie Rock Band Audition",
                    category = "Audition",
                    dateText = "Saturday • 2:00 PM",
                    locationName = "Ayala Center Cebu",
                    posX = 0.54f,
                    posY = 0.38f,
                    payText = "₱6,000 / gig",
                    contactInfo = "auditions@cebuindie.org",
                    description = "Open auditions for lead vocalists and rhythm guitarists for an upcoming 6-city Visayas regional tour. Bring 2 prepared original or cover songs.",
                    status = "ACTIVE",
                    posterName = "Cebu Indie Records",
                    postedTime = System.currentTimeMillis() - 1000 * 3600 * 12
                ),
                GigEntity(
                    title = "Acoustic Duo Collab - Mango Ave",
                    category = "Collab",
                    dateText = "Every Friday • 8:00 PM",
                    locationName = "General Maxilom Ave",
                    posX = 0.48f,
                    posY = 0.46f,
                    payText = "₱3,000 / set",
                    contactInfo = "lounge@mangotavern.ph",
                    description = "Looking for a female vocalist or cajon player for a weekly acoustic residency. Chill indie pop, Vispop, and mellow R&B repertoire.",
                    status = "ACTIVE",
                    posterName = "Mango Tavern",
                    postedTime = System.currentTimeMillis() - 1000 * 3600 * 20
                ),
                GigEntity(
                    title = "Studio Bassist Needed",
                    category = "Session",
                    dateText = "Past (Dec 10)",
                    locationName = "Mandaue City",
                    posX = 0.65f,
                    posY = 0.30f,
                    payText = "₱3,500 / session",
                    contactInfo = "tracks@cebusound.com",
                    description = "Studio session bassist needed for recording 4 EP tracks at our Mandaue studio. (Position filled)",
                    status = "FILLED",
                    posterName = "Cebu Sound Lab",
                    postedTime = System.currentTimeMillis() - 1000 * 3600 * 48
                ),
                GigEntity(
                    title = "Urgent: Drummer Needed for Wedding",
                    category = "Session",
                    dateText = "Oct 24, 2024 • 4:00 PM",
                    locationName = "Talisay Seaside",
                    posX = 0.26f,
                    posY = 0.72f,
                    payText = "₱7,000 / event",
                    contactInfo = "johndoe@email.com",
                    description = "Drummer needed for seaside wedding reception. Standard love songs and dance floor pop set.",
                    status = "ACTIVE",
                    flagCount = 3,
                    flagReason = "Inappropriate Content",
                    posterName = "John Doe",
                    postedTime = System.currentTimeMillis() - 1000 * 3600 * 60
                ),
                GigEntity(
                    title = "Looking for a Bassist - Cover Band",
                    category = "Cover Band",
                    dateText = "Oct 23, 2024 • 7:00 PM",
                    locationName = "Talamban",
                    posX = 0.58f,
                    posY = 0.18f,
                    payText = "₱5,000 / gig",
                    contactInfo = "thevibe@cebubands.com",
                    description = "Seeking permanent bassist for 90s alternative cover band. Weekly rehearsals in Talamban.",
                    status = "ACTIVE",
                    flagCount = 1,
                    flagReason = "Spam / Duplicate",
                    posterName = "The Vibe",
                    postedTime = System.currentTimeMillis() - 1000 * 3600 * 72
                ),
                GigEntity(
                    title = "Session Guitarist Available - Studio Only",
                    category = "Session",
                    dateText = "Oct 20, 2024 • 1:00 PM",
                    locationName = "SM Seaside SRP",
                    posX = 0.35f,
                    posY = 0.65f,
                    payText = "₱4,000 / session",
                    contactInfo = "maria.santos@music.ph",
                    description = "Experienced session guitarist available for remote or in-studio tracking.",
                    status = "ACTIVE",
                    flagCount = 2,
                    flagReason = "Wrong Category",
                    posterName = "Maria Santos",
                    postedTime = System.currentTimeMillis() - 1000 * 3600 * 96
                )
            )
            dao.insertAll(initialGigs)
        }
    }
}
