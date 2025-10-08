# Setup & Configuration

## Prerequisites
- Java 17+
- Maven 3.6+
- MySQL 8.0+
- VirusTotal API key (optional for security checks)

## Database
```sql
CREATE DATABASE domain_checker;
CREATE USER 'domain_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON domain_checker.* TO 'domain_user'@'localhost';
FLUSH PRIVILEGES;
```

## Application properties
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/domain_checker
spring.datasource.username=domain_user
spring.datasource.password=your_password

virustotal.api.key=YOUR_VIRUSTOTAL_API_KEY
virustotal.api.url=https://www.virustotal.com/api/v3/domains/

http.client.timeout=5000
http.client.connection-timeout=3000

app.security.user=admin
app.security.pass=admin123
server.port=8080
```

## Build & run
```bash
mvn clean compile
mvn spring-boot:run
```
OR
```bash
mvn clean package -DskipTests
java -jar target/domain_checker.jar
```

