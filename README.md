# mem-limit-test

Spring Boot application for testing memory limits with HttpBin-style request/response inspection endpoints.

## Features

### Request/Response Inspection Endpoints

This application provides HttpBin-style endpoints for inspecting HTTP requests and responses:

#### Basic Inspection

- **GET /ip** - Returns the client's IP address
  ```bash
  curl http://localhost:8080/ip
  ```

- **GET /headers** - Returns all HTTP headers from the request
  ```bash
  curl http://localhost:8080/headers
  ```

- **GET /user-agent** - Returns the User-Agent header
  ```bash
  curl http://localhost:8080/user-agent
  ```

#### HTTP Method Endpoints

- **GET /get** - Echoes GET request details including query parameters, headers, and origin
  ```bash
  curl "http://localhost:8080/get?param1=value1&param2=value2"
  ```

- **POST /post** - Echoes POST request details including body, form data, JSON, and headers
  ```bash
  # With JSON
  curl -X POST -H "Content-Type: application/json" \
    -d '{"name":"test","value":"data"}' \
    http://localhost:8080/post
  
  # With form data
  curl -X POST -H "Content-Type: application/x-www-form-urlencoded" \
    -d "field1=value1&field2=value2" \
    http://localhost:8080/post
  ```

- **PUT /put** - Echoes PUT request details
- **PATCH /patch** - Echoes PATCH request details
- **DELETE /delete** - Echoes DELETE request details

#### Flexible Endpoint

- **ANY /anything** - Accepts any HTTP method and returns complete request details
- **ANY /anything/{path}** - Same as /anything but with arbitrary path segments
  ```bash
  curl "http://localhost:8080/anything/users/123/profile?tab=settings"
  curl -X POST -H "Content-Type: application/json" \
    -d '{"data":"test"}' \
    http://localhost:8080/anything
  ```

### Response Format

All inspection endpoints return JSON with the following structure:

```json
{
  "args": {},           // Query parameters
  "data": "",           // Raw request body
  "files": {},          // Uploaded files (if any)
  "form": {},           // Form data (if any)
  "headers": {},        // Request headers
  "json": null,         // Parsed JSON body (if any)
  "method": "GET",      // HTTP method
  "origin": "127.0.0.1",// Client IP address
  "url": "http://..."   // Full request URL
}
```

## Building and Running

### Prerequisites

- Java 8 or higher
- Maven 3.x

### Build

```bash
mvn clean package
```

### Run

```bash
java -jar target/mem-limit-test-1.0.0.jar
```

The application will start on port 8080 by default.

## Configuration

Configuration options are available in `src/main/resources/application.properties`:

- `server.port` - Server port (default: 8080)
- `logging.level.com.example.memlimit` - Logging level

## Health Check

The application includes Spring Boot Actuator endpoints:

```bash
curl http://localhost:8080/actuator/health
```

