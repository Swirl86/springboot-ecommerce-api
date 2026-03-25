# Spring Boot E-commerce API

![CI](https://github.com/Swirl86/springboot-ecommerce-api/actions/workflows/ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen)

**ecom-engine** is a backend e‑commerce API built with Spring Boot. The project is developed step by step with a focus on clean architecture, modular design, and modern Java practices. The goal is to strengthen my understanding of backend development by building a real-world style API from the ground up.

This project starts simple and will grow gradually, with many small and clear commits that document the entire development process.

---

## 📚 Project Goals

- Improve backend development skills in Java
- Practice clean and modular architecture
- Build a realistic API with real-world patterns
- Maintain a clear and readable commit history
- Grow the project gradually in small steps
  
---

## 📦 Technologies
| Layer / Concern        | Technologies                                  |
| ---------------------- | ---------------------------------------------- |
| Language               | Java 17                                        |
| Framework              | Spring Boot 3.2.5                              |
| API                    | Spring Web (Spring MVC), Springdoc OpenAPI     |
| Architecture           | Layered design (Controller → Service → Repo)   |
| Data                   | H2 (dev), PostgreSQL (planned), Spring Data JPA |
| Auth (planned)         | Spring Security, JWT                           |
| Build & Dependency     | Maven                                          |
| Utilities              | Lombok, Spring Boot DevTools                   |
| Testing                | JUnit 5, Mockito, Spring Boot Test             |
| Tooling                | IntelliJ IDEA                                  |

---

## 🚀 Features

### ✅ Implemented

<details>
  <summary><strong>Product Domain</strong></summary>

- Product entity (JPA)
- ProductRequest & ProductResponse DTOs
- ProductService with CRUD logic
- ProductController (REST API)
- Unit tests (JUnit + Mockito)
- Integration tests (Spring Boot Test + TestRestTemplate)

</details>

<details>
  <summary><strong>Category Domain</strong></summary>

- Category entity (JPA)
- CategoryRequest & CategoryResponse DTOs
- CategoryService with CRUD logic
- CategoryController (REST API)
- Integration with Product domain
- Controller tests + integration tests

</details>

<details>
  <summary><strong>API Documentation</strong></summary>

- Added **OpenApiConfig** with API metadata
- Swagger UI enabled via **Springdoc OpenAPI**
- Controllers documented with:
  - `@Tag`
  - `@Operation`
  - `@ApiResponses`
- Easy-to-navigate interactive API documentation

📄 **Swagger UI:**  
http://localhost:8080/swagger-ui.html

</details>
  
##

### 🛠️ Planned

<details>
  <summary><strong>Product Enhancements</strong></summary>

- Pagination & sorting
- Search & filtering

</details>

<details>
  <summary><strong>User & Authentication</strong></summary>

- User model
- Registration & login
- JWT authentication
- Role-based authorization

</details>

<details>
  <summary><strong>Shopping Flow</strong></summary>

- Cart
- Orders
- Checkout flow

</details>

<details>
  <summary><strong>Data & Persistence</strong></summary>

- PostgreSQL support

</details>

<details>
  <summary><strong>Testing Improvements</strong></summary>

- More integration tests
- Test factories/builders
- Manual API testing with Postman

</details>

<details>
  <summary><strong>Tooling & Dev Experience</strong></summary>

- Docker support
- CI pipeline (GitHub Actions)
- Extended API documentation (schemas, examples)

</details>

---

## 📘 API Documentation (Swagger / OpenAPI)

This project uses **Springdoc OpenAPI** to automatically generate API documentation.

- Interactive UI  
  👉 http://localhost:8080/swagger-ui.html

- Configured in  
  `OpenApiConfig.java`

- Controllers are documented using:
  - `@Tag` for grouping
  - `@Operation` for summaries/descriptions
  - `@ApiResponses` for status codes

This makes the API easy to explore, test, and understand.

---

## 🧪 Testing

- Unit tests for services and controllers  
- Integration tests for full request → database flow  
- Category and Product domains fully covered  
- Uses:
  - `@WebMvcTest`
  - `@SpringBootTest`
  - `TestRestTemplate`
  - Mockito

### 📋 Useful commands to run tests from terminal

| Command                         | Description                                      |
|---------------------------------|--------------------------------------------------|
| `mvn test`                      | Runs all unit and integration tests              |
| `mvn verify`                    | Full build pipeline (compile + test + verify)    |
| `cd ecom-engine && mvn test`    | Runs tests inside the ecom-engine module         |

---

## 🧱 Architecture Overview

Controller → Service → Repository → Database
DTOs ↔ Entities

GlobalExceptionHandler for consistent error responses

The project follows a clean, layered structure to keep logic separated and maintainable.

