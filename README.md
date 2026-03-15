# 📸 VibeLens - Backend

<div align="center">

![Live Website](https://img.shields.io/badge/Live-https://vibelens.me-00D09C?style=for-the-badge)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-7.0-47A248?style=for-the-badge&logo=mongodb&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)

**RESTful API backend for VibeLens - A modern photo sharing platform**

[Live Demo](https://vibelens.me) •
[Features](#-features) •
[Getting Started](#-getting-started) •
[API Documentation](#-api-documentation) •
[Architecture](#-architecture)

</div>

---

## ✨ Features

### Core Features
- 🔐 **Authentication** - JWT + OAuth2 (Google) authentication with role-based access
- 📷 **Photo Management** - Upload, CRUD operations with Cloudinary integration
- ❤️ **Social Features** - Likes, comments, shares, favorites
- 👥 **User Relations** - Follow/unfollow system with follower/following counts
- 💬 **Real-time Messaging** - WebSocket with Socket.IO for instant messaging
- 🔍 **Search** - User and photo search with pagination
- 📰 **Newsfeed** - Personalized feed algorithm based on followed users
- 🤖 **AI Integration** - Caption suggestions and photo recommendations
- 📊 **Analytics** - User activity tracking and post analytics
- 🏷️ **Tags** - Photo tagging and discovery system

### Technical Features
- 📊 **Caching** - Redis for performance optimization and session management
- 📈 **Monitoring** - Prometheus + Actuator metrics for observability
- 🐰 **Message Queue** - RabbitMQ for async task processing (email, notifications)
- 📧 **Email Service** - Password reset and notification emails via SMTP
- 📝 **API Docs** - Swagger/OpenAPI interactive documentation
- 🐳 **Containerization** - Full Docker and Docker Compose support

---

## 🚀 Getting Started

### Prerequisites

- **Java** 21+
- **Maven** 3.9+
- **MongoDB** 7.0+
- **Redis** 7.0+
- **RabbitMQ** 3.13+ (optional, for async tasks)
- **Docker** & **Docker Compose** (recommended)

### Installation

#### Option 1: Local Development

```bash
# Clone repository
git clone <repository-url>
cd backend-photo-share

# Create environment file
cp .env.example .env
# Edit .env with your configurations

# Run with Maven
./mvnw spring-boot:run
```

#### Option 2: Docker Compose (Recommended)

```bash
# Start all services (MongoDB, Redis, RabbitMQ, Backend)
docker-compose -f docker-compose.yml up -d

# View logs
docker-compose logs -f app
```

#### Option 3: Production Docker Compose

```bash
# Full production stack with frontend, backend, and infrastructure
docker-compose -f ../docker-compose.production.yml up -d
```

### Environment Variables

Create a `.env` file in the root directory:

```env
# Application
APP_PORT=8080
APP_URL=http://localhost:8080

# MongoDB
MONGO_HOST=localhost
MONGO_PORT=27017
MONGO_DATABASE=vibelens
MONGO_USERNAME=admin
MONGO_PASSWORD=your-password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your-password

# RabbitMQ (Optional - for async tasks)
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000

# Cloudinary
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret

# OAuth2 (Google)
GOOGLE_CLIENT_ID=your-client-id
GOOGLE_CLIENT_SECRET=your-client-secret

# Email (SMTP)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email
MAIL_PASSWORD=your-app-password

# Frontend URL (CORS)
FRONTEND_URL=http://localhost:5173
```

---

## 📖 API Documentation

### Interactive Documentation

When running locally, access Swagger UI at:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

### API Endpoints Overview

#### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/register` | Register new user |
| `POST` | `/api/auth/login` | Login and get JWT |
| `POST` | `/api/auth/forgot-password` | Request password reset |
| `POST` | `/api/auth/reset-password` | Reset password with token |
| `GET` | `/oauth2/authorization/google` | OAuth2 Google login |

#### Users

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/users/me` | Get current user profile |
| `GET` | `/api/users/{id}` | Get user by ID |
| `PUT` | `/api/users/profile` | Update profile |
| `PUT` | `/api/users/profile/image` | Update profile image |

#### Photos

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/photos` | Upload new photo |
| `GET` | `/api/photos/{id}` | Get photo details |
| `GET` | `/api/photos/user/{userId}` | Get user's photos |
| `DELETE` | `/api/photos/{id}` | Delete photo |

#### Social Interactions

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/likes/{photoId}` | Like a photo |
| `DELETE` | `/api/likes/{photoId}` | Unlike a photo |
| `POST` | `/api/comments` | Add comment |
| `DELETE` | `/api/comments/{id}` | Delete comment |
| `POST` | `/api/favorites/{photoId}` | Save to favorites |
| `DELETE` | `/api/favorites/{photoId}` | Remove from favorites |
| `POST` | `/api/shares` | Share a photo |

#### Follow System

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/follow/{userId}` | Follow user |
| `DELETE` | `/api/follow/{userId}` | Unfollow user |
| `GET` | `/api/follow/{userId}/followers` | Get followers |
| `GET` | `/api/follow/{userId}/following` | Get following |

#### Newsfeed & Search

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/newsfeed` | Get personalized feed |
| `GET` | `/api/search/users` | Search users |
| `GET` | `/api/search/explore` | Get explore feed |

#### Messages

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/messages/conversations` | Get conversations |
| `GET` | `/api/messages/{conversationId}` | Get messages |
| `POST` | `/api/messages` | Send message |

#### AI Features

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/ai/caption` | Generate caption |
| `GET` | `/api/recommendations/related/{photoId}` | Get related photos |
| `GET` | `/api/recommendations/users` | Get suggested users |

---

## 🏗️ Architecture

### Project Structure

```
src/main/java/share_app/tphucshareapp/
├── TphucshareappApplication.java    # Main application
│
├── config/                          # Configuration classes
│   ├── SecurityConfig.java          # Spring Security
│   ├── WebConfig.java               # CORS, MVC config
│   ├── RedisConfig.java             # Redis configuration
│   ├── CloudinaryConfig.java        # Cloudinary setup
│   └── SocketIOConfig.java          # WebSocket config
│
├── controller/                      # REST Controllers
│   ├── AuthController.java
│   ├── UserController.java
│   ├── PhotoController.java
│   ├── CommentController.java
│   ├── LikeController.java
│   ├── FollowController.java
│   ├── FavoriteController.java
│   ├── ShareController.java
│   ├── MessageController.java
│   ├── NewsfeedController.java
│   ├── SearchController.java
│   ├── RecommendationController.java
│   ├── AIController.java
│   ├── TagController.java
│   └── HealthController.java
│
├── service/                         # Business logic
│   ├── auth/
│   │   └── AuthService.java
│   ├── user/
│   │   └── UserService.java
│   ├── photo/
│   │   └── PhotoService.java
│   ├── comment/
│   │   └── CommentService.java
│   ├── like/
│   │   └── LikeService.java
│   ├── follow/
│   │   └── FollowService.java
│   ├── favorite/
│   │   └── FavoriteService.java
│   ├── share/
│   │   └── ShareService.java
│   ├── message/
│   │   └── MessageService.java
│   ├── search/
│   │   └── SearchService.java
│   ├── ai/
│   │   └── AIService.java
│   ├── email/
│   │   └── EmailService.java
│   ├── tag/
│   │   └── TagService.java
│   └── scheduler/
│       └── SchedulerService.java
│
├── model/                           # Domain entities
│   ├── User.java
│   ├── Photo.java
│   ├── Comment.java
│   ├── Like.java
│   ├── Follow.java
│   ├── Favorite.java
│   ├── Share.java
│   ├── Message.java
│   ├── Conversation.java
│   └── Tag.java
│
├── dto/                             # Data Transfer Objects
│   ├── request/
│   └── response/
│
├── repository/                      # MongoDB repositories
│
├── security/                        # Security components
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── OAuth2SuccessHandler.java
│
├── enums/                           # Enumerations
│   └── UserRole.java
│
├── event/                           # Event handling
│
└── exceptions/                      # Custom exceptions
    ├── GlobalExceptionHandler.java
    └── CustomExceptions.java
```

### System Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              CLIENT                                      │
│                        (React Frontend)                                  │
└─────────────────────────────────┬───────────────────────────────────────┘
                                  │
                    ┌─────────────┴─────────────┐
                    │                           │
                    ▼                           ▼
         ┌─────────────────┐         ┌─────────────────┐
         │   REST API      │         │   WebSocket     │
         │   (Port 8080)   │         │   (Port 9092)   │
         └────────┬────────┘         └────────┬────────┘
                  │                           │
                  └─────────────┬─────────────┘
                                │
                                ▼
         ┌─────────────────────────────────────────────┐
         │              SPRING BOOT APP                │
         │  ┌─────────────────────────────────────┐   │
         │  │           Controllers               │   │
         │  └─────────────────┬───────────────────┘   │
         │                    │                       │
         │  ┌─────────────────▼───────────────────┐   │
         │  │            Services                 │   │
         │  └─────────────────┬───────────────────┘   │
         │                    │                       │
         │  ┌─────────────────▼───────────────────┐   │
         │  │           Repositories              │   │
         │  └─────────────────────────────────────┘   │
         └─────────────────────────────────────────────┘
                                │
          ┌─────────────────────┼─────────────────────┐
          │                     │                     │
          ▼                     ▼                     ▼
┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐
│    MongoDB      │   │     Redis       │   │   Cloudinary    │
│   (Database)    │   │    (Cache)      │   │   (Storage)     │
└─────────────────┘   └─────────────────┘   └─────────────────┘
```

### Entity State Diagrams

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         USER LIFECYCLE                                  │
│                                                                         │
│   ┌───────────┐     POST /register     ┌───────────┐                   │
│   │  UNKNOWN  │───────────────────────►│REGISTERED │                   │
│   └───────────┘                        └─────┬─────┘                   │
│                                              │                          │
│                                    POST /login                          │
│                                              │                          │
│                                              ▼                          │
│                        ┌─────────────────────────────────┐              │
│                        │          AUTHENTICATED          │              │
│                        │  ┌─────────────────────────┐   │              │
│                        │  │  - Access all features  │   │              │
│                        │  │  - JWT token valid      │   │              │
│                        │  └─────────────────────────┘   │              │
│                        └─────────────┬───────────────────┘              │
│                                      │                                  │
│                            Token expired / Logout                       │
│                                      │                                  │
│                                      ▼                                  │
│                              ┌───────────────┐                          │
│                              │ UNAUTHENTICATED│                         │
│                              └───────────────┘                          │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘


┌─────────────────────────────────────────────────────────────────────────┐
│                         PHOTO LIFECYCLE                                 │
│                                                                         │
│  ┌──────────┐   POST /photos   ┌──────────┐   Store    ┌──────────┐   │
│  │ CREATING │─────────────────►│ UPLOADED │───────────►│  ACTIVE  │   │
│  └──────────┘                  └──────────┘            └────┬─────┘   │
│                                                             │          │
│                              ┌───────────────────────────────┤          │
│                              │               │               │          │
│                              ▼               ▼               ▼          │
│                        ┌──────────┐   ┌──────────┐   ┌──────────┐      │
│                        │  LIKED   │   │COMMENTED │   │  SHARED  │      │
│                        └──────────┘   └──────────┘   └──────────┘      │
│                                                                         │
│                    DELETE /photos/{id}                                  │
│                              │                                          │
│                              ▼                                          │
│                        ┌──────────┐                                     │
│                        │ DELETED  │                                     │
│                        └──────────┘                                     │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘


┌─────────────────────────────────────────────────────────────────────────┐
│                       FOLLOW RELATIONSHIP                               │
│                                                                         │
│        ┌───────────────┐                    ┌───────────────┐           │
│        │ NOT_FOLLOWING │◄──────────────────►│   FOLLOWING   │           │
│        └───────────────┘  POST/DELETE       └───────────────┘           │
│                           /follow/{id}                                  │
│                                                                         │
│   Effects:                                                              │
│   ┌─────────────────────────────────────────────────────────────┐      │
│   │  Follow:                                                     │      │
│   │    - Add to follower's following list                       │      │
│   │    - Add to target's followers list                         │      │
│   │    - Update counts on both users                            │      │
│   │    - Target's posts appear in follower's newsfeed           │      │
│   │                                                              │      │
│   │  Unfollow:                                                   │      │
│   │    - Remove from both lists                                  │      │
│   │    - Update counts                                           │      │
│   │    - Remove from newsfeed                                    │      │
│   └─────────────────────────────────────────────────────────────┘      │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

| Category | Technology |
|----------|------------|
| **Framework** | Spring Boot 3.4.3 |
| **Language** | Java 21 |
| **Database** | MongoDB 7.0 |
| **Cache** | Redis 7.x |
| **Message Queue** | RabbitMQ 3.13 |
| **Authentication** | Spring Security + JWT + OAuth2 (Google) |
| **File Storage** | Cloudinary |
| **Real-time** | Netty Socket.IO |
| **Documentation** | SpringDoc OpenAPI |
| **Monitoring** | Spring Actuator + Prometheus |
| **Email** | Spring Mail |
| **Mapping** | ModelMapper |

---

## 📊 Monitoring

### Health Check

```bash
GET /actuator/health
```

### Prometheus Metrics

```bash
GET /actuator/prometheus
```

### Available Endpoints

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Application health status |
| `/actuator/info` | Application information |
| `/actuator/metrics` | Application metrics |
| `/actuator/prometheus` | Prometheus format metrics |

---

## 🐳 Docker

### Development Stack (docker-compose.yml)

```yaml
services:
  app:         # Spring Boot application
  mongodb:     # MongoDB database
  redis:       # Redis cache
  rabbitmq:    # RabbitMQ message broker
```

### Production Stack (docker-compose.production.yml)

Full production deployment including:
- Frontend (Nginx)
- Backend (Spring Boot)
- MongoDB
- Redis
- RabbitMQ

### Commands

```bash
# Development
docker-compose up -d --build

# Production
docker-compose -f ../docker-compose.production.yml up -d --build

# View logs
docker-compose logs -f

# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

---

## 🔧 Development

### Running Tests

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report
```

### Build for Production

```bash
# Create JAR file
./mvnw clean package -DskipTests

# Run JAR
java -jar target/tphucshareapp-0.0.1-SNAPSHOT.jar
```

### Code Quality

- Follow Java naming conventions
- Use Lombok for boilerplate reduction
- Write unit tests for services
- Document APIs with OpenAPI annotations

---

## 📄 License

This project is private and for educational purposes.

---

<div align="center">

**Built with ❤️ using Spring Boot**

[Live Demo](https://vibelens.me) • [API Docs](https://vibelens.me/api-docs)

</div>
