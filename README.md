# 📸 Photo Share - Frontend

<div align="center">

![React](https://img.shields.io/badge/React-19.1-61DAFB?style=for-the-badge&logo=react&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-6.3-646CFF?style=for-the-badge&logo=vite&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-5.7-3178C6?style=for-the-badge&logo=typescript&logoColor=white)
![Socket.IO](https://img.shields.io/badge/Socket.IO-4.8-010101?style=for-the-badge&logo=socket.io&logoColor=white)

**A modern, Instagram-inspired photo sharing application**

[Features](#-features) •
[Getting Started](#-getting-started) •
[Architecture](#-architecture) •
[Documentation](#-documentation)

</div>

---

## ✨ Features

### Core Features
- 📷 **Photo Sharing** - Upload, view, and interact with photos
- ❤️ **Social Interactions** - Like, comment, and save posts
- 🔍 **Search & Explore** - Discover users and trending content
- 💬 **Real-time Messaging** - Direct messages with WebSocket
- 👤 **User Profiles** - View profiles, followers, and following
- 🔐 **Authentication** - JWT-based auth with OAuth2 (Google)

### Technical Features
- ⚡ **Optimistic Updates** - Instant UI feedback for likes/follows
- ♾️ **Infinite Scroll** - Smooth pagination for feeds
- 📱 **Responsive Design** - Mobile-first approach
- 🔄 **Real-time Updates** - WebSocket for live notifications
- 🤖 **AI Integration** - Smart caption suggestions

---

## 🚀 Getting Started

### Prerequisites

- **Node.js** >= 18.x
- **npm** >= 9.x
- Backend server running (see [backend README](../backend-photo-share/README.md))

### Installation

\`\`\`bash
# Clone repository
git clone <repository-url>
cd frontend-photo-share

# Install dependencies
npm install

# Create environment file
cp .env.example .env

# Start development server
npm run dev
\`\`\`

### Environment Variables

\`\`\`env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_SOCKET_URL=http://localhost:9092
\`\`\`

### Available Scripts

| Command | Description |
|---------|-------------|
| \`npm run dev\` | Start development server |
| \`npm run build\` | Build for production |
| \`npm run preview\` | Preview production build |
| \`npm run lint\` | Run ESLint |

---

## 🏗️ Architecture

### Project Structure

\`\`\`
src/
├── assets/                 # Static assets
│   └── styles/            # CSS modules & global styles
│       ├── base.css       # Reset & base styles
│       ├── variables.css  # CSS custom properties
│       ├── components/    # Component-specific styles
│       ├── layout/        # Layout styles
│       └── pages/         # Page-specific styles
│
├── components/            # React components
│   ├── common/           # Reusable UI components
│   │   └── Loader.jsx    # Loading spinner
│   ├── features/         # Feature-specific components
│   │   ├── PhotoCard.jsx
│   │   ├── PhotoModal.jsx
│   │   ├── ShareModal.jsx
│   │   ├── FollowListModal.jsx
│   │   ├── SuggestedUsers.jsx
│   │   ├── AiCaptionAssistant.jsx
│   │   └── AiCreatorDashboard.jsx
│   └── layout/           # Layout components
│       ├── Layout.jsx
│       └── SideBar.jsx
│
├── config/               # App configuration
│   └── ApiConfig.ts     # API endpoints
│
├── context/             # React Context providers
│   └── AuthContext.jsx  # Authentication state
│
├── hooks/               # Custom React hooks
│   ├── useAuth.ts           # Authentication logic
│   ├── useCreatePost.ts     # Post creation logic
│   ├── useOptimisticLike.ts # Like with optimistic UI
│   └── useUserProfile.ts    # Profile data fetching
│
├── pages/               # Route pages
│   ├── auth/            # Authentication pages
│   │   ├── Login.jsx
│   │   ├── Register.jsx
│   │   ├── ForgotPassword.jsx
│   │   ├── ResetPassword.jsx
│   │   └── OAuth2RedirectHandler.jsx
│   ├── home/
│   │   └── Home.jsx     # Newsfeed
│   ├── search/
│   │   └── Search.jsx   # Search & Explore
│   ├── create/
│   │   └── Create.jsx   # Create post
│   ├── messages/
│   │   └── Messages.jsx # Direct messages
│   └── profile/
│       ├── Profile.jsx
│       └── EditProfileForm.jsx
│
├── services/            # API service layer
│   ├── authService.ts
│   ├── userService.ts
│   ├── photoService.ts
│   ├── commentService.ts
│   ├── likeService.ts
│   ├── followService.ts
│   ├── favoriteService.ts
│   ├── messageService.ts
│   ├── searchService.ts
│   ├── shareService.ts
│   ├── tagService.ts
│   ├── newsfeedService.ts
│   ├── recommendationService.ts
│   ├── aiService.ts
│   └── socketService.ts
│
├── types/              # TypeScript definitions
│   └── api.ts
│
├── utils/              # Utility functions
│   ├── constants.ts    # App constants
│   ├── helpers.ts      # Helper functions
│   ├── storage.ts      # localStorage utilities
│   ├── toastService.ts # Toast notifications
│   └── ProtectedRoute.jsx
│
├── App.jsx             # Root component
└── main.jsx            # Entry point
\`\`\`

### State Management

The application uses **React Context** + **Custom Hooks** pattern:

\`\`\`
┌─────────────────────────────────────────┐
│              AuthContext                │
│  ┌─────────────────────────────────┐   │
│  │  user, token, isAuthenticated   │   │
│  │  login(), logout(), register()  │   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────┐
│            Custom Hooks                 │
│  ┌─────────────┐  ┌─────────────────┐  │
│  │  useAuth    │  │ useUserProfile  │  │
│  └─────────────┘  └─────────────────┘  │
│  ┌─────────────┐  ┌─────────────────┐  │
│  │useCreatePost│  │useOptimisticLike│  │
│  └─────────────┘  └─────────────────┘  │
└─────────────────────────────────────────┘
\`\`\`

### Data Flow

\`\`\`
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│   User   │────►│   Page   │────►│  Service │────►│   API    │
│  Action  │     │Component │     │  Layer   │     │ Backend  │
└──────────┘     └──────────┘     └──────────┘     └──────────┘
                      │                                  │
                      │◄─────────────────────────────────┘
                      │         Response
                      ▼
              ┌──────────────┐
              │  State Update │
              │  (Context/    │
              │   useState)   │
              └──────────────┘
\`\`\`

---

## 📖 Documentation

### State Machine Diagrams

Detailed state diagrams for all components are available in:

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

\`\`\`typescript
// Example: useOptimisticLike hook
const handleLike = async () => {
  // 1. Optimistic update (immediate)
  setIsLiked(true);
  setLikesCount(prev => prev + 1);
  
  try {
    // 2. API call
    await likePhoto(photoId);
  } catch (error) {
    // 3. Rollback on error
    setIsLiked(false);
    setLikesCount(prev => prev - 1);
  }
};
\`\`\`

#### Infinite Scroll

\`\`\`typescript
// Intersection Observer pattern
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
\`\`\`

---

## 🛠️ Tech Stack

| Category | Technology |
|----------|------------|
| **Framework** | React 19.1 |
| **Build Tool** | Vite 6.3 |
| **Language** | TypeScript 5.7 |
| **Routing** | React Router 7.6 |
| **HTTP Client** | Axios 1.9 |
| **Real-time** | Socket.IO Client 4.8 |
| **Icons** | Lucide React |
| **Notifications** | React Toastify |
| **Image Crop** | React Easy Crop |

---

## 🔧 Development

### Code Style

- ESLint with React Hooks plugin
- TypeScript strict mode
- CSS Modules for component styles

### Git Workflow

\`\`\`bash
# Feature branch
git checkout -b feature/your-feature

# Commit with conventional commits
git commit -m "feat: add new feature"
git commit -m "fix: resolve bug"
git commit -m "docs: update readme"
\`\`\`

---

## 📄 License

This project is private and for educational purposes.

---

<div align="center">

**Made with ❤️ using React + Vite**

</div>
