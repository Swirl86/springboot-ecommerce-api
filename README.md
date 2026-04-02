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
| Auth                   | Spring Security 6, JWT (custom implementation) |
| Build & Dependency     | Maven                                          |
| Utilities              | Lombok, Spring Boot DevTools                   |
| Testing                | JUnit 5, Mockito, Spring Boot Test (MockMvc)   |
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
  <summary><strong>Authentication & Security</strong></summary>

- User entity & Role model
- Registration & login endpoints
- JWT generation & validation (custom implementation)
- JwtAuthenticationFilter
- Role-based authorization (RBAC)
- Custom AccessDeniedHandler & AuthenticationEntryPoint
- waggerSecurityConfig (Swagger allowed only in dev)
- SecurityRules for centralized endpoint management
- AuthController + AuthService
- Controller tests + service tests

</details>

<details>
<summary><strong>Cart Domain</strong></summary>

- Cart & CartItem entities
- CartService with add/remove/update logic
- CartController (REST API)
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
  <summary><strong>Shopping Flow</strong></summary>

- Orders
- Checkout flow

</details>

<details>
  <summary><strong>Data & Persistence</strong></summary>

- PostgreSQL support

</details>

<details>
  <summary><strong>Testing Improvements</strong></summary>
  
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

This project uses **Springdoc OpenAPI** to automatically generate API documentation. This makes the API easy to explore, test, and understand.

- Interactive UI  
  👉 http://localhost:8080/swagger-ui.html

- Controllers are documented using:
  - `@Tag` for grouping
  - `@Operation` for summaries/descriptions
  - `@ApiResponses` for status codes
   
- Swagger access is restricted to the development profile through SwaggerSecurityConfig.

---

## 🔧 Managing Spring Profiles

The application determines its active profile through the `SPRING_PROFILES_ACTIVE` environment variable. This avoids hard‑coding a profile in `application.yml` nd ensures that each environment loads the correct configuration:

- **Development** → `application-dev.yml`  
- **Testing** → `@ActiveProfiles("test")` + `application-test.properties`  
- **Production** → `application-prod.yml`  

This also prevents development‑only components annotated with `@Profile("dev")` (such as the `DataSeeder`) from running during tests or in production.

##

### 🖥️ Setting the Profile in IntelliJ (Run Configuration)

1. Open **Run → Edit Configurations**
2. At **Environment variables**, add:
`SPRING_PROFILES_ACTIVE=dev`

This activates the `dev` profile whenever the application is started from the IDE.

---

## 🧪 Testing

The project includes a comprehensive test suite covering services, controllers, and full integration flows.

<details>
  <summary><strong>What’s covered</strong></summary>

- **Unit tests** for service layer  
  - Mockito‑based tests for business logic  
  - Repository interactions mocked  
- **Controller tests** using `@WebMvcTest`  
  - Validates request/response handling  
  - Security rules tested with mock users  
- **Integration tests** using `@SpringBootTest` + MockMvc  
  - Full request → service → database flow  
  - JWT authentication tested end‑to‑end  
  - Product, Category, Cart, and Auth flows covered  

</details>

<details>
  <summary><strong>Technologies used</strong></summary>

- `@SpringBootTest` (full context integration tests)  
- `@WebMvcTest` (controller‑only tests)  
- `MockMvc` (HTTP request simulation)  
- `Mockito` (mocking dependencies)  
- `@ActiveProfiles("test")` (isolated test configuration)  
- H2 in‑memory database  

</details>

<details>
  <summary><strong>Test Support Utilities</strong></summary>

The project includes a dedicated `testsupport` package containing reusable helpers for cleaner and more maintainable tests:

- **Test data builders** for creating consistent DTOs and entities  
- **Mock utilities** for simplifying service and controller tests  
- **Shared constants** used across multiple test classes  

This keeps the test suite expressive, reduces duplication, and makes it easier to add new tests as the project grows.

</details>

### 📋 Useful commands to run tests from terminal

| Command      | Description                                      |
|--------------|--------------------------------------------------|
| `mvn test`   | Runs all unit and integration tests              |
| `mvn verify` | Full build pipeline (compile + test + verify)    |

---
## 🧱 Architecture Overview

The project follows a clean, layered architecture designed for clarity, testability, and long‑term maintainability.

<details>
  <summary><strong>📐 Layered Structure</strong></summary>

- **Controller** → Handles HTTP requests and responses  
- **Service** → Business logic and domain rules  
- **Repository** → Data access via Spring Data JPA  
- **Database** → H2 (dev/test), PostgreSQL (planned)  
- **DTOs ↔ Entities** → Clear separation between API models and persistence models  

</details>

<details>
  <summary><strong>⚙️ Cross‑Cutting Concerns</strong></summary>

- **GlobalExceptionHandler** for consistent error responses  
- **Security layer** with JWT, RBAC, filters, handlers, and centralized rules  
- **Validation** using Jakarta Bean Validation  
- **Swagger/OpenAPI** for interactive API documentation  

</details>

The result is a modular and maintainable structure where each layer has a clear responsibility and can be tested independently.


