@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

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
    val text = bytes.decodeToString()
    if (!jsWriteStandardStream(isStderr = isStderr, text = text)) {
        print(text)
    }
    return WinconWriteOutcome.ok(data.size)
}

// Guard process access directly so headless browser bundles never invoke libc-kotlin's Node-only N-API bridge.
private fun jsWriteStandardStream(
    isStderr: Boolean,
    text: String,
): Boolean =
    js(
        "(typeof process !== 'undefined' && process && ((isStderr ? process.stderr : process.stdout) && typeof (isStderr ? process.stderr : process.stdout).write === 'function') ? ((isStderr ? process.stderr : process.stdout).write(text), true) : false)",
    )
