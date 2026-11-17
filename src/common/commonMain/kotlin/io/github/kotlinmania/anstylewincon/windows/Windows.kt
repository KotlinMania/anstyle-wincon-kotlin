// port-lint: source windows.rs
package io.github.kotlinmania.anstylewincon.windows

import ai.solace.tui.anstyle.AnsiColor
import io.github.kotlinmania.anstylewincon.ConsoleStreamHandle
import io.github.kotlinmania.anstylewincon.WinconColorPair
import io.github.kotlinmania.anstylewincon.WinconColorsOutcome
import io.github.kotlinmania.anstylewincon.WinconUnitOutcome
import io.github.kotlinmania.anstylewincon.platformGetConsoleColors
import io.github.kotlinmania.anstylewincon.platformInitialConsoleColors
import io.github.kotlinmania.anstylewincon.platformSetConsoleColors

// Low-level wincon-styling

internal const val FOREGROUND_BLUE_BITS: UShort = 1u
internal const val FOREGROUND_GREEN_BITS: UShort = 2u
internal const val FOREGROUND_RED_BITS: UShort = 4u
internal const val FOREGROUND_INTENSITY_BITS: UShort = 8u
internal const val FOREGROUND_CYAN_BITS: UShort = 3u
internal const val FOREGROUND_MAGENTA_BITS: UShort = 5u
internal const val FOREGROUND_YELLOW_BITS: UShort = 6u
internal const val FOREGROUND_WHITE_BITS: UShort = 7u

/**
 * Cached [getColors] call for standard output.
 */
public fun stdoutInitialColors(): WinconColorsOutcome = platformInitialConsoleColors(isStderr = false)

/**
 * Cached [getColors] call for standard error.
 */
public fun stderrInitialColors(): WinconColorsOutcome = platformInitialConsoleColors(isStderr = true)

/**
 * Apply colors to future writes on [stream].
 *
 * Make sure any buffers are first flushed or else these colors will apply.
 */
public fun setColors(
    stream: ConsoleStreamHandle,
    fg: AnsiColor,
    bg: AnsiColor,
): WinconUnitOutcome = platformSetConsoleColors(stream.rawHandle, fg, bg)

/**
 * Get the colors currently active on [stream].
 */
public fun getColors(stream: ConsoleStreamHandle): WinconColorsOutcome = platformGetConsoleColors(stream.rawHandle)

/**
 * Unpack Windows `CONSOLE_CHARACTER_ATTRIBUTES` into foreground and background [AnsiColor]s.
 */
public fun unpackConsoleColors(attributes: UShort): WinconColorPair {
    val attrInt = attributes.toInt()
    val bg = fromNibble(((attrInt ushr 4) and 0xF).toUShort())
    val fg = fromNibble((attrInt and 0xF).toUShort())
    return WinconColorPair(fg = fg, bg = bg)
}

/**
 * Pack foreground and background [AnsiColor]s into Windows `CONSOLE_CHARACTER_ATTRIBUTES`.
 */
public fun packConsoleColors(
    fg: AnsiColor,
    bg: AnsiColor,
): UShort {
    val bgNibble = toNibble(bg).toInt()
    val fgNibble = toNibble(fg).toInt()
    return ((bgNibble shl 4) or fgNibble).toUShort()
}

/**
 * Decode a 4-bit Win32 console attribute nibble into an [AnsiColor].
 */
public fun fromNibble(color: UShort): AnsiColor {
    val base =
        if ((color and FOREGROUND_WHITE_BITS) == FOREGROUND_WHITE_BITS) {
            // 3 bits high
            AnsiColor.White
        } else if ((color and FOREGROUND_CYAN_BITS) == FOREGROUND_CYAN_BITS) {
            // 2 bits high
            AnsiColor.Cyan
        } else if ((color and FOREGROUND_YELLOW_BITS) == FOREGROUND_YELLOW_BITS) {
            // 2 bits high
            AnsiColor.Yellow
        } else if ((color and FOREGROUND_MAGENTA_BITS) == FOREGROUND_MAGENTA_BITS) {
            // 2 bits high
            AnsiColor.Magenta
        } else if ((color and FOREGROUND_RED_BITS) == FOREGROUND_RED_BITS) {
            // 1 bit high
            AnsiColor.Red
        } else if ((color and FOREGROUND_GREEN_BITS) == FOREGROUND_GREEN_BITS) {
            // 1 bit high
            AnsiColor.Green
        } else if ((color and FOREGROUND_BLUE_BITS) == FOREGROUND_BLUE_BITS) {
            // 1 bit high
            AnsiColor.Blue
        } else {
            // 0 bits high
            AnsiColor.Black
        }
    return base.bright((color and FOREGROUND_INTENSITY_BITS) == FOREGROUND_INTENSITY_BITS)
}

/**
 * Encode an [AnsiColor] into a 4-bit Win32 console attribute nibble.
 */
public fun toNibble(color: AnsiColor): UShort {
    var attributes =
        when (color.bright(false)) {
            AnsiColor.Black -> 0u
            AnsiColor.Red -> FOREGROUND_RED_BITS
            AnsiColor.Green -> FOREGROUND_GREEN_BITS
            AnsiColor.Yellow -> FOREGROUND_YELLOW_BITS
            AnsiColor.Blue -> FOREGROUND_BLUE_BITS
            AnsiColor.Magenta -> FOREGROUND_MAGENTA_BITS
            AnsiColor.Cyan -> FOREGROUND_CYAN_BITS
            AnsiColor.White -> FOREGROUND_WHITE_BITS
            AnsiColor.BrightBlack,
            AnsiColor.BrightRed,
            AnsiColor.BrightGreen,
            AnsiColor.BrightYellow,
            AnsiColor.BrightBlue,
            AnsiColor.BrightMagenta,
            AnsiColor.BrightCyan,
            AnsiColor.BrightWhite,
            -> error("brights were toggled off")
        }
    if (color.isBright()) {
        attributes = attributes or FOREGROUND_INTENSITY_BITS
    }
    return attributes
}
