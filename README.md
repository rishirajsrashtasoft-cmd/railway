# Domain Checker Application

A Spring Boot application that checks domain reachability, HTTP status, and security using VirusTotal API.

## Features

- **Ping Check**: Tests if a domain is reachable using `InetAddress.isReachable()`
- **HTTP Status Check**: Sends HTTP GET requests to check response status codes (tries HTTP first, then HTTPS)
- **Security Check**: Uses VirusTotal API to check domain safety with your API key
- **Database Storage**: Stores all check results in MySQL database
- **Web Interface**: Beautiful responsive UI built with Thymeleaf and Bootstrap
- **Bulk Checking**: Check multiple domains at once (like your PowerShell script)
- **Exports**: Download results as CSV, PDF, or Word
- **REST API**: JSON endpoints for programmatic access
- **Windows Helpers**: Batch/PowerShell scripts and desktop shortcut launcher

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- VirusTotal API key (optional, for security checks)

## Setup Instructions

### 1. Database Setup

Create a MySQL database:

```sql
CREATE DATABASE domain_checker;
CREATE USER 'domain_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON domain_checker.* TO 'domain_user'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Configuration

Edit `src/main/resources/application.properties`:

```properties
# Update with your MySQL credentials
spring.datasource.url=jdbc:mysql://localhost:3306/domain_checker
spring.datasource.username=domain_user
spring.datasource.password=your_password

# VirusTotal API key
virustotal.api.key=YOUR_VIRUSTOTAL_API_KEY
virustotal.api.url=https://www.virustotal.com/api/v3/domains/

# HTTP client timeouts (milliseconds)
http.client.timeout=5000
http.client.connection-timeout=3000

# Optional basic UI credentials (if used)
app.security.user=admin
app.security.pass=admin123

# Server port
server.port=8080
```

### 3. Build and Run (Maven)

```bash
# Build the application
mvn clean compile

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 4. Run as JAR

```bash
mvn clean package -DskipTests
java -jar target/domain_checker.jar
```

### 5. Windows helper scripts (root folder)

- `build-and-run.bat` — builds the JAR and starts the app
- `run-jar.bat` — runs the already built JAR
- `domain-checker-launcher.bat` — launcher used by desktop shortcut
- PowerShell helpers to create shortcuts: `create-desktop-shortcut.ps1`, `create-jar-shortcut.ps1`, `make-shortcut.ps1`, `create-shortcut-launcher.ps1`

Desktop shortcut flow: shortcut → launcher BAT → runs `java -jar target\domain_checker.jar`.

### 6. Docker (optional)

Example multi-stage image:

```dockerfile
# Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/domain_checker.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
```

Build and run:

```bash
docker build -t domain-checker .
docker run -p 8080:8080 -e virustotal.api.key=YOUR_VT_KEY domain-checker
```

## Usage

### Web Interface

