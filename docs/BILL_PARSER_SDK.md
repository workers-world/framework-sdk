# Parser pack extraction notes (internal)

Historical note: channel-specific parsers may live in the consuming application and gradually move into optional SDK modules.

This file is **not** a user guide. Prefer README + PROGRESS_GUIDE for public docs.

## Goal (aspirational)

Define a `ChannelParser` SPI (`supports`, `parse`) and shared parse models in `framework-sdk-core` without coupling to app DAOs.

## Non-goals

- Domain-specific classification pipelines stay in the consuming app until SPI stabilizes.
- Field masking / secrets remain app-level.
