package io.github.kotlinmania.anstylewincon

import ai.solace.tui.anstyle.AnsiColor
import io.github.kotlinmania.anstylewincon.ansi.formatAnsiColoredBytes
import io.github.kotlinmania.libc.unix.fflush
import io.github.kotlinmania.libc.unix.putchar

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
    if (!isStderr) {
        for (byte in bytes) {
            if (putchar(byte.toInt() and 0xFF) < 0) {
                return WinconWriteOutcome.err(WinconIoError.writeFailed("stdout write failed"))
            }
        }
        fflush(null)
    } else {
        print(bytes.decodeToString())
    }
    return WinconWriteOutcome.ok(data.size)
}
