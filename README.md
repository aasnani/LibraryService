# Library Management Microservice

A Java-based microservice for managing a book catalog, library members, and borrowing transactions.

---

## 1. Project Requirements & Implementation

| Requirement | Implementation |
| :--- | :--- |
| **Catalog Management** | CRUD endpoints for the `Book` entity. |
| **Member Management** | CRUD endpoints for the `Member` entity. |
| **Borrowing/Returns** | `LoanService` handles checkout and return logic, including inventory updates. |
| **Borrowing Rules** | Enforced via `LoanPolicyProperties` using values defined in `application.yaml`. |
| **Persistence** | PostgreSQL database with schema management via Flyway. |
| **Security** | Spring Security 6 using Basic Authentication with `USER`, `LIBRARIAN`, and `ADMIN` roles. |
| **Observability** | Spring Boot Actuator for health/metrics and Logback for JSON-structured logging. |
| **Testing** | Unit tests and Integration tests using Testcontainers. |

---

## 2. Architecture & Database Schema

### Project Structure
The project is organized into two modules to separate core domain logic from the application and web layers:
* **`library-core`**: Contains the domain entities, repository interfaces, and core business services.
* **`library-service-application`**: Contains the Spring Boot entry point, REST controllers, security configurations, and DTOs.

### Database Schema
The persistence layer is managed by **Flyway** and consists of three primary tables:



* **`books`**: Stores inventory details including `isbn`, `total_copies`, and `available_copies`.
* **`members`**: Stores user registration data including unique `email` and names.
* **`loans`**: A junction table linking members and books. It tracks the `borrowed_at` timestamp, the calculated `due_date`, and the nullable `returned_at` field.

### Automated Triggers & Auditing
To ensure data integrity and consistent auditing, the database utilizes PostgreSQL triggers:
* **Audit Function**: A shared PL/pgSQL function `update_modified_column()` is used to automatically refresh the `updated_at` timestamp.
* **Update Triggers**: Attached to all three core tables (`books`, `members`, `loans`), these triggers fire on every `UPDATE` operation to maintain an accurate record of when data was last modified without requiring manual application-level intervention.

---

## 3. Borrowing Rules
The following rules are enforced in the service layer:
1. **Loan Limit**: Members cannot exceed a defined number of active loans.
2. **Overdue Block**: Members with at least one overdue loan are blocked from further borrowing.
3. **Loan Duration**: Due dates are calculated based on a configurable number of days from the borrowing date.

---

## 4. Prerequisites
To build and run this service, you need:
* **Java 21**
* **Docker & Docker Compose**: Required for running the database stack and executing integration tests via Testcontainers.
* **Bash Environment**: To execute the `project.sh` utility script.

---

## 5. How to Run

### Using the Utility Script
A `project.sh` script is included to manage the development lifecycle:

* **Build**: `./project.sh build`
* **Test**: `./project.sh test`
* **Build Docker Image**: `./project.sh build_image`
* **Run Stack**: `./project.sh run`

---

## 6. Testing
The project includes a comprehensive test suite:
* **Unit Tests**: Test business logic in isolation using JUnit 5 and Mockito.
* **Integration Tests**: Verify persistence and repository logic using **Testcontainers** to spin up a real PostgreSQL instance.
* **Commands**:
    * Run all tests: `./project.sh test`
    * Run specific tests via Gradle: `./gradlew test --tests "com.library.*"`

---

## 7. API Documentation & Testing

The service provides OpenAPI 3.0 documentation. Once the service is running, it can be accessed and tested at the following URLs:

* **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **OpenAPI Spec (JSON)**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 8. Future Improvements

* **Fine Tracking**: Logic to calculate and persist financial penalties for late returns.
* **Soft Deletes**: Implementing a deletion flag for records to maintain data history.
* **Search Filters**: Adding multi-criteria filtering for the book catalog.
