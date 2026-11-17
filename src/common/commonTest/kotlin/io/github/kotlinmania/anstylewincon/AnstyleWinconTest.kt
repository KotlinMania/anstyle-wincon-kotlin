// port-lint: tests windows.rs
package io.github.kotlinmania.anstylewincon

import ai.solace.tui.anstyle.AnsiColor
import io.github.kotlinmania.anstylewincon.ansi.writeColored
import io.github.kotlinmania.anstylewincon.windows.fromNibble
import io.github.kotlinmania.anstylewincon.windows.packConsoleColors
import io.github.kotlinmania.anstylewincon.windows.toNibble
import io.github.kotlinmania.anstylewincon.windows.unpackConsoleColors
import io.github.kotlinmania.io.Buffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

public class AnstyleWinconTest {
    @Test
    public fun toFromNibble() {
        val colors =
            listOf(
                AnsiColor.Black,
                AnsiColor.Red,
                AnsiColor.Green,
                AnsiColor.Yellow,
                AnsiColor.Blue,
                AnsiColor.Magenta,
                AnsiColor.Cyan,
                AnsiColor.White,
                AnsiColor.BrightBlack,
                AnsiColor.BrightRed,
                AnsiColor.BrightGreen,
                AnsiColor.BrightYellow,
                AnsiColor.BrightBlue,
                AnsiColor.BrightMagenta,
                AnsiColor.BrightCyan,
                AnsiColor.BrightWhite,
            )
        for (expected in colors) {
            val nibble = toNibble(expected)
            val actual = fromNibble(nibble)
            assertEquals(expected, actual, "Intermediate: $nibble")
        }
        for (expectedFg in colors) {
            for (expectedBg in colors) {
                val packed = packConsoleColors(expectedFg, expectedBg)
                val unpacked = unpackConsoleColors(packed)
                assertEquals(WinconColorPair(expectedFg, expectedBg), unpacked)
            }
        }
    }

    @Test
    public fun ansiWriteColoredPlainAndStyled() {
        val payload = "hello wincon".encodeToByteArray()

        val plainStream = BufferWinconStream()
        val plainOutcome = plainStream.writeColored(fg = null, bg = null, data = payload)
        assertTrue(plainOutcome.isOk)
        assertEquals(payload.size, plainOutcome.bytesWritten)
        assertEquals("hello wincon", plainStream.readUtf8())

        val fgStream = BufferWinconStream()
        val fgOutcome = fgStream.writeColored(fg = AnsiColor.Red, bg = null, data = payload)
        assertTrue(fgOutcome.isOk)
        assertEquals(payload.size, fgOutcome.bytesWritten)
        assertEquals("\u001B[31mhello wincon\u001B[0m", fgStream.readUtf8())

        val bgStream = BufferWinconStream()
        val bgOutcome = bgStream.writeColored(fg = null, bg = AnsiColor.Blue, data = payload)
        assertTrue(bgOutcome.isOk)
        assertEquals(payload.size, bgOutcome.bytesWritten)
        assertEquals("\u001B[44mhello wincon\u001B[0m", bgStream.readUtf8())

        val bothBuffer = Buffer()
        val bothOutcome = writeColored(bothBuffer, fg = AnsiColor.BrightGreen, bg = AnsiColor.Black, data = payload)
        assertTrue(bothOutcome.isOk)
        assertEquals(payload.size, bothOutcome.bytesWritten)
        assertEquals("\u001B[92m\u001B[40mhello wincon\u001B[0m", BufferWinconStream(bothBuffer).readUtf8())

        val detached = WinconIoError.brokenPipe()
        assertEquals(WinconIoErrorKind.BrokenPipe, detached.kind)
        assertEquals("console is detached", detached.message)
        assertFalse(WinconWriteOutcome.err(detached).isOk)
    }
}
