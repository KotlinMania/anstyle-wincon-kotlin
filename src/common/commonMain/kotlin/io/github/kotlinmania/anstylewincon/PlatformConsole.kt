package io.github.kotlinmania.anstylewincon

import ai.solace.tui.anstyle.AnsiColor

internal expect fun platformInitialConsoleColors(isStderr: Boolean): WinconColorsOutcome

internal expect fun platformGetConsoleColors(handle: Long): WinconColorsOutcome

internal expect fun platformSetConsoleColors(
    handle: Long,
    fg: AnsiColor,
    bg: AnsiColor,
): WinconUnitOutcome

internal expect fun platformWriteStandardStream(
    isStderr: Boolean,
    fg: AnsiColor?,
    bg: AnsiColor?,
    data: ByteArray,
): WinconWriteOutcome
