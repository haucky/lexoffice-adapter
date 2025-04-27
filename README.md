# lexoffice-adapter
### What are we building?
- The service acts as a middleware to mediate between the imaginary ContactSync Software and the Lexoffice API. The
  middleware is not just a 1:1 proxy but also takes the form of
  - an adapter, a consumer/client-driven contract which exposes only a subset of the contact aggregate
  - some type of aggregation service, resulting in a new composite contact object with the expanded country code
  - some type of anti-corruption layer in the sense that it abstracts away the legacy error format and instead exposes
    a Problem JSON (RFC-7807)
- In this codebase the middleware is just called _**adapter**_ and the Lexoffice API either **_Lexoffice_** or **_upstream API_**

## Local setup
```bash
docker build -t lexoffice-adapter -f deployment/Dockerfile .
docker run -p 8080:8080 -e LEXOFFICE_API_TOKEN=<your_token> lexoffice-adapter
```
- Required env: API Token for Lexoffice API (`LEXOFFICE_API_TOKEN`)
- Optional env:  `SHARED_JWT_SECRET`. For additional context have a look the security considerations below.
- To get started have a look at the [`http/`](http/), some request files already configured against local/prod environment

## Assumptions & Decisions
### What about the domain boundaries?

- Since the adapter does apply very little domain logic, I decided against the separation of DTOs and domain objects. For
  request/response body of both adapter and Lexoffice API one shared set of DTOs was used. I only duplicated DTOs
  when they were diverging, in the case of the country code / name. Some people might argue, you should have a
  completely separated set of DTOs since they belong to two different contracts, but in this case I disagree
  because the middleware is
  subset of the Lexoffice API (apart from error handling which also have a separate set of DTOs).
- There is two places where error codes are managed. In the `GlobalExceptionHandler` and in the `LexofficeErrorDecoder`.
  I've decided for this approach because I wanted to have a clear separation between the errors originating from
  upstream (type `LexofficeException`) vs from the adapter (type `AdapterException` and all other default exceptions
  e.g. `MethodArgumentNotValidException`).`GlobalExceptionHandler` handles all exceptions but Lexoffice related
  exceptions are just wrapped into Problem JSON with status codes already defined in the `LexofficeErrorDecoder`.

### What about the API design?

- Contrary to the Lexoffice API, incoming requests to the middleware are strictly validated regarding the structure.
  This is a explicit decision, breaking Postel's Law. Semantically, request are validated but only for simple
  validations (like data types or size limits). We do not validate any more elaborate business logic since we do not
  want introduce logical coupling to the Lexoffice API ('Smart endpoints, dumb pipes'). The same can be done for incoming responses of the Lexoffice API (not done yet), with the exception that we do also allow
  unknown fields (we would break the adapter/client whenever the Lexoffice API would be extended).

- Most of my time I spent on handling different error types/codes (this is still far from a production-ready state)
  since I did not just want to forward everything from the upstream API. A pure proxy would have been a lot quicker to
  implement.

- For this task, it is assumed that no person-typed contacts are in the dataset or added via the Lexoffice API. The
  adapter does not allow this. Accounting for two different contact types would make pagination a bit more complicated
  since the adapter could not delegate pagination to upstream. The adapter would need to implement some kind of buffer
  where person-typed contacts are filtered out and at the same time dynamically load more pages till the requested page
  size of company-typed contacts is met. This seems very clunky to me. It would be probably worth it to request an
  additional type filter feature in the upstream Lexoffice API. Same goes for the archived flag. Since the adapter does
  not support the concept of archiving, we do not filter for it.

### What about Security?

- The middleware is not directly frontend facing (and not public) but interacts with the ContactSync backend.
  Therefore, I do not need to worry about CORS/Cookies and other related frontend-related protections. The backend
  authenticates/authorizes with the adapter through client credentials / JWT Token. According to least-privilege
  principle the backend only has access to the contact resource and not the metrics (that scope would only be given to
  some metrics collection service like e.g. Prometheus) or cache invalidation (that scope would be given to an admin
  role for example)

