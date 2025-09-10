# Spendi Backend

Spendi Backend is the server-side application for **Spendi** – a personal finance management system.  
It provides REST API for tracking expenses, managing stores, and handling transactions.

---

## 🚀 Tech Stack

- **Language**: Java 21
- **Build Tool**: Maven
- **Database**: MongoDB
- **Cache**: Redis
<!-- TODO - **Validation**: Jakarta Bean Validation (Hibernate Validator)  -->
- **Architecture**: Clean layered architecture (Controller → Service → Repository)
- **Error Handling**: DomainException + unified API error responses

---

## 📂 Project Structure

```
src/
 ├── main/
 │   ├── java/com/spendi/
 │   │   ├── core/        # Core modules (response, exceptions, base classes)
 │   │   ├── modules/     # Business modules (transactions, stores, users, etc.)
 │   │   └── App.java  # Application entry point
 │   └── resources/
 │       └── application.yml  # Configurations
 └── test/                  # Unit and integration tests
```

---

## ⚡ Features

- Expense and income tracking
- Support for multiple stores (physical or online)
- Transaction details with itemized purchase lists
- Unified API responses (success & error)
- Strongly typed error handling with `DomainException`
- MongoDB for persistence, Redis for caching

---

## 🔧 Setup & Run

### Prerequisites
- Java 21
- Maven 3.9+
- MongoDB running locally or in Docker
- Redis running locally or in Docker

### Clone and build

```bash
git clone https://github.com/chyVacheck/spendi-backend.git
cd spendi-backend
mvn clean install
```

### Run the application

```bash
<!-- TODO написать корректную команду для запуска  -->
mvn spring-boot:run
```

Or build a jar and run:

```bash
mvn package
java -jar target/spendi-backend-0.0.1-SNAPSHOT.jar
```

---

## ⚙️ Configuration

Environment variables are managed through `application.yml` or `.env` file.

Example:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/
	  db: spendi
redis:
  host: localhost
  port: 6379
```

---

## 🛠 Development Notes

- Use **ServiceResponse** for consistent API replies.
- Throw **DomainException** in services for business logic errors.
- Follow clean separation: Controller → Service → Repository.

---

## 📌 Roadmap

- [ ] User authentication & roles
- [ ] Budget planning
- [ ] Statistics & analytics module
- [ ] Integration with bank APIs

---

## 👨‍💻 Author

Created by **Dmytro Shakh**  
[LinkedIn](https://www.linkedin.com/in/dmytro-shakh/)
