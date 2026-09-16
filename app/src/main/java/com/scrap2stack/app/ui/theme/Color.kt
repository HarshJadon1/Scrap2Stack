package com.scrap2stack.app.ui.theme

import androidx.compose.ui.graphics.Color

// Modern Scrap2Stack Brand Palette (Developer Slate & Neon Accent)
val PrimaryBlue = Color(0xFF3B82F6)        // Vibrant Electric Blue
val PrimaryBlueLight = Color(0xFF60A5FA)   // Light Sky Blue
val PrimaryBlueDark = Color(0xFF1D4ED8)    // Deep Royal Blue

val RevivalEmerald = Color(0xFF10B981)     // Vibrant Revival Emerald
val RevivalEmeraldLight = Color(0xFF34D399)// Light Emerald
val RevivalEmeraldDark = Color(0xFF047857) // Dark Emerald

// Dark Theme Surfaces (Rich Midnight Slate)
val SlateBackgroundDark = Color(0xFF0F172A) // Slate 900
val SlateSurfaceDark = Color(0xFF1E293B)    // Slate 800
val SlateSurfaceVariantDark = Color(0xFF334155) // Slate 700
val SlateOnSurfaceDark = Color(0xFFF8FAFC)  // Slate 50

// Light Theme Surfaces (Clean Studio Slate)
val SlateBackgroundLight = Color(0xFFF8FAFC) // Slate 50
val SlateSurfaceLight = Color(0xFFFFFFFF)    // Pure White
val SlateSurfaceVariantLight = Color(0xFFF1F5F9) // Slate 100
val SlateOnSurfaceLight = Color(0xFF0F172A)  // Slate 900

// Status Colors
val StatusAbandoned = Color(0xFFEF4444)     // Crimson Red
val StatusReviving = Color(0xFFF59E0B)      // Amber Orange
val StatusCompleted = Color(0xFF10B981)     // Emerald Green
val StatusIdea = Color(0xFF3B82F6)          // Tech Blue
val StatusPaused = Color(0xFF64748B)        // Muted Slate

// Legacy mappings for backwards compatibility
val TechBlue = PrimaryBlue
val TechBlueLight = PrimaryBlueLight
val TechBlueDark = PrimaryBlueDark

val RevivalGreen = RevivalEmerald
val RevivalGreenLight = RevivalEmeraldLight
val RevivalGreenDark = RevivalEmeraldDark

val ScrapGray = SlateSurfaceDark
val ScrapBlack = SlateBackgroundDark
val ScrapWhite = SlateOnSurfaceDark

val Primary = PrimaryBlue
val OnPrimary = Color.White
val PrimaryContainer = PrimaryBlueDark
val OnPrimaryContainer = Color.White

val Secondary = RevivalEmerald
val OnSecondary = Color.White
val SecondaryContainer = RevivalEmeraldDark
val OnSecondaryContainer = Color.White

val Background = SlateBackgroundDark
val OnBackground = SlateOnSurfaceDark
val Surface = SlateSurfaceDark
val OnSurface = SlateOnSurfaceDark
