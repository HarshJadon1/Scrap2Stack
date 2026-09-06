# Scrap2Stack API Documentation

Base URL: `http://localhost:8080/api/v1`

## Authentication

### Register
`POST /auth/register`
Body:
```json
{
  "name": "John Doe",
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123"
}
```

### Login
`POST /auth/login`
Body:
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

## Projects

### List Projects
`GET /projects`
Query Params: `page`, `limit`, `search`, `status`

### Create Project (Protected)
`POST /projects`
Headers: `Authorization: Bearer <token>`
Body:
```json
{
  "name": "Project Name",
  "description": "Description",
  "status": "ABANDONED"
}
```
