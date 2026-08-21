package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Mind Store Ultra-Modern Color Palette
val NeonCyan = Color(0xFF00E5FF)
val ElectricBlue = Color(0xFF2563EB)
val CyberIndigo = Color(0xFF6366F1)
val UltraViolet = Color(0xFF8B5CF6)
val NeonPink = Color(0xFFEC4899)
val EmeraldSuccess = Color(0xFF10B981)
val AmberWarning = Color(0xFFF59E0B)
val RoseError = Color(0xFFEF4444)

// Backward compatibility aliases
val CyanAccent = NeonCyan
val VioletAccent = UltraViolet
val ElectricBlueAccent = ElectricBlue


// Dark Theme Colors (Deep Obsidian & Glow)
val DarkBackground = Color(0xFF090D16)
val DarkSurface = Color(0xFF0F172A)
val DarkSurfaceCard = Color(0xFF162033)
val DarkSurfaceHighlight = Color(0xFF223049)
val DarkBorder = Color(0xFF2D3D58)
val DarkBorderGlow = Color(0xFF3B82F6)
val DarkTextPrimary = Color(0xFFF8FAFC)
val DarkTextSecondary = Color(0xFF94A3B8)
val DarkTextMuted = Color(0xFF64748B)

// Light Theme Colors (Crisp Modern Frost)
val LightBackground = Color(0xFFF8FAFD)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceCard = Color(0xFFFFFFFF)
val LightSurfaceHighlight = Color(0xFFF1F5F9)
val LightBorder = Color(0xFFE2E8F0)
val LightBorderGlow = Color(0xFF93C5FD)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF475569)
val LightTextMuted = Color(0xFF94A3B8)

// Gradients
val HeroGradientDark = Brush.linearGradient(
    colors = listOf(Color(0xFF1E1B4B), Color(0xFF0F172A), Color(0xFF090D16))
)

val HeroGradientLight = Brush.linearGradient(
    colors = listOf(Color(0xFFEEF2FF), Color(0xFFE0E7FF), Color(0xFFF8FAFD))
)

val BrandGradient = Brush.horizontalGradient(
    colors = listOf(NeonCyan, ElectricBlue, CyberIndigo)
)

val AccentGradient = Brush.horizontalGradient(
    colors = listOf(CyberIndigo, UltraViolet, NeonPink)
)

val DownloadButtonGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF2563EB), Color(0xFF4F46E5), Color(0xFF7C3AED))
)

val EmeraldGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF059669), Color(0xFF10B981), Color(0xFF34D399))
)

