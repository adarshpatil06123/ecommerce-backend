@echo off
echo ================================
echo Starting All Microservices
echo ================================
echo.

echo Starting Auth Service (Port 8081)...
start "Auth Service" cmd /k "cd /d %~dp0auth-service && mvn -q spring-boot:run -Dspring-boot.run.profiles=dev"
timeout /t 5 /nobreak >nul

echo Starting Product Service (Port 8082)...
start "Product Service" cmd /k "cd /d %~dp0product-service && mvn -q spring-boot:run -Dspring-boot.run.profiles=dev"
timeout /t 5 /nobreak >nul

echo Starting Order Service (Port 8083)...
start "Order Service" cmd /k "cd /d %~dp0order-service && mvn -q spring-boot:run -Dspring-boot.run.profiles=dev"
timeout /t 5 /nobreak >nul

echo Starting Payment Service (Port 8084)...
start "Payment Service" cmd /k "cd /d %~dp0payment-service && mvn -q spring-boot:run -Dspring-boot.run.profiles=dev"
timeout /t 5 /nobreak >nul

echo.
echo ================================
echo All services are starting...
echo Please wait 30-45 seconds for all services to be ready
echo ================================
echo.
echo Service URLs:
echo Auth Service:    http://localhost:8081
echo Product Service: http://localhost:8082
echo Order Service:   http://localhost:8083
echo Payment Service: http://localhost:8084
echo.
pause
