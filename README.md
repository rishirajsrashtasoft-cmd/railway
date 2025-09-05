# Domain Checker Application

A Spring Boot application that checks domain reachability, HTTP status, and security using VirusTotal API.

## Features

- **Ping Check**: Tests if a domain is reachable using `InetAddress.isReachable()`
- **HTTP Status Check**: Sends HTTP GET requests to check response status codes (tries HTTP first, then HTTPS)
- **Security Check**: Uses VirusTotal API to check domain safety with your API key
- **Database Storage**: Stores all check results in MySQL database
- **Web Interface**: Beautiful responsive UI built with Thymeleaf and Bootstrap
- **Bulk Checking**: Check multiple domains at once (like your PowerShell script)
- **REST API**: JSON endpoints for programmatic access

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

# VirusTotal API key is already configured from your PowerShell script
virustotal.api.key=5830515a915de1fbf030c26f92a0eb22c85dac98ce2de397d080f24c52aaa7d3
```

### 3. Build and Run

```bash
# Build the application
mvn clean compile

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Usage

### Web Interface

1. **Home Page** (`/`): Enter a domain name and click "Check Domain"
2. **Bulk Check Page** (`/bulk`): Enter multiple domains (like your PowerShell script's domains.txt)
3. **Results Page** (`/domains/list`): View all domain check results with statistics

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
│           └── domain-list.html                # Results display table
└── test/
    └── java/                                   # Test classes (placeholder)
```

## Implementation Details

### Domain Checking Logic

1. **Ping Check**: Uses `InetAddress.isReachable()` with 3-second timeout
2. **HTTP Check**: Attempts HTTPS first, falls back to HTTP if needed
3. **VirusTotal Check**: Calls VirusTotal API v3 to get domain reputation

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
   - Check API rate limits (500 requests/day for free tier)
   - Ensure internet connectivity

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

## License

This project is provided as-is for educational and demonstration purposes.

## Contributing

Feel free to submit issues and pull requests to improve the application.


Ah, I understand! You want to know how to update the JAR file and desktop shortcut after making code changes. Here's the process:
🔄 After Making Code Changes - Update JAR & Shortcut:
Step 1: Rebuild the JAR File

# This rebuilds domain_checker.jar with your new code
mvn clean package -DskipTests
Step 2: The JAR is Automatically Updated
The file target\domain_checker.jar gets replaced with your new code
Your desktop shortcut automatically uses the new JAR (no changes needed!)
Step 3: Test the Updated Application
# Double-click your desktop shortcut
# OR run manually:
.\run-jar.bat
🎯 Simple Workflow:
1. ✏️  Change code in any .java or .html file
2. 🔨  Run: mvn clean package -DskipTests  
3. 🚀  Double-click desktop shortcut
4. ✅  Your changes are live!
⚡ Even Easier - Use the Build & Run Script:
# This does BOTH steps at once:
# 1. Rebuilds the JAR with your changes
# 2. Starts the application immediately
.\build-and-run.bat
🔧 The Magic:
Desktop shortcut → Points to domain-checker-launcher.bat
Launcher → Runs java -jar target\domain_checker.jar
JAR file → Gets replaced when you run mvn package
Result → Desktop shortcut automatically uses your updated code!
📋 No Need to Recreate Shortcut Because:
✅ Shortcut points to the launcher script
✅ Launcher script points to domain_checker.jar
✅ JAR file gets replaced with new version
✅ Same filename, new code inside!
You only need to rebuild the JAR - the shortcut stays the same forever! 🎉
