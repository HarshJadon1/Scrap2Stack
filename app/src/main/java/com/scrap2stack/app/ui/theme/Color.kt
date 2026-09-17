package com.scrap2stack.app.ui.theme

import androidx.compose.ui.graphics.Color

// Premium Scrap2Stack Pro Palette (Obsidian Slate & Electric Neon)
val PrimaryIndigo = Color(0xFF6366F1)        // Electric Violet Indigo
val PrimaryIndigoLight = Color(0xFF818CF8)   // Light Indigo
val PrimaryIndigoDark = Color(0xFF4F46E5)    // Deep Royal Indigo

val RevivalEmerald = Color(0xFF10B981)     // Vibrant Revival Emerald
val RevivalEmeraldLight = Color(0xFF34D399)// Light Emerald
val RevivalEmeraldDark = Color(0xFF059669) // Dark Emerald

// Premium Dark Theme Surfaces (Deep Obsidian Slate)
val SlateBackgroundDark = Color(0xFF090D16) // Deep Obsidian Slate
val SlateSurfaceDark = Color(0xFF111827)    // Rich Slate 900
val SlateSurfaceVariantDark = Color(0xFF1F2937) // Slate 800
val SlateOnSurfaceDark = Color(0xFFF9FAFB)  // Slate 50

// Premium Light Theme Surfaces (Crisp Studio Light)
val SlateBackgroundLight = Color(0xFFF8FAFC) // Slate 50
val SlateSurfaceLight = Color(0xFFFFFFFF)    // Pure White
val SlateSurfaceVariantLight = Color(0xFFF1F5F9) // Slate 100
val SlateOnSurfaceLight = Color(0xFF0F172A)  // Slate 900

// Status Accent Colors
val StatusAbandoned = Color(0xFFF43F5E)     // Coral Crimson
val StatusReviving = Color(0xFFF59E0B)      // Amber Orange
val StatusCompleted = Color(0xFF10B981)     // Emerald Green
val StatusIdea = Color(0xFF6366F1)          // Tech Indigo
val StatusPaused = Color(0xFF64748B)        // Muted Slate

// Legacy mappings for backwards compatibility
val TechBlue = PrimaryIndigo
val TechBlueLight = PrimaryIndigoLight
val TechBlueDark = PrimaryIndigoDark

val PrimaryBlue = PrimaryIndigo
val PrimaryBlueLight = PrimaryIndigoLight
val PrimaryBlueDark = PrimaryIndigoDark

val RevivalGreen = RevivalEmerald
val RevivalGreenLight = RevivalEmeraldLight
val RevivalGreenDark = RevivalEmeraldDark

val ScrapGray = SlateSurfaceDark
val ScrapBlack = SlateBackgroundDark
val ScrapWhite = SlateOnSurfaceDark

val Primary = PrimaryIndigo
val OnPrimary = Color.White
val PrimaryContainer = PrimaryIndigoDark
val OnPrimaryContainer = Color.White

val Secondary = RevivalEmerald
val OnSecondary = Color.White
val SecondaryContainer = RevivalEmeraldDark
val OnSecondaryContainer = Color.White

val Background = SlateBackgroundDark
val OnBackground = SlateOnSurfaceDark
val Surface = SlateSurfaceDark
val OnSurface = SlateOnSurfaceDark
