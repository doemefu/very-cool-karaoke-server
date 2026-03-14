# Contributing to Karaokee

## API Documentation

This project auto-generates **two live API docs** when the server is running:

| Tool | URL | Purpose |
|---|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html | REST endpoints |
| Springwolf | http://localhost:8080/springwolf/asyncapi-ui.html | WebSocket/STOMP events |

No manual doc updates needed — both are generated from code annotations at startup.

---

## REST Endpoints (springdoc-openapi)

Annotate your controllers with standard Spring + OpenAPI annotations:

```java
@RestController
@RequestMapping("/sessions")
@Tag(name = "Sessions", description = "Karaoke session lifecycle")
public class SessionController {

    @PostMapping
    @Operation(summary = "Create a new karaoke session")
    @ApiResponse(responseCode = "201", description = "Session created")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<SessionGetDTO> createSession(@RequestBody SessionPostDTO dto) {
        // ...
    }
}
```

**Useful annotations:**
- `@Tag` – groups endpoints in Swagger UI
- `@Operation(summary = "...")` – describes an endpoint
- `@ApiResponse(responseCode = "...", description = "...")` – documents status codes
- `@Parameter(description = "...")` – describes path/query parameters

---

## WebSocket Events (springwolf-stomp)

Annotate your STOMP message handlers so Springwolf picks them up:

```java
@AsyncListener(operation = @AsyncOperation(
    channelName = "/topic/sessions/{sessionId}/reactions",
    description = "Broadcast when a participant sends a live reaction (S9)"
))
@MessageMapping("/sessions/{sessionId}/reactions")
public void handleReaction(@DestinationVariable Long sessionId, ReactionPostDTO dto) {
    // server processes and broadcasts to /topic/sessions/{sessionId}/reactions
}
```

---

## application.properties

The following Springwolf properties are required (already configured):

```properties
springwolf.enabled=true
springwolf.docket.info.title=Karaokee API
springwolf.docket.info.version=1.0.0
springwolf.docket.info.description=AsyncAPI documentation for Karaokee WebSocket events
springwolf.docket.base-package=ch.uzh.ifi.hase.soprafs26
springwolf.docket.servers.stomp.protocol=stomp
springwolf.docket.servers.stomp.host=localhost:8080/ws
```

Do not remove these — the app will fail to start without `title` and `version`.

---

## Static API Spec

A hand-written OpenAPI YAML spec is available at:

```
src/main/resources/karaokee-openapi.yaml
```

This was used as the design specification before implementation. From M3 onwards,
the live Swagger UI at `/swagger-ui.html` is the source of truth.

To view the YAML spec interactively: paste it into [editor.swagger.io](https://editor.swagger.io).

---

## Commit Standards (required by TAs)

Every commit **must** reference a GitHub Issue number:

```
git commit -m "feat: implement POST /sessions endpoint (#12)"
git commit -m "fix: return 409 when username already taken (#5)"
```