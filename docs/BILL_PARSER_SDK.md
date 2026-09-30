# Bill parser extraction plan (channel parsers)

## Goal

Move channel-specific bill parsers from reconciliation into reusable `framework-sdk-core` (or `framework-sdk-parser`) modules without coupling to reconciliation DAOs.

## Phases

1. **Inventory** — list parsers under `com.my.reconciliation.service.parser` and classify: pure parse vs DB side effects.
2. **Core API** — define `ChannelParser` SPI: `supports(channel)`, `parse(InputStream, ParseContext) -> ParseResult`.
3. **Shared models** — relocate `ParseResult`, row DTOs, and PDF/Excel helpers already in SDK optional deps (POI, PDFBox, Tabula).
4. **Gradual move** — extract one channel (e.g. CMB credit) into SDK with golden-file tests; reconciliation keeps thin delegating wrappers.
5. **Versioning** — publish parser pack version aligned with `framework-sdk-bom`.

## Non-goals (initial)

- LLM classification pipelines stay in reconciliation until SPI stabilizes.
- Encryption-aware field masking remains app-level.
