# Scala Parking Payment Service

Lightweight Play Framework microservice that manages parking payments, fee calculations and receipts.

## Overview

This repository implements a simple parking payment API using Play Framework and Slick. Core responsibilities include:

- Accepting payment requests and persisting payment records
- Calculating parking fees
- Processing refunds and generating receipts
- Providing a health endpoint for readiness checks

## Features

- REST endpoints for create/calculate/process/refund/receipt
- Layered architecture: controllers → services → repositories → tables
- Slick-based persistence with database evolutions
- Unit tests with ScalaTest

## Tech stack

- Scala 2.13
- Play Framework 2.9
- Slick
- MySQL (or compatible RDBMS)
- sbt build tool

## Prerequisites

- Java 17
- sbt (1.8+ recommended)
- A running MySQL instance (or set JDBC URL to another DB)

## Quickstart (local)

1. Configure your DB in [conf/application.conf](conf/application.conf#L1).
2. Run database evolutions (Play will apply them automatically on start unless disabled).
3. Start the service:

```bash
sbt run
```

The app listens on port 9000 by default.

## API (implemented routes)

- `GET /api/health` — service health
- `POST /api/payments` — create a payment
- `POST /api/payments/calculate` — calculate fee for a request
- `GET /api/payments/:id` — fetch payment by id
- `POST /api/payments/:id/process` — mark payment processed
- `POST /api/payments/:id/refund` — issue a refund
- `GET /api/payments/:id/receipt` — fetch receipt

See the `app/controllers/PaymentController.scala` for request/response details.

## Development

- Compile: `sbt compile`
- Run tests: `sbt test`
- Play console / dev mode: `sbt run` (hot reload enabled)

## Contributing

1. Fork the repo and create a feature branch.
2. Add tests for new behavior.
3. Open a PR with a clear description of the change.

## Where to look in the codebase

- Controller: `app/controllers/PaymentController.scala` ([app/controllers/PaymentController.scala](app/controllers/PaymentController.scala#L1))
- Service: `app/services/PaymentService.scala` ([app/services/PaymentService.scala](app/services/PaymentService.scala#L1))
- Repository / Table: `app/repositories/PaymentRepository.scala`, `app/tables/PaymentTable.scala`
- DTOs / Mappers: `app/dtos/PaymentDTO.scala`, `app/mappers/PaymentMapper.scala`
- Fee logic: `app/utils/calculateParkingFee.scala`

## Next steps / TODO

- Finish remaining endpoint implementations and validations
- Add integration tests that run against a real DB (Testcontainers)
- Externalize secrets and DB config for production deployments

---

If you want, I can also add a minimal example `curl` request for creating a payment or update the routes documentation with sample JSON payloads.
