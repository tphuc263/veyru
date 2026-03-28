# 📸 VibeLens - Backend

<div align="center">

![Neo4j](https://img.shields.io/badge/Neo4j-5.19-008CC1?style=for-the-badge&logo=neo4j&logoColor=white)
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
| `POST` | `/api/v1/auth/register` | Register new user |
| `POST` | `/api/v1/auth/login` | Login and get JWT |
| `POST` | `/api/v1/auth/forgot-password` | Request password reset |
| `POST` | `/api/v1/auth/reset-password` | Reset password with token |
| `GET` | `/api/v1/auth/validate-reset-token` | Validate reset token |
| `GET` | `/oauth2/authorization/google` | OAuth2 Google login |

#### Users

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/users/me` | Get current user profile |
| `GET` | `/api/v1/users/{id}` | Get user by ID |
| `PUT` | `/api/v1/users/profile` | Update profile |
| `PUT` | `/api/v1/users/profile/image` | Update profile image |

#### Photos

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/photos` | Upload new photo (multipart) |
| `GET` | `/api/v1/photos/{id}` | Get photo details |
| `GET` | `/api/v1/photos/user/{userId}` | Get user's photos |
| `GET` | `/api/v1/photos/user/{userId}/paged` | Get user's photos (paginated) |
| `DELETE` | `/api/v1/photos/{id}` | Delete photo |

#### Social Interactions

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/likes/{photoId}` | Like a photo |
| `DELETE` | `/api/v1/likes/{photoId}` | Unlike a photo |
| `GET` | `/api/v1/likes/photo/{photoId}` | Get likes for a photo |
| `POST` | `/api/v1/comments` | Add comment |
| `GET` | `/api/v1/comments/photo/{photoId}` | Get comments for a photo |
| `DELETE` | `/api/v1/comments/{id}` | Delete comment |
| `POST` | `/api/v1/favorites/{photoId}` | Save to favorites |
| `DELETE` | `/api/v1/favorites/{photoId}` | Remove from favorites |
| `GET` | `/api/v1/favorites` | Get user's favorites |
| `POST` | `/api/v1/shares` | Share a photo |

#### Follow System

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/follow/{userId}` | Follow user |
| `DELETE` | `/api/v1/follow/{userId}` | Unfollow user |
| `GET` | `/api/v1/follow/{userId}/followers` | Get followers |
| `GET` | `/api/v1/follow/{userId}/following` | Get following |

#### Newsfeed & Search

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/newsfeed` | Get personalized feed |
| `GET` | `/api/v1/newsfeed/graph` | Get graph-based feed |
| `GET` | `/api/v1/newsfeed/explore` | Get explore feed |
| `GET` | `/api/v1/search/users` | Search users |
| `GET` | `/api/v1/search/explore` | Get explore feed |
| `GET` | `/api/v1/search/photos` | Search photos |

#### Messages

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/messages/conversations` | Get conversations |
| `GET` | `/api/v1/messages/{conversationId}` | Get messages in conversation |
| `POST` | `/api/v1/messages` | Send message |
| `PUT` | `/api/v1/messages/{conversationId}/read` | Mark messages as read |

#### Notifications

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/notifications` | Get user notifications |
| `PUT` | `/api/v1/notifications/{id}/read` | Mark notification as read |
| `PUT` | `/api/v1/notifications/read-all` | Mark all as read |

#### Graph Feed

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/graph-feed` | Get Dijkstra-ranked personalized feed |
| `GET` | `/api/v1/graph-feed/explore` | Get graph-based explore feed |

#### AI & Recommendations

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/ai/caption` | Generate AI caption suggestion |
| `GET` | `/api/v1/recommendations/users` | Get suggested users to follow |
| `GET` | `/api/v1/recommendations/related/{photoId}` | Get related photos |

#### Tags

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/tags/popular` | Get popular tags |
| `GET` | `/api/v1/tags/{tag}/photos` | Get photos by tag |

#### User Tags

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/user-tags` | Tag users in a photo |
| `DELETE` | `/api/v1/user-tags/{id}` | Remove user tag |

#### System

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/health` | Health check |
| `GET` | `/actuator/health` | Actuator health |
| `GET` | `/actuator/prometheus` | Prometheus metrics |

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
│   ├── PostController.java
│   ├── CommentController.java
│   ├── LikeController.java
│   ├── FollowController.java
│   ├── FavoriteController.java
│   ├── ShareController.java
│   ├── MessageController.java
│   ├── NewsfeedController.java
│   ├── GraphFeedController.java
│   ├── SearchController.java
│   ├── RecommendationController.java
│   ├── AIController.java
│   ├── TagController.java
│   ├── UserTagController.java
│   ├── NotificationController.java
│   └── HealthController.java
│
├── service/                         # Business logic
│   ├── auth/
│   │   └── AuthService.java
│   ├── user/
│   │   ├── UserService.java
│   │   └── UserAvatarCacheService.java
│   ├── photo/
│   │   ├── PhotoService.java
│   │   └── CloudinaryService.java
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
│   ├── newsfeed/
│   │   └── NewsfeedService.java
│   ├── search/
│   │   └── SearchService.java
│   ├── ai/
│   │   ├── AIService.java
│   │   └── RecommendationService.java
│   ├── graph/
│   │   ├── Neo4jGraphService.java
│   │   ├── GraphSyncService.java
│   │   └── GraphFeedService.java
│   ├── notification/
│   │   └── NotificationService.java
│   ├── email/
│   │   └── EmailService.java
│   ├── tag/
│   │   └── TagService.java
│   └── scheduler/
│       └── SchedulerService.java
│
├── model/                           # Domain entities (MongoDB)
│   ├── User.java
│   ├── Photo.java
│   ├── Comment.java
│   ├── Like.java
│   ├── Follow.java
│   ├── Favorite.java
│   ├── Share.java
│   ├── Message.java
│   ├── Conversation.java
│   ├── Notification.java
│   └── Tag.java
│
│   ├── dto/                             # Data Transfer Objects
│   │   ├── request/
│   │   └── response/
│   │
│   ├── repository/                      # MongoDB repositories
│   │
│   ├── security/                        # Security components
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── OAuth2SuccessHandler.java
│   │   └── OAuth2FailureHandler.java
│   │
│   ├── graph/                           # Neo4j domain & repositories
│   │   ├── UserNode.java
│   │   ├── PhotoNode.java
│   │   └── (Relationships managed via Cypher)
│   │
│   ├── enums/                           # Enumerations
│   │   └── UserRole.java
│   │
│   ├── event/                           # Spring event handling
│   │   ├── PhotoCreatedEvent.java
│   │   ├── NotificationEvent.java
│   │   └── PhotoEventListener.java
│   │
│   ├── notification/                    # RabbitMQ notification
│   │   ├── NotificationProducer.java
│   │   └── NotificationConsumer.java
│   │
│   ├── websocket/                        # Socket.IO handling
│   │   └── SocketIOEventHandler.java
│   │
│   └── exceptions/                      # Custom exceptions
│       ├── GlobalExceptionHandler.java
│       └── CustomExceptions.java
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
         │   /api/v1/*     │         │   Socket.IO     │
         └────────┬────────┘         └────────┬────────┘
                  │                           │
                  └─────────────┬─────────────┘
                                │
                                ▼
         ┌──────────────────────────────────────────────────────┐
         │                   SPRING BOOT APP                     │
         │  ┌──────────────────────────────────────────────┐   │
         │  │  Security Layer: JWT Filter + OAuth2          │   │
         │  └──────────────────────────────────────────────┘   │
         │  ┌──────────────────────────────────────────────┐   │
         │  │  Controllers (20 REST endpoints)             │   │
         │  └──────────────────────────────────────────────┘   │
         │  ┌──────────────────────────────────────────────┐   │
         │  │  Services (auth, user, photo, social,         │   │
         │  │           message, newsfeed, ai, graph...)   │   │
         │  └──────────────────────────────────────────────┘   │
         │  ┌──────────────────────────────────────────────┐   │
         │  │  Event System: PhotoCreatedEvent → Listener   │   │
         │  └──────────────────────────────────────────────┘   │
         │  ┌──────────────────────────────────────────────┐   │
         │  │  RabbitMQ: Notification Producer → Consumer   │   │
         │  └──────────────────────────────────────────────┘   │
         └──────────────────────────────────────────────────────┘
                                │
          ┌──────────┬──────────┼────────┬──────────┬──────────┐
          │          │          │        │          │          │
          ▼          ▼          ▼        ▼          ▼          ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│    MongoDB      │ │     Redis       │ │   RabbitMQ      │ │     Neo4j       │
│   (Database)    │ │    (Cache)      │ │   (Queue)       │ │   (Graph DB)    │
│   Users, Photos │ │  Avatars, Feed │ │ Notifications   │ │ Follow graph,  │
│   Comments...   │ │  Sessions      │ │ Emails (async)  │ │ Feed ranking    │
└─────────────────┘ └─────────────────┘ └─────────────────┘ └─────────────────┘
                                │
                    ┌───────────┴───────────┐
                    │    Cloudinary         │
                    │  (Image Storage)      │
                    └───────────────────────┘
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
| **Graph Database** | Neo4j 5.19 (Cypher queries, Dijkstra algorithm) |
| **Cache** | Redis 7.x |
| **Message Queue** | RabbitMQ 3.13 |
| **Authentication** | Spring Security + JWT + OAuth2 (Google) |
| **File Storage** | Cloudinary |
| **Real-time** | Netty Socket.IO 2.0.12 |
| **Documentation** | SpringDoc OpenAPI |
| **Monitoring** | Spring Actuator + Prometheus |
| **Email** | Spring Mail |
| **Mapping** | ModelMapper |
| **Build** | Maven |

---

## 🗄️ Neo4j Graph Database

Neo4j stores the social graph and enables advanced feed ranking with Dijkstra-based algorithms.

### Node Types

| Node | Properties |
|------|------------|
| `User` | userId, username, avatarUrl, followerCount, followingCount |
| `Photo` | photoId, imageUrl, likeCount, commentCount, shareCount, tags, createdAt |

### Relationship Types

| Type | From → To | Properties |
|------|-----------|------------|
| `FOLLOWS` | User → User | weight=1.0, followedAt |
| `LIKED` | User → Photo | weight=1.0, likedAt |
| `POSTED_BY` | Photo → User | - |
| `COMMENTED` | User → Photo | weight=1.0, commentedAt |

### Feed Ranking Algorithm

The `Neo4jGraphService.getFeedWithDijkstra()` calculates a weighted relevance score for each photo:

```
relevanceScore = FOLLOW_WEIGHT × followScore
              + ENGAGEMENT_WEIGHT × engagementScore
              + RECENCY_WEIGHT × recencyScore
              + CONTENT_QUALITY_WEIGHT × qualityScore

Weights:
- FOLLOW_WEIGHT: 0.3 (mutual followers boost score)
- ENGAGEMENT_WEIGHT: 0.4 (likes×2 + comments×5 normalized)
- RECENCY_WEIGHT: 0.2 (<24h=100pts, <7d=50pts, else=10pts)
- CONTENT_QUALITY_WEIGHT: 0.1 (caption + tag density)
```

### Graph Sync

`GraphSyncService` synchronizes MongoDB data to Neo4j:
- Batch processing: 50 entities per batch
- Retry mechanism: 3 attempts with exponential backoff
- Schedulable via `SchedulerService` (configurable cron)

---

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

## 🔄 CI/CD

GitHub Actions automatically builds and deploys on every push to `main`.

### GitHub Actions Workflow (`.github/workflows/deploy.yml`)

```yaml
on:
  push:
    branches: [main]

jobs:
  build-and-deploy:
    steps:
      - Checkout code
      - Log in to GHCR (GitHub Container Registry)
      - Build & push Docker image to GHCR
      - SSH to VPS and run:
        - docker compose pull backend
        - docker compose up -d backend
```

### GitHub Secrets Required

| Secret | Description |
|--------|-------------|
| `VPS_HOST` | VPS IP address or hostname |
| `VPS_USER` | SSH username |
| `VPS_SSH_KEY` | Private SSH key for VPS access |

### Manual Deployment

```bash
# Push to main branch triggers auto-deploy
git push origin main
```

### Container Registry

- **Registry**: `ghcr.io/photo-sharing-platform/backend-photo-share`
- **Tags**: `latest`, `sha-{commit-hash}`

---

## 🐳 Docker

### Development Stack

```bash
# Start all services locally (MongoDB, Redis, RabbitMQ, Neo4j, Backend)
docker-compose -f docker-compose.yml up -d

# View logs
docker-compose logs -f backend
```

### Production Stack

```bash
# Deploy full stack on VPS (infrastructure + backend + frontend + SSL)
# Located in parent directory: ../docker-compose.yml
docker-compose -f ../docker-compose.yml up -d

# Stop services
docker-compose -f ../docker-compose.yml down
```

### Build Docker Image Locally

```bash
# Build backend image
docker build -t backend-photo-share .

# Run with docker-compose
docker-compose up -d --build
```

---

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
