# Gradsy Project Documentation

---

## 1. Project Overview

**Project Name:** Gradsy  
**Description:**  
Gradsy is a web-based application designed to manage quizzes, learning materials, and user roles (students, teachers). It provides a platform for teachers to create and manage quizzes and materials, and for students to attempt quizzes and access learning resources. The application uses a modern Java backend (Spring Boot), a Vaadin-based frontend, and an H2 file-based database for local development.

---

## 2. Technology Stack

- **Backend:** Java 17, Spring Boot 3.4.4
- **Frontend:** Vaadin 24, React, TypeScript
- **Database:** H2 (file-based, for local development)
- **Build Tools:** Maven
- **Other:** JPA (Hibernate), Vaadin TestBench, Docker (optional)

---

## 3. Directory and File Structure

### Root Directory
- **README.md**: Project introduction and summary.
- **pom.xml**: Maven build configuration, dependencies, plugins, and project metadata.
- **package.json**: Frontend dependencies and scripts for building the UI.
- **tsconfig.json**: TypeScript compiler configuration for the frontend.
- **types.d.ts**: TypeScript module/type declarations for custom imports.
- **Dockerfile**: (Optional) Docker image build instructions.
- **docker-compose.yml**: (Optional) Multi-container Docker setup.
- **.gitignore**: Specifies files/folders to be ignored by Git.
- **.hintrc**: Configuration for code linting/hinting.
- **mvnw, mvnw.cmd, .mvn/**: Maven wrapper scripts and configuration for consistent builds.
- **data/**: Stores H2 database files for local persistence.
- **uploads/**: Stores uploaded files (e.g., PDFs) during app usage.

### `src/` Directory
- **main/**: Contains the main application code.
  - **java/**: Java source code, organized by package.
    - **com/example/application/**: Main application package.
      - **Application.java**: Main entry point for the Spring Boot app.
      - **data/**: Entity classes and repositories for database access.
      - **views/**: UI views for login, signup, dashboards, quizzes, etc.
      - **services/**: Business logic and service classes.
      - **security/**: Security configuration and user authentication.
      - **repositories/**: Custom repository interfaces.
      - **controllers/**: REST and file upload controllers.
  - **resources/**: Application resources.
    - **application.properties**: Main configuration (DB, server, logging, etc.).
    - **banner.txt**: ASCII art banner shown at startup.
    - **data.sql, init.sql, schema.sql**: SQL scripts for DB initialization.
    - **META-INF/**: Static resources (icons, images, offline page).
  - **frontend/**: Frontend source code.
    - **index.html**: Main HTML entry point.
    - **generated/**: Auto-generated frontend files (routes, themes, etc.).
    - **themes/**: Custom Vaadin themes.
    - **views/**: Frontend view components.

- **test/**: Contains test code.
  - **java/**: Java test code, organized by package.
    - **views/**: Test cases for UI views.
    - **it/**: Integration and end-to-end tests.

---

## 4. Configuration Files

### `pom.xml`
Defines project metadata, dependencies (Spring Boot, Vaadin, H2, JPA, etc.), plugins, and build profiles.

### `package.json`
Lists frontend dependencies (React, Vaadin, TypeScript, etc.) and scripts for building/testing the UI.

### `application.properties`
Configures database connection, JPA, server port, logging, file upload limits, and H2 console access.

### `tsconfig.json` & `types.d.ts`
TypeScript compiler options and custom type/module declarations for the frontend.

---

## 5. How the Project Works

1. **Startup:**
   - The application starts via `Application.java` (Spring Boot main class).
   - The ASCII banner from `banner.txt` is displayed.
   - Spring Boot auto-configures the application using `application.properties`.

2. **Database:**
   - Uses H2 file-based DB for local development (`data/prodDb.mv.db`).
   - Entities in `data/` package map to DB tables.
   - Repositories provide CRUD access to entities.

3. **Backend Logic:**
   - Services in `services/` handle business logic (user management, quizzes, materials).
   - Controllers in `controllers/` expose REST endpoints and file upload/download features.
   - Security is managed via `security/` (authentication, authorization).

4. **Frontend:**
   - Vaadin and React-based UI in `frontend/` and `views/`.
   - Views for login, signup, dashboards, quizzes, materials, etc.
   - Communicates with backend via REST endpoints and Vaadin APIs.

5. **File Uploads:**
   - Uploaded files are stored in the `uploads/` directory.

6. **Testing:**
   - Test cases for UI and integration in `test/java/`.

---

## 6. How to Run the Project

1. **Prerequisites:**
   - Java 17+
   - Maven

2. **Build and Run:**
   - `./mvnw clean install` (builds the project)
   - `./mvnw spring-boot:run` (runs the app)
   - Access the app at `http://localhost:8085`

3. **H2 Console:**
   - Access at `http://localhost:8085/h2-console` (JDBC URL: `jdbc:h2:file:./data/prodDb`)

---

## 7. Folder and File Details

### Root Files
- **README.md**: Project summary.
- **pom.xml**: Maven build and dependency management.
- **package.json**: Frontend dependencies and scripts.
- **tsconfig.json**: TypeScript config.
- **types.d.ts**: TypeScript type declarations.
- **Dockerfile/docker-compose.yml**: (Optional) For containerization.
- **.gitignore**: Files/folders to ignore in version control.
- **mvnw, mvnw.cmd, .mvn/**: Maven wrapper for consistent builds.
- **data/**: H2 database files (local data persistence).
- **uploads/**: Uploaded files (e.g., PDFs).

### `src/main/java/com/example/application/`
- **Application.java**: Main Spring Boot entry point.
- **data/**: Entity classes (User, Quiz, Note, etc.) and repositories.
- **views/**: UI views for login, signup, dashboards, quizzes, materials, etc.
- **services/**: Business logic (user, quiz, material, note services).
- **security/**: Security config and user authentication.
- **repositories/**: Custom repository interfaces.
- **controllers/**: REST and file upload controllers.

### `src/main/resources/`
- **application.properties**: Main configuration file.
- **banner.txt**: Startup banner.
- **data.sql, init.sql, schema.sql**: SQL scripts for DB setup.
- **META-INF/**: Static resources (icons, images, offline page).

### `src/main/frontend/`
- **index.html**: Main HTML entry point.
- **generated/**: Auto-generated frontend files.
- **themes/**: Custom Vaadin themes.
- **views/**: Frontend view components.

### `src/test/java/`
- **views/**: Test cases for UI views.
- **it/**: Integration and end-to-end tests.

---

## 8. Additional Notes

- **Database files in `data/`** are for local development and should not be committed to version control for production.
- **Uploads in `uploads/`** are user-generated and can be cleaned up as needed.
- **Docker files** are optional and only needed if you want to containerize the app.
- **All configuration files** are well-commented for easy customization.

---

# End of Documentation 