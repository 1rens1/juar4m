package com.rensdev.juar4m.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.rensdev.juar4m.R


// val provider = GoogleFont.Provider(
//	providerAuthority = "com.google.android.gms.fonts",
//	providerPackage = "com.google.android.gms",
//	certificates = R.array.com_google_android_gms_fonts_certs
//)


@OptIn(ExperimentalTextApi::class)
fun createVariableFontFamily(fontResId: Int): FontFamily {
	return FontFamily(
		Font(
			fontResId,
			variationSettings = FontVariation.Settings(FontVariation.weight(100)),
			weight = FontWeight.Thin
		),
		Font(
			fontResId,
			variationSettings = FontVariation.Settings(FontVariation.weight(200)),
			weight = FontWeight.ExtraLight
		),
		Font(
			fontResId,
			variationSettings = FontVariation.Settings(FontVariation.weight(300)),
			weight = FontWeight.Light
		),
		Font(
			fontResId,
			variationSettings = FontVariation.Settings(FontVariation.weight(400)),
			weight = FontWeight.Normal
		),
		Font(
			fontResId,
			variationSettings = FontVariation.Settings(FontVariation.weight(500)),
			weight = FontWeight.Medium
		),
		Font(
			fontResId,
			variationSettings = FontVariation.Settings(FontVariation.weight(600)),
			weight = FontWeight.SemiBold
		),
		Font(
			fontResId,
			variationSettings = FontVariation.Settings(FontVariation.weight(700)),
			weight = FontWeight.Bold
		),
		Font(
			fontResId,
			variationSettings = FontVariation.Settings(FontVariation.weight(800)),
			weight = FontWeight.ExtraBold
		),
		Font(
			fontResId,
			variationSettings = FontVariation.Settings(FontVariation.weight(900)),
			weight = FontWeight.Black
		),
	)
}

val GeistFontFamily = createVariableFontFamily(R.font.geist_variable_wght)
val GeistMonoFontFamily = createVariableFontFamily(R.font.geistmono_variable_wght)


// val RubikFont = GoogleFont("Rubik")
//
// val RubikFontFamily = FontFamily(
//	Font(
//		googleFont = RubikFont,
//		fontProvider = provider
//	)
//)

val Typography = Typography(
	displayLarge = TextStyle(
		fontFamily = GeistFontFamily,
		fontWeight = FontWeight.Bold,
		fontSize = 57.sp,
		lineHeight = 57.sp,
		letterSpacing = (-0.02).em
	),
	displayMedium = TextStyle(
		fontFamily = GeistFontFamily,
		fontWeight = FontWeight.Bold,
		fontSize = 45.sp,
		lineHeight = 45.sp,
		letterSpacing = (-0.02).em
	),
	displaySmall = TextStyle(
		fontFamily = GeistFontFamily,
		fontWeight = FontWeight.Bold,
		fontSize = 36.sp,
		lineHeight = 36.sp,
		letterSpacing = (-0.02).em
	),

	headlineLarge = TextStyle(
		fontFamily = GeistFontFamily,
		fontWeight = FontWeight.SemiBold,
		fontSize = 32.sp,
		lineHeight = 32.sp,
		letterSpacing = (-0.02).em
	),
	headlineMedium = TextStyle(
		fontFamily = GeistFontFamily,
		fontWeight = FontWeight.SemiBold,
		fontSize = 28.sp,
		lineHeight = 28.sp,
		letterSpacing = (-0.02).em
	),
	headlineSmall = TextStyle(
		fontFamily = GeistFontFamily,
		fontWeight = FontWeight.SemiBold,
		fontSize = 24.sp,
		lineHeight = 24.sp,
		letterSpacing = (-0.02).em
	),

	titleLarge = TextStyle(
		fontFamily = GeistFontFamily,
		fontWeight = FontWeight.Medium,
		fontSize = 22.sp,
		lineHeight = 22.sp,
		letterSpacing = (-0.02).em
	),
	titleMedium = TextStyle(
		fontFamily = GeistFontFamily,
		fontWeight = FontWeight.Medium,
		fontSize = 16.sp,
		lineHeight = 16.sp,
		letterSpacing = (-0.02).em
	),
	titleSmall = TextStyle(
		fontFamily = GeistFontFamily,
		fontWeight = FontWeight.Medium,
		fontSize = 14.sp,
		lineHeight = 14.sp,
		letterSpacing = 0.em
	),

	bodyLarge = TextStyle(
		fontFamily = GeistFontFamily,
		fontWeight = FontWeight.Medium,
		fontSize = 16.sp,
		lineHeight = 16.sp,
		letterSpacing = (-0.02).em
	),
	bodyMedium = TextStyle(
		fontFamily = GeistFontFamily,
		fontWeight = FontWeight.Medium,
		fontSize = 14.sp,
		lineHeight = 14.sp,
		letterSpacing = (-0.02).em
	),
	bodySmall = TextStyle(
		fontFamily = GeistFontFamily,
		fontWeight = FontWeight.Medium,
		fontSize = 12.sp,
		lineHeight = 12.sp,
		letterSpacing = 0.em
	),

	labelLarge = TextStyle(
		fontFamily = GeistFontFamily,
		fontWeight = FontWeight.Medium,
		fontSize = 14.sp,
		lineHeight = 14.sp,
		letterSpacing = 0.03.em
	),
	labelMedium = TextStyle(
		fontFamily = GeistFontFamily,
		fontWeight = FontWeight.Medium,
		fontSize = 12.sp,
		lineHeight = 12.sp,
		letterSpacing = 0.03.em
	),
	labelSmall = TextStyle(
		fontFamily = GeistFontFamily,
		fontWeight = FontWeight.Medium,
		fontSize = 10.sp,
		lineHeight = 10.sp,
		letterSpacing = 0.03.em
	)
)
