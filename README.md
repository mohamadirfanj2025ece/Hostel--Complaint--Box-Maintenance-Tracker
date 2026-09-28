# ComplaintBox – Hostel Maintenance Complaint Tracker

A Spring Boot REST API where hostel residents register maintenance complaints and the warden tracks and resolves them. Built as a college project.

## Problem Statement

Hostel students usually report maintenance problems (water leakage, fan or light not working, cleaning problems, etc.) verbally to the warden. Verbal complaints are easily forgotten or delayed, and nobody can see how many complaints are pending.

## Solution

ComplaintBox is a simple system to **register** and **track** maintenance complaints. Every complaint is saved in a database with a status (`OPEN`, `IN_PROGRESS`, `RESOLVED`). The warden can see all complaints, update their status, and find complaints that have been waiting for too long.

## Objectives

* Replace verbal complaints with recorded complaints.
* Let residents see the status of their own complaints.
* Let the warden manage complaints in a clear workflow.
* Highlight complaints that are overdue (more than 5 days, not resolved).
* Practice REST API design, Spring Security, JPA and MySQL.

## Features

* Registration and login with BCrypt-encrypted passwords
* Two roles with role-based access: `USER` and `ADMIN`
* Create complaints with category and priority
* Status workflow that cannot move backward
* Overdue calculation (never stored, always calculated)
* Admin dashboard counts (calculated from the database)
* Input validation (Jakarta Validation)
* Global exception handling with simple JSON messages
* Unit tests for the business rules (no database needed)

## User Role (`USER` = hostel resident)

* Register and login
* Create a complaint
* View **only their own** complaints and their status

A USER cannot see other users' complaints, cannot change status, and cannot use admin APIs.

## Admin Role (`ADMIN` = warden / maintenance staff)

* Login
* View all complaints (oldest unresolved first)
* Change complaint status
* View overdue complaints
* View dashboard counts

Student registration always creates a `USER`. Admin registration is available only while no admin account exists, allowing the first admin to bootstrap the system without a registration key. A user cannot select the `ADMIN` role from student registration.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 17 | Programming language |
| Maven | Build tool |
| Spring Boot 3.3 | Application framework |
| Spring Web | REST controllers |
| Spring Data JPA + Hibernate | Database access |
| Spring Security | Login and role-based access |
| Jakarta Validation | Input validation |
| MySQL | Database |
| JUnit + Mockito | Unit tests |
| Postman | API testing |

## Architecture

```
Postman
   |
   v
Controller
   |
   v
Service
   |
   v
Repository
   |
   v
MySQL
```

* **Postman** – the client. It sends HTTP requests (JSON) and shows responses.
* **Controller** – receives the request, checks the JSON is valid (`@Valid`), calls the service and returns the response. It has no business logic.
* **Service** – the "brain". It contains the business rules: status workflow, overdue calculation, dashboard counts, and registration rules.
* **Repository** – talks to the database using Spring Data JPA. We only write method names such as `findByEmail`; Spring creates the SQL.
* **MySQL** – stores the data permanently.

Other packages: `entity` (database tables), `dto` (request/response objects), `enums` (fixed value lists), `config` (security), `exception` (error handling).

## Database Design

Database name: `complaintbox_db`

**users**

| Column | Type | Notes |
|---|---|---|
| id | BIGINT | Primary key |
| name | VARCHAR(50) | |
| email | VARCHAR(100) | Unique, used to login |
| register_number | VARCHAR(50) | Unique student register number |
| department | VARCHAR(100) | Student department |
| hostel_block | VARCHAR(50) | Hostel block |
| floor | VARCHAR(20) | Hostel floor |
| phone_number | VARCHAR(20) | Student contact number |
| password | VARCHAR(100) | BCrypt hash |
| role | VARCHAR(20) | `USER` or `ADMIN` |
| room_number | VARCHAR(10) | |
| created_at | DATETIME | |

**complaints**

| Column | Type | Notes |
|---|---|---|
| id | BIGINT | Primary key |
| user_id | BIGINT | Foreign key to users |
| category | VARCHAR(20) | `PLUMBING`, `ELECTRICAL`, `CLEANING`, `OTHER` |
| title | VARCHAR(100) | Short complaint summary |
| hostel_block | VARCHAR(50) | Block where the issue is located |
| floor | VARCHAR(20) | Floor where the issue is located |
| room_number | VARCHAR(10) | |
| landmark | VARCHAR(100) | Optional nearby location detail |
| description | VARCHAR(500) | |
| priority | VARCHAR(20) | `LOW`, `MEDIUM`, `HIGH` |
| status | VARCHAR(20) | `OPEN`, `IN_PROGRESS`, `RESOLVED` |
| created_at | DATETIME | |
| updated_at | DATETIME | |

