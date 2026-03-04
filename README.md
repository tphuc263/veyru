# 📸 Photo Share - Backend

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

**RESTful API backend for the Photo Share application**

[Features](#-features) •
[Getting Started](#-getting-started) •
[API Documentation](#-api-documentation) •
[Architecture](#-architecture)

</div>

---

## ✨ Features

### Core Features
- 🔐 **Authentication** - JWT + OAuth2 (Google) authentication
- 📷 **Photo Management** - Upload, CRUD operations with Cloudinary
- ❤️ **Social Features** - Likes, comments, shares, favorites
- 👥 **User Relations** - Follow/unfollow system
- 💬 **Real-time Messaging** - WebSocket with Socket.IO
- 🔍 **Search** - User search with pagination
- 📰 **Newsfeed** - Personalized feed algorithm
- 🤖 **AI Integration** - Caption suggestions and recommendations

### Technical Features
- 📊 **Caching** - Redis for performance optimization
- 📈 **Monitoring** - Prometheus + Actuator metrics
- 📧 **Email Service** - Password reset emails
- 📝 **API Docs** - Swagger/OpenAPI documentation
- 🐳 **Containerization** - Docker support

---

## 🚀 Getting Started

### Prerequisites

- **Java** 21+
- **Maven** 3.9+
- **MongoDB** 6.0+
- **Redis** 7.0+
- **Docker** (optional)

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

#### Option 2: Docker Compose

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f app
```

### Environment Variables

Create a `.env` file in the root directory:

```env
# MongoDB
MONGO_HOST=localhost
MONGO_PORT=27017
MONGO_DATABASE=share_app_database

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

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

# AI Service (Optional)
AI_SERVICE_URL=http://localhost:5000
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
| **Database** | MongoDB |
| **Cache** | Redis |
| **Authentication** | Spring Security + JWT + OAuth2 |
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

### Docker Compose Services

```yaml
services:
  app:         # Spring Boot application
  mongodb:     # MongoDB database
  redis:       # Redis cache
```

### Commands

```bash
# Build and start
docker-compose up -d --build

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

</div>
