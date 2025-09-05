@echo off
echo Building Domain Checker Application...
echo.
mvn clean package -DskipTests
echo.
echo Build complete! Starting application...
echo.
echo The application will be available at: http://localhost:8080
echo.
echo Press Ctrl+C to stop the application
echo.
java -jar target\domain_checker.jar