# PickBox

## Description

PickBox is a file storage application built using Spring Boot and React. It provides a simple and intuitive interface for users to manage their files and folders. It also provides features like file upload, download, delete, and search.

## Tech Stack

- Spring Boot
- React
- MongoDB
- AWS S3 (Directly compatible, not implemented)
- Next.js

## Setup

- Clone the repository
- Setup MongoDB in any preferred way and add the connection string in the [application.properties](src/main/resources/application.properties) file
  - `spring.data.mongodb.uri=<connection-string>`
- Use the maven wrapper to build the backend
  - `./mvnw clean install`
- Use the npm to build the frontend
  - `cd ui && npm run build`
- Run the backend
  - `./mvnw spring-boot:run`
- Run the frontend
  - `cd ui && npm run dev`

## Running the application

- Open [http://localhost:3000](http://localhost:3000) in a browser

## Implementation Details

### Overview

PickBox is designed with modularity and scalability in mind. It uses MongoDB for data and a flexible storage layer that can handle anything from your local disk (default) to cloud buckets like AWS S3. Everything is built on interfaces, so if you ever need to swap out the database or storage system, it's a breeze.
