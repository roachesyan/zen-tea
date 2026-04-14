# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
./mvnw spring-boot:run                    # Run backend (port 8080)
./mvnw compile                            # Compile only
./mvnw test                               # Run all tests
./mvnw test -Dtest=CategoryServiceTest    # Run single test class
./mvnw test -Dtest=CategoryServiceTest#testCreate  # Run single test method
./mvnw clean package -DskipTests          # Build JAR without tests
```

## Architecture

Spring Boot 3.3.6 / Java 17 / MyBatis-Plus 3.5.5 / SQLite

### Package Layout (`com.zentea`)

```
common/          # Shared: Result<T> wrapper, ResultCode enum, BusinessException, OrderNoGenerator
config/          # SecurityConfig, MybatisPlusConfig (pagination), SwaggerConfig, JwtProperties
controller/      # REST controllers — one per resource
dto/request/     # Inbound DTOs with Jakarta validation annotations (@NotBlank, @Size)
dto/response/    # Outbound DTOs with @Builder pattern
entity/          # MyBatis-Plus entities (@TableName, @TableId AUTO, auto-fill timestamps)
mapper/          # Interfaces extending BaseMapper<Entity> — no XML mappers
security/        # JwtAuthenticationFilter, JwtTokenProvider, SecurityUser (UserDetails)
service/         # Business interfaces (IService pattern)
service/impl/    # @Service implementations with @Transactional
```

### Request Flow

```
HTTP Request → JwtAuthenticationFilter → Controller → Service → Mapper → SQLite
                                    ↓
                          GlobalExceptionHandler → Result<T> response
```

### Key Patterns

- **Response wrapping**: All controllers return `Result<T>`. Never return raw objects.
- **Exceptions**: Throw `BusinessException(ResultCode)` from services — caught by `GlobalExceptionHandler` and wrapped into `Result`.
- **DTOs**: Controllers accept `dto/request/*` classes (validated with `@Valid`) and return `dto/response/*` classes. Entities never leave the service layer.
- **Queries**: Use MyBatis-Plus `LambdaQueryWrapper` — not string-based queries. No XML mapper files.
- **Timestamps**: `createTime`/`updateTime` auto-filled via MyBatis-Plus `@TableField(fill = FieldFill.INSERT/INSERT_UPDATE)`.
- **Soft delete**: `deleted` field managed by MyBatis-Plus logical delete (1=deleted, 0=active). Not all entities have this — check before assuming.

### Authentication

- JWT Bearer tokens via `Authorization: Bearer <token>` header.
- Access tokens expire in 15 min, refresh tokens in 7 days (configurable in `application.yml` under `zentea.jwt`).
- Roles: `CUSTOMER`, `ADMIN` — stored in `user.role` field.
- Public endpoints: `/api/auth/**`, `GET /api/products/**`, `GET /api/categories/**`.
- `@PreAuthorize` available via `@EnableMethodSecurity` for fine-grained control.

### Database

- SQLite file at `./zentea.db` (configurable via `zentea.db.path` or `ZENTEA_DB_PATH` env var).
- Schema in `src/main/resources/schema.sql` — auto-init is currently disabled (`spring.sql.init.mode=never`).
- Seed data in `src/main/resources/data.sql` — includes default admin account.
- 6 tables: `user`, `refresh_token`, `category`, `product`, `orders`, `order_item`.
- Snake_case columns auto-mapped to camelCase Java fields.

### Swagger

- UI at `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON at `/v3/api-docs`

## Adding a New Resource

Follow the existing pattern (e.g., Category module):

1. Create entity in `entity/` with `@TableName`, `@TableId(type = IdType.AUTO)`, timestamp fields.
2. Create mapper interface in `mapper/` extending `BaseMapper<Entity>` with `@Mapper`.
3. Create request/response DTOs in `dto/request/` and `dto/response/` with validation annotations.
4. Create service interface in `service/` extending `IService<Entity>`.
5. Create service implementation in `service/impl/` with `@Service`, `@Transactional`, and `@Slf4j`.
6. Create controller in `controller/` with `@RestController`, `@RequestMapping("/api/{resource}")`, Swagger `@Tag`.
7. Add public/protected endpoint rules in `SecurityConfig.securityFilterChain` if needed.
