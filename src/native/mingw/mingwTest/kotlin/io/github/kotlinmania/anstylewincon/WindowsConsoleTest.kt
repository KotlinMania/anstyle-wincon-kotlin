package io.github.kotlinmania.anstylewincon

import ai.solace.tui.anstyle.AnsiColor
import io.github.kotlinmania.anstylewincon.windows.getColors
import io.github.kotlinmania.anstylewincon.windows.setColors
import io.github.kotlinmania.anstylewincon.windows.stderrInitialColors
import io.github.kotlinmania.anstylewincon.windows.stdoutInitialColors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

public class WindowsConsoleTest {
    @Test
    public fun detachedAndInvalidHandlesReportExpectedErrors() {
        val nullHandle = ConsoleStreamHandle(0L)
        val getZero = getColors(nullHandle)
        assertFalse(getZero.isOk)
        assertEquals(WinconIoErrorKind.BrokenPipe, getZero.error?.kind)

        val setZero = setColors(nullHandle, fg = AnsiColor.White, bg = AnsiColor.Black)
        assertFalse(setZero.isOk)
        assertEquals(WinconIoErrorKind.BrokenPipe, setZero.error?.kind)

        val invalidHandle = ConsoleStreamHandle(-1L)
        val getInvalid = getColors(invalidHandle)
        assertFalse(getInvalid.isOk)
        assertNotNull(getInvalid.error)
    }

    @Test
    public fun standardConsoleInitialQueriesExecuteCleanly() {
        val stdoutQuery = stdoutInitialColors()
        if (stdoutQuery.isOk) {
            assertNotNull(stdoutQuery.colors)
        } else {
            assertNotNull(stdoutQuery.error)
        }

        val stderrQuery = stderrInitialColors()
        if (stderrQuery.isOk) {
            assertNotNull(stderrQuery.colors)
        } else {
            assertNotNull(stderrQuery.error)
        }
    }
}
