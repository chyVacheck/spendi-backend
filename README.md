# Spendi Backend

[![Java Version](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/Maven-3.9%2B-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

Spendi Backend is a robust, scalable server-side application for **Spendi** – a comprehensive personal finance management system. Built with modern Java technologies, it provides a RESTful API for managing financial transactions, user accounts, and store information.

## 🚀 Features

- **Expense & Income Tracking** - Record and categorize financial transactions
- **Multi-Store Support** - Manage transactions across various physical and online stores
- **User Management** - Secure authentication and user profile management
- **RESTful API** - Clean, consistent API design with proper HTTP status codes
- **Documented** - Comprehensive JavaDoc and inline code documentation
- **Container Ready** - Easy deployment with Docker

## 🛠 Tech Stack

- **Language**: Java 21
- **Framework**: Javalin 5.6.2 (Lightweight web framework)
- **Build Tool**: Maven 3.9+
- **Database**: MongoDB (Document storage)
- **Caching**: Redis (Session and data caching)
- **Validation**: Jakarta Bean Validation (Hibernate Validator)
- **Architecture**: Clean layered architecture (Controller → Service → Repository)
- **Error Handling**: DomainException with unified API error responses
- **API Documentation**: OpenAPI/Swagger (Planned)

## 📦 Prerequisites

- Java Development Kit (JDK) 21 or later
- Maven 3.9 or later
- MongoDB 5.0+ (running locally)
- Git (for version control)

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/chyVacheck/spendi-backend.git
cd spendi-backend
```

### 2. Build the Application

```bash
mvn clean install
```

### 3. Configure the Application

Create a `.env` file in the project root with your configuration:

```env
# MongoDB Configuration
MONGODB_URI=mongodb://localhost:27017/
MONGODB_DB=spendi

# Server Configuration
SERVER_PORT=6070
NODE_ENV=development
```

### 4. Run the Application

#### Option 1: Using Start Scripts

```bash
# macOS/Linux
./start.sh

# Windows
start.bat
```

#### Option 2: Using Maven Directly

```bash
# Development mode with auto-reload
mvn clean compile exec:java

# Or build and run the JAR
mvn clean package
java -jar target/spendi-1.0-SNAPSHOT.jar
```

#### Option 3: Using Dev Script (Recommended for Development)

```bash
# Make the script executable (first time only)
chmod +x dev.sh

# Show all available commands
./dev.sh

# Start the application
./dev.sh start

# Run in development mode with hot-reload
./dev.sh dev
```

## 🏗 Project Structure

```
spendi-backend/
├── src/
│   ├── main/
│   │   ├── java/com/spendi/
│   │   │   ├── config/         # Application configuration
│   │   │   ├── core/           # Core components (base classes, utilities)
│   │   │   │   ├── base/       # Base classes for controllers, services, etc.
│   │   │   │   ├── exceptions/ # Custom exceptions
│   │   │   │   └── response/   # API response models
│   │   │   ├── modules/        # Business modules
│   │   │   │   ├── auth/       # Authentication module
│   │   │   │   ├── user/       # User management
│   │   │   │   └── ...         # Other business modules
│   │   │   └── App.java        # Application entry point
│   │   └── resources/          # Configuration files
│   │       └── application.yml # Main configuration
│   └── test/                   # Unit and integration tests
├── .gitignore                 # Git ignore rules
├── dev.sh                     # Development utility script
├── pom.xml                    # Maven configuration
└── README.md                  # This file
```

## ⚙️ Configuration

Configuration is managed through multiple sources with the following priority (highest to lowest):

1. Environment variables
2. `.env` file in the project root

## 🛠 Development

### Available Scripts

Use the `dev.sh` script for common development tasks:

```bash
# Build the project
./dev.sh build

# Run tests
./dev.sh test

# Format code (using Google Java Format)
./dev.sh format

# Run checkstyle
./dev.sh checkstyle

# Clean build artifacts
./dev.sh clean
```

### Code Style

This project follows the Google Java Style Guide with some modifications. Before committing, please ensure your code passes the following checks:

```bash
# Check code style
mvn checkstyle:check

# Format code (if you have the formatter plugin installed)
mvn com.coveo:fmt-maven-plugin:format
```

## 📚 API Documentation

API documentation is available at `/api-docs` when running in development mode. (Planned)

## 🐳 Docker Support

### Using Docker Compose (Recommended)

```bash
# Start MongoDB and Redis
# docker-compose -f docker-compose.dev.yml up -d

# Build and run the application
# docker-compose -f docker-compose.yml up --build
```

### Building a Docker Image

```bash
# Build the application JAR
mvn clean package

# Build the Docker image
docker build -t spendi-backend:latest .

# Run the container
docker run -p 7000:7000 --env-file .env spendi-backend:latest
```

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Dmytro Shakh**  
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-blue?style=flat&logo=linkedin)](https://www.linkedin.com/in/dmytro-shakh/)
[![GitHub](https://img.shields.io/badge/GitHub-Follow-black?style=flat&logo=github)](https://github.com/chyVacheck)

## 📌 Roadmap

- [X] User authentication
- [ ] Docs
- [ ] Budget planning and management
- [ ] Statistics & analytics dashboard
- [ ] Integration with bank APIs
- [ ] Multi-currency support
- [ ] Automated backups
- [ ] Email notifications
- [ ] Mobile app API endpoints
- [ ] Cloud deployment
- [ ] User feedback integration
- [ ] Scalability improvements
- [ ] Performance optimizations
- [ ] Security enhancements
- [ ] Code documentation
- [ ] Community support
