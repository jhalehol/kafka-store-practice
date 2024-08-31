# Sales Information Microservices with Kafka

## Overview

This repository demonstrates a microservices architecture that uses Apache Kafka for communication between services. The system handles and serves sales information through a scalable, event-driven approach.

### Key Components

- **Microservices:** A set of interconnected microservices for managing different aspects of sales information, including order processing, inventory management, and reporting.
- **Apache Kafka:** A distributed event streaming platform used for inter-service communication and data processing.
- **Kafka Topics:** Channels for publishing and consuming sales events and information.

## Architecture

- **Order Service Producer:** Processes incoming sales orders and publishes order events to Kafka.
- **Intermediate Procesor:** Listens for order events, updates inventory levels, and publishes inventory updates to Kafka.
- **Reporting Service:** Consumes Kafka events to generate and display sales reports.

## Getting Started

### Prerequisites

- Docker and Docker Compose for running Kafka and Zookeeper locally.
- Java21
- Docker compose


### Setup

1. **Clone the Repository**

   ```bash
   git clone https://github.com/jhalehol/kafka-stores-usecase
   cd kafka-stores-usecase

1. **Run Kafka

   ```bash
   cd sales-producer
   docker-compose up

2. **Create Topics:

   ```bash
   ./create-topics.sh
