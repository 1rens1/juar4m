@file:Suppress("unused")

package com.rensdev.juar4m.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

val Black = Color.Black
val DarkGray = Color.DarkGray
val Gray = Color.Gray
val LightGray = Color.LightGray
val White = Color.White
val Red = Color.Red
val Green = Color.Green
val Blue = Color.Blue
val Yellow = Color.Yellow
val Cyan = Color.Cyan
val Magenta = Color.Magenta
val Transparent = Color.Transparent

fun colorMix(a: Color, b: Color, ratio: Float): Color =
	Color(ColorUtils.blendARGB(a.toArgb(), b.toArgb(), ratio))
