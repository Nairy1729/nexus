package com.nexus.core.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Typography carries the futurism — scale, weight and rhythm, never novelty fonts.
 *
 * Rules of the house:
 *  - Numbers are large and light (tabular feel).
 *  - Names are large and confident.
 *  - Metadata is small, quiet, and letterspaced.
 *  - Eyebrows are 11sp uppercase with generous tracking — the only "graphic" type gesture.
 */
@Immutable
data class NexusTypeScale(
    // Hero numerals: clock, durations
    val timeHero: TextStyle,
    // Dialer entry
    val numberDisplay: TextStyle,
    // Call-screen names (rendered uppercase by the call screens)
    val hero: TextStyle,
    val title: TextStyle,
    val headline: TextStyle,
    val subhead: TextStyle,
    val body: TextStyle,
    val meta: TextStyle,
    val label: TextStyle,
    val micro: TextStyle,
    // Keypad
    val keyNumber: TextStyle,
    val keyLetters: TextStyle,
    // Data values (profile stats)
    val statValue: TextStyle,
)

object NexusType {

    val Scale = NexusTypeScale(
        timeHero = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.W200,
            fontSize = 64.sp,
            lineHeight = 68.sp,
            letterSpacing = (-1).sp,
        ),
        numberDisplay = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.W300,
            fontSize = 36.sp,
            lineHeight = 44.sp,
            letterSpacing = 1.5.sp,
        ),
        hero = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.W600,
            fontSize = 34.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.5.sp,
        ),
        title = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.W600,
            fontSize = 27.sp,
            lineHeight = 34.sp,
        ),
        headline = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.W600,
            fontSize = 20.sp,
            lineHeight = 26.sp,
        ),
        subhead = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontFamily.SansSerif.let { FontWeight.W500 },
            fontSize = 17.sp,
            lineHeight = 24.sp,
        ),
        body = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.W400,
            fontSize = 15.sp,
            lineHeight = 22.sp,
        ),
        meta = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.W500,
            fontSize = 13.sp,
            lineHeight = 18.sp,
        ),
        label = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.W600,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            letterSpacing = 1.6.sp,
        ),
        micro = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.W500,
            fontSize = 10.sp,
            lineHeight = 13.sp,
            letterSpacing = 0.8.sp,
        ),
        keyNumber = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.W400,
            fontSize = 30.sp,
            lineHeight = 32.sp,
        ),
        keyLetters = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.W600,
            fontSize = 9.sp,
            lineHeight = 11.sp,
            letterSpacing = 1.4.sp,
        ),
        statValue = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.W600,
            fontSize = 21.sp,
            lineHeight = 26.sp,
        ),
    )

    /** Material bridge so stock primitives inherit our rhythm. */
    val Material = Typography(
        displayLarge = Scale.hero,
        headlineLarge = Scale.title,
        headlineMedium = Scale.headline,
        titleLarge = Scale.headline,
        titleMedium = Scale.subhead,
        titleSmall = Scale.meta,
        bodyLarge = Scale.body,
        bodyMedium = Scale.body,
        bodySmall = Scale.meta,
        labelLarge = Scale.label,
        labelMedium = Scale.meta,
        labelSmall = Scale.micro,
    )
}
