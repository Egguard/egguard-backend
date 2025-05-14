# EggGuard Backend

## Overview

EggGuard is an automated farm management system designed to help poultry farmers monitor and collect eggs more efficiently. The backend provides APIs for:

- Registering and tracking eggs detected by robots
- Managing notifications from robots in the farm
- Monitoring farm activity and statistics

This Spring Boot application uses PostgreSQL for data storage and Cloudinary for image management.

## Table of Contents

- [Requirements](#requirements)
- [Project Setup](#project-setup)
- [Development Environment](#development-environment)
- [Production Deployment](#production-deployment)
- [Linux Installation](#linux-installation)
- [API Documentation](#api-documentation)
- [Integration with Robot](#integration-with-robot)

## Requirements

- Java 21
- Docker and Docker Compose
- Maven (or use the included Maven wrapper)
- PostgreSQL (via Docker)
- Cloudinary account (for image storage)

## Project Setup

1. Clone this repository
2. Create a `.env` file based on the `.env.example` template:
   ```
   cp .env.example .env
   ```
3. Update the `.env` file with your specific configuration values, including Cloudinary credentials

## Development Environment

1. Make sure you have your `.env` created based on the `.env.example` file
2. Inject the environment variables in your IDE (IntelliJ IDEA recommended)
3. Start the database:
   ```
   docker-compose up --build
   ```
4. Run the backend application using your IDE's "Run" button or with:
   ```
   ./mvnw spring-boot:run
   ```

## Production Deployment

To run the application in production mode:

```
docker-compose --profile production up --build
```

This will start both the PostgreSQL database and the Spring Boot application in Docker containers.

## Linux Installation

For Linux environments:

1. Install Docker (only needed once):

   ```
   ./install_docker_linux.sh
   ```

2. Run the complete backend stack:
   ```
   sudo docker compose --profile production up --build
   ```

## API Documentation

The API documentation is automatically generated using SpringDoc OpenAPI and can be accessed at:

- Swagger UI: `http://[server-address]:8081/swagger-ui.html`
- OpenAPI specification: `http://[server-address]:8081/v3/api-docs`

You can also find the OpenAPI specification in YAML format at `docs/openapi.yaml`.

## Integration with Robot

To use the backend with robots:

- Install the backend on a Linux machine (can be the same machine running ROS)
- Ensure the backend is accessible on the same LAN as the robot
- Configure the robot to communicate with the backend API endpoints
- Use the API to register eggs and send notifications from the robot

## Database Schema

The application uses a PostgreSQL database with migrations managed by Flyway.
Main entities include:

- Farms
- Robots
- Eggs
- Notifications
- Users and Roles

## Testing

Run the test suite with:

```
./mvnw test
```
