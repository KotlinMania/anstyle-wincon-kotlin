package io.github.kotlinmania.anstylewincon

import ai.solace.tui.anstyle.AnsiColor
import io.github.kotlinmania.anstylewincon.windows.packConsoleColors
import io.github.kotlinmania.anstylewincon.windows.unpackConsoleColors
import io.github.kotlinmania.libc.windows.fflush
import io.github.kotlinmania.libc.windows.fputc
import io.github.kotlinmania.libc.windows.putchar
import io.github.kotlinmania.windowssys.windows.win32.foundation.GetLastError
import io.github.kotlinmania.windowssys.windows.win32.storage.filesystem.FlushFileBuffers
import io.github.kotlinmania.windowssys.windows.win32.system.console.CONSOLE_SCREEN_BUFFER_INFO
import io.github.kotlinmania.windowssys.windows.win32.system.console.GetConsoleScreenBufferInfo
import io.github.kotlinmania.windowssys.windows.win32.system.console.GetStdHandle
import io.github.kotlinmania.windowssys.windows.win32.system.console.STD_ERROR_HANDLE
import io.github.kotlinmania.windowssys.windows.win32.system.console.STD_OUTPUT_HANDLE
import io.github.kotlinmania.windowssys.windows.win32.system.console.SetConsoleTextAttribute

private var cachedStdoutInitialColors: WinconColorsOutcome? = null
private var cachedStderrInitialColors: WinconColorsOutcome? = null
private val cachedStderrFile by lazy {
    io.github.kotlinmania.libc.windows
        .fdopen(2, "wb")
}

internal actual fun platformInitialConsoleColors(isStderr: Boolean): WinconColorsOutcome {
    if (isStderr) {
        val existing = cachedStderrInitialColors
        if (existing != null) return existing
        val queried = platformGetConsoleColors(GetStdHandle(STD_ERROR_HANDLE))
        cachedStderrInitialColors = queried
        return queried
    }
    val existing = cachedStdoutInitialColors
    if (existing != null) return existing
    val queried = platformGetConsoleColors(GetStdHandle(STD_OUTPUT_HANDLE))
    cachedStdoutInitialColors = queried
    return queried
}

internal actual fun platformGetConsoleColors(handle: Long): WinconColorsOutcome {
    if (handle == 0L) {
        return WinconColorsOutcome.err(WinconIoError.brokenPipe())
    }
    val info = CONSOLE_SCREEN_BUFFER_INFO()
    if (GetConsoleScreenBufferInfo(handle, info) == 0) {
        return WinconColorsOutcome.err(WinconIoError.fromRawOsError(GetLastError().toInt()))
    }
    return WinconColorsOutcome.ok(unpackConsoleColors(info.wAttributes))
}

internal actual fun platformSetConsoleColors(
    handle: Long,
    fg: AnsiColor,
    bg: AnsiColor,
): WinconUnitOutcome {
    if (handle == 0L) {
        return WinconUnitOutcome.err(WinconIoError.brokenPipe())
    }
    val attributes = packConsoleColors(fg = fg, bg = bg)
    if (SetConsoleTextAttribute(handle, attributes) == 0) {
        return WinconUnitOutcome.err(WinconIoError.fromRawOsError(GetLastError().toInt()))
    }
    return WinconUnitOutcome.ok()
}

private fun flushStream(
    isStderr: Boolean,
    handle: Long,
) {
    if (!isStderr) {
        fflush(null)
    }
    FlushFileBuffers(handle)
}

internal actual fun platformWriteStandardStream(
    isStderr: Boolean,
    fg: AnsiColor?,
    bg: AnsiColor?,
    data: ByteArray,
): WinconWriteOutcome {
    val initialOutcome = platformInitialConsoleColors(isStderr = isStderr)
    val initial = initialOutcome.colors
    if (!initialOutcome.isOk || initial == null) {
        return WinconWriteOutcome.err(initialOutcome.error ?: WinconIoError.brokenPipe())
    }
    val handle = GetStdHandle(if (isStderr) STD_ERROR_HANDLE else STD_OUTPUT_HANDLE)
    val nonDefault = fg != null || bg != null

    if (nonDefault) {
        flushStream(isStderr = isStderr, handle = handle)
        val setOutcome =
            platformSetConsoleColors(
                handle = handle,
                fg = fg ?: initial.fg,
                bg = bg ?: initial.bg,
            )
        if (!setOutcome.isOk) {
            return WinconWriteOutcome.err(setOutcome.error ?: WinconIoError.brokenPipe())
        }
    }

    if (!isStderr) {
        for (byte in data) {
            if (putchar(byte.toInt() and 0xFF) < 0) {
                return WinconWriteOutcome.err(WinconIoError.writeFailed("stdout write failed"))
            }
        }
    } else {
        val stderrFile = cachedStderrFile
        if (stderrFile != null) {
            for (byte in data) {
                if (fputc(byte.toInt() and 0xFF, stderrFile) < 0) {
                    return WinconWriteOutcome.err(WinconIoError.writeFailed("stderr write failed"))
                }
            }
            fflush(stderrFile)
        } else {
            print(data.decodeToString())
        }
    }

    if (nonDefault) {
        flushStream(isStderr = isStderr, handle = handle)
        val restoreOutcome =
            platformSetConsoleColors(
                handle = handle,
                fg = initial.fg,
                bg = initial.bg,
            )
        if (!restoreOutcome.isOk) {
            return WinconWriteOutcome.err(restoreOutcome.error ?: WinconIoError.brokenPipe())
        }
    }

    return WinconWriteOutcome.ok(data.size)
}
