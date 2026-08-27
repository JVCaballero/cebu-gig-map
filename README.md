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

## 4. Database Architecture & Schema Specification

The data layer employs an **Offline-First Architecture**. The mobile client reads from and writes to a local SQLite database powered by **Room**, ensuring instantaneous UI responses, smooth 60fps map panning, and full offline accessibility. In subsequent phases, a background synchronization worker connects this local cache with a cloud database.

```
┌────────────────────────────────────────────────────────────────────────┐
│                        DATA LAYER ARCHITECTURE                         │
├───────────────────────────────────┬────────────────────────────────────┤
│ CLIENT-SIDE (Current Scaffolding) │ BACKEND / CLOUD (Target Prod)      │
│ • SQLite via Android Room 2.6.1   │ • PostgreSQL 16 with PostGIS OR    │
│ • Reactive Kotlin StateFlow / DAO │ • Google Cloud Firestore (Geohash) │
│ • Optimistic UI local commits     │ • Bi-directional Sync via REST/gRPC│
└───────────────────────────────────┴────────────────────────────────────┘
```

---

### A. Current Client-Side Schema (Room Database v1)

The local SQLite table is defined in `com.example.data.GigEntity`:

#### Table: `gigs`
| Column Name | SQLite Type | Constraints | Description |
|---|---|---|---|
| `id` | `INTEGER` | `PRIMARY KEY AUTOINCREMENT` | Unique identifier for local storage and sync mapping |
| `title` | `TEXT` | `NOT NULL` | Gig headline (e.g., *"Sinulog Festival Stage"*) |
| `category` | `TEXT` | `NOT NULL` | Category tag: `Cover Band`, `Marching Band`, `Session`, `Audition`, `Collab` |
| `dateText` | `TEXT` | `NOT NULL` | Human-readable gig schedule or performance window |
| `locationName` | `TEXT` | `NOT NULL` | Named venue, district, or nearest Cebu landmark |
| `posX` | `REAL` | `NOT NULL DEFAULT 0.5` | Normalized X coordinate on Cebu map vector space ($0.0 \dots 1.0$) |
| `posY` | `REAL` | `NOT NULL DEFAULT 0.5` | Normalized Y coordinate on Cebu map vector space ($0.0 \dots 1.0$) |
| `payText` | `TEXT` | `NOT NULL DEFAULT 'Negotiable'` | Compensation details (e.g., *₱5,000 / set*, *₱1,500/call*) |
| `contactInfo` | `TEXT` | `NOT NULL` | Direct organizer phone, email, or social media link |
| `description` | `TEXT` | `NOT NULL DEFAULT ''` | Setlist details, backline specs, requirements |
| `status` | `TEXT` | `NOT NULL DEFAULT 'ACTIVE'` | Life cycle: `ACTIVE`, `FILLED`, `PENDING_REVIEW`, `HIDDEN` |
| `flagCount` | `INTEGER` | `NOT NULL DEFAULT 0` | Total community reports submitted against listing |
| `flagReason` | `TEXT` | `NOT NULL DEFAULT ''` | Latest or dominant report rationale |
| `postedTime` | `INTEGER` | `NOT NULL` | Epoch timestamp in milliseconds |
| `isSaved` | `INTEGER` | `NOT NULL DEFAULT 0` | Boolean flag ($0/1$) tracking user bookmarks |
| `hasApplied` | `INTEGER` | `NOT NULL DEFAULT 0` | Boolean flag ($0/1$) tracking active applications |
| `posterName` | `TEXT` | `NOT NULL DEFAULT 'Community Member'` | Display name of the opportunity creator |

---

### B. Target Production Relational Schema (PostgreSQL + PostGIS)

For high-scale cloud deployments, the single denormalized table will be migrated into a fully normalized, relational schema featuring geospatial queries (`ST_DWithin`, `ST_Point`), role-based access control, and dedicated application records.

```
                      ┌───────────────┐
                      │     users     │
                      └───────┬───────┘
                              │ 1
                              │
             ┌────────────────┼────────────────┐
             │ *              │ *              │ *
     ┌───────▼───────┐ ┌──────▼───────┐ ┌──────▼───────┐
     │     venues    │ │     gigs     │ │ applications │
     └───────┬───────┘ └──────┬───────┘ └──────────────┘
             │ 1              │ 1
             │                │
             └────────►┌──────▼──────────────┐
                     * │   gig_requirements  │
                       └─────────────────────┘
```

