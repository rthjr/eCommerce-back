**Summary**
- **Eureka server** runs on port 8761 and is configured as a standalone server (does not register with itself).

**Eureka server (repo reference)**
- `eureka` config: [eureka/src/main/resources/application.yml](eureka/src/main/resources/application.yml#L1-L7) — sets `server.port: 8761` and `eureka.client.registerWithEureka: false` / `fetchRegistry: false`.

**Config Server (how it's used here)**
- `configserver` runs on port 8888 and is configured in native mode to serve local YAML files: [configserver/src/main/resources/application.yml](configserver/src/main/resources/application.yml#L1-L20).
- Per-service config files live under `configserver/src/main/resources/config/` (for example: [configserver/src/main/resources/config/product-service.yml](configserver/src/main/resources/config/product-service.yml#L1-L80)). Those files include `eureka.client` blocks which provide the Eureka URL and registration settings for the corresponding service.

**How services obtain Eureka configuration in this repo**
- Two approaches are used in the codebase:
	- Centralized via Config Server: services that include the Spring Cloud Config client and import the config server will receive their `eureka.client` properties from the config repo (example: `product` uses `spring.config.import: optional:configserver:http://localhost:8888` — see [product/src/main/resources/application.yml](product/src/main/resources/application.yml#L1-L4) and the corresponding service config at [configserver/src/main/resources/config/product-service.yml](configserver/src/main/resources/config/product-service.yml#L48-L60)). The `product` module also has the `spring-cloud-starter-config` dependency in its POM ([product/pom.xml](product/pom.xml#L1-L40)).
	- Local per-service config: some services keep `eureka.client` settings in their own `application.yml` (examples: [payment/src/main/resources/application.yml](payment/src/main/resources/application.yml#L1-L30), [notification/src/main/resources/application.yml](notification/src/main/resources/application.yml#L1-L30)). Those will not depend on the config server for Eureka settings.

**Does Config Server configure Eureka and make services use it automatically?**
- The Config Server can supply Eureka client properties (URL, register/fetch flags) to clients. Clients will use whatever `eureka.client` properties are present in their final configuration (local file + config server overrides + environment variables). In this repo, some services consume Eureka settings from the Config Server while others define them locally — so both patterns are present.
- Note: `configserver` itself does not register with Eureka in this project (no Eureka client dependency in [configserver/pom.xml](configserver/pom.xml#L1-L40)).

**Practical notes & recommendations**
- If you want all services to get Eureka configuration from the Config Server: ensure every service includes `spring-cloud-starter-config` and either adds `spring.config.import=configserver:` (or relies on the default) or has a `bootstrap`/import config so the config client runs early. Then move `eureka.client` blocks to `configserver/src/main/resources/config/<service>.yml`.
- If a service cannot reach the Config Server at startup, it will fall back to local properties or fail to start (depending on your `spring.config.import` settings and whether config is mandatory). Start `configserver` (and `eureka`) early so clients can retrieve config and register correctly.

**Files I inspected**
- [eureka/src/main/resources/application.yml](eureka/src/main/resources/application.yml#L1-L7)
- [configserver/src/main/resources/application.yml](configserver/src/main/resources/application.yml#L1-L20)
- [configserver/src/main/resources/config/product-service.yml](configserver/src/main/resources/config/product-service.yml#L48-L60)
- [product/src/main/resources/application.yml](product/src/main/resources/application.yml#L1-L4)
- [product/pom.xml](product/pom.xml#L1-L40)
- [gateway/pom.xml](gateway/pom.xml#L1-L40)
- [payment/src/main/resources/application.yml](payment/src/main/resources/application.yml#L1-L30)
- [notification/src/main/resources/application.yml](notification/src/main/resources/application.yml#L1-L30)

If you want, I can: (a) consolidate all `eureka.client` settings into the Config Server and update service `application.yml` files to import the config server, or (b) list which services currently use the Config Server vs local config. Which would you like next?

**Architecture Diagram (Mermaid)**
Below diagram shows how services fetch configuration and register with Eureka in this project.

```mermaid
flowchart LR
	CS[Config Server\n(port 8888, native)]
	E[ Eureka Server\n(port 8761) ]
	P[Product Service]
	G[API Gateway]
	Pay[Payment Service]
	N[Notification Service]
	O[Order Service]
	U[User Service]
	LF[(local YAML files\nconfigserver/src/main/resources/config/)]

	P -->|register| E
	G -->|register| E
	Pay -->|register| E
	N -->|register| E
	O -->|register| E
	U -->|register| E

	P -->|fetch config| CS
	G -->|fetch config| CS
	Pay -.->|uses local config| LF
	N -.->|uses local config| LF

	CS -->|serves files from| LF
	CS -.->|not registered with| E

	classDef srv fill:#f9f,stroke:#333,stroke-width:1px;
	class P,G,Pay,N,O,U srv;
```

Use this to visualize startup ordering: start `configserver` and `eureka` before services that depend on them.