1. **Home Page** (`/`): Enter a domain name and click "Check Domain"
2. **Bulk Check Page** (`/bulk`): Enter multiple domains (like your PowerShell script's domains.txt)
3. **Results Page** (`/domains/list`): View all domain check results with statistics
4. Export buttons on the results page: CSV, PDF, Word

### REST API Endpoints

- `POST /domains` - Submit domain for checking (form data)
- `GET /domains/list` - View all results (HTML)
- `GET /domains/{id}` - Get specific result (JSON)
- `POST /api/check?domain=example.com` - Check domain via API (JSON response)
- `POST /domains/{id}/delete` - Delete a result

### Example API Usage

```bash
# Check a domain via API
curl -X POST "http://localhost:8080/api/check?domain=google.com"

# Response example:
{
  "id": 1,
  "domain": "google.com",
  "reachable": true,
  "httpStatus": 200,
  "virusCheck": "Safe",
  "createdAt": "2024-01-15T10:30:00"
}
```

## Project Structure

```
src/
├── main/
│   ├── java/com/example/domainchecker/
│   │   ├── DomainCheckerApplication.java       # Main Spring Boot application
│   │   ├── entity/
│   │   │   └── DomainResult.java               # JPA entity for domain results
│   │   ├── repository/
│   │   │   └── DomainResultRepository.java     # JPA repository interface
│   │   ├── service/
│   │   │   └── DomainCheckerService.java       # Business logic for domain checking
│   │   └── controller/
│   │       └── DomainController.java           # Web controllers and REST endpoints
│   └── resources/
│       ├── application.properties              # Application configuration
│       └── templates/
│           ├── index.html                      # Domain input form
│           ├── domain-list.html                # Results display table
│           └── bulk-check.html                 # Bulk input page
├── build-and-run.bat                           # Build then run
├── run-jar.bat                                 # Run packaged JAR
├── domain-checker-launcher.bat                 # Desktop launcher
├── create-desktop-shortcut.ps1                 # Create launcher shortcut
├── create-jar-shortcut.ps1                     # Create direct JAR shortcut
├── make-shortcut.ps1                           # Alternate shortcut helper
├── create-shortcut-launcher.ps1                # Launcher shortcut helper
├── Dockerfile                                  # Container build (optional)
└── test/
    └── java/                                   # Test classes (placeholder)
```

## Documentation

Detailed docs are available in the `documentation` folder:
- [Overview](documentation/overview.md)
- [Setup & Configuration](documentation/setup.md)
- [Usage](documentation/usage.md)
- [How it works](documentation/how-it-works.md)
- [Exports](documentation/exports.md)
- [Selenium](documentation/selenium.md)
- [Troubleshooting](documentation/troubleshooting.md)
- [FAQ](documentation/faq.md)
- [Windows Scripts & Shortcuts](documentation/windows-scripts.md)

## Implementation Details

### Domain Checking Logic

1. **Ping Check**: Uses `InetAddress.isReachable()` with 3-second timeout
2. **HTTP Check**: Attempts HTTP first, then HTTPS if needed
3. **VirusTotal Check**: Calls VirusTotal API v3 to get domain reputation

Exports:
- CSV via OpenCSV
- PDF via iText 7
- Word via Apache POI (XWPF)

## How it works (end‑to‑end)

1. A user submits a domain from the home page (or multiple domains from Bulk).
2. `DomainCheckerService` cleans the input (strips protocol/path) and performs checks:
   - Ping reachability via `InetAddress.isReachable()`
   - HTTP status via `HttpURLConnection` (tries HTTP then HTTPS)
   - Optional VirusTotal v3 lookup when `virustotal.api.key` is set
   - A quick curl-style check using Java `HttpClient` for latency/status summary
   - Best-effort headless Selenium load (title, page source length, load time)
3. A `DomainResult` entity is saved to MySQL with all captured fields and timestamps.
4. The results table (`/domains/list`) queries DB, applies in-memory filters and sorting, paginates, and shows quick stats (reachable/safe/unsafe).
5. Export actions (`/export/csv`, `/export/pdf`, `/export/word`) render the currently filtered list into the chosen format.

### Data lifecycle
- New checks create rows in `domain_results` with fields for ping, HTTP, VirusTotal, curl summary, and Selenium.
- Bulk checks loop through lines, ignoring blanks/quotes/tabs, and store each result.
- Delete actions remove specific rows by `id`.
- Filters/pagination do not mutate data; exports reflect the filtered view. The curl summary may be refreshed at export time to reflect current connectivity.

### Exports: columns and meaning
- CSV includes: Domain, Reachable, HTTP Status, Curl Check, Virus Check, Checked At, Virus Details, Selenium Reachable, Selenium Title, Selenium Source Length, Selenium Load Time (ms), Selenium Error.
- PDF/Word include a summary header and a compact table with Selenium columns. PDF also appends VirusTotal flagging details inline when present.

### Selenium check (optional)
- Requires Chrome/Chromium and a compatible driver (Selenium Manager can auto-manage).
- Runs headless with `--headless=new` and captures: navigation success, page title, source length, and elapsed time.
- Failures are non-fatal and recorded in `seleniumError`; other checks still proceed.

### Filters, sorting, pagination
- Filters: by domain substring, reachability (yes/no), safety (safe/unsafe via VirusTotal text), and date range.
- Sorting: by `createdAt` ascending/descending (default desc).
- Pagination: 50 items per page with total counters and page controls.

### Desktop shortcut and scripts
- Shortcuts call `domain-checker-launcher.bat`, which runs `java -jar target\domain_checker.jar`.
- Helper PowerShell scripts in the repo create shortcuts for the launcher or JAR.
- Use `build-and-run.bat` to compile and start the app; `run-jar.bat` to run an existing JAR.

### Security and API keys
- Set `virustotal.api.key` in `application.properties` or as an environment variable. Respect API rate limits.
- Server listens on `server.port` (8080 by default). If exposing externally, put it behind a reverse proxy and enable auth as needed.

## FAQ

- Why does ping show No but HTTP works? Many hosts block ICMP ping while allowing HTTP.
- Why is VirusTotal "Not Checked"? Add a valid API key or you exceeded rate limits.
- PDF/Word export is large/slow: Reduce page size with filters or skip Selenium columns if you customize the templates.
- Selenium fails locally: Ensure Chrome is installed and on PATH; update drivers; try disabling headless to debug.

### Database Schema

The `domain_results` table contains:
- `id` (Primary Key)
- `domain` (VARCHAR) - The domain name
- `reachable` (BOOLEAN) - Ping result
- `http_status` (INT) - HTTP response code
- `virus_check` (VARCHAR) - Security check result
- `created_at` (TIMESTAMP) - When the check was performed

### Security Features

- Input validation and sanitization
- SQL injection protection via JPA
- XSS protection in templates
- API rate limiting considerations

## Troubleshooting

### Common Issues

1. **Database Connection Error**
   - Verify MySQL is running
   - Check credentials in `application.properties`
   - Ensure database exists

2. **VirusTotal API Issues**
   - Verify API key is valid
   - Check your account's API rate limits/quotas (per-minute and daily)
   - Ensure internet connectivity
   - Test with curl to see exact status and headers:
     ```bash
     curl -s -D - -H "x-apikey: YOUR_KEY" -H "Accept: application/json" \
       "https://www.virustotal.com/api/v3/domains/example.com"
     ```
     - 200: OK
     - 401: Invalid/revoked key
     - 403/429: Permission/quota/rate limit
     - 5xx/timeout: transient/network issues

3. **Domain Not Reachable**
   - Some domains block ping requests
   - Firewall may block outgoing connections
   - DNS resolution issues

### Logs

Check application logs for detailed error information:
```bash
# Enable debug logging
logging.level.com.example.domainchecker=DEBUG
```

## Optional: Daily automation

If you need unattended daily re-checks:
- Use Windows Task Scheduler to call a secured internal job endpoint or run the JAR with a batch mode argument (e.g., `--job=daily`).
- Respect VirusTotal rate limits with small delays and retries on 429/5xx.
- De-duplicate alert emails (send only on state change or once per day).

## License

This project is provided as-is for educational and demonstration purposes.

## Contributing

Feel free to submit issues and pull requests to improve the application.