#### 1. Table: `users`
Represents registered musicians, venue owners, bandleaders, and moderators.
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    google_id VARCHAR(128) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    avatar_url TEXT,
    role VARCHAR(32) NOT NULL DEFAULT 'MUSICIAN', -- 'MUSICIAN', 'ORGANIZER', 'BANDLEADER', 'MODERATOR'
    bio TEXT,
    primary_instruments TEXT[], -- ARRAY['Bass', 'Vocals', 'Drums']
    genres TEXT[], -- ARRAY['Bisrock', 'Jazz', 'Reggae', 'Pop']
    contact_phone VARCHAR(32),
    instagram_handle VARCHAR(64),
    facebook_url TEXT,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

#### 2. Table: `venues`
Permanent and semi-permanent Cebu live music hubs and event venues.
```sql
CREATE TABLE venues (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    address TEXT NOT NULL,
    city VARCHAR(64) NOT NULL DEFAULT 'Cebu City', -- 'Cebu City', 'Mandaue', 'Lapu-Lapu', 'Talisay'
    district VARCHAR(64), -- 'IT Park', 'Fuente Osmeña', 'Lahug', 'SRP'
    coordinates GEOMETRY(Point, 4326) NOT NULL, -- PostGIS WGS84 coordinates
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    capacity INTEGER,
    backline_available TEXT, -- 'Drumkit, 2x Guitar Amps, Bass Amp, PA System'
    contact_phone VARCHAR(32),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_venues_geo ON venues USING GIST(coordinates);
```

#### 3. Table: `gigs`
Core opportunity listings.
```sql
CREATE TYPE gig_category AS ENUM (
    'COVER_BAND', 'MARCHING_BAND', 'SESSION', 'AUDITION', 'COLLAB'
);

CREATE TYPE gig_status AS ENUM (
    'DRAFT', 'ACTIVE', 'FILLED', 'EXPIRED', 'PENDING_REVIEW', 'HIDDEN'
);

CREATE TABLE gigs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organizer_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    venue_id UUID REFERENCES venues(id) ON DELETE SET NULL,
    custom_location_name VARCHAR(150),
    coordinates GEOMETRY(Point, 4326) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    title VARCHAR(150) NOT NULL,
    category gig_category NOT NULL,
    description TEXT NOT NULL,
    performance_date TIMESTAMPTZ NOT NULL,
    call_time TIMESTAMPTZ,
    duration_hours NUMERIC(4, 2) DEFAULT 3.0,
    pay_amount_cents BIGINT, -- In Philippine Centavos (e.g. 500000 = ₱5,000.00)
    pay_currency VARCHAR(3) NOT NULL DEFAULT 'PHP',
    pay_type VARCHAR(32) NOT NULL DEFAULT 'PER_SET', -- 'PER_SET', 'PER_HOUR', 'FLAT', 'VOLUNTEER'
    status gig_status NOT NULL DEFAULT 'ACTIVE',
    flag_count INTEGER NOT NULL DEFAULT 0,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_gigs_geo ON gigs USING GIST(coordinates);
CREATE INDEX idx_gigs_status_date ON gigs(status, performance_date DESC);
CREATE INDEX idx_gigs_category ON gigs(category);
```

#### 4. Table: `gig_requirements`
Specific instrument slots and roles needed for a gig.
```sql
CREATE TABLE gig_requirements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    gig_id UUID NOT NULL REFERENCES gigs(id) ON DELETE CASCADE,
    instrument_role VARCHAR(64) NOT NULL, -- 'Bassist', 'Lead Guitarist', 'Snare Drummer'
    quantity_needed INTEGER NOT NULL DEFAULT 1,
    quantity_filled INTEGER NOT NULL DEFAULT 0,
    experience_level VARCHAR(32) DEFAULT 'INTERMEDIATE', -- 'BEGINNER', 'INTERMEDIATE', 'PROFESSIONAL'
    must_bring_gear BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_gig_reqs_gig ON gig_requirements(gig_id);
```

#### 5. Table: `applications`
Submissions from musicians auditioning or applying for gigs.
```sql
CREATE TYPE application_status AS ENUM (
    'SUBMITTED', 'SHORTLISTED', 'ACCEPTED', 'REJECTED', 'WITHDRAWN'
);

CREATE TABLE applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    gig_id UUID NOT NULL REFERENCES gigs(id) ON DELETE CASCADE,
    applicant_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    instrument_role VARCHAR(64) NOT NULL,
    pitch_note TEXT,
    demo_audio_url TEXT,
    status application_status NOT NULL DEFAULT 'SUBMITTED',
    applied_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    reviewed_at TIMESTAMPTZ,
    UNIQUE(gig_id, applicant_id, instrument_role)
);

CREATE INDEX idx_applications_user ON applications(applicant_id);
CREATE INDEX idx_applications_gig ON applications(gig_id);
```

