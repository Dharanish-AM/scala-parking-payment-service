# Scala Parking Payment Service

A robust, production-ready backend service built with Scala and the Play Framework to manage parking payments, fee calculations, and receipt generation. This service handles the end-to-end flow from vehicle entry to payment processing and refund management.

## 🧠 Project Overview

The service simulates a real-world parking management system used in malls, airports, and parking lots.

### 🔁 Real-World Flow

`Vehicle Enters` → `Vehicle Exits` → `System Calculates Fee` → `User Pays` → `Receipt Generated` → `(Optional Refund)`

---

## 🚀 Key Features

- **Fee Calculation Engine**: Intelligent logic based on duration with support for:
  - ⏱️ **Grace Period**: First 15 minutes are free.
  - 💰 **Hourly Rate**: Fixed rate of ₹20/hour.
  - 🧢 **Daily Cap**: Maximum charge of ₹200 per 24-hour period.
- **Payment Lifecycle Management**: Tracks payments through `PENDING`, `SUCCESS`, and `FAILED` states.
- **Robust Validations**:
  - Future date checks for entry and exit times.
  - Logic to ensure exit time is always after entry time.
- **Idempotency**: Prevents duplicate payment processing for the same transaction.
- **Refund System**: Allows reversing successful payments while preventing double refunds.
- **Receipt Generation**: Provides proof of payment with unique transaction IDs and timestamps.
- **Health Monitoring**: Enhanced health checks including real-time database connectivity verification.

---

## 🛠️ Tech Stack

- **Language**: [Scala 2.13.18](https://www.scala-lang.org/)
- **Framework**: [Play Framework 2.8.x](https://www.playframework.com/)
- **Persistence**: [Slick 5.1.0](https://scala-slick.org/) (Functional Relational Mapping)
- **Database**: [MySQL 8.0](https://www.mysql.com/)
- **Testing**: [ScalaTest](https://www.scalatest.org/), [Mockito](https://site.mockito.org/)
- **Dependency Injection**: Guice

---

## 📂 Project Structure

```text
app/
├── controllers/    # API endpoints (Health, Home, Payment)
├── services/       # Core business logic (Fee calculation, Payment processing)
├── repositories/    # Database access layer
├── models/         # Domain models (Payment, Receipt, Status)
├── tables/         # Slick table mappings
├── dtos/           # Data Transfer Objects for API requests/responses
├── mappers/        # Logic to convert between Models and DTOs
└── utils/          # Utility helpers (DatabaseSupport trait, calculation logic)
conf/               # Configuration (routes, application.conf, evolutions)
test/               # Unit and integration tests
```

---

## ⚙️ Getting Started

### Prerequisites

- Java 8 or higher
- [sbt](https://www.scala-sbt.org/)
- MySQL 8.0

### Database Setup

1. Create a database named `scala-parking-payment-service`.
2. Update the credentials in `conf/application.conf` if necessary:

   ```hocon
   slick.dbs.default.db.user = "root"
   slick.dbs.default.db.password = "your_password"
   ```

### Running the Application

1. Clone the repository and navigate to the root directory.
2. Compile the project:

   ```bash
   sbt compile
   ```

3. Run the application:

   ```bash
   sbt run
   ```

   The service will be available at `http://localhost:9000`.

---

## 🔌 API Endpoints

### Health & UI

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/` | Home Page |
| `GET` | `/api/health` | Service & DB Health Check |

### Payment Management

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/payments` | Create a new payment record |
| `POST` | `/api/payments/:id/calculate` | Calculate fee for a specific payment |
| `POST` | `/api/payments/:id/process` | Process a payment (Simulates gateway) |
| `GET` | `/api/payments/:id` | Get detailed payment status |
| `POST` | `/api/payments/:id/refund` | Initiate a refund for a successful payment |
| `GET` | `/api/payments/:id/receipt` | Generate/Retrieve payment receipt |

---

## 🧪 Testing

Execute the test suite (Unit, Service, and Repository tests) using:

```bash
sbt test
```

---

## 📝 License

This project is licensed under the MIT License.