- For 'simulating' an oauth flow, the `AuthorizationController` provides JWT tokens for user client (with scope
  read/write) and admin client (with scope admin). In an oauth setting this would be a separate authorization service,
  not sharing the same secret (instead asymmetric approach), and you would
  of course authenticate before retrieving the tokens. This setup is very basic and not near production ready.

- Same applies for integration with the Lexoffice API. Currently the adapter impersonates my personal test account to
  interact with the Lexoffice API. In real life, considering the Partner/Integration scenario, we are working in, you
  would have for example an oauth2 flow to access user specific data.

### What about Performance?

- The fact that Lexoffice is a B2C product for freelancers and small companies and resource access is always
  user/company specific we can assume that caching access to single contacts or companies is probably not worth it. This
  is not the case for the country code expansion.

### What about metrics?

- I just did have time to add some basic Spring Actuator metrics and measure the two different types of latency
    - End-to-end request latency: How much time takes the application from incoming to outgoing request. This already comes out of the box:
      - `/actuator/metrics/http.server.requests?tag=uri:%2Fv1%2Fcontacts%2F%7Bid%7D` (`GET` single contact by id)
      - `/actuator/metrics/http.server.requests?tag=uri:/v1/contacts` (`GET` all contacts)
      - `/actuator/metrics/http.server.requests?tag=uri:/v1/contacts&tag=method:POST` (`POST` single contact)
    - Outbound call latency: How long does it take to call and receive a response to the client?
      - `/actuator/metrics/outbound.lexoffice.contacts.create`
      - `/actuator/metrics/outbound.lexoffice.contacts.getById`
      - `/actuator/metrics/outbound.lexoffice.contacts.getAll`
    - Those latencies are much more useful when properly aggregated (e.g. p50/p99) for instance with Prometheus (endpoint is already exposed)
    - Remember that metrics are lazily initialized, first make a request before you check metrics
- Other common metrics (but not implemented)
    - Throughput (RPS)
    - Error rate (within adapter and upstream)
    - Client patterns (request size, which client)
    - Parallel connections (already out of the box)
    - JVM metrics (already out of the box)

### What about deployment/CI/CD?
- Is deployed on a private Hetzner VM and accessible under https://lexoffice-adapter.bulbt.com/
- CI/CD is GitHub Actions seen under `.github/workflows/deploy.yaml`
- Secrets injected into pipeline via GitHub Secrets
- Steps
  - Running tests
  - Pushing code to `dokku` for further build and deploy steps
- Things to consider for a more production ready environment
  - DevSecOps (SAST/DAST/IAST, vulnerability scanning for container images, secret scanning like talisman)
  - Introduce different environments like dev, staging, prod
  - Blue/Green and Rolling Deployment
  - E2E Testing against real Lexoffice environment
  - Compile documentation and host them as static site
  - Contract tests (for official integration partners)

### Testing strategy?

- Followed the testing pyramid, testing as much as possible at a unit test level. Not too heavy controller tests, mostly
  testing validation errors and their format. One integration test for happy path (per endpoint) and one or
  more important sad paths. I'm still not super happy with the test suite, could be more and better tests.

### What would I do with more time?

- Even more testing (for instance authorization/scopes are not tested yet)
- Think and discuss more about proper status codes
- More elaborate CI/CD pipeline also with E2E tests
- Tracing
- Rate limiting (or as suggested a token bucket algorithm on adapter side)
- Circuit breaker with exponential backoff
- Apart from injecting Lexoffice Api token at runtime, also inject Lexoffice host url and adapter ports at build time

### What else is important?

- I did leave some TODO comments on purpose, for discussion or things I plan to do
- For the OpenAPI docs I explain some reasons, exceptions for each status code. In a real-life product I would not leak
  information like internal exceptions to the API consumer