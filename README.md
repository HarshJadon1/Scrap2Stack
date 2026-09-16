# Scrap2Stack 🚀
> **From Abandoned Ideas to Real Production Projects**

Scrap2Stack is an end-to-end developer collaboration platform designed to discover, analyze, and revive abandoned or incomplete software projects using AI-driven matching, structured roadmaps, team workspaces, and an append-only contribution reputation system (Charms).

---

## 🛠️ Technology Stack & Architecture

### **Android Client**
- **Language:** Kotlin 2.2.x
- **UI Framework:** Jetpack Compose with Material 3 Design System
- **Architecture:** Clean Architecture + MVVM + Repository Pattern
- **Navigation:** Navigation Compose with typed backstack state management
- **Networking & Backend Integration:** Supabase Kotlin SDK 3.0.1 (`auth-kt`, `postgrest-kt`, `realtime-kt`)
- **Session Management:** Supabase `SettingsSessionManager` with persistent auto-refresh tokens across app restarts

### **Backend & Database**
- **Core Platform:** Supabase (Cloud PostgreSQL + PostgREST API)
- **Authentication:** Supabase Auth (JWT & Email/Password)
- **Security:** Strict Row Level Security (RLS) policies, immutable triggers, and `SECURITY DEFINER` stored functions
- **Database Abstraction:** `developer_profiles` View for privacy-focused developer discovery without exposing user emails

---

## 📋 Core System Flows

```
[ Authentication & Persistence ]
       │
       ▼
[ Splash Screen (Session Restoration) ]
       │
       ├──► Unauthenticated ──► [ Login / Register ]
       │                                 │
       ▼                                 ▼
[ Main Screen / Bottom Navigation ] ◄────┘
       │
       ├──► Home Tab (Charms Summary, Featured & Recent Projects)
       ├──► Discover Tab (Filter & Search Real Database Projects)
       ├──► Projects Tab (My Created, Joined & Completed Projects)
       ├──► Alerts Tab (Collaboration Requests & System Notifications)
       └──► Profile Tab (Editable Developer Info, Skills, Social Links)
```

1. **Authentication & Session Persistence**
   - Direct integration with Supabase Auth.
   - On app startup, `SplashScreen` waits asynchronously for session restoration from local persistent storage (`SharedPreferences`).
   - Authenticated sessions attach the user's JWT Bearer token to all PostgREST requests.

2. **Developer Profiles & Privacy**
   - User profiles stored in `public.profiles`.
   - Private user email is hidden from public discovery using the `public.developer_profiles` view.
   - Developers can edit name, bio, experience level, skills array, interests array, and portfolio links.

3. **Project Lifecycle & Creation**
   - Users can create new projects (`IDEA`, `ABANDONED`, `INCOMPLETE`, `PAUSED`, `REVIVING`, `COMPLETED`).
   - Database triggers enforce system-managed fields (`revival_score`, `quality_score`, `progress`, `analysis_id`) so clients cannot forge scores or fake progress.

4. **Collaboration & Team Membership**
   - Developers request to join projects (`collaboration_requests`).
   - Project owners accept or reject requests.
   - Acceptance triggers the `respond_to_collaboration` RPC function, which atomically injects the developer into `project_members`.

5. **Private Workspaces & Tasks**
   - Only accepted project members and the project owner can access the workspace, roadmap items, and task board.
   - Tasks are assigned only to verified project members.

6. **Charms Reputation System**
   - Append-only contribution ledger (`charm_contributions`).
   - Point allocation is strictly server-controlled (`TASK_COMPLETED = 10`, `TASK_HELPED = 5`, `PROJECT_CONTRIBUTION = 15`, `PROJECT_REVIVED = 50`, `PROJECT_SHIPPED = 100`). Clients cannot submit arbitrary points.

---

## 🔒 Security & Data Integrity

- **Row Level Security (RLS):** Every user-data table in Supabase has RLS enabled with explicit policies for `SELECT`, `INSERT`, `UPDATE`, and `DELETE`.
- **System Field Immutability:** Triggers on `projects`, `profiles`, `tasks`, and `notifications` prevent client modification of protected fields (e.g., owner IDs, charms, or notification titles).
- **Zero Fake Data:** All system data comes strictly from real authenticated user actions and live Supabase queries.

---

## 💻 Build & Development Setup

### **Prerequisites**
- Android Studio 2026.1+
- JDK 11+
- Android SDK 35/36

### **Building the Android App**
```bash
./gradlew :app:compileDebugKotlin
```

### **Deploying to Connected Device**
```bash
./gradlew :app:installDebug
```

---

## 📄 License & Attribution
Developed for the **Software Innovation / SIH Platform**. All rights reserved.
