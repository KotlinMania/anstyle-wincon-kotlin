// port-lint: source ansi.rs
package io.github.kotlinmania.anstylewincon.ansi

import ai.solace.tui.anstyle.AnsiColor
import ai.solace.tui.anstyle.Reset
import io.github.kotlinmania.anstylewincon.WinconWriteOutcome
import io.github.kotlinmania.io.Buffer
import io.github.kotlinmania.io.Sink
import io.github.kotlinmania.io.readByteArray
import io.github.kotlinmania.io.writeString

// Low-level ANSI-styling

/**
 * Write ANSI colored text to [stream].
 */
public fun writeColored(
    stream: Sink,
    fg: AnsiColor?,
    bg: AnsiColor?,
    data: ByteArray,
): WinconWriteOutcome {
    val nonDefault = fg != null || bg != null

    if (nonDefault) {
        if (fg != null) {
            stream.writeString(fg.renderFg().toString())
        }
        if (bg != null) {
            stream.writeString(bg.renderBg().toString())
        }
    }
    stream.write(data, 0, data.size)
    if (nonDefault) {
        stream.writeString(Reset.render().toString())
    }
    return WinconWriteOutcome.ok(data.size)
}

internal fun formatAnsiColoredBytes(
    fg: AnsiColor?,
    bg: AnsiColor?,
    data: ByteArray,
): ByteArray {
    val buffer = Buffer()
    writeColored(buffer, fg, bg, data)
    return buffer.readByteArray()
}
