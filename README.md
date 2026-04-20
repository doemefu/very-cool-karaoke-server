# Karaokee – Server

> Remove the shame, enhance the game by making karaoke parties hustle-free.

Karaokee is a web-based karaoke session tool for real-time multi-user interaction.
A host creates a session that guests join from their own device. Participants collaboratively
build a song queue, vote on the next track, follow lyrics on-screen, and send live reactions
during performances.

---

## Deployments

| Environment     | URL                                                        |
|-----------------|------------------------------------------------------------|
| Server (prod)   | https://sopra-fs26-group-22-server.oa.r.appspot.com        |
| Client (prod)   | https://sopra-fs26-group-22-client.vercel.app              |
| GitHub (server) | https://github.com/sopra-fs26-group-22/karaoke-server      |
| GitHub (client) | https://github.com/sopra-fs26-group-22/karaoke-client      |
| SonarQube       | TBD                                                        |
| Docker          | https://hub.docker.com/repository/docker/2026sopragroup22  |

---

## API Documentation

Start the server locally, then open:

| Tool | URL | Covers |
|---|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html | All REST endpoints (from YAML) |
| Springwolf | http://localhost:8080/springwolf/asyncapi-ui.html | All WebSocket/STOMP events |

Swagger UI renders directly from `karaokee-openapi.json`, so it always shows **all endpoints** — including ones not yet implemented. Unimplemented endpoints return `501 Not Implemented` until overridden in a controller.

---

## Setup & Development

For a full setup guide including Google Cloud deployment, Docker, and Vercel, follow the official SoPra FS26 tutorial:

👉 **https://luciocanepa.github.io/soprafs26_tutorial_1/**

### Quick start (local)

```bash
./gradlew bootRun
```

The server starts on `http://localhost:8080`.

### Run tests

```bash
./gradlew test
```

### Development mode (auto-reload)

Open two terminals:

```bash
# Terminal 1
./gradlew build --continuous -xtest

# Terminal 2
./gradlew bootRun
```

---

## IntelliJ: Rendered API Specs

To get live rendering of the OpenAPI and AsyncAPI YAML files directly in IntelliJ:

### OpenAPI (Swagger UI in editor)

1. Install the **OpenAPI Specifications** plugin:
   `IntelliJ → Settings → Plugins → search "OpenAPI Specifications"` (by JetBrains)
2. Open `karaokee-openapi.json` – a Swagger UI preview appears in the editor split view.
3. A `Run` gutter icon lets you start a local Swagger UI server for the file.

### YAML Schema Validation

To get autocompletion and validation for both spec files in IntelliJ:

1. `Settings → Languages & Frameworks → Schemas and DTDs → JSON Schema Mappings`
2. Add mapping:
    - `karaokee-openapi.json` → Schema URL: `https://spec.openapis.org/oas/3.0/schema/2021-09-28`
    - `karaokee-asyncapi.yaml` → Schema URL: `https://asyncapi.com/schema-store/3.0.0.json`

---

## How We Develop Endpoints (YAML-first)

This project uses an **API-first** workflow. The YAML file is the single source of truth for all REST endpoints — not the Java code.

### How it differs from the classic Spring approach

**Classic approach:** You write a `@RestController` with `@GetMapping`/`@PostMapping` directly, add Swagger annotations manually, and the spec is derived from code.

**Our approach:** The contract is defined first in YAML, interfaces are generated from it, and controllers implement those interfaces.

```
karaokee-openapi.json  (you edit this)
        ↓  ./gradlew build  (runs automatically)
*Api.java interfaces  (auto-generated — never edit these)
        ↓  you write this
*Controller.java  (your implementation)
        ↓
*Service.java  (your business logic)
```

### Workflow: changing or adding an endpoint

**Step 1 — Edit the YAML**

Open `src/main/resources/static/karaokee-openapi.json` and add or change the endpoint definition.

**Step 2 — Regenerate the interfaces**

```bash
./gradlew build
# or just the generation step:
./gradlew openApiGenerate
```

The `*Api.java` interfaces in `build/generated/` are updated automatically. If your controller's `@Override` method signature no longer matches, you get a **compile error** — that's intentional, it tells you exactly what to update.

**Step 3 — Implement in the controller**

Each `*Api` interface has a corresponding controller stub in `src/main/java/.../controller/`:

| Interface | Controller | Endpoints |
|---|---|---|
| `AuthApi` | `AuthController` | `POST /users`, `POST /auth/login`, `POST /auth/logout` |
| `UsersApi` | `UsersController` | `PUT /users/{userId}`, `GET /users/{userId}/sessions` |
| `SessionsApi` | `SessionsController` | `POST /sessions`, `GET/PUT /sessions/{id}`, participants, review |
| `SongsApi` | `SongsController` | Song queue management, search, skip |
| `VotingApi` | `VotingController` | Voting rounds and votes |
| `ReactionsApi` | `ReactionsController` | `POST /sessions/{id}/reactions` |

Open the relevant controller — each unimplemented method has a `// TODO` comment. Override it and delegate to a service:

```java
// In SessionsController.java
@Override
public ResponseEntity<SessionGetDTO> sessionsPost(SessionPostDTO sessionPostDTO) {
    SessionGetDTO result = sessionService.createSession(sessionPostDTO);
    return ResponseEntity.status(201).body(result);
}
```

**Step 4 — Business logic goes in the Service**

Controllers only translate HTTP ↔ service calls. All logic (validation, DB access, calculations) belongs in `*Service.java`.

### What the generated interfaces actually do

You might wonder why the interfaces exist at all if we already have the controller stubs. They are not just for documentation — they do active work:

| What | How |
|---|---|
| **Route mapping** | `@RequestMapping` lives on the interface. Your controller needs zero `@GetMapping`/`@PostMapping`. |
| **Swagger docs** | All `@Operation`, `@ApiResponse`, `@Parameter` annotations are on the interface. That's what populates `/swagger-ui.html` with descriptions, response codes, and examples — for free. |
| **Input validation** | `@Valid` and `@NotNull` on method parameters come from the interface. Spring enforces them on your implementation automatically. |
| **501 fallback** | Methods you haven't overridden yet return `501 Not Implemented`. Without this, an unregistered route returns 404, which is misleading ("does this endpoint exist?"). 501 says: yes, it exists, it's just not done yet. |
| **Compile-time safety** | If the YAML changes and you regenerate, any controller method whose signature no longer matches the interface becomes a **compile error**. The compiler tells you exactly what to fix — you can't accidentally ship a controller that's out of sync with the spec. |

The result: your controller only needs to contain the actual logic. Everything else is handled.

### Rules

- **Never edit `*Api.java` files** — they live in `build/generated/` and are overwritten on every build
- **Never add `@GetMapping` / `@PostMapping` to controllers** — routing comes from the interface
- **Never add `@Operation` or `@ApiResponse` to controllers** — Swagger docs come from the interface
- One controller per interface, one service per domain
- Keep controllers thin: receive request → call service → return response

---

## Commit Standards

Every commit **must** reference a GitHub Issue number — required for TA grading:

```bash
git commit -m "feat: implement POST /sessions endpoint (#12)"
git commit -m "fix: return 409 when username already taken (#5)"
```

See [CONTRIBUTING.md](./CONTRIBUTING.md) for full annotation guidelines (REST + WebSocket).