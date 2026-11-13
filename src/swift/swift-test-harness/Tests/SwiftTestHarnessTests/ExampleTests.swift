#if canImport(Testing)
import Testing
import AnstyleWincon

@Suite("AnstyleWincon Swift Export Tests")
struct AnstyleWinconExportTests {
    @Test("AnstyleWincon swift module imported cleanly")
    func testSwiftModuleLoads() throws {
        #expect(Bool(true), "AnstyleWincon swift module imported cleanly")
    }
}
#elseif canImport(XCTest)
import XCTest
import AnstyleWincon

final class AnstyleWinconExportTests: XCTestCase {
    func testSwiftModuleLoads() throws {
        XCTAssertTrue(true, "AnstyleWincon swift module imported cleanly")
    }
}
#endif