**Relationship:** one User has many Complaints (`users 1 ---- * complaints`).

Category is an **enum** instead of a separate table, because the four categories are fixed. This keeps the code simpler. A status-history table was not added to keep the project easy to explain.

## API List

| Method | URL | Role | Purpose |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register a new USER |
| POST | `/api/auth/student/register` | Public | Register a student with personal details |
| POST | `/api/auth/admin/register` | Public, first-admin setup only | Create the first admin account |
| POST | `/api/auth/login` | Public | Check login, returns role |
| GET | `/api/students/me` | USER | View own personal details |
| PUT | `/api/students/me/profile` | USER | Complete own profile on first login |
| POST | `/api/complaints` | USER | Create a complaint |
| GET | `/api/complaints/my` | USER | View my complaints |
| GET | `/api/admin/complaints` | ADMIN | View all complaints |
| PUT | `/api/admin/complaints/{id}/status` | ADMIN | Change status |
| GET | `/api/admin/complaints/overdue` | ADMIN | View overdue complaints |
| GET | `/api/admin/dashboard` | ADMIN | Dashboard counts |

Details and sample JSON: see `POSTMAN_API_GUIDE.md`.

**How login works:** the API uses HTTP Basic Authentication. `/api/auth/login` checks the email and password and returns the role. For the other APIs, Postman sends the same email and password in the Basic Auth header on every request (no tokens, no sessions), and Spring Security checks the role.

## Business Rules

1. New registrations always get the role `USER`. A request cannot choose `ADMIN`.
2. A new complaint always starts as `OPEN`; `createdAt` and `updatedAt` are set by the backend.
3. If priority is not given, it is `MEDIUM`.
4. Status flow (checked in the **service layer**):
   * `OPEN → IN_PROGRESS` allowed
   * `IN_PROGRESS → RESOLVED` allowed
   * `OPEN → RESOLVED` allowed
   * Everything else (any backward move, or same status) is rejected with `{"message": "Invalid status change"}`
5. A complaint is **overdue** when it is not `RESOLVED` **and** more than 5 days have passed since `createdAt`. It is calculated, never stored.
6. A USER only ever sees their own complaints.
7. Only ADMIN can view all complaints, change status, see overdue complaints and the dashboard.

## MySQL Setup

1. Install MySQL 8 and start the server.
2. Create the database (choose one):
   * Run `database.sql` in MySQL Workbench, **or**
   * Run `CREATE DATABASE complaintbox_db;`, **or**
   * Do nothing: the connection URL contains `createDatabaseIfNotExist=true`, so the app creates it.
3. Open `src/main/resources/application.properties` and put your MySQL password:
   ```
   spring.datasource.username=root
   spring.datasource.password=YOUR_PASSWORD
   ```
4. Tables are created or updated automatically by Hibernate (`ddl-auto=update`). No demo accounts or sample complaints are inserted.
5. The first admin can register at `/admin-register.html`. Once an admin exists, public admin registration is closed; no registration key is required.

Pages: `/`, `/student-register.html`, `/student-login.html`, `/admin-login.html`, and `/admin-register.html`.

## IntelliJ IDEA Setup

1. Unzip the project.
2. In IntelliJ: **File → Open** and select the folder that contains `pom.xml` (or select `pom.xml` and choose **Open as Project**).
3. Wait for Maven to download the dependencies (bottom-right progress bar).
4. Make sure the Project SDK is **JDK 17 or newer** (**File → Project Structure → Project**).
5. Update the password in `application.properties`.
6. Open `ComplaintBoxApplication.java` and click the green **Run** button.
7. The server starts on `http://localhost:8080`.

## Maven Run Commands

Maven must be installed (or use the Maven tool window inside IntelliJ).

```
mvn clean test          # run unit tests (no MySQL needed)
mvn clean package       # build the jar
mvn spring-boot:run     # start the application (MySQL must be running)
```

## Postman Testing

1. Start MySQL and run the application.
2. For register and login, send JSON in **Body → raw → JSON**.
3. For all other APIs, open the **Authorization** tab, choose **Basic Auth**, and enter the email and password of the account.
4. Follow the 10-step demo in `POSTMAN_API_GUIDE.md`.

## Tests

`ComplaintServiceTest` and `AuthServiceTest` use JUnit and Mockito. They test the status workflow, overdue calculation, dashboard counts, default priority, sorting, registration role and login. They use mock repositories, so MySQL is not needed to run them.
