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
- Setup MongoDB in any preferred way and add the connection string in the .env file
- Use the maven wrapper to build the backend
  - `./mvnw clean install`
- Use the npm to build the frontend
  - `npm run build`
- Run the backend
  - `./mvnw spring-boot:run`
- Run the frontend
  - `npm run dev`

## Running the application

- Open the application in your browser
- Go to [http://localhost:3000](http://localhost:3000)