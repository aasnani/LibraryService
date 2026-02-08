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

## 2. Architecture & Schema

### Project Structure
The project is organized into two modules:
* **`library-core`**: Contains the domain entities, repository interfaces, and core business services.
* **`library-service-application`**: Contains the Spring Boot entry point, REST controllers, security configurations, and DTOs.



### Database Schema
The database consists of three primary tables: `books`, `members`, and `loans`. Automated PostgreSQL triggers manage `updated_at` timestamps for auditing purposes.



---

## 3. Borrowing Rules
The following rules are enforced in the service layer:
1. **Loan Limit**: Members cannot exceed a defined number of active loans.
2. **Overdue Block**: Members with at least one overdue loan are blocked from further borrowing.
3. **Loan Duration**: Due dates are calculated based on a configurable number of days from the borrowing date.

---

## 4. How to Run

### Using the Utility Script
A `project.sh` script is included for management:

* **Build**: `./project.sh build`
* **Test**: `./project.sh test`
* **Build Docker Image**: `./project.sh build_image`
* **Run Stack**: `./project.sh run`

---

## 5. API Documentation & Testing

The service provides OpenAPI 3.0 documentation. Once the service is running, it can be accessed and tested at the following URLs:

* **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **OpenAPI Spec (JSON)**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 6. Future Improvements

* **Fine Tracking**: Logic to calculate and persist financial penalties for late returns.
* **Soft Deletes**: Implementing a deletion flag for records to maintain data history.
* **Search Filters**: Adding multi-criteria filtering for the book catalog.
