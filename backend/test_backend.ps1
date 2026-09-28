# Scrap2Stack Backend & Database Verification Script
# Usage: powershell -ExecutionPolicy Bypass -File backend\test_backend.ps1

$baseUrl = "https://xablikvmpjmwzypsvdfh.supabase.co"
$apiKey  = "sb_publishable_q5fZjuM2UVHutlghxcwmJQ_1MExQH5b"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "       SCRAP2STACK BACKEND & DATABASE VERIFICATION        " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "Target URL: $baseUrl`n"

# 1. Test Auth Gateway
Write-Host "[1/3] Testing Supabase Auth Gateway..." -NoNewline
try {
    $authRes = curl.exe -s -X POST "$baseUrl/auth/v1/signup" -H "apikey: $apiKey" -H "Content-Type: application/json" -d "{}"
    if ($authRes -match "anonymous_provider_disabled" -or $authRes -match "msg") {
        Write-Host " [PASS]" -ForegroundColor Green
    } else {
        Write-Host " [WARNING] Unexpected auth response: $authRes" -ForegroundColor Yellow
    }
} catch {
    Write-Host " [FAIL]: $($_.Exception.Message)" -ForegroundColor Red
}

# 2. Test Tables
Write-Host "`n[2/3] Testing REST API Database Tables:"
$tables = @(
    "projects", 
    "profiles", 
    "developer_profiles", 
    "tasks", 
    "chat_messages", 
    "user_charms", 
    "collaboration_requests", 
    "project_members", 
    "roadmaps",
    "roadmap_items"
)

$passed = 0
$failed = 0

foreach ($t in $tables) {
    Write-Host "  - Testing '$t'... " -NoNewline
    $raw = curl.exe -s -H "apikey: $apiKey" "$baseUrl/rest/v1/$t`?select=*&limit=1"
    
    if ($raw -match '^\[' -or $raw -eq "[]") {
        Write-Host "OK (Accessible)" -ForegroundColor Green
        $passed++
    } elseif ($raw -match "42501") {
        Write-Host "PERMISSION DENIED (Run supabase_schema.sql)" -ForegroundColor Red
        $failed++
    } elseif ($raw -match "PGRST205") {
        Write-Host "TABLE MISSING (Run supabase_schema.sql)" -ForegroundColor Red
        $failed++
    } else {
        Write-Host "RESPONSE: $raw" -ForegroundColor Yellow
    }
}

Write-Host "`n==========================================================" -ForegroundColor Cyan
Write-Host "SUMMARY: $passed passed, $failed need attention." -ForegroundColor $(if ($failed -eq 0) { "Green" } else { "Yellow" })
if ($failed -gt 0) {
    Write-Host "FIX: Copy content of backend\supabase_schema.sql and run it in the Supabase Dashboard SQL Editor." -ForegroundColor Magenta
}
Write-Host "==========================================================" -ForegroundColor Cyan
