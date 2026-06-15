# Performance Testing

## MySQL Integration Test

Docker Desktop must be running. The test starts a disposable MySQL 8 container,
loads the full application context, creates the schema, seeds chat data, and
verifies both response behavior and SQL query count.

```powershell
.\gradlew.bat integrationTest
```

The regular unit test task excludes Docker-based integration tests:

```powershell
.\gradlew.bat test
```

## Chat Message Load Test

Start the backend with a MySQL database and prepare a user, access token, and
chat session containing representative messages.

Start local MySQL and Redis:

```powershell
docker compose -f monitoring/docker-compose.local-backend.yml up -d
```

Start the backend with the `performance` profile. The explicit seed flag creates
a test user, a session, and 200 messages, then logs `SESSION_ID` and
`ACCESS_TOKEN`.

```powershell
$env:MYSQL_URL="localhost"
$env:MYSQL_PORT="3307"
$env:MYSQL_DB="tinypay"
$env:MYSQL_USER="tinypay"
$env:MYSQL_PASSWORD="tinypay"
$env:REDIS_HOST="localhost"
$env:REDIS_PORT="6380"
$env:JWT_SECRET="01234567890123456789012345678901"
$env:AWS_ACCESS_KEY="test"
$env:AWS_SECRET_KEY="test"
$env:S3_BUCKET_NAME="test"
$env:GOOGLE_CLIENT_ID="test"
$env:COOLSMS_API_KEY="test"
$env:COOLSMS_API_SECRET="test"
$env:COOLSMS_FROM_NUMBER="01000000000"
$env:DIFY_BASE_URL="http://localhost"
$env:DIFY_CHAT_ANALYSIS_API_KEY="test"
$env:DIFY_SERVICE_EXECUTION_API_KEY="test"
$env:BLOCKCHAIN_RPC_URL="http://localhost:8545"
$env:MOCK_USDC_ADDRESS="0x0000000000000000000000000000000000000001"
$env:TINY_PAYMENT_ADDRESS="0x0000000000000000000000000000000000000002"
$env:SERVER_WALLET_ADDRESS="0x0000000000000000000000000000000000000003"
$env:SERVER_WALLET_PRIVATE_KEY="1111111111111111111111111111111111111111111111111111111111111111"
$env:RECEIVER_WALLET_ADDRESS="0x0000000000000000000000000000000000000004"
$env:WALLET_ENCRYPTION_KEY="AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
$env:SPRING_PROFILES_ACTIVE="performance"
$env:PERFORMANCE_SEED_ENABLED="true"

.\gradlew.bat bootRun
```

Start Prometheus and Grafana:

```powershell
docker compose -f monitoring/docker-compose.yml up -d
```

Verify:

- Prometheus target: http://localhost:9090/targets
- Grafana: http://localhost:3000
- Grafana login: `admin` / `admin`
- Dashboard: `TinyPay / TinyPay Performance`

Run k6 through Docker and send k6 metrics to Prometheus:

```powershell
$env:BASE_URL="http://host.docker.internal:8080"
$env:ACCESS_TOKEN="<access-token>"
$env:SESSION_ID="<session-id>"

docker run --rm `
  --network monitoring_default `
  -e BASE_URL=$env:BASE_URL `
  -e ACCESS_TOKEN=$env:ACCESS_TOKEN `
  -e SESSION_ID=$env:SESSION_ID `
  -e K6_PROMETHEUS_RW_SERVER_URL="http://prometheus:9090/api/v1/write" `
  -e K6_PROMETHEUS_RW_TREND_STATS="p(95),p(99),min,max" `
  -v "${PWD}/load-tests:/scripts" `
  grafana/k6 run -o experimental-prometheus-rw /scripts/chat-messages.js
```

For a safe first run, add `-e SMOKE=true`. This runs 5 virtual users for 30
seconds instead of the full ramping scenario.

Default acceptance thresholds:

- HTTP error rate below 1%
- p95 response time below 500 ms
- p99 response time below 1 second

Run the scenario against a non-production environment. Compare p95, p99,
request rate, database CPU, connection pool usage, and slow queries before and
after performance changes.

The local Grafana and Prometheus ports bind only to `127.0.0.1`. The backend
`/actuator/prometheus` endpoint does not require JWT so Prometheus can scrape
it. In shared or production environments, restrict this endpoint with a private
network, firewall, or a dedicated management port.

Stop monitoring services:

```powershell
docker compose -f monitoring/docker-compose.yml down
```
