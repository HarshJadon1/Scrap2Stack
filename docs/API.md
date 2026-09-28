# Scrap2Stack API Documentation

This document describes the API interface for the Scrap2Stack platform, used by both the **Android Application** and the **Web Client**.

## Base URL
```text
https://xablikvmpjmwzypsvdfh.supabase.co
```

## Public Headers
```http
apikey: sb_publishable_q5fZjuM2UVHutlghxcwmJQ_1MExQH5b
Authorization: Bearer <USER_JWT_TOKEN_OR_ANON_KEY>
Content-Type: application/json
```

---

## 1. Authentication (`/auth/v1`)

### **Register / Sign Up**
`POST /auth/v1/signup`
```json
{
  "email": "developer@example.com",
  "password": "SecurePassword123",
  "data": {
    "full_name": "Alex Mercer",
    "username": "alexmercer"
  }
}
```

### **Login / Sign In**
`POST /auth/v1/token?grant_type=password`
```json
{
  "email": "developer@example.com",
  "password": "SecurePassword123"
}
```
**Response:**
```json
{
  "access_token": "eyJhbGciOi...",
  "token_type": "bearer",
  "expires_in": 3600,
  "user": {
    "id": "e98e2689-...",
    "email": "developer@example.com"
  }
}
```

---

## 2. Projects API (`/rest/v1/projects`)

### **List Projects (Filter & Search)**
`GET /rest/v1/projects?select=*&order=created_at.desc&limit=20`

### **Filter by Status**
`GET /rest/v1/projects?select=*&status=eq.ABANDONED`

### **Create New Project**
`POST /rest/v1/projects`
```json
{
  "name": "AI Code Alchemist",
  "description": "Reviving abandoned open-source scrap code with multi-agent orchestration.",
  "technologies": ["Kotlin", "Compose", "Supabase", "Python"],
  "required_skills": ["Android", "Go", "Docker"],
  "status": "IDEA",
  "revival_score": 68
}
```

---

## 3. Workspace Tasks API (`/rest/v1/tasks`)

### **Get Tasks by Project**
`GET /rest/v1/tasks?select=*&project_id=eq.<PROJECT_ID>&order=created_at.asc`

### **Create Task**
`POST /rest/v1/tasks`
```json
{
  "project_id": "<PROJECT_ID>",
  "title": "Build WebSocket Chat UI",
  "skill": "Kotlin",
  "priority": "HIGH",
  "status": "TODO"
}
```

### **Update Task Status (Kanban Drag & Drop)**
`PATCH /rest/v1/tasks?id=eq.<TASK_ID>`
```json
{
  "status": "COMPLETED"
}
```

---

## 4. Team Chat API (`/rest/v1/chat_messages`)

### **Fetch Recent Messages**
`GET /rest/v1/chat_messages?select=*&project_id=eq.<PROJECT_ID>&order=created_at.asc`

### **Send Chat Message**
`POST /rest/v1/chat_messages`
```json
{
  "project_id": "<PROJECT_ID>",
  "sender_id": "<USER_ID>",
  "sender_name": "Harsh Jadon",
  "message": "Let's review the Bayesian skill confidence engine!"
}
```

### **Realtime Subscription (JavaScript SDK)**
```javascript
import { supabase } from './supabaseClient'

const channel = supabase
  .channel('realtime:chat')
  .on(
    'postgres_changes',
    { event: 'INSERT', schema: 'public', table: 'chat_messages', filter: `project_id=eq.${projectId}` },
    (payload) => {
      console.log('New message received:', payload.new)
    }
  )
  .subscribe()
```

---

## 5. Collaboration Requests (`/rest/v1/collaboration_requests`)

### **Send Invitation / Request**
`POST /rest/v1/collaboration_requests`
```json
{
  "project_id": "<PROJECT_ID>",
  "sender_id": "<USER_ID>",
  "receiver_id": "<DEVELOPER_ID>",
  "status": "PENDING",
  "message": "I think your experience with Jetpack Compose would be a great fit!"
}
```

### **Accept / Reject Request**
`PATCH /rest/v1/collaboration_requests?id=eq.<REQUEST_ID>`
```json
{
  "status": "ACCEPTED"
}
```

---

## 6. Reputation Charms API (`/rest/v1/user_charms`)

### **Fetch User Contribution Ledger**
`GET /rest/v1/user_charms?select=*&user_id=eq.<USER_ID>&order=created_at.desc`
