# anstyle-wincon-kotlin

Kotlin Multiplatform port of Rust [`anstyle-wincon`](https://crates.io/crates/anstyle-wincon) (`3.0.2`) for styling legacy Windows consoles and ANSI streams across all 17 retained KotlinMania KMP targets and Swift Export.

## Installation

```kotlin
dependencies {
    implementation("io.github.kotlinmania:anstyle-wincon-kotlin:0.1.0")
}
```

## Kotlin Usage

```kotlin
import ai.solace.tui.anstyle.AnsiColor
import io.github.kotlinmania.anstylewincon.BufferWinconStream

val stream = BufferWinconStream()
stream.writeColored(fg = AnsiColor.Red, bg = AnsiColor.Blue, data = "hello".encodeToByteArray())
```

## Swift Usage

```swift
import AnstyleWincon

let stream = BufferWinconStream()
let bytes: [Int8] = Array("hello".utf8).map { Int8(bitPattern: $0) }
let outcome = stream.writeColored(fg: .Red, bg: .Blue, data: bytes)
```

## License

Dual-licensed under [MIT](tmp/anstyle-wincon/LICENSE-MIT) or [Apache-2.0](tmp/anstyle-wincon/LICENSE-APACHE), matching upstream `anstyle-wincon`.