#### 6. Table: `moderation_reports`
Audit log of flagged listings and admin moderation outcomes.
```sql
CREATE TABLE moderation_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    gig_id UUID NOT NULL REFERENCES gigs(id) ON DELETE CASCADE,
    reporter_id UUID REFERENCES users(id) ON DELETE SET NULL,
    reason_code VARCHAR(32) NOT NULL, -- 'SPAM', 'INAPPROPRIATE', 'SCAM_PAY', 'WRONG_CATEGORY', 'DUPLICATE'
    details TEXT,
    resolved BOOLEAN NOT NULL DEFAULT FALSE,
    resolved_action VARCHAR(32), -- 'DISMISSED', 'CONTENT_HIDDEN', 'USER_WARNED', 'DELETED'
    resolved_by UUID REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    resolved_at TIMESTAMPTZ
);

CREATE INDEX idx_reports_unresolved ON moderation_reports(resolved) WHERE resolved = FALSE;
```

---

### C. Cloud Firestore Document Alternative Model

If adopting Google Firebase for serverless real-time synchronization:

```json
// Collection: gigs/{gigId}
{
  "title": "Sinulog Festival Stage Band",
  "category": "Cover Band",
  "organizerId": "user_cebu_987",
  "organizerName": "Sinulog Arts Committee",
  "geopoint": { "_latitude": 10.3157, "_longitude": 123.8854 },
  "geohash": "w79j3q8v",
  "locationName": "Fuente Osmeña Circle",
  "pay": { "amount": 5000, "currency": "PHP", "type": "per_set" },
  "performanceDate": "2025-01-18T18:00:00Z",
  "status": "ACTIVE",
  "flagCount": 0,
  "requirements": [
    { "role": "Bassist", "qty": 1, "filled": 0 },
    { "role": "Keys", "qty": 1, "filled": 1 }
  ],
  "createdAt": "2024-11-20T10:00:00Z"
}

// Subcollection: gigs/{gigId}/applications/{appId}
{
  "applicantId": "user_musician_123",
  "applicantName": "Kiko Santos",
  "role": "Bassist",
  "pitch": "5 years playing Sinulog street stages. Have own active 5-string bass and amp.",
  "demoUrl": "https://storage.googleapis.com/.../bass_demo.mp3",
  "status": "SUBMITTED",
  "appliedAt": "2024-11-20T14:30:00Z"
}
```

---

### D. Data Synchronization & Migration Strategy

1. **Room Database Migrations**:
   - When evolving the local database schema, incremental migrations (`Migration(1, 2)`) will execute SQL alter commands without data loss.
2. **Delta Sync Protocol**:
   - The Android client periodically queries the backend with `?since_timestamp=<lastSyncMillis>`.
   - The backend responds with created, updated, and deleted records (`soft delete` with `is_deleted = TRUE`).
3. **Offline Sync Queue (`WorkManager`)**:
   - Actions taken offline (posting a gig, submitting an audition) are stored in an encrypted local `outbox_actions` table.
   - Android `WorkManager` with `NetworkType.CONNECTED` automatically dequeues and submits pending operations with exponential backoff.

---

## 5. Multi-Phase Improvement & Scaling Roadmap

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

## 6. Known Scaffolding Items & Next Action Items

| Component | Current Prototype State | Target Production Implementation | Priority |
|---|---|---|---|
| **Data Source** | Local Room DB pre-seeded with 5 Cebu listings | Cloud Firestore / REST API with Room offline cache | High |
| **Authentication** | Simulated user identity in local session | Google Sign-In via `CredentialManager` | High |
| **Push Notifications** | Local mock activity log dialog | Firebase Cloud Messaging (FCM) push alerts | Medium |
| **Map Rendering** | Custom 2D Vector Canvas | Maps Compose SDK with cluster support | Medium |
| **Application Submissions**| Appends to local JSON string in Room | Dedicated `applications` relational collection | Medium |
| **Direct Contact** | Opens mailto / external handle text | Native in-app chat with push notifications | Low |

---

## 7. Development & Build Instructions

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

## 8. Testing & Quality Assurance

- **UI Test Identifiers**: All major interactive elements include standard Compose `Modifier.testTag` attributes (e.g., `notifications_button`, `theme_toggle_button`, `search_field`, `category_chip_<name>`).
- **Robolectric Local Testing**: Unit and CUJ tests can run without an active emulator using Robolectric and Roborazzi for automated screenshot regression tracking.

---

## 9. License & Community

Created for the **Cebu Music & Arts Community**. Designed to celebrate the unique culture of Sinulog, Cebuano indie bands, marching band traditions, and session artists across the Queen City of the South.
