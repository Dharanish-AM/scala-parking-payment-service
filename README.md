Scala Parking Payment Service

A small Play Framework service in Scala that handles parking payments, receipts, and payment status tracking. This repository provides a minimal API, data models, and persistence for managing parking payments.

## Features

- Create and update payments
- Calculate parking fees
- Store receipts and payment status
- Simple controllers and repository layer for persistence

## Project Structure

- `app/controllers` - Play controllers (Health, Home, Payment)
- `app/models` - Domain models (`Payment`, `Receipt`, `PaymentStatus`)
- `app/dtos` - Data transfer objects
- `app/mappers` - Mapping between models and DTOs
- `app/repositories` - Persistence layer
- `app/services` - Business logic (`PaymentService`)
- `app/tables` - Slick or DB table mappings
- `app/utils` - Utility helpers (fee calculation)
- `conf` - Play configuration and routes

## Requirements

- Java 8+ (or compatible JVM)
- sbt

## Setup

1. Install sbt: https://www.scala-sbt.org/
2. From the project root, fetch dependencies and compile:

```bash
sbt compile
```

## Run

Start the Play application:

```bash
sbt run
```

The service will start on the configured port (default 9000). Visit `http://localhost:9000`.

## API Endpoints

- `GET /health` - Health check
- `GET /` - Home page
- `POST /payments` - Create a payment (accepts JSON `PaymentDTO`)
- `GET /payments/:id` - Get payment by id

Refer to `conf/routes` for the full routing table.

## Development

- Code is organized under `app/` and compiled into `target/` by sbt.
- Use your IDE (IntelliJ with Scala plugin or Metals) for development.

## Tests

Run the test suite with:

```bash
sbt test
```

## Next Steps

- Add integration tests and CI pipeline
- Provide example cURL requests and Postman collection
- Add database migration scripts for production-ready deployments

## License

This project is provided as-is. Add a LICENSE file to declare terms.
