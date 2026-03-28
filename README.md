# 📸 VibeLens - Frontend

<div align="center">

![Live Website](https://img.shields.io/badge/Live-https://vibelens.me-00D09C?style=for-the-badge)
![React](https://img.shields.io/badge/React-19.1-61DAFB?style=for-the-badge&logo=react&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-6.3-646CFF?style=for-the-badge&logo=vite&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-5.7-3178C6?style=for-the-badge&logo=typescript&logoColor=white)
![Socket.IO](https://img.shields.io/badge/Socket.IO-4.8-010101?style=for-the-badge&logo=socket.io&logoColor=white)

**A modern, Instagram-inspired photo sharing platform**

[Live Demo](https://vibelens.me) •
[Features](#-features) •
[Getting Started](#-getting-started) •
[Architecture](#-architecture) •
[Documentation](#-documentation)

</div>

---

## ✨ Features

### Core Features
- 📷 **Photo Sharing** - Upload, view, and interact with photos
- ❤️ **Social Interactions** - Like, comment, save posts, and share
- 🔍 **Search & Explore** - Discover users and trending content
- 💬 **Real-time Messaging** - Direct messages with WebSocket (Socket.IO)
- 👤 **User Profiles** - View profiles, followers, and following lists
- 🔐 **Authentication** - JWT-based auth with OAuth2 (Google)
- 🏷️ **Tags** - Tag and discover photos by tags
- 🔔 **Notifications** - Real-time notifications for likes, comments, follows

### Technical Features
- ⚡ **Optimistic Updates** - Instant UI feedback for likes/follows
- ♾️ **Infinite Scroll** - Smooth pagination for feeds
- 📱 **Responsive Design** - Mobile-first approach
- 🔄 **Real-time Updates** - WebSocket for live notifications and messages
- 🤖 **AI Integration** - Smart caption suggestions
- 🎨 **Image Cropping** - Easy crop tool for profile/cover photos
- 🔐 **Protected Routes** - Auth-gated pages with redirect

---

## 🚀 Getting Started

### Prerequisites

- **Node.js** >= 18.x
- **npm** >= 9.x
- Backend server running (see [backend README](../backend-photo-share/README.md))

### Installation

```bash
# Clone repository
git clone <repository-url>
cd frontend-photo-share

# Install dependencies
npm install

# Create environment file
cp .env.example .env

# Start development server
npm run dev
```

### Environment Variables

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_SOCKET_URL=http://localhost:9092
VITE_OAUTH_URL=/oauth2/authorization/google
```

### Available Scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Start development server |
| `npm run build` | Build for production |
| `npm run build:docker` | Build Docker image |
| `npm run preview` | Preview production build |
| `npm run lint` | Run ESLint |
| `npm run seed` | Seed data via API (requires running backend) |

---

## 🏗️ Architecture

### Project Structure

```
src/
├── assets/
│   └── styles/
│       ├── base.css
│       ├── variables.css
│       ├── components/
│       ├── layout/
│       └── pages/
│
├── components/
│   ├── common/
│   │   └── Loader.jsx
│   ├── features/
│   │   ├── PhotoCard.jsx
│   │   ├── PhotoModal.jsx
│   │   ├── ShareModal.jsx
│   │   ├── FollowListModal.jsx
│   │   ├── SuggestedUsers.jsx
│   │   ├── AiCaptionAssistant.jsx
│   │   └── AiCreatorDashboard.jsx
│   └── layout/
│       ├── Layout.jsx
│       └── SideBar.jsx
│
├── config/
│   └── ApiConfig.ts
│
├── context/
│   ├── AuthContext.jsx
│   └── SocketContext.jsx
│
├── hooks/
│   ├── useAuth.ts
│   ├── useCreatePost.ts
│   ├── useOptimisticLike.ts
│   └── useUserProfile.ts
│
├── pages/
│   ├── auth/
│   │   ├── Login.jsx
│   │   ├── Register.jsx
│   │   ├── ForgotPassword.jsx
│   │   ├── ResetPassword.jsx
│   │   └── OAuth2RedirectHandler.jsx
│   ├── home/
│   │   └── Home.jsx
│   ├── search/
│   │   └── Search.jsx
│   ├── create/
│   │   └── Create.jsx
│   ├── messages/
│   │   └── Messages.jsx
│   ├── notifications/
│   │   └── Notifications.jsx
│   └── profile/
│       ├── Profile.jsx
│       └── EditProfileForm.jsx
│
├── services/                  # 17 API services
│   ├── api.ts
│   ├── authService.ts
│   ├── userService.ts
│   ├── photoService.ts
│   ├── postService.ts
│   ├── commentService.ts
│   ├── likeService.ts
│   ├── followService.ts
│   ├── favoriteService.ts
│   ├── shareService.ts
│   ├── messageService.ts
│   ├── searchService.ts
│   ├── newsfeedService.ts
│   ├── recommendationService.ts
│   ├── notificationService.ts
│   ├── aiService.ts
│   └── socketService.ts
│
├── types/
│   └── api.ts
│
├── utils/
│   ├── constants.ts
│   ├── helpers.ts
│   ├── storage.ts
│   ├── toastService.ts
│   └── ProtectedRoute.jsx
│
├── App.jsx
└── main.jsx
```

### State Management

```
┌─────────────────────────────────────────┐
│              AuthContext                │
│  user, token, isAuthenticated           │
│  login(), logout(), register()          │
└─────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────┐
│            SocketContext               │
│  socket, connected                      │
│  joinRoom(), sendMessage()               │
└─────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────┐
│            Custom Hooks                 │
│  useAuth    │ useUserProfile             │
│  useCreatePost │ useOptimisticLike       │
└─────────────────────────────────────────┘
```

### Data Flow

```
User Action → Page Component → Service Layer → API Backend
     ↑                                        │
     └────────────── Response ─────────────────┘
     │
     ▼
State Update (Context / useState)
```

### Socket.IO Integration

Real-time messaging and notifications:

```
ON:  new_message   → append to chat
ON:  message_read  → update read status
ON:  notification  → show toast + badge
ON:  user_online   → update online status

EMIT: join_room    → join conversation
EMIT: send_message → send to server
EMIT: mark_read    → mark as read
```

---

## 📖 Documentation

### State Machine Diagrams

📄 **[docs/STATE_DIAGRAMS.md](docs/STATE_DIAGRAMS.md)**

Includes:
- Authentication Flow
- Photo Interactions
- Like State (Optimistic Update)
- Newsfeed / Infinite Scroll
- Messages / Real-time Chat
- Profile Page
- Search & Explore
- App Navigation

### Key Patterns

#### Optimistic Updates

```typescript
const handleLike = async () => {
  setIsLiked(true);
  setLikesCount(prev => prev + 1);

  try {
    await likePhoto(photoId);
  } catch (error) {
    setIsLiked(false);
    setLikesCount(prev => prev - 1);
  }
};
```

#### Infinite Scroll

```typescript
useEffect(() => {
  const observer = new IntersectionObserver(
    entries => {
      if (entries[0].isIntersecting && hasMore && !loading) {
        loadMore();
      }
    },
    { threshold: 0.1 }
  );

  if (loadTriggerRef.current) {
    observer.observe(loadTriggerRef.current);
  }

  return () => observer.disconnect();
}, [hasMore, loading]);
```

#### Image Cropping

```typescript
const onCropComplete = (croppedArea, croppedAreaPixels) => {
  const canvas = getCroppedImg(imageRef.current, croppedAreaPixels);
};
```

---

## 🛠️ Tech Stack

| Category | Technology |
|----------|------------|
| **Framework** | React 19.1 |
| **Build Tool** | Vite 6.3 |
| **Language** | TypeScript 5.7 |
| **Routing** | React Router DOM 7.6 |
| **HTTP Client** | Axios 1.9 |
| **Real-time** | Socket.IO Client 4.8 |
| **Icons** | Lucide React |
| **Notifications** | React Toastify |
| **Image Crop** | React Easy Crop |
| **Emoji Picker** | Emoji Picker React |
| **Testing Data** | Faker JS |

---

## 🔧 Development

### Code Style

- ESLint with React Hooks plugin
- TypeScript strict mode
- CSS Modules for component styles

### Git Workflow

```bash
git checkout -b feature/your-feature
git commit -m "feat: add new feature"
git commit -m "fix: resolve bug"
git push origin main
```

---

## 🔄 CI/CD

GitHub Actions automatically builds and deploys on every push to `main`.

### Workflow

```yaml
on:
  push:
    branches: [main]

jobs:
  build-and-deploy:
    steps:
      - Checkout code
      - Log in to GHCR
      - Build & push Docker image with build args:
          VITE_API_BASE_URL=/api/v1
          VITE_SOCKET_URL=
          VITE_OAUTH_URL=/oauth2/authorization/google
      - SSH to VPS:
          docker compose pull frontend
          docker compose up -d --no-deps frontend
```

### GitHub Secrets Required

| Secret | Description |
|--------|-------------|
| `VPS_HOST` | VPS IP address or hostname |
| `VPS_USER` | SSH username |
| `VPS_SSH_KEY` | Private SSH key for VPS access |

### Container Registry

- **Registry**: `ghcr.io/photo-sharing-platform/frontend-photo-share`
- **Tags**: `latest`, `sha-{commit-hash}`

---

## 📄 License

This project is private and for educational purposes.

---

<div align="center">

**Made with ❤️ using React + Vite**

[Live Demo](https://vibelens.me) • [Backend API](https://vibelens.me/api-docs)

</div>
