# Scrap2Stack 🚀
> **From Abandoned Ideas to Production Stacks**

Scrap2Stack is an end-to-end open-source project revival platform that pairs abandoned repositories and ideas with eager developers using **predictive Data Science algorithms**, **AI roadmap generation**, **real-time workspaces**, and an **append-only reputation ledger (Charms)**.

---

## 🌟 Key Features

### 1. 🔬 Data Science & Analytics V2 Engines
- **`SkillTaxonomyEngine`:** Semantic N-Gram Jaccard taxonomy matching that unifies technology synonyms (e.g. *Ktor* ➔ *Kotlin*, *React* ➔ *Frontend*, *PostgreSQL* ➔ *Database*).
- **`RevivalVelocityEngine`:** Predictive delivery analytics calculating Story Point velocity, burnup points, cadence, and ship forecast with an **80% Confidence Interval** (\(\Delta t \pm Z_{0.80} \cdot \sigma\)). Automatically detects bottleneck risks and stalled states.
- **`ContributionVerificationEngine`:** Anti-gaming verification ledger that computes Bayesian skill confidence:
  $$C = 0.25 + 0.70 \cdot (1 - e^{-0.4 \cdot N})$$
  Diff-weighted charms reward meaningful code contributions while filtering burst activity.
- **`ScrapAIEngine (SRVM)`:** Software Revival Viability Model calculating multi-factor viability scores across completeness, architecture, maintainability, and demand.

### 2. 👥 Developer Synergy Matching & Verified Badges
- Matching algorithm combines skill overlap, experience weighting, and Bayesian verified skill confidence.
- Developers with proven project track records display **Electric Mint Verified Badges** on matching cards.

### 3. 💼 Collaborative Project Workspace
- **Live Velocity & Burnup Dashboard:** Real-time health status pills (*Accelerating*, *On Track*, *At Risk*, *Stalled*), cadence metrics, and automated bottleneck advisories.
- **Interactive Kanban Board:** Drag-and-drop task lifecycle (`TODO` ➔ `IN_PROGRESS` ➔ `REVIEW` ➔ `COMPLETED`) with **Gemini AI-powered task solution generator**.
- **Realtime Team Chat:** WebSocket-driven live chat featuring Markdown code snippet bubbles with syntax highlighting and ergonomic one-tap reaction bars.
- **AI Sprint Roadmap:** Automated phase-by-phase revival milestones convert directly into Kanban tasks.
- **One-Click Project Shipping:** Final celebration workflow awarding +100 Charms and publishing to the community showcase.

### 4. 🌐 Shared App & Web Cloud Backend
- Unified Supabase Cloud PostgreSQL database powering both the **Android App** and the **Web Platform**.
- Realtime WebSocket replication for instant bi-directional messaging, task updates, and collaboration invites.
- Automated migration script (`backend/supabase_schema.sql`) and verification test (`backend/test_backend.ps1`).

---

## 🛠️ Technology Stack

| Layer | Technologies |
| :--- | :--- |
| **Mobile Client** | Kotlin 2.2+, Jetpack Compose, Material 3, Navigation Compose, Clean Architecture + MVVM |
| **Data & Cache** | Supabase Kotlin SDK 3.0.1 (`auth-kt`, `postgrest-kt`, `realtime-kt`), In-Memory AppCache |
| **Backend & Cloud DB** | Supabase (Cloud PostgreSQL 15+, PostgREST, GoTrue Auth, Realtime Engine) |
| **AI Integration** | Google Gemini Generative AI SDK, ScrapAI Heuristics Engine |
| **Testing** | JUnit 4, Kotlinx Coroutines Test, Compose UI Testing (30/30 Unit Tests Passing) |

---

## 📋 Core System Architecture

```
[ Android App ]                    [ Web Application ]
       │                                   │
       └───► Supabase Cloud Gateway ◄──────┘
             https://xablikvmpjmwzypsvdfh.supabase.co
                       │
       ┌───────────────┼───────────────┐
       ▼               ▼               ▼
[ PostgREST API ]  [ GoTrue Auth ]  [ Realtime WebSockets ]
       │               │               │
       └───────────────┼───────────────┘
                       ▼
             [ PostgreSQL Database ]
       ├── projects
       ├── profiles & developer_profiles
       ├── tasks
       ├── chat_messages
       ├── user_charms
       ├── collaboration_requests
       ├── project_members
       └── roadmaps & roadmap_items
```

---

## 🚀 Getting Started

### **Android Prerequisites**
- Android Studio Ladybug / Meerkat (2024.2+)
- JDK 17 or JDK 21
- Android SDK 35/36

### **Building & Running Tests**
```powershell
# 1. Run Android Unit Test Suite (30 Tests)
.\gradlew testDebugUnitTest

# 2. Build Debug APK
.\gradlew assembleDebug

# 3. Install on Connected Device / Emulator
.\gradlew installDebug
```

### **Backend Verification**
To verify live Supabase cloud database connectivity and permissions:
```powershell
powershell -ExecutionPolicy Bypass -File backend\test_backend.ps1
```

---

## 📚 Documentation Links
- **[API Documentation](docs/API.md):** Complete REST, Auth, and Realtime endpoints.
- **[Backend Guide](backend/README.md):** Database setup, migrations, and Web SDK integration.
- **[Supabase Schema Migration](backend/supabase_schema.sql):** One-click SQL setup script.

---

## 📄 License & Attribution
Developed for the **Scrap2Stack Developer Ecosystem**. Built with Kotlin, Jetpack Compose, Supabase, and Gemini AI.
