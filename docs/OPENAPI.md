# OpenAPI generation with @ApiName

Framework SDK controllers annotate endpoints with `@ApiName` (operation id, DTO input/output types, description). Application projects can expose OpenAPI via **springdoc-openapi**:

1. Add `springdoc-openapi-starter-webmvc-ui` to the app `pom.xml`.
2. Provide an `OpenAPI` `@Bean` (title, version) in app config.
3. Optionally add a `GroupedOpenApi` bean for `/api/**` paths.

springdoc discovers Spring MVC `@PostMapping` methods; use `@ApiName.description` as the human-readable summary in custom `OperationCustomizer` beans if you need strict alignment between ApiName metadata and OpenAPI operation text.

Recommended next step: a small `ApiNameOpenApiCustomizer` in `framework-sdk-autoconfigure` that reads `@ApiName` at runtime and sets `operationId` + schema refs from `input`/`output` class arrays.
