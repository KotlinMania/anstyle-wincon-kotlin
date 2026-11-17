package io.github.kotlinmania.anstylewincon

import ai.solace.tui.anstyle.AnsiColor
import io.github.kotlinmania.anstylewincon.ansi.formatAnsiColoredBytes

internal actual fun platformInitialConsoleColors(isStderr: Boolean): WinconColorsOutcome =
    WinconColorsOutcome.err(WinconIoError.brokenPipe())

internal actual fun platformGetConsoleColors(handle: Long): WinconColorsOutcome =
    WinconColorsOutcome.err(WinconIoError.brokenPipe())

internal actual fun platformSetConsoleColors(
    handle: Long,
    fg: AnsiColor,
    bg: AnsiColor,
): WinconUnitOutcome = WinconUnitOutcome.err(WinconIoError.brokenPipe())

internal actual fun platformWriteStandardStream(
    isStderr: Boolean,
    fg: AnsiColor?,
    bg: AnsiColor?,
    data: ByteArray,
): WinconWriteOutcome {
    val bytes = formatAnsiColoredBytes(fg = fg, bg = bg, data = data)
    val stream = if (isStderr) System.err else System.out
    stream.write(bytes)
    stream.flush()
    return WinconWriteOutcome.ok(data.size)
}
