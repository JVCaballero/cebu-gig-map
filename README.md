# Cebu Creative Gig Map (Beta Scaffolding)

> **Connecting Cebu's Creative Pulse** — An interactive geospatial platform and community gig board designed for Cebuano musicians, session artists, marching bands, indie acts, and venue organizers across Cebu City, Mactan, Mandaue, and surrounding areas.

---

## 1. Executive Summary & Vision

The **Cebu Creative Gig Map** bridges the gap in the local live music ecosystem. Rather than relying on fragmented social media groups, temporary chat threads, and word-of-mouth recommendations, this application unites venue owners, bandleaders, session musicians, and festival coordinators on a shared, real-time interactive canvas. 

This repository currently contains the **scaffolding prototype**, featuring a complete Android Jetpack Compose client, reactive MVVM architecture, an offline-first Room persistence layer, a custom high-performance 2D vector map engine tailored to Cebu's geography, an icon-only navigation system, and interactive moderation workflows.

---

## 2. Architecture & Current Implementation

```
app/src/main/java/com/example/
├── MainActivity.kt                  # Entry point, Edge-to-Edge setup, Icon Navigation, Theme controller
├── data/
│   ├── AppDatabase.kt               # Room database setup & pre-populated seeding
│   ├── GigDao.kt                    # Reactive Flow queries, status filters, CRUD operations
│   ├── GigEntity.kt                 # Data models for gigs, applicant lists, coordinates, flags
│   └── GigRepository.kt             # Data access repository coordinating Room with UI flows
├── ui/
│   ├── components/
│   │   ├── ApplyDialog.kt           # Role/Instrument application submission modal
│   │   ├── CebuMapCanvas.kt         # Custom 2D vector map engine with pan, zoom, & landmark snapping
│   │   ├── CommonUi.kt              # Reusable Category badges, tactile bounce modifiers, cards
│   │   ├── FlagDialog.kt            # Community reporting dialog with reason categorization
│   │   └── NotificationsDialog.kt   # Community activity and moderation updates modal
│   ├── screens/
│   │   ├── AdminDashboardScreen.kt  # Moderation analytics, queue management, and content actions
│   │   ├── HomeScreen.kt            # Hero hub, mission highlights, featured gigs, "How It Works"
│   │   ├── MapExploreScreen.kt      # Interactive live map, category filtering, search, Map/List view
│   │   ├── PostGigScreen.kt         # Pin-dropping opportunity poster with auto-landmark detection
│   │   └── SavedGigsScreen.kt       # Musician portfolio of saved gigs and active applications
│   └── theme/
│       ├── Color.kt                 # Electric Teal, Sunset Orange, and Cebu Map palette tokens
│       ├── Theme.kt                 # Material 3 dynamic & custom Dark/Light color schemes
│       └── Type.kt                  # Clean typography hierarchy
```

### Core Technologies
- **UI Toolkit**: Modern **Jetpack Compose** with Material Design 3 (M3).
- **State Management**: Kotlin Coroutines & `StateFlow` leveraging `collectAsStateWithLifecycle` inside `GigViewModel`.
- **Local Persistence**: **Room Database 2.6.1** with SQLite caching and pre-seeded sample gigs across Cebu's key creative districts (Fuente Osmeña, IT Park Lahug, Ayala Center, Mandaue, Mactan, Colon Street).
- **Custom Map Engine**: Hardware-accelerated Compose `Canvas` with gesture transformations (pinch-to-zoom, pan, bounds clamping), trigonometric landmark proximity detection, and animated radar pulses.
- **Haptics & Tactile Physics**: Custom `bounceClickable` modifier for spring-physics scale feedback on all interactive elements.

---

## 3. Scaffolding Capabilities Implemented

1. **Home / Hero Dashboard (`HomeScreen.kt`)**:
   - Welcome banner detailing Cebu's live music challenge and platform solutions.
   - Interactive KPI counters displaying active opportunities in the province.
   - Featured Gig Spotlight with direct routing to the interactive map.
   - "How It Works" structured bento guide for both organizers and musicians.

2. **Live Map & Feed Explorer (`MapExploreScreen.kt`)**:
   - Dual-mode view: switchable between the interactive **2D Vector Map** and a structured **Scrollable List Feed**.
   - Real-time search across venue names, band genres, instruments, and poster details.
   - Category filtering: *Cover Band, Marching Band, Session, Audition, Collab*.
   - Floating interactive detail preview card with single-tap bookmarking, application, and flagging.

3. **Geospatial Pin Dropper (`PostGigScreen.kt`)**:
   - Interactive mini-map allowing organizers to tap anywhere in Metro Cebu to drop a venue pin.
   - Proximity math (`getNearestLandmark`) that automatically snaps and names the nearest landmark (e.g., Busay, SM Seaside, IT Park).
   - Form fields for date/time, compensation (e.g., *₱5,000 / set*), contact details, and requirements.

4. **Moderation Dashboard (`AdminDashboardScreen.kt`)**:
   - Moderation analytics (Total Active Gigs, Pending Reviews, Recent Flags).
   - Flag queue search and reason-specific filtering (*Spam, Inappropriate, Wrong Category*).
   - Quick action controls: **Approve (Green)**, **Hide/Unhide (Secondary Outlined)**, and **Delete (Error)**.

5. **Musician Portfolio (`SavedGigsScreen.kt`)**:
   - Consolidated portfolio view showing bookmarked gigs and submitted applications.

---

## 4. Multi-Phase Improvement & Scaling Roadmap

