# BaseMessage serialization (Jackson)

`framework-sdk-core` uses Jackson `ObjectMapper` for `BaseMessage#get` / `getDto` coercion when `data` values are not already typed instances.

## Migration from Fastjson

- **Removed**: direct `com.alibaba.fastjson` dependency from SDK modules.
- **Apps**: drop explicit `fastjson` from application `pom.xml` after upgrading to `framework-sdk` 1.3+.
- **Spring**: register a shared mapper via `BaseMessage.setObjectMapper` (done in `FrameworkMessageAutoConfiguration`).

## Testing

See `BaseMessageTest` in `framework-sdk-core` for round-trip and map-to-POJO cases.
