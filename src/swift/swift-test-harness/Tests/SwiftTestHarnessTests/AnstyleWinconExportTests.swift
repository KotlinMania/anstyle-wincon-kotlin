import Testing
import AnstyleWincon

@Suite("AnstyleWincon exported behavior")
struct AnstyleWinconExportTests {
    @Test func bufferStreamFormatsPlainAndColoredBytes() {
        let stream = BufferWinconStream()
        let payload: [Int8] = Array("ok".utf8).map { Int8(bitPattern: $0) }

        let plain = stream.writeColored(fg: nil, bg: nil, data: payload)
        #expect(plain.isOk)
        #expect(plain.bytesWritten == 2)
        #expect(stream.readUtf8() == "ok")

        let styled = stream.writeColored(fg: .Red, bg: .Blue, data: payload)
        #expect(styled.isOk)
        #expect(styled.bytesWritten == 2)
        #expect(stream.readUtf8() == "\u{1B}[31m\u{1B}[44mok\u{1B}[0m")
    }

    @Test func winconNibblePackingRoundTripsColors() {
        let packed = packConsoleColors(fg: .BrightYellow, bg: .Cyan)
        let unpacked = unpackConsoleColors(attributes: packed)
        #expect(unpacked.fg == .BrightYellow)
        #expect(unpacked.bg == .Cyan)
        #expect(fromNibble(color: toNibble(color: .BrightMagenta)) == .BrightMagenta)
    }
}
