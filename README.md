# 🛍️ E-Commerce Backend (Microservices Architecture)

## 📌 Overview

This project is a **production-ready, microservices-based E-Commerce backend** built with **Java 17** and **Spring Boot 3.2.0**.
It implements order creation, inventory management, and payment tracking using a clean modular structure and real-world technologies like **MySQL**, **Redis**, **Kafka**, **AWS S3**, **Eureka**, and **Spring Cloud Gateway**.

> 🧠 Designed to demonstrate enterprise-grade backend architecture — scalable, fault-tolerant, and fully Dockerized.

---

## 🧩 Microservices Architecture

| Service             | Port   | Responsibility                                               |
| ------------------- | ------ | ------------------------------------------------------------ |
| **Auth Service**    | `8081` | User registration, JWT authentication, and login             |
| **Product Service** | `8082` | Product catalog, CRUD, stock management, AWS S3 image upload |
| **Order Service**   | `8083` | Order creation, validation, inter-service communication      |
| **Payment Service** | `8084` | Payment simulation and event-driven processing via Kafka     |
| **Eureka Server**   | `8761` | Service discovery and registration                           |
| **API Gateway**     | `8080` | Single entry point routing to all services                   |

---

## ⚙️ Tech Stack

* **Language:** Java 17
* **Framework:** Spring Boot 3.2.0
* **Databases:** MySQL (one per service)
* **Authentication:** JWT + Spring Security
* **Caching:** Redis (10-minute TTL)
* **Event System:** Apache Kafka + Zookeeper
* **File Storage:** AWS S3 (image uploads)
* **Service Discovery:** Spring Cloud Netflix Eureka
* **API Gateway:** Spring Cloud Gateway
* **Containerization:** Docker + Docker Compose
* **Build Tool:** Maven

---

## 🧱 Project Structure

```
ecommerce-backend/
│
├── auth-service/
├── product-service/
├── order-service/
├── payment-service/
│
├── eureka-server/
├── gateway-service/
│
├── common/                  # Shared DTOs, Enums, Exceptions
├── docker-compose.yml       # Full environment orchestration
├── .env                     # Environment variables (excluded from repo)
└── README.md
```

---

## 🚀 Features

✅ Modular **microservices** with independent databases
✅ **JWT Authentication** for secure API access
✅ **AWS S3 integration** for product image uploads
✅ **Redis caching** for product performance (90–95% faster reads)
✅ **Kafka event-driven flow**: asynchronous order → payment processing
✅ **Spring Cloud Eureka** for service discovery
✅ **API Gateway** central routing & filtering
✅ **Dockerized environment** for one-command startup
✅ **Comprehensive documentation** (`AWS_S3_SETUP.md`, `REDIS_SETUP.md`, `KAFKA_SETUP.md`, `DOCKER_SETUP.md`)

---

## 🧰 Setup Instructions

### 1️⃣ Prerequisites

* Java 17
* Maven 3.9+
* Docker Desktop (Running)
* AWS Account (for S3 integration)

### 2️⃣ Environment Variables (`.env`)

Create a `.env` file at the root:

```
MYSQL_ROOT_PASSWORD=root
AWS_ACCESS_KEY_ID=your_aws_access_key
AWS_SECRET_ACCESS_KEY=your_aws_secret_key
JWT_SECRET=your_secret
```

### 3️⃣ Build the project

```bash
mvn clean package -DskipTests
```

### 4️⃣ Start with Docker

```bash
docker compose up -d --build
```

### 5️⃣ Verify services

Visit:

* Eureka Dashboard → [http://localhost:8761](http://localhost:8761)
* Gateway API → [http://localhost:8080](http://localhost:8080)

---

## 🧪 Testing APIs (via Gateway)

Example routes:

| Action               | Method | Endpoint                      |
| -------------------- | ------ | ----------------------------- |
| Register user        | POST   | `/auth/register`              |
| Login user           | POST   | `/auth/login`                 |
| Create product       | POST   | `/products`                   |
| Get all products     | GET    | `/products`                   |
| Upload product image | POST   | `/products/{id}/upload-image` |
| Create order         | POST   | `/orders`                     |
| Get user orders      | GET    | `/orders/user/{userId}`       |
| Make payment         | POST   | `/payments`                   |

Use Postman or curl commands from `TESTING.
## 📄 License

This project is licensed under the MIT License.
