// port-lint: source stream.rs
package io.github.kotlinmania.anstylewincon

import ai.solace.tui.anstyle.AnsiColor
import io.github.kotlinmania.anstylewincon.ansi.writeColored as writeAnsiColored
import io.github.kotlinmania.io.Buffer
import io.github.kotlinmania.io.Sink
import io.github.kotlinmania.io.readString

/**
 * Extend byte sinks and standard console streams with wincon styling.
 */
public interface WinconStream {
    /**
     * Write colored text to the stream.
     */
    public fun writeColored(
        fg: AnsiColor?,
        bg: AnsiColor?,
        data: ByteArray,
    ): WinconWriteOutcome
}

/**
 * [WinconStream] backed by a [Buffer].
 */
public class BufferWinconStream(
    public val buffer: Buffer = Buffer(),
) : WinconStream {
    override fun writeColored(
        fg: AnsiColor?,
        bg: AnsiColor?,
        data: ByteArray,
    ): WinconWriteOutcome = writeAnsiColored(buffer, fg, bg, data)

    /**
     * Drains the buffered bytes as UTF-8 text.
     */
    public fun readUtf8(): String = buffer.readString()
}

/**
 * [WinconStream] writing ANSI escape sequences into a [Sink].
 */
public class SinkWinconStream(
    public val sink: Sink,
) : WinconStream {
    override fun writeColored(
        fg: AnsiColor?,
        bg: AnsiColor?,
        data: ByteArray,
    ): WinconWriteOutcome = writeAnsiColored(sink, fg, bg, data)
}

/**
 * [WinconStream] writing colored output to standard output.
 */
public object StdoutWinconStream : WinconStream {
    override fun writeColored(
        fg: AnsiColor?,
        bg: AnsiColor?,
        data: ByteArray,
    ): WinconWriteOutcome = platformWriteStandardStream(isStderr = false, fg = fg, bg = bg, data = data)
}

/**
 * [WinconStream] writing colored output to standard error.
 */
public object StderrWinconStream : WinconStream {
    override fun writeColored(
        fg: AnsiColor?,
        bg: AnsiColor?,
        data: ByteArray,
    ): WinconWriteOutcome = platformWriteStandardStream(isStderr = true, fg = fg, bg = bg, data = data)
}

/**
 * Write colored text to this [Sink] via [io.github.kotlinmania.anstylewincon.ansi.writeColored].
 */
public fun Sink.writeColored(
    fg: AnsiColor?,
    bg: AnsiColor?,
    data: ByteArray,
): WinconWriteOutcome = writeAnsiColored(this, fg, bg, data)

/**
 * Write colored text to this [Buffer] via [io.github.kotlinmania.anstylewincon.ansi.writeColored].
 */
public fun Buffer.writeColored(
    fg: AnsiColor?,
    bg: AnsiColor?,
    data: ByteArray,
): WinconWriteOutcome = writeAnsiColored(this, fg, bg, data)
