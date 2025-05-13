# Gift Card Winner

**Gift Card Winner** is a Spring Boot + Spring Batch application that performs the following batch jobs:

1. **User Import Job** – Fetches users from an external API.
2. **Order Import Job** – Reads and imports order data from a CSV file.
3. **Winner Selection Job** – Selects a weekly gift card winner based on a configurable order amount threshold.

This application is ideal for automating data ingestion and reward logic in an e-commerce setting.

---

## Features

- Fetch user data from `https://jsonplaceholder.typicode.com/users`.
- Read order data from a CSV file.
- Persist users and orders into a relational database.
- Select a gift card winner randomly from users whose total order amount exceeds a threshold.
- Retry logic for API calls.
- Unit testing using Mockito, Spring Batch Test, and H2.
- Docker support for containerized deployment.

---

## Prerequisites

- Java 17+
- Maven 3.8.6+
- Docker (optional)
- Database:
    - H2 for testing (in-memory)
    - PostgreSQL or other JDBC-supported DB for production

---

## Project Structure

```
GiftCardWinner/
├── src/
│   ├── main/
│   │   ├── java/com/ecom/giftcardwinner/
│   │   │   ├── config/              # Batch configuration
│   │   │   ├── controller/          # Controller (Winner details)
│   │   │   ├── schduler/            # Job definitions
│   │   │   ├── model/               # Entity classes (User, Order)
│   │   │   ├── processor/           # Data processors
│   │   │   ├── reader/              # API and CSV readers
│   │   │   ├── repository/          # Spring Data JPA repositories
│   │   │   └── writer/              # Database writers
│   │   │   └── tasklet/             # table truncate and winner selection tasklet
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/ecom/giftcardwinner/
│           ├── config/
│           └── controller/
│           ├── reader/
│           ├── schduler/
│           ├── tasklet/
├── Dockerfile
├── pom.xml
└── README.md
```

---

## Setup & Installation

### Local Development

1. **Clone the repo**
```bash
git clone https://github.com/Sugandhab/GiftCardWinner.git
cd GiftCardWinner
```

2. **Build the project**
```bash
mvn clean install
```

3. **Configure Application As per need** – Update `src/main/resources/application.properties`:
```properties
# ------------------------
# BATCH CONFIGURATION
# ------------------------
batch.chunk-size=10
batch.skip-limit=10
batch.sql.truncate-orders=TRUNCATE TABLE orders RESTART IDENTITY
batch.winner.eligibility.amount=20

# ------------------------
# H2 DATABASE CONFIG
# ------------------------
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.password=
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
spring.datasource.username=sa

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# ------------------------
# JPA / HIBERNATE CONFIG
# ------------------------
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.show-sql=true

# ------------------------
# SPRING BATCH CONFIG
# ------------------------
spring.batch.job.enabled=false
spring.batch.jdbc.initialize-schema=always

# ------------------------
# SCHEDULER CONFIG
# ------------------------
# Fixed rate execution in milliseconds (e.g., every 24 hours = 86400000 ms, 7 days = 604,800,000 ms)
scheduler.fixed-rate-ms=86400000
scheduler.initial-delay-ms=10000
scheduler.run-on-startup=false

# ------------------------
# USER API CONFIG
# ------------------------
user.api.initial-backoff-ms=1000
user.api.max-retries=3
user.api.url=https://jsonplaceholder.typicode.com/users

# ------------------------
# CSV CONFIG
# ------------------------
order.csv.path=${ORDER_CSV_PATH:/app/data/order.csv}
```

4. **Run the app**
```bash
mvn spring-boot:run
```

---

## Running Tests

```bash
mvn test
```

- Unit tests included.
- Uses Mockito and H2.
- Debug logs can be enabled:
---

## Docker Usage

### Build Docker Image
```bash
docker build -t giftcardwinner:latest .
```

### Run Docker Container
```bash
docker run -p 8080:8080   -e ORDER_CSV_PATH=/app/data/order.csv   -v "$(pwd)/data:/app/data"   --name giftcardwinner giftcardwinner:latest
```

### Stop and Remove
```bash
docker stop giftcardwinner
docker rm giftcardwinner
```

## CSV File Mounting for Scheduled Winner Job

To ensure the job runs correctly, **make sure you mount the correct CSV file containing order data** into the Docker container. This file is required for the batch job to process purchases and select a winner.
- Replace $(pwd)/data/order.csv with the absolute path to your actual CSV file, if needed.
- The value of ORDER_CSV_PATH must match the target path inside the container (/app/data/order.csv).

### Example Docker Run Command

```bash
docker run -p 8080:8080 \
  -e ORDER_CSV_PATH=/app/data/order.csv \
  -v "$(pwd)/data/order.csv:/app/data/order.csv" \
  --name giftcardwinner \
  giftcardwinner:latest
```

---

## Winner Selection Logic

- Orders are read from CSV.
- Users with total order amount > `batch.winner.eligibility.amount` are eligible.
- A winner is selected randomly in memory from the eligible users
- The winner is then persisted in the winner table along with a timestamp.

---

## API Endpoints

These endpoints are exposed via `WinnerController` for easy access winner data:

### `GET /api/winner/latest`
- Returns the most recently selected gift card winner.
- Example response:
```json
{
  "userId": 3,
  "name": "Clementine Bauch",
  "amount":103.34,
  "createdAt":"2025-05-12T23:05:40.372011"
}
```

### `GET /api/winner/all`
- Returns list of all winners.
- Example response:
```json
[{"userId":5,"name":"Chelsey Dietrich","amount":103.34,"createdAt":"2025-05-12T23:05:40.372011"},..]
```

Both endpoints run on:  
**http://localhost:8080/api/winner/**

---
