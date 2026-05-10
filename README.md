# Scala Parking Payment Service

A Play Framework service for managing parking payments, fees, and receipts.

## Current Status

The project is in an early but working state.

- The codebase compiles successfully with `sbt compile`.
- The test suite passes with `sbt test`.
- The main payment creation flow is wired through controller, service, mapper, and repository layers.
- Routes are defined for health and payment-related endpoints.
- Several payment endpoints still return placeholder responses and are not fully implemented yet.
- Database and Play configuration are present for local development with MySQL.

## Tech Stack

- Scala 2.13.18
- Play Framework 2.9.1
- Slick for persistence
- MySQL 8
- ScalaTest for testing

## Implemented API Routes

- `GET /api/health`
- `POST /api/payments`
- `POST /api/payments/calculate`
- `GET /api/payments/:id`
- `POST /api/payments/:id/process`
- `POST /api/payments/:id/refund`
- `GET /api/payments/:id/receipt`

## Local Setup

1. Make sure Java 17 and SBT are installed.
2. Start MySQL locally.
3. Update `conf/application.conf` if your database credentials differ from the default local values.
4. Run the application with `sbt run`.

## Configuration

- Application port: `9000`
- Default database: `scala-parking-payment-service`
- Evolutions: enabled for the default database

## Next Work

- Implement the remaining payment endpoints.
- Add fee calculation rules and validation.
- Replace local-only secrets and database credentials with environment-based configuration.
- Add and expand integration tests around payment flows.
