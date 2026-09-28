# Scrap2Stack Backend & Database

The backend for Scrap2Stack is powered by **Supabase (Cloud PostgreSQL + PostgREST API + GoTrue Auth + Realtime WebSocket Engine)**, serving both the **Android Application** and the **Web Application**.

A supplementary Go Gin backend service is located in `cmd/` and `internal/` for background worker tasks and legacy microservices.

---

## 🏗️ Architecture & Infrastructure

- **Database:** PostgreSQL 15+ (Hosted on Supabase)
- **API Layer:** Supabase PostgREST (Auto-generated instant REST APIs)
- **Authentication:** Supabase GoTrue Auth (JWT + Email/Password + OAuth)
- **Realtime Engine:** Supabase Realtime (PostgreSQL WAL replication via WebSockets)
- **Target URL:** `https://xablikvmpjmwzypsvdfh.supabase.co`
- **Public Anon Key:** `sb_publishable_q5fZjuM2UVHutlghxcwmJQ_1MExQH5b`

---

## 🗄️ Database Tables & Schema

All 10 core tables are defined and managed via `backend/supabase_schema.sql`:

| Table Name | Description | Key Fields |
| :--- | :--- | :--- |
| `projects` | Abandoned / side projects | `id`, `owner_id`, `name`, `description`, `technologies`, `required_skills`, `status`, `revival_score`, `github_url` |
| `profiles` | User profile registry | `id`, `name`, `username`, `email`, `bio`, `experience_level`, `skills`, `charms` |
| `developer_profiles` | Public privacy view | Omits private emails, exposes public developer matchmaking data |
| `tasks` | Kanban task items | `id`, `project_id`, `title`, `priority`, `status`, `skill`, `assignee_id` |
| `chat_messages` | Workspace real-time chat | `id`, `project_id`, `sender_id`, `sender_name`, `message`, `created_at` |
| `user_charms` | Append-only reputation ledger | `id`, `user_id`, `project_id`, `contribution_type`, `charms`, `description` |
| `collaboration_requests` | Match invitations & join requests | `id`, `project_id`, `sender_id`, `receiver_id`, `status`, `message` |
| `project_members` | Team membership & roles | `id`, `project_id`, `user_id`, `role`, `joined_at` |
| `roadmaps` | High-level project roadmaps | `id`, `project_id`, `title`, `description`, `status`, `generated_by_ai` |
| `roadmap_items` | Sprint phases & milestones | `id`, `roadmap_id`, `project_id`, `title`, `status`, `required_skills`, `milestone` |

---

## 🚀 Setup & Migration

### **1. Apply Database Schema & Permissions**
Copy the content of [`backend/supabase_schema.sql`](supabase_schema.sql) and paste it into the **Supabase Dashboard -> SQL Editor**, then click **Run**. This:
- Grants schema access to `anon`, `authenticated`, and `service_role`.
- Creates missing tables (`chat_messages`, `user_charms`, etc.).
- Sets up Row Level Security (RLS) policies.
- Enables Realtime replication on `chat_messages`, `tasks`, and `collaboration_requests`.

### **2. Automated Verification Test**
To verify that all 10 tables and Auth are healthy and accessible:
```powershell
powershell -ExecutionPolicy Bypass -File backend\test_backend.ps1
```

Expected output:
```text
==========================================================
       SCRAP2STACK BACKEND & DATABASE VERIFICATION        
==========================================================
[1/3] Testing Supabase Auth Gateway... [PASS]
[2/3] Testing REST API Database Tables:
  - Testing 'projects'... OK (Accessible)
  - Testing 'profiles'... OK (Accessible)
  - Testing 'developer_profiles'... OK (Accessible)
  - Testing 'tasks'... OK (Accessible)
  - Testing 'chat_messages'... OK (Accessible)
  - Testing 'user_charms'... OK (Accessible)
  - Testing 'collaboration_requests'... OK (Accessible)
  - Testing 'project_members'... OK (Accessible)
  - Testing 'roadmaps'... OK (Accessible)
  - Testing 'roadmap_items'... OK (Accessible)
==========================================================
SUMMARY: 10 passed, 0 need attention.
==========================================================
```

---

## 🌐 Connecting from the Website (React / Next.js / Vue)

Install the Supabase JS SDK:
```bash
npm install @supabase/supabase-js
```

Initialize the client (`supabaseClient.js`):
```javascript
import { createClient } from '@supabase/supabase-js'

export const supabase = createClient(
  'https://xablikvmpjmwzypsvdfh.supabase.co',
  'sb_publishable_q5fZjuM2UVHutlghxcwmJQ_1MExQH5b'
)
```
