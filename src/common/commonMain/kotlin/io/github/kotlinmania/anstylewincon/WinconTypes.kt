package io.github.kotlinmania.anstylewincon

import ai.solace.tui.anstyle.AnsiColor

/**
 * Classifies an I/O failure returned by terminal color or stream operations.
 */
public enum class WinconIoErrorKind {
    BrokenPipe,
    RawOs,
    WriteFailed,
}

/**
 * Error returned by console color or styled write operations.
 */
public class WinconIoError(
    public val kind: WinconIoErrorKind,
    public val rawOsError: Int? = null,
    public val message: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WinconIoError) return false
        return kind == other.kind && rawOsError == other.rawOsError && message == other.message
    }

    override fun hashCode(): Int {
        var result = kind.hashCode()
        result = 31 * result + (rawOsError ?: 0)
        result = 31 * result + message.hashCode()
        return result
    }

    override fun toString(): String = "WinconIoError(kind=$kind, rawOsError=$rawOsError, message=$message)"

    public companion object {
        /**
         * Creates an error matching upstream `IoError::BrokenPipe` (`"console is detached"`).
         */
        public fun brokenPipe(): WinconIoError =
            WinconIoError(
                kind = WinconIoErrorKind.BrokenPipe,
                rawOsError = null,
                message = "console is detached",
            )

        /**
         * Creates an error from an OS error code, matching `std::io::Error::from_raw_os_error`.
         */
        public fun fromRawOsError(code: Int): WinconIoError =
            WinconIoError(
                kind = WinconIoErrorKind.RawOs,
                rawOsError = code,
                message = "os error $code",
            )

        /**
         * Creates an error describing a stream write failure.
         */
        public fun writeFailed(message: String): WinconIoError =
            WinconIoError(
                kind = WinconIoErrorKind.WriteFailed,
                rawOsError = null,
                message = message,
            )
    }
}

/**
 * Foreground and background console color pair.
 */
public class WinconColorPair(
    public val fg: AnsiColor,
    public val bg: AnsiColor,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WinconColorPair) return false
        return fg == other.fg && bg == other.bg
    }

    override fun hashCode(): Int = 31 * fg.hashCode() + bg.hashCode()

    override fun toString(): String = "WinconColorPair(fg=$fg, bg=$bg)"
}

/**
 * Result of writing colored bytes via [WinconStream] or ANSI formatting helpers.
 */
public class WinconWriteOutcome private constructor(
    public val isOk: Boolean,
    public val bytesWritten: Int,
    public val error: WinconIoError?,
) {
    /**
     * Returns the number of payload bytes written or raises an [IllegalStateException] on error.
     */
    public fun unwrap(): Int {
        if (!isOk) {
            throw IllegalStateException(error?.message ?: "wincon write failed")
        }
        return bytesWritten
    }

    public companion object {
        public fun ok(bytesWritten: Int): WinconWriteOutcome =
            WinconWriteOutcome(isOk = true, bytesWritten = bytesWritten, error = null)

        public fun err(error: WinconIoError): WinconWriteOutcome =
            WinconWriteOutcome(isOk = false, bytesWritten = 0, error = error)
    }
}

/**
 * Unit-valued outcome returned by console attribute mutation helpers.
 */
public class WinconUnitOutcome private constructor(
    public val isOk: Boolean,
    public val error: WinconIoError?,
) {
    /**
     * Asserts success or raises an [IllegalStateException] carrying the underlying error.
     */
    public fun unwrap() {
        if (!isOk) {
            throw IllegalStateException(error?.message ?: "wincon operation failed")
        }
    }

    public companion object {
        public fun ok(): WinconUnitOutcome = WinconUnitOutcome(isOk = true, error = null)

        public fun err(error: WinconIoError): WinconUnitOutcome = WinconUnitOutcome(isOk = false, error = error)
    }
}

/**
 * Result of querying active console foreground and background colors.
 */
public class WinconColorsOutcome private constructor(
    public val isOk: Boolean,
    public val colors: WinconColorPair?,
    public val error: WinconIoError?,
) {
    /**
     * Returns the queried [WinconColorPair] or raises an [IllegalStateException] on error.
     */
    public fun unwrap(): WinconColorPair {
        val resolved = colors
        if (!isOk || resolved == null) {
            throw IllegalStateException(error?.message ?: "wincon color query failed")
        }
        return resolved
    }

    public companion object {
        public fun ok(colors: WinconColorPair): WinconColorsOutcome =
            WinconColorsOutcome(isOk = true, colors = colors, error = null)

        public fun err(error: WinconIoError): WinconColorsOutcome =
            WinconColorsOutcome(isOk = false, colors = null, error = error)
    }
}

/**
 * Opaque Windows console stream handle wrapper mirroring `RawHandle` / `AsHandle`.
 */
public class ConsoleStreamHandle(
    public val rawHandle: Long,
)
