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
| **Security** | Spring Security 6 using Basic Auth (Plaintext) with `USER`, `LIBRARIAN`, and `ADMIN` roles. |
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
* **`members`**: Stores user registration data including unique `email`.
* **`loans`**: A junction table linking members and books. Contains foreign key references to `book_id` and `member_id`. It tracks the `borrowed_at` timestamp, the calculated `due_date`, and the nullable `returned_at` field.

### Automated Triggers & Auditing
To ensure data integrity, the database utilizes PostgreSQL triggers:
* **Audit Function**: A shared PL/pgSQL function `update_modified_column()` automatically refreshes the `updated_at` timestamp.
* **Update Triggers**: Attached to all core tables, these fire on every `UPDATE` operation to maintain an accurate audit trail at the database level.

---

## 3. Borrowing Rules
The following rules are enforced in the service layer:
1. **Loan Limit**: Members cannot exceed a defined number of active loans.
2. **Overdue Block**: Members with at least one overdue loan are blocked from further borrowing.
3. **Loan Duration**: Due dates are calculated based on a configurable number of days from the borrowing date.

---

## 4. Security & Authentication

The API is secured using **HTTP Basic Authentication**. Credentials are managed via an in-memory store initialized from `src/main/resources/auth/users.json`. Passwords in this version are stored and compared in **plaintext**.

### How to Authenticate
* **Swagger UI**: Click the **"Authorize"** button and enter a `username` and `password` from `users.json`.
* **Postman**: In the "Authorization" tab, select **Basic Auth** and enter the credentials.
* **Roles**: Authorization is role-based (e.g., only `LIBRARIAN` or `ADMIN` can modify the catalog).

---

## 5. Observability

### Actuator & Metrics
The service exposes health and performance data via Spring Boot Actuator. In addition to standard JVM and HTTP metrics, the following **custom business metrics** are tracked via the `MeterRegistry`:
* `library.books.checkedout.total`: Incremented on successful book loans.
* `library.books.returned.total`: Incremented on successful book returns.



### Structured Logging
Logback is configured to output logs in **JSON format**, including `traceId` and `spanId` to support distributed tracing and log aggregation.

---

## 6. Prerequisites & Running

### Prerequisites
* **Java 21**
* **Docker & Docker Compose**: Required for the database and Integration tests.
* **Bash Environment**: To execute the utility script.

### Running the Service
Use the `project.sh` script to manage the lifecycle:
* **Build**: `./project.sh build`
* **Run Stack**: `./project.sh run`
* **Test**: `./project.sh test` (Executes both Unit and Testcontainers-based Integration tests).

---

## 7. API Documentation & Postman

* **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **OpenAPI Spec (JSON)**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Postman Collection
You can import the following JSON snippet into Postman to test the service health and metrics:

```json
{
    "info": {
        "_postman_id": "030b0e3e-a12f-42c0-a19e-5ba340803123",
        "name": "Library Service",
        "schema": "[https://schema.getpostman.com/json/collection/v2.1.0/collection.json](https://schema.getpostman.com/json/collection/v2.1.0/collection.json)"
    },
    "item": [
        {
            "name": "Service Health Check",
            "request": {
                "method": "GET",
                "url": { "raw": "http://localhost:8080/actuator/health", "host": ["localhost"], "port": "8080", "path": ["actuator", "health"] }
            }
        },
        {
            "name": "Service Metrics List",
            "request": {
                "method": "GET",
                "url": { "raw": "http://localhost:8080/actuator/metrics", "host": ["localhost"], "port": "8080", "path": ["actuator", "metrics"] }
            }
        }
    ]
}
---

## 8. Future Improvements

* **Fine Tracking**: Logic to calculate and persist financial penalties for late returns.
* **Soft Deletes**: Implementing a deletion flag for records to maintain data history.
* **Search Filters**: Adding multi-criteria filtering for the book catalog.
* **Encrypted Password**: Adding support for encrypted/salted passwords.
