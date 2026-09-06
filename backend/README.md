# Scrap2Stack Backend

Go-based backend for the Scrap2Stack developer platform.

## Architecture
- **Language**: Go (Golang)
- **Framework**: Gin Gonic
- **Database**: MongoDB
- **Auth**: JWT & bcrypt

## Requirements
- Go 1.23+
- MongoDB

## Setup
1. Copy `.env.example` to `.env` and adjust variables.
2. Run `go mod download`

## Running
```bash
go run cmd/server/main.go
```

## Seed Data
```bash
go run cmd/seed/main.go
```

## API Documentation
See `docs/API.md` for detailed endpoint information.
