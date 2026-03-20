# Spring Boot E‑Commerce API

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
| API                    | Spring Web (Spring MVC)                        |
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
- **Product Domain**
  - Product entity (JPA)
  - ProductRequest & ProductResponse DTOs
  - ProductService with CRUD‑logik
  - ProductController (REST API)
  - H2 in‑memory database for development
  - Unit tests (JUnit + Mockito)
  - Integration tests (Spring Boot Test + TestRestTemplate)
  
##

### 🛠️ Planned
- **Product Enhancements**
  - Category model & relations
  - Pagination & sorting
  - Search & filtering

- **User & Authentication**
  - User model
  - Registration & login
  - JWT authentication
  - Role‑based authorization

- **Shopping Flow**
  - Cart
  - Orders
  - Checkout flow

- **Data & Persistence**
  - PostgreSQL support

- **Testing Improvements**
  - More integration tests
  - Manual API testing with Postman
  - Unit tests with JUnit + Mockito

- **Tooling & Dev Experience**
  - API documentation (Swagger / Springdoc)
  - Docker support
  - CI pipeline (GitHub Actions)
