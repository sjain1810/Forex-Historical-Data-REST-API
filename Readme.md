# Forex Historical Exchange Data REST API

## Introduction

This project is a REST API that scrapes historical exchange data from Yahoo Finance for specified currency pairs and periods, and stores it in an in-memory H2 database. The data is periodically updated using a CRON job.


## Features

- **Scrape historical exchange data from Yahoo Finance.**
- **Store scraped data in a H2 database.**
- **Periodically update data using CRON jobs.**
- **REST API endpoints to query the stored data.**
- **Swagger documentation for API endpoints.**
- **Custom Logger System**: All levels of logs are generated and stored into a file.


## Tech Stack

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [H2 Database](https://www.h2database.com/html/main.html)
- [Swagger](https://swagger.io/)
- [Java 21](https://openjdk.org/projects/jdk/21/)


## Getting Started

### Prerequisites

- Java 21
- Gradle


### Installation

1. **Clone the repository:**

    ```bash
    git clone https://github.com/your-repo/forex-data-api.git
    cd forex-data-api
    ```

2. **Build and run the application:**

    ```bash
    ./gradlew build
    ./gradlew bootRun
    ```

3. **Access Swagger UI:**

   Swagger documentation is available at `http://localhost:8080/swagger-ui/`.

- The server will start running on `http://localhost:8080`.

## Deployment

The service is hosted on Render. You can access the Swagger UI at the following URL:

**Deployment Link:** [https://forex-historical-data-rest-api-1.onrender.com/swagger-ui](https://forex-historical-data-rest-api-1.onrender.com/swagger-ui)

Please note that due to Render's free tier auto-sleep feature, you might experience a brief delay on the first load after periods of inactivity.

### Database Configuration

The application uses an H2 in-memory database, configured automatically by Spring Boot.

**Configuration File**: `src/main/resources/application.properties`

```properties
# H2 Database Configuration
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA/Hibernate Configuration (Optional)
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
```
## Database Schema Management

### Liquibase Integration

This project uses Liquibase for managing database schema changes. Liquibase helps in versioning database changes and ensures that the database schema is consistent across different environments.

### Liquibase Changeset

The following Liquibase changeset creates the `forex_data` table with the required columns:

**Changeset File:** `src/main/resources/db/changelog/db.changelog-master.yaml`

```yaml
databaseChangeLog:
  - changeSet:
      id: 1
      author: Sahil Jain
      changes:
        - createTable:
            tableName: forex_data
            columns:
              - column:
                  name: id
                  type: bigint
                  autoIncrement: true
                  constraints:
                    primaryKey: true
              - column:
                  name: currency_pair
                  type: varchar(255)
                  constraints:
                    nullable: false
              - column:
                  name: date
                  type: date
                  constraints:
                    nullable: false
              - column:
                  name: rate
                  type: decimal(19, 6)
                  constraints:
                    nullable: false
                    unique: true
                    check:
                      condition: rate > 0
```


### Logging System

The application includes a custom logging system configured with Logback. Logs can be viewed in the console output and are also stored in files based on the Logback configuration.

**Configuration File:** `src/main/resources/logback-spring.xml`

The Logback configuration allows you to specify how logs are handled, including log levels and file storage.

**Example Logback Configuration:**

```xml
<configuration>

    <appender name="myConsoleAppender" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="myFileAppender" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/forexData.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>logs/forexData-%d{yy-MM-dd_HH-mm}.%i.log</fileNamePattern>
            <maxFileSize>10MB</maxFileSize>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="myConsoleAppender" />
        <appender-ref ref="myFileAppender" />
    </root>
    
</configuration>
```

### API Endpoints

#### POST /api/forex-data

Description: Scrape and store historical exchange data.

**Query Parameters**:

- `from` (string, required): The base currency code (e.g., GBP, AED).
- `to` (string, required): The target currency code (e.g., INR).
- `period` (string, required): The timeframe for the historical data (e.g., 1W, 1M, 3M, 6M, 1Y).

**Example URL**: `http://localhost:8080/api/forex-data?from=GBP&to=INR&period=1W`

**Responses**:

- `200 OK`: List of Scrapped Data.
- `400 Bad Request`: Invalid input parameters.
- `500 Internal Server Error`: Failed to scrape data.

**Example Response**:
    
    [
        {
        "id": 1,
        "currencyPair": "USDINR=X",
        "date": "2024-08-26",
        "open": 82.50,
        "high": 83.00,
        "low": 81.75,
        "close": 82.90,
        "adjClose": 82.90,
        "volume": 1000000
        }
    ]

#### GET /api/forex-data/retrieve

Description: Retrieve the data from the in-memory database.

**Example URL**: `http://localhost:8080/api/forex-data/retrieve`

**Responses**:

- `200 OK`: List of stored Forex data.
- `500 Internal Server Error`: Failed to retrieve data.

**Example Response**:

    [
        {
        "id": 1,
        "currencyPair": "USDINR=X",
        "date": "2024-08-26",
        "open": 82.50,
        "high": 83.00,
        "low": 81.75,
        "close": 82.90,
        "adjClose": 82.90,
        "volume": 1000000
        }
    ]

## CRON Jobs

The application also utilizes CRON jobs to periodically update the data:

### Jobs Scheduled

- **Daily Job**:
    - Updates data for the last week (`1W` period).
    - Runs every day at IST 02:20 PM.

- **Weekly Job**:
    - Updates data for the last month (`1M` period).
    - Runs every Saturday at IST 02:20 PM

- **Monthly Job**:
    - Updates data for the last 3 months (`3M`), 6 months (`6M`), and 9 months (`9M`).
    - Runs every month on 1st at IST 02:20 PM

- **Yearly Job**:
    - Updates data for the last year (`1Y`).
    - Runs on January 1st at IST 02:20 PM

### Swagger Documentation

Swagger documentation is available at `http://localhost:8080/swagger-ui.html`.

### Configuration

#### 1. Database Configuration

The in-memory database is configured in `src/main/resources/application.properties`.

#### 2. CRON Job Configuration

The CRON jobs are configured in `src/main/java/com/example/forexData/config`.

### 3. Swagger Configuration

The Swagger is configured in `src/main/java/com/example/forexData/config`

### License

This project is licensed under the MIT License.