To elevate this prototype into a production-grade platform supporting tens of thousands of simultaneous users across the Philippines, the following phased roadmap is planned:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            DEVELOPMENT PHASES                               │
├─────────────────┬─────────────────┬─────────────────┬───────────────────────┤
│ PHASE 1         │ PHASE 2         │ PHASE 3         │ PHASE 4 & 5           │
│ Backend & Cloud │ Google Maps SDK │ AI Moderation   │ Escrow Payments       │
│ Sync & Auth     │ & Geofencing    │ & Matchmaking   │ & QuadTree Scale      │
└─────────────────┴─────────────────┴─────────────────┴───────────────────────┘
```

### Phase 1: Backend Architecture & Cloud Synchronization
* **Google Sign-In & Role-Based Auth**:
  - Integrate Android Jetpack `CredentialManager` with Google Identity Services.
  - Implement user roles: `Musician/Artist`, `Bandleader/Manager`, `Venue/Bar Owner`, and `Platform Moderator`.
* **Cloud Database & Realtime Sync**:
  - Connect a scalable cloud database (Cloud Firestore / Supabase / Cloud SQL via Ktor REST API).
  - Implement bidirectional synchronization: write to local Room cache first (optimistic UI updates) and sync to the cloud in the background.
* **Media & Audio Storage**:
  - Enable musicians to attach 30-second audio audition reels, demo links, and Electronic Press Kits (EPKs) hosted on Cloud Storage.

### Phase 2: Production Mapping & Geospatial Enhancements
* **Native Maps SDK Integration**:
  - Provide a toggle or transition from the custom canvas to the **Google Maps SDK for Android** (`com.google.maps.android:maps-compose`) for satellite imagery, real-time traffic, and 3D building outlines.
* **GPS Location Services & "Near Me" Radius**:
  - Integrate `FusedLocationProviderClient` to detect the musician's current GPS coordinates.
  - Add a distance slider (e.g., *Within 5 km, 15 km, 30 km*) to surface gigs closest to the user.
* **Festival Mode & Temporary Zones**:
  - Support polygon-bounded festival areas (e.g., Sinulog Grand Parade route, Kasadya sa SRP, Cebu Music Festival) with temporary high-density pin clusters.

### Phase 3: Automated Content Moderation & AI Workflows
* **Gemini API Anti-Spam Pipeline**:
  - Pass newly posted gigs through the **Gemini 2.5 Flash API** to automatically detect suspicious postings, scam pay rates, duplicate text, or inappropriate descriptions before they reach the map.
* **AI Smart Musician Matchmaker**:
  - Enable organizers to click *"Find Matching Musicians"* to run an embedding-based similarity match against local registered artists based on genre, instruments, and availability.
* **Automated Expiry Sweeper**:
  - Background Cloud Function / WorkManager worker to mark past-dated gigs as `ARCHIVED` automatically.

### Phase 4: Direct Messaging & Escrow Payouts
* **In-App Real-Time Messaging**:
  - Private direct messaging channel between the venue organizer and the applicant to discuss setlists, technical riders, and soundcheck schedules.
* **Secure Gig Deposits & Escrow**:
  - Optional integration with local Philippine payment gateways (GCash, Maya, QR Ph) to allow venue owners to lock in down payments/escrows for booked artists, protecting musicians against no-shows and last-minute cancellations.

### Phase 5: High-Scale Infrastructure & Performance Optimization
* **Geospatial Pin Clustering (QuadTree)**:
  - Implement dynamic marker clustering so that hundreds of pins in dense areas (like IT Park or Mango Avenue) merge into numbered cluster bubbles when zoomed out.
* **Paging 3 Architecture**:
  - Implement cursor-based pagination for the gig feed to handle infinite scrolling over 100,000+ historical listings with minimal memory footprint.
* **Offline Conflict Resolution**:
  - Use Conflict-Free Replicated Data Types (CRDTs) or Last-Write-Wins timestamps for seamless offline drafting and synchronization when reconnecting to Wi-Fi/LTE.

---

## 5. Known Scaffolding Items & Next Action Items

| Component | Current Prototype State | Target Production Implementation | Priority |
|---|---|---|---|
| **Data Source** | Local Room DB pre-seeded with 5 Cebu listings | Cloud Firestore / REST API with Room offline cache | High |
| **Authentication** | Simulated user identity in local session | Google Sign-In via `CredentialManager` | High |
| **Push Notifications** | Local mock activity log dialog | Firebase Cloud Messaging (FCM) push alerts | Medium |
| **Map Rendering** | Custom 2D Vector Canvas | Maps Compose SDK with cluster support | Medium |
| **Application Submissions**| Appends to local JSON string in Room | Dedicated `applications` relational collection | Medium |
| **Direct Contact** | Opens mailto / external handle text | Native in-app chat with push notifications | Low |

---

## 6. Development & Build Instructions

### Prerequisites
- **Android SDK**: `minSdk 26`, `targetSdk 35`, `compileSdk 35`
- **Language**: Kotlin 2.0.21 with Jetpack Compose (BOM 2024.11.00)
- **JVM Target**: Java 17

### Gradle Commands
```bash
# Compile and assemble Debug APK
gradle :app:assembleDebug

# Run JVM Unit Tests
gradle :app:testDebugUnitTest

# Lint verification
gradle :app:lint
```

---

## 7. Testing & Quality Assurance

- **UI Test Identifiers**: All major interactive elements include standard Compose `Modifier.testTag` attributes (e.g., `notifications_button`, `theme_toggle_button`, `search_field`, `category_chip_<name>`).
- **Robolectric Local Testing**: Unit and CUJ tests can run without an active emulator using Robolectric and Roborazzi for automated screenshot regression tracking.

---

## 8. License & Community

Created for the **Cebu Music & Arts Community**. Designed to celebrate the unique culture of Sinulog, Cebuano indie bands, marching band traditions, and session artists across the Queen City of the South.
