# Backend Architecture & State Diagrams

> Technical documentation for the Photo Share Backend API

## Table of Contents

- [System Architecture](#system-architecture)
- [Entity State Machines](#entity-state-machines)
- [API Flow Diagrams](#api-flow-diagrams)
- [Database Schema](#database-schema)

---

## System Architecture

### High-Level Overview

```
                              ┌─────────────────────────────────────┐
                              │           EXTERNAL SERVICES          │
                              │  ┌─────────┐  ┌─────────┐  ┌─────┐  │
                              │  │Cloudinary│  │ Google  │  │SMTP │  │
                              │  │ (Images) │  │ OAuth2  │  │Email│  │
                              │  └─────────┘  └─────────┘  └─────┘  │
                              └─────────────────┬───────────────────┘
                                                │
┌───────────────────────────────────────────────┼───────────────────────────────────────────────┐
│                                   BACKEND APPLICATION                                         │
│                                                                                               │
│   ┌───────────────────────────────────────────────────────────────────────────────────────┐  │
│   │                              PRESENTATION LAYER                                        │  │
│   │                                                                                        │  │
│   │   ┌────────────────────────────┐           ┌────────────────────────────┐            │  │
│   │   │      REST Controllers      │           │    WebSocket Handler       │            │  │
│   │   │  ┌────────────────────┐   │           │  ┌────────────────────┐   │            │  │
│   │   │  │ AuthController     │   │           │  │ SocketIOServer     │   │            │  │
│   │   │  │ UserController     │   │           │  │ - onConnect        │   │            │  │
│   │   │  │ PhotoController    │   │           │  │ - onMessage        │   │            │  │
│   │   │  │ CommentController  │   │           │  │ - onTyping         │   │            │  │
│   │   │  │ LikeController     │   │           │  │ - onDisconnect     │   │            │  │
│   │   │  │ FollowController   │   │           │  └────────────────────┘   │            │  │
│   │   │  │ MessageController  │   │           │                            │            │  │
│   │   │  │ NewsfeedController │   │           │      Port: 9092            │            │  │
│   │   │  │ SearchController   │   │           │                            │            │  │
│   │   │  │ AIController       │   │           └────────────────────────────┘            │  │
│   │   │  └────────────────────┘   │                                                      │  │
│   │   │       Port: 8080          │                                                      │  │
│   │   └────────────────────────────┘                                                     │  │
│   └───────────────────────────────────────────────────────────────────────────────────────┘  │
│                                            │                                                  │
│   ┌───────────────────────────────────────────────────────────────────────────────────────┐  │
│   │                               SECURITY LAYER                                           │  │
│   │                                                                                        │  │
│   │   ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐                   │  │
│   │   │ SecurityConfig   │  │ JwtTokenProvider │  │ JwtAuthFilter    │                   │  │
│   │   │ - CORS           │  │ - generateToken  │  │ - validateToken  │                   │  │
│   │   │ - Endpoints      │  │ - validateToken  │  │ - setAuthContext │                   │  │
│   │   │ - OAuth2         │  │ - parseToken     │  │                  │                   │  │
│   │   └──────────────────┘  └──────────────────┘  └──────────────────┘                   │  │
│   │                                                                                        │  │
│   └───────────────────────────────────────────────────────────────────────────────────────┘  │
│                                            │                                                  │
│   ┌───────────────────────────────────────────────────────────────────────────────────────┐  │
│   │                               BUSINESS LAYER                                           │  │
│   │                                                                                        │  │
│   │   ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐   │  │
│   │   │ AuthService │ │ UserService │ │PhotoService │ │ LikeService │ │FollowService│   │  │
│   │   └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘   │  │
│   │                                                                                        │  │
│   │   ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐   │  │
│   │   │CommentService│ │MessageService│ │SearchService│ │ AIService │ │EmailService │   │  │
│   │   └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘   │  │
│   │                                                                                        │  │
│   └───────────────────────────────────────────────────────────────────────────────────────┘  │
│                                            │                                                  │
│   ┌───────────────────────────────────────────────────────────────────────────────────────┐  │
│   │                              DATA ACCESS LAYER                                         │  │
│   │                                                                                        │  │
│   │   ┌──────────────────────────────────────────────────────────────────────────────┐   │  │
│   │   │                        Spring Data MongoDB Repositories                       │   │  │
│   │   │  UserRepository │ PhotoRepository │ CommentRepository │ MessageRepository    │   │  │
│   │   │  LikeRepository │ FollowRepository │ FavoriteRepository │ ShareRepository    │   │  │
│   │   └──────────────────────────────────────────────────────────────────────────────┘   │  │
│   │                                                                                        │  │
│   └───────────────────────────────────────────────────────────────────────────────────────┘  │
│                                                                                               │
└───────────────────────────────────────────────────────────────────────────────────────────────┘
                                            │
                    ┌───────────────────────┼───────────────────────┐
                    │                       │                       │
                    ▼                       ▼                       ▼
           ┌───────────────┐       ┌───────────────┐       ┌───────────────┐
           │   MongoDB     │       │    Redis      │       │  File System  │
           │               │       │               │       │  (Cloudinary) │
           │  Collections: │       │  Cache Keys:  │       │               │
           │  - users      │       │  - user:*     │       │  - photos/    │
           │  - photos     │       │  - feed:*     │       │  - avatars/   │
           │  - comments   │       │  - session:*  │       │               │
           │  - likes      │       │               │       │               │
           │  - follows    │       │               │       │               │
           │  - messages   │       │               │       │               │
           │  - conversations│     │               │       │               │
           │  - favorites  │       │               │       │               │
           │  - shares     │       │               │       │               │
           │  - tags       │       │               │       │               │
           └───────────────┘       └───────────────┘       └───────────────┘
```

---

## Entity State Machines

### 1. User Authentication State

```
                    ┌───────────────────────────────────────────────────────────────┐
                    │                    AUTHENTICATION FLOW                         │
                    │                                                                │
                    │   ┌─────────────┐                                             │
                    │   │   UNKNOWN   │                                             │
                    │   └──────┬──────┘                                             │
                    │          │                                                     │
                    │          │ POST /api/auth/register                            │
                    │          │ {username, email, password}                        │
                    │          ▼                                                     │
                    │   ┌─────────────┐                                             │
                    │   │ REGISTERED  │─────────┐                                   │
                    │   └──────┬──────┘         │                                   │
                    │          │                │ Validation Error                  │
                    │          │                │ (duplicate email/username)        │
                    │          │                ▼                                    │
                    │          │         ┌─────────────┐                            │
                    │          │         │   ERROR     │                            │
                    │          │         │ 400/409     │                            │
                    │          │         └─────────────┘                            │
                    │          │                                                     │
                    │          │ POST /api/auth/login                               │
                    │          │ {identifier, password}                             │
                    │          ▼                                                     │
                    │   ┌─────────────┐         ┌─────────────┐                     │
                    │   │ VALIDATING  │────────►│   FAILED    │                     │
                    │   └──────┬──────┘ invalid │ 401 Unauth  │                     │
                    │          │                └─────────────┘                     │
                    │          │ valid                                              │
                    │          ▼                                                     │
                    │   ┌─────────────────────────────────────────┐                 │
                    │   │              AUTHENTICATED               │                 │
                    │   │  ┌─────────────────────────────────┐   │                 │
                    │   │  │  JWT Token Generated            │   │                 │
                    │   │  │  - userId in payload            │   │                 │
                    │   │  │  - expiration: 24h              │   │                 │
                    │   │  │  - role: USER/ADMIN             │   │                 │
                    │   │  └─────────────────────────────────┘   │                 │
                    │   └─────────────────────┬───────────────────┘                 │
                    │                         │                                      │
                    │          ┌──────────────┼──────────────┐                      │
                    │          │              │              │                      │
                    │          ▼              ▼              ▼                      │
                    │   Token Expired   Manual Logout   Invalid Token               │
                    │          │              │              │                      │
                    │          └──────────────┼──────────────┘                      │
                    │                         │                                      │
                    │                         ▼                                      │
                    │                  ┌─────────────┐                              │
                    │                  │UNAUTHENTICATED│                            │
                    │                  └─────────────┘                              │
                    │                                                                │
                    └───────────────────────────────────────────────────────────────┘
```

### 2. Photo Lifecycle

```
                    ┌───────────────────────────────────────────────────────────────┐
                    │                     PHOTO LIFECYCLE                            │
                    │                                                                │
                    │   ┌─────────────┐                                             │
                    │   │  CREATING   │                                             │
                    │   │             │                                             │
                    │   │  Client:    │                                             │
                    │   │  - image    │                                             │
                    │   │  - caption  │                                             │
                    │   │  - tags[]   │                                             │
                    │   └──────┬──────┘                                             │
                    │          │                                                     │
                    │          │ POST /api/photos (multipart/form-data)             │
                    │          ▼                                                     │
                    │   ┌─────────────┐                                             │
                    │   │  UPLOADING  │                                             │
                    │   │             │                                             │
                    │   │  Server:    │                                             │
                    │   │  - Validate │─────────────┐                               │
                    │   │  - Cloudinary│            │ Error                         │
                    │   │  - Save DB  │            ▼                                │
                    │   └──────┬──────┘     ┌─────────────┐                         │
                    │          │            │ UPLOAD_FAIL │                         │
                    │          │            │  400/500    │                         │
                    │          │            └─────────────┘                         │
                    │          │ Success                                            │
                    │          ▼                                                     │
                    │   ┌─────────────────────────────────────────────┐             │
                    │   │                  ACTIVE                      │             │
                    │   │                                              │             │
                    │   │   Data in MongoDB:                          │             │
                    │   │   - id, userId, imageUrl                    │             │
                    │   │   - caption, tags[]                         │             │
                    │   │   - likeCount, commentCount, shareCount     │             │
                    │   │   - createdAt                               │             │
                    │   │                                              │             │
                    │   └──────────────────────┬───────────────────────┘             │
                    │                          │                                     │
                    │     ┌────────────────────┼────────────────────┐               │
                    │     │                    │                    │               │
                    │     ▼                    ▼                    ▼               │
                    │  ┌────────┐         ┌────────┐         ┌────────┐            │
                    │  │ LIKED  │         │COMMENTED│        │ SHARED │            │
                    │  │        │         │        │         │        │            │
                    │  │ POST   │         │ POST   │         │ POST   │            │
                    │  │/likes  │         │/comments│        │/shares │            │
                    │  └───┬────┘         └───┬────┘         └───┬────┘            │
                    │      │                  │                  │                  │
                    │      └──────────────────┼──────────────────┘                  │
                    │                         │                                     │
                    │                         ▼                                     │
                    │                   ACTIVE (updated counts)                     │
                    │                         │                                     │
                    │                         │ DELETE /api/photos/{id}            │
                    │                         ▼                                     │
                    │                  ┌─────────────┐                              │
                    │                  │   DELETED   │                              │
                    │                  │             │                              │
                    │                  │ - Remove DB │                              │
                    │                  │ - Cloudinary│                              │
                    │                  │ - Cascade   │                              │
                    │                  └─────────────┘                              │
                    │                                                                │
                    └───────────────────────────────────────────────────────────────┘
```

### 3. Social Interactions

```
                    ┌───────────────────────────────────────────────────────────────┐
                    │                    LIKE INTERACTION                            │
                    │                                                                │
                    │   ┌─────────────┐      POST /api/likes/{photoId}              │
                    │   │  NOT_LIKED  │◄────────────────────────────────────────┐   │
                    │   └──────┬──────┘                                         │   │
                    │          │                                                │   │
                    │          │ POST /api/likes/{photoId}                     │   │
                    │          │                                                │   │
                    │          ▼                                                │   │
                    │   ┌─────────────┐                                        │   │
                    │   │   LIKED     │                                        │   │
                    │   │             │                                        │   │
                    │   │ Effects:    │                                        │   │
                    │   │ - Create Like doc                                    │   │
                    │   │ - Increment photo.likeCount                          │   │
                    │   └──────┬──────┘                                        │   │
                    │          │                                                │   │
                    │          │ DELETE /api/likes/{photoId}                   │   │
                    │          │                                                │   │
                    │          └────────────────────────────────────────────────┘   │
                    │                                                                │
                    └───────────────────────────────────────────────────────────────┘


                    ┌───────────────────────────────────────────────────────────────┐
                    │                   FOLLOW INTERACTION                           │
                    │                                                                │
                    │   ┌─────────────┐      DELETE /api/follow/{userId}            │
                    │   │NOT_FOLLOWING│◄────────────────────────────────────────┐   │
                    │   └──────┬──────┘                                         │   │
                    │          │                                                │   │
                    │          │ POST /api/follow/{userId}                     │   │
                    │          │                                                │   │
                    │          ▼                                                │   │
                    │   ┌─────────────┐                                        │   │
                    │   │  FOLLOWING  │                                        │   │
                    │   │             │                                        │   │
                    │   │ Effects:    │                                        │   │
                    │   │ - Create Follow doc                                  │   │
                    │   │ - Add to user.followingIds[]                         │   │
                    │   │ - Increment follower.followingCount                  │   │
                    │   │ - Increment target.followerCount                     │   │
                    │   │ - Include in newsfeed                                │   │
                    │   └──────┬──────┘                                        │   │
                    │          │                                                │   │
                    │          │ DELETE /api/follow/{userId}                   │   │
                    │          │                                                │   │
                    │          │ Effects:                                      │   │
                    │          │ - Delete Follow doc                           │   │
                    │          │ - Remove from followingIds[]                  │   │
                    │          │ - Decrement counts                            │   │
                    │          │ - Remove from newsfeed                        │   │
                    │          │                                                │   │
                    │          └────────────────────────────────────────────────┘   │
                    │                                                                │
                    └───────────────────────────────────────────────────────────────┘


                    ┌───────────────────────────────────────────────────────────────┐
                    │                   FAVORITE (SAVE) INTERACTION                  │
                    │                                                                │
                    │   ┌─────────────┐     DELETE /api/favorites/{photoId}         │
                    │   │  NOT_SAVED  │◄────────────────────────────────────────┐   │
                    │   └──────┬──────┘                                         │   │
                    │          │                                                │   │
                    │          │ POST /api/favorites/{photoId}                 │   │
                    │          │                                                │   │
                    │          ▼                                                │   │
                    │   ┌─────────────┐                                        │   │
                    │   │   SAVED     │                                        │   │
                    │   │             │                                        │   │
                    │   │ Effects:    │                                        │   │
                    │   │ - Create Favorite doc                                │   │
                    │   │ - Show in profile Saved tab                          │   │
                    │   └──────┬──────┘                                        │   │
                    │          │                                                │   │
                    │          │ DELETE /api/favorites/{photoId}               │   │
                    │          │                                                │   │
                    │          └────────────────────────────────────────────────┘   │
                    │                                                                │
                    └───────────────────────────────────────────────────────────────┘
```

### 4. Message/Conversation State

```
                    ┌───────────────────────────────────────────────────────────────┐
                    │                   CONVERSATION LIFECYCLE                       │
                    │                                                                │
                    │   ┌─────────────┐                                             │
                    │   │   START     │                                             │
                    │   │  (No Conv)  │                                             │
                    │   └──────┬──────┘                                             │
                    │          │                                                     │
                    │          │ POST /api/messages                                 │
                    │          │ (first message to user)                            │
                    │          ▼                                                     │
                    │   ┌─────────────────────────────────────────────┐             │
                    │   │              CONVERSATION                    │             │
                    │   │                                              │             │
                    │   │   MongoDB: conversations                    │             │
                    │   │   - id                                      │             │
                    │   │   - participants: [userId1, userId2]        │             │
                    │   │   - lastMessage                             │             │
                    │   │   - lastMessageAt                           │             │
                    │   │   - unreadCount: {userId: count}            │             │
                    │   │                                              │             │
                    │   └──────────────────────┬───────────────────────┘             │
                    │                          │                                     │
                    │   ┌──────────────────────┼──────────────────────┐             │
                    │   │                      │                      │             │
                    │   ▼                      ▼                      ▼             │
                    │  New Message        Read Messages          WebSocket          │
                    │  POST /messages     GET /messages/{id}     Events             │
                    │   │                      │                      │             │
                    │   │                      │                      │             │
                    │   └──────────────────────┼──────────────────────┘             │
                    │                          │                                     │
                    │                          ▼                                     │
                    │                   Update lastMessage                          │
                    │                   Update unreadCount                          │
                    │                                                                │
                    └───────────────────────────────────────────────────────────────┘


                    ┌───────────────────────────────────────────────────────────────┐
                    │                   WEBSOCKET EVENTS                             │
                    │                                                                │
                    │   Client                              Server                   │
                    │      │                                   │                     │
                    │      │──── connect ─────────────────────►│                     │
                    │      │◄─── connected ───────────────────│                     │
                    │      │                                   │                     │
                    │      │──── join_room(userId) ───────────►│                     │
                    │      │                                   │                     │
                    │      │──── send_message ────────────────►│                     │
                    │      │                                   │── save to DB        │
                    │      │◄─── new_message ─────────────────│                     │
                    │      │                                   │                     │
                    │      │──── typing ──────────────────────►│                     │
                    │      │◄─── user_typing ─────────────────│                     │
                    │      │                                   │                     │
                    │      │◄─── user_online ─────────────────│                     │
                    │      │◄─── user_offline ────────────────│                     │
                    │      │                                   │                     │
                    │      │──── disconnect ──────────────────►│                     │
                    │      │                                   │                     │
                    │                                                                │
                    └───────────────────────────────────────────────────────────────┘
```

---

## API Flow Diagrams

### Authentication Flow

```
┌────────────────────────────────────────────────────────────────────────────────────────────┐
│                              JWT AUTHENTICATION FLOW                                        │
│                                                                                             │
│   ┌──────────┐         ┌──────────┐         ┌──────────┐         ┌──────────┐            │
│   │  Client  │         │  Filter  │         │Controller│         │ Service  │            │
│   └────┬─────┘         └────┬─────┘         └────┬─────┘         └────┬─────┘            │
│        │                    │                    │                    │                   │
│        │ POST /api/auth/login                   │                    │                   │
│        │ {identifier, password}                 │                    │                   │
│        │───────────────────────────────────────►│                    │                   │
│        │                    │                    │                    │                   │
│        │                    │                    │ authenticate(dto)  │                   │
│        │                    │                    │───────────────────►│                   │
│        │                    │                    │                    │                   │
│        │                    │                    │                    │ Find user         │
│        │                    │                    │                    │ Verify password   │
│        │                    │                    │                    │ Generate JWT      │
│        │                    │                    │                    │                   │
│        │                    │                    │◄───────────────────│                   │
│        │                    │                    │   {jwt, user}      │                   │
│        │◄───────────────────────────────────────│                    │                   │
│        │ 200 OK {jwt, id, username, email, role}│                    │                   │
│        │                    │                    │                    │                   │
│        │                    │                    │                    │                   │
│   ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ SUBSEQUENT REQUESTS ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─                        │
│        │                    │                    │                    │                   │
│        │ GET /api/users/me                      │                    │                   │
│        │ Authorization: Bearer <jwt>            │                    │                   │
│        │───────────────────►│                    │                    │                   │
│        │                    │                    │                    │                   │
│        │                    │ Extract token      │                    │                   │
│        │                    │ Validate JWT       │                    │                   │
│        │                    │ Set SecurityContext│                    │                   │
│        │                    │                    │                    │                   │
│        │                    │───────────────────►│                    │                   │
│        │                    │                    │ getCurrentUser()   │                   │
│        │                    │                    │───────────────────►│                   │
│        │                    │                    │◄───────────────────│                   │
│        │                    │                    │    UserDTO         │                   │
│        │◄───────────────────────────────────────│                    │                   │
│        │ 200 OK {user data}  │                    │                    │                   │
│        │                    │                    │                    │                   │
└────────────────────────────────────────────────────────────────────────────────────────────┘
```

### Photo Upload Flow

```
┌────────────────────────────────────────────────────────────────────────────────────────────┐
│                              PHOTO UPLOAD FLOW                                              │
│                                                                                             │
│   ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐           │
│   │  Client  │    │Controller│    │ Service  │    │Cloudinary│    │ MongoDB  │           │
│   └────┬─────┘    └────┬─────┘    └────┬─────┘    └────┬─────┘    └────┬─────┘           │
│        │               │               │               │               │                  │
│        │ POST /api/photos             │               │               │                  │
│        │ multipart/form-data          │               │               │                  │
│        │ - image: File                │               │               │                  │
│        │ - caption: String            │               │               │                  │
│        │ - tags: String[]             │               │               │                  │
│        │──────────────►│               │               │               │                  │
│        │               │               │               │               │                  │
│        │               │ createPhoto() │               │               │                  │
│        │               │──────────────►│               │               │                  │
│        │               │               │               │               │                  │
│        │               │               │ upload(image) │               │                  │
│        │               │               │──────────────►│               │                  │
│        │               │               │◄──────────────│               │                  │
│        │               │               │   {imageUrl}  │               │                  │
│        │               │               │               │               │                  │
│        │               │               │ save(Photo)   │               │                  │
│        │               │               │──────────────────────────────►│                  │
│        │               │               │◄──────────────────────────────│                  │
│        │               │               │   {savedPhoto}│               │                  │
│        │               │               │               │               │                  │
│        │               │               │ updateUser    │               │                  │
│        │               │               │ (photoCount++)│               │                  │
│        │               │               │──────────────────────────────►│                  │
│        │               │               │               │               │                  │
│        │               │◄──────────────│               │               │                  │
│        │               │   PhotoDTO    │               │               │                  │
│        │◄──────────────│               │               │               │                  │
│        │ 201 Created   │               │               │               │                  │
│        │ {photo data}  │               │               │               │                  │
│        │               │               │               │               │                  │
└────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## Database Schema

### Collections Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           MONGODB COLLECTIONS                                │
│                                                                              │
│   ┌────────────────────────────────────────────────────────────────────┐   │
│   │                           USERS                                     │   │
│   ├────────────────────────────────────────────────────────────────────┤   │
│   │  _id: ObjectId                                                      │   │
│   │  username: String (unique, indexed)                                │   │
│   │  email: String (unique, indexed)                                   │   │
│   │  phoneNumber: String (unique, sparse)                              │   │
│   │  password: String (hashed)                                         │   │
│   │  role: Enum (USER, ADMIN)                                          │   │
│   │  imageUrl: String                                                   │   │
│   │  bio: String                                                        │   │
│   │  createdAt: Instant                                                 │   │
│   │  photoCount: Long                                                   │   │
│   │  followerCount: Long                                                │   │
│   │  followingCount: Long                                               │   │
│   │  followingIds: [String]                                             │   │
│   │  resetToken: String                                                 │   │
│   │  resetTokenExpiry: Instant                                          │   │
│   └────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
│   ┌────────────────────────────────────────────────────────────────────┐   │
│   │                           PHOTOS                                    │   │
│   ├────────────────────────────────────────────────────────────────────┤   │
│   │  _id: ObjectId                                                      │   │
│   │  userId: String (indexed)                                           │   │
│   │  imageUrl: String                                                   │   │
│   │  caption: String                                                    │   │
│   │  tags: [String]                                                     │   │
│   │  likeCount: Long                                                    │   │
│   │  commentCount: Long                                                 │   │
│   │  shareCount: Long                                                   │   │
│   │  createdAt: Instant (indexed)                                       │   │
│   └────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
│   ┌────────────────────────────────────────────────────────────────────┐   │
│   │                           COMMENTS                                  │   │
│   ├────────────────────────────────────────────────────────────────────┤   │
│   │  _id: ObjectId                                                      │   │
│   │  photoId: String (indexed)                                          │   │
│   │  userId: String                                                     │   │
│   │  content: String                                                    │   │
│   │  createdAt: Instant                                                 │   │
│   └────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
│   ┌────────────────────────────────────────────────────────────────────┐   │
│   │                           LIKES                                     │   │
│   ├────────────────────────────────────────────────────────────────────┤   │
│   │  _id: ObjectId                                                      │   │
│   │  photoId: String (indexed)                                          │   │
│   │  userId: String (indexed)                                           │   │
│   │  createdAt: Instant                                                 │   │
│   │  [compound index: photoId + userId (unique)]                        │   │
│   └────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
│   ┌────────────────────────────────────────────────────────────────────┐   │
│   │                           FOLLOWS                                   │   │
│   ├────────────────────────────────────────────────────────────────────┤   │
│   │  _id: ObjectId                                                      │   │
│   │  followerId: String (indexed)                                       │   │
│   │  followingId: String (indexed)                                      │   │
│   │  createdAt: Instant                                                 │   │
│   │  [compound index: followerId + followingId (unique)]                │   │
│   └────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
│   ┌────────────────────────────────────────────────────────────────────┐   │
│   │                         CONVERSATIONS                               │   │
│   ├────────────────────────────────────────────────────────────────────┤   │
│   │  _id: ObjectId                                                      │   │
│   │  participants: [String] (indexed)                                   │   │
│   │  lastMessage: String                                                │   │
│   │  lastMessageAt: Instant                                             │   │
│   │  unreadCount: Map<String, Integer>                                  │   │
│   └────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
│   ┌────────────────────────────────────────────────────────────────────┐   │
│   │                           MESSAGES                                  │   │
│   ├────────────────────────────────────────────────────────────────────┤   │
│   │  _id: ObjectId                                                      │   │
│   │  conversationId: String (indexed)                                   │   │
│   │  senderId: String                                                   │   │
│   │  content: String                                                    │   │
│   │  createdAt: Instant                                                 │   │
│   │  readBy: [String]                                                   │   │
│   └────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
│   ┌────────────────────────────────────────────────────────────────────┐   │
│   │                          FAVORITES                                  │   │
│   ├────────────────────────────────────────────────────────────────────┤   │
│   │  _id: ObjectId                                                      │   │
│   │  userId: String (indexed)                                           │   │
│   │  photoId: String (indexed)                                          │   │
│   │  createdAt: Instant                                                 │   │
│   └────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
│   ┌────────────────────────────────────────────────────────────────────┐   │
│   │                           SHARES                                    │   │
│   ├────────────────────────────────────────────────────────────────────┤   │
│   │  _id: ObjectId                                                      │   │
│   │  photoId: String (indexed)                                          │   │
│   │  userId: String                                                     │   │
│   │  platform: String                                                   │   │
│   │  createdAt: Instant                                                 │   │
│   └────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
│   ┌────────────────────────────────────────────────────────────────────┐   │
│   │                            TAGS                                     │   │
│   ├────────────────────────────────────────────────────────────────────┤   │
│   │  _id: ObjectId                                                      │   │
│   │  name: String (unique, indexed)                                     │   │
│   │  usageCount: Long                                                   │   │
│   └────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Entity Relationships

```
                    ┌───────────────────────────────────────────────────────────────┐
                    │                 ENTITY RELATIONSHIPS                           │
                    │                                                                │
                    │                     ┌──────────┐                               │
                    │           ┌─────────│   USER   │─────────┐                     │
                    │           │         └────┬─────┘         │                     │
                    │           │              │               │                     │
                    │     1:N   │         1:N  │          1:N  │                     │
                    │           │              │               │                     │
                    │           ▼              ▼               ▼                     │
                    │     ┌──────────┐   ┌──────────┐   ┌──────────┐                │
                    │     │  PHOTO   │   │  FOLLOW  │   │CONVERSATION│               │
                    │     └────┬─────┘   └──────────┘   └─────┬────┘                │
                    │          │                              │                      │
                    │     ┌────┼────┬───────────┐             │                      │
                    │     │    │    │           │             │                      │
                    │    1:N  1:N  1:N         1:N           1:N                     │
                    │     │    │    │           │             │                      │
                    │     ▼    ▼    ▼           ▼             ▼                      │
                    │  ┌────┐┌────┐┌────┐  ┌────────┐   ┌──────────┐                │
                    │  │LIKE││COMMENT││SHARE│  │FAVORITE│   │ MESSAGE  │                │
                    │  └────┘└────┘└────┘  └────────┘   └──────────┘                │
                    │                                                                │
                    └───────────────────────────────────────────────────────────────┘
```

---

*Last updated: March 2026*
