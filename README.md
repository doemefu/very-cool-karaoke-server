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

Both docs are auto-generated at runtime. Start the server locally, then open:

| Tool | URL | Covers |
|---|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html | All REST endpoints |
| Springwolf | http://localhost:8080/springwolf/asyncapi-ui.html | All WebSocket/STOMP events |

The raw specs are also available:
```
GET http://localhost:8080/v3/api-docs.yaml        → OpenAPI YAML
GET http://localhost:8080/springwolf/docs.yaml    → AsyncAPI YAML
```

For the M2 design specs (before implementation), see:
- `src/main/resources/karaokee-openapi.yaml` → paste into [editor.swagger.io](https://editor.swagger.io)
- `src/main/resources/karaokee-asyncapi.yaml` → paste into [editor.swagger.io](https://editor.swagger.io)

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
2. Open `karaokee-openapi.yaml` – a Swagger UI preview appears in the editor split view.
3. A `Run` gutter icon lets you start a local Swagger UI server for the file.

### YAML Schema Validation

To get autocompletion and validation for both spec files in IntelliJ:

1. `Settings → Languages & Frameworks → Schemas and DTDs → JSON Schema Mappings`
2. Add mapping:
    - `karaokee-openapi.yaml` → Schema URL: `https://spec.openapis.org/oas/3.0/schema/2021-09-28`
    - `karaokee-asyncapi.yaml` → Schema URL: `https://asyncapi.com/schema-store/3.0.0.json`

---

## Implementing an API Interface

The REST endpoints are defined as generated interfaces (`SessionsApi`, `SongsApi`, etc.).
They contain all route mappings and Swagger annotations — **never edit them directly**.

To implement an endpoint, create a controller that implements the interface:

### 1. Create a controller

```java
@RestController
public class SessionController implements SessionsApi {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }
}
```

### 2. Generate method stubs

In IntelliJ: right-click the class name → **Generate → Implement Methods** → select all → OK

### 3. Fill in the logic

```java
@Override
public ResponseEntity<SessionGetDTO> sessionsPost(SessionPostDTO sessionPostDTO) {
    SessionGetDTO result = sessionService.createSession(sessionPostDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
}
```

Methods you haven't implemented yet return `501 Not Implemented` by default — implement them incrementally.

**Rules:**
- One controller per interface (`SessionsApi` → `SessionController`, `SongsApi` → `SongController`, etc.)
- Business logic belongs in a `@Service` class, not in the controller

---

## Commit Standards

Every commit **must** reference a GitHub Issue number — required for TA grading:

```bash
git commit -m "feat: implement POST /sessions endpoint (#12)"
git commit -m "fix: return 409 when username already taken (#5)"
```

See [CONTRIBUTING.md](./CONTRIBUTING.md) for full annotation guidelines (REST + WebSocket).