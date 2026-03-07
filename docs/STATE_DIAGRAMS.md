# State Machine Diagrams

> Visual documentation of state transitions in the Photo Share application

## Table of Contents

- [1. Authentication Flow](#1-authentication-flow)
- [2. Photo Interaction](#2-photo-interaction)
- [3. Like State (Optimistic Update)](#3-like-state-optimistic-update)
- [4. Newsfeed / Infinite Scroll](#4-newsfeed--infinite-scroll)
- [5. Messages / Real-time Chat](#5-messages--real-time-chat)
- [6. Profile Page](#6-profile-page)
- [7. Search & Explore](#7-search--explore)
- [8. App Navigation](#8-app-navigation)
- [9. Socket.IO Global Connection Lifecycle](#9-socketio-global-connection-lifecycle-new---fixed-architecture)

---

## 1. Authentication Flow

```
                              ┌─────────────────┐
                              │   APP_INIT      │
                              └────────┬────────┘
                                       │
                                       ▼
                              ┌─────────────────┐
                              │ CHECKING_AUTH   │◄──────────────────────┐
                              └────────┬────────┘                       │
                                       │                                │
                    ┌──────────────────┴──────────────────┐            │
                    │                                     │            │
                    ▼                                     ▼            │
         ┌─────────────────┐                   ┌─────────────────┐     │
         │ UNAUTHENTICATED │                   │  AUTHENTICATED  │     │
         └────────┬────────┘                   └────────┬────────┘     │
                  │                                     │              │
                  ▼                                     │              │
         ┌─────────────────┐                           │              │
         │   LOGIN_FORM    │◄──────────┐               │              │
         └────────┬────────┘           │               │              │
                  │                    │               │              │
                  │ submit             │ error         │ logout       │
                  ▼                    │               │              │
         ┌─────────────────┐           │               │              │
         │   VALIDATING    │───────────┘               │              │
         └────────┬────────┘                           │              │
                  │                                    │              │
                  │ success                            │              │
                  └────────────────────────────────────┼──────────────┘
                                                       │
                                                       ▼
                                              ┌─────────────────┐
                                              │     LOGOUT      │
                                              │  (clear data)   │
                                              └─────────────────┘
```

### States Description

| State | Description |
|-------|-------------|
| `APP_INIT` | Application starting, checking localStorage |
| `CHECKING_AUTH` | Validating stored token |
| `UNAUTHENTICATED` | No valid session, show public routes |
| `AUTHENTICATED` | Valid session, access protected routes |
| `LOGIN_FORM` | User entering credentials |
| `VALIDATING` | API call in progress |
| `LOGOUT` | Clearing session data |

---

## 2. Photo Interaction

```
                    ┌───────────────────────────────────────────────────────┐
                    │                                                       │
                    ▼                                                       │
         ┌─────────────────┐                                               │
         │    PHOTO_IDLE   │◄──────────────────────────────────────────────┤
         └────────┬────────┘                                               │
                  │                                                         │
                  │ click photo                                            │
                  ▼                                                         │
         ┌─────────────────┐                                               │
         │ LOADING_DETAIL  │                                               │
         └────────┬────────┘                                               │
                  │                                                         │
       ┌──────────┴──────────┐                                             │
       │                     │                                             │
       ▼                     ▼                                             │
┌─────────────┐      ┌─────────────┐                                       │
│   ERROR     │      │   LOADED    │───────────────────────────────────────┤
└─────────────┘      └──────┬──────┘                                       │
                            │                                              │
          ┌─────────────────┼─────────────────┐                            │
          │                 │                 │                            │
          ▼                 ▼                 ▼                            │
   ┌────────────┐    ┌────────────┐    ┌────────────┐                      │
   │   LIKE     │    │  COMMENT   │    │   SAVE     │                      │
   │  TOGGLE    │    │  SUBMIT    │    │  TOGGLE    │                      │
   └─────┬──────┘    └─────┬──────┘    └─────┬──────┘                      │
         │                 │                 │                              │
         └─────────────────┴─────────────────┴──────────────────────────────┘
                                   │
                                   │ close modal
                                   ▼
                          ┌─────────────────┐
                          │   PHOTO_IDLE    │
                          └─────────────────┘
```

### Interactions Available

| Action | Trigger | Effect |
|--------|---------|--------|
| Like Toggle | Click heart / Double-click photo | Optimistic UI update |
| Comment Submit | Enter comment + submit | Add to comments list |
| Save Toggle | Click bookmark icon | Add/remove from favorites |
| Share | Click share icon | Open share modal |

---

## 3. Like State (Optimistic Update)

```
         ┌─────────────────────────────────────────┐
         │                                         │
         ▼                                         │
┌─────────────────┐                               │
│    NOT_LIKED    │                               │
│  (heart empty)  │                               │
└────────┬────────┘                               │
         │                                         │
         │ click heart                             │
         ▼                                         │
┌─────────────────┐          ┌─────────────────┐  │
│   PROCESSING    │─────────►│     LIKED       │  │
│ (optimistic UI) │  success │  (heart filled) │  │
└────────┬────────┘          └────────┬────────┘  │
         │                            │           │
         │ error                      │ click     │
         │ (rollback)                 ▼           │
         │                   ┌─────────────────┐  │
         └───────────────────│   PROCESSING    │──┘
                             │ (optimistic UI) │
                             └─────────────────┘
```

### Key Concept: Optimistic Updates

1. **Immediate UI feedback** - Button state changes instantly
2. **API call in background** - Request sent asynchronously
3. **Rollback on error** - Revert if API fails
4. **Count synchronization** - Like count updates immediately

---

## 4. Newsfeed / Infinite Scroll

```
         ┌───────────────────────────────────────────────────┐
         │                                                   │
         ▼                                                   │
┌─────────────────┐                                         │
│   INIT_LOADING  │                                         │
│  (first fetch)  │                                         │
└────────┬────────┘                                         │
         │                                                   │
         │ success                                           │
         ▼                                                   │
┌─────────────────┐    scroll to bottom    ┌─────────────┐  │
│  DISPLAYING     │───────────────────────►│  LOADING    │  │
│    POSTS        │◄───────────────────────│   MORE      │  │
└────────┬────────┘    posts appended      └──────┬──────┘  │
         │                                        │          │
         │                                        │ no more  │
         │                                        ▼          │
         │                                 ┌─────────────┐   │
         │                                 │  HAS_MORE   │   │
         │                                 │   =false    │   │
         │                                 └─────────────┘   │
         │                                                   │
         │ pull to refresh                                   │
         └───────────────────────────────────────────────────┘
```

### Pagination Parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| `page` | 0 | Current page index |
| `size` | 10 | Items per page |
| `hasMore` | true | More content available |

---

## 5. Messages / Real-time Chat

```
                              ┌─────────────────┐
                              │  INITIALIZING   │
                              └────────┬────────┘
                                       │
                                       │ load conversations
                                       ▼
                    ┌─────────────────────────────────────┐
                    │           CONVERSATIONS             │
                    │              LOADED                 │
                    └───────────────────┬─────────────────┘
                                        │
                                        │ select conversation
                                        ▼
         ┌─────────────────────────────────────────────────────┐
         │                  ACTIVE_CHAT                        │
         │  ┌─────────────────────────────────────────────┐   │
         │  │                                             │   │
         │  ▼                                             │   │
         │  ┌─────────────┐   type      ┌─────────────┐   │   │
         │  │    IDLE     │────────────►│   TYPING    │   │   │
         │  └──────┬──────┘             └──────┬──────┘   │   │
         │         │                           │          │   │
         │         │ send                      │ stop     │   │
         │         ▼                           │          │   │
         │  ┌─────────────┐                    │          │   │
         │  │  SENDING    │◄───────────────────┘          │   │
         │  └──────┬──────┘                               │   │
         │         │                                      │   │
         │    ┌────┴────┐                                 │   │
         │    │         │                                 │   │
         │    ▼         ▼                                 │   │
         │  ┌─────┐  ┌─────┐                              │   │
         │  │ OK  │  │FAIL │                              │   │
         │  └──┬──┘  └──┬──┘                              │   │
         │     │        │                                 │   │
         │     └────────┴─────────────────────────────────┘   │
         │                                                     │
         └─────────────────────────────────────────────────────┘


         ┌─────────────────────────────────────────────────────┐
         │               WEBSOCKET_STATE                       │
         │                                                     │
         │  DISCONNECTED ──► CONNECTING ──► CONNECTED          │
         │        ▲              │               │              │
         │        │              │               │              │
         │        │    error     │               │ events:      │
         │        └──────────────┘               │ - user_online│
         │        │                              │ - user_typing│
         │        └──────────────────────────────┘ - new_message│
         │                                                     │
         └─────────────────────────────────────────────────────┘
```

### WebSocket Events

| Event | Direction | Description |
|-------|-----------|-------------|
| `user_online` | Server → Client | User came online |
| `user_offline` | Server → Client | User went offline |
| `user_typing` | Bidirectional | Typing indicator |
| `new_message` | Server → Client | New message received |
| `send_message` | Client → Server | Send new message |

---

## 6. Profile Page

```
                              ┌─────────────────┐
                              │ LOADING_PROFILE │
                              └────────┬────────┘
                                       │
                    ┌──────────────────┴──────────────────┐
                    │                                     │
                    ▼                                     ▼
         ┌─────────────────┐                   ┌─────────────────┐
         │     ERROR       │                   │ PROFILE_LOADED  │
         └─────────────────┘                   └────────┬────────┘
                                                        │
                    ┌───────────────────────────────────┤
                    │                                   │
                    ▼                                   ▼
         ┌─────────────────┐                 ┌─────────────────┐
         │   POSTS_TAB     │◄───────────────►│   SAVED_TAB     │
         │  (photo grid)   │   tab switch    │  (favorites)    │
         └────────┬────────┘                 └────────┬────────┘
                  │                                   │
                  ▼                                   ▼
         ┌─────────────────┐                 ┌─────────────────┐
         │  LOADING_MORE   │                 │ LOADING_FAVS    │
         │ (infinite load) │                 └─────────────────┘
         └─────────────────┘


         ┌─────────────────────────────────────────────────────┐
         │               FOLLOW_STATE                          │
         │                                                     │
         │  ┌─────────────┐  click   ┌─────────────────┐      │
         │  │NOT_FOLLOWING│─────────►│   PROCESSING    │      │
         │  └─────────────┘          └────────┬────────┘      │
         │        ▲                           │                │
         │        │    error                  │ success        │
         │        │◄──────────────────────────┤                │
         │        │                           ▼                │
         │        │                  ┌─────────────────┐       │
         │        │◄─────────────────│   FOLLOWING     │       │
         │        │     click        └─────────────────┘       │
         │                                                     │
         └─────────────────────────────────────────────────────┘
```

### Profile Tabs

| Tab | Visibility | Content |
|-----|------------|---------|
| Posts | All users | User's uploaded photos |
| Saved | Own profile only | Bookmarked photos |

---

## 7. Search & Explore

```
         ┌─────────────────────────────────────────────────────┐
         │                    SEARCH_PAGE                      │
         │                                                     │
         │  ┌─────────────────────────────────────────────┐   │
         │  │              SEARCH_INPUT                    │   │
         │  │                                              │   │
         │  │   EMPTY ──────────► HAS_QUERY ──────► EMPTY │   │
         │  │            type          clear (X)          │   │
         │  └─────────────────────────────────────────────┘   │
         │                                                     │
         └─────────────────────────────────────────────────────┘
                                       │
                    ┌──────────────────┴──────────────────┐
                    │                                     │
               (no query)                            (has query)
                    │                                     │
                    ▼                                     ▼
         ┌─────────────────┐                   ┌─────────────────┐
         │   EXPLORE_MODE  │                   │   SEARCH_MODE   │
         └────────┬────────┘                   └────────┬────────┘
                  │                                     │
                  ▼                                     ▼
         ┌─────────────────┐                   ┌─────────────────┐
         │ LOADING_EXPLORE │                   │   DEBOUNCING    │
         └────────┬────────┘                   │   (500ms)       │
                  │                            └────────┬────────┘
                  ▼                                     │
         ┌─────────────────┐                           ▼
         │  EXPLORE_GRID   │                   ┌─────────────────┐
         │ (trending/new)  │                   │ SEARCHING_USERS │
         └────────┬────────┘                   └────────┬────────┘
                  │                                     │
                  │ scroll                    ┌─────────┴─────────┐
                  ▼                           │                   │
         ┌─────────────────┐                  ▼                   ▼
         │  LOADING_MORE   │          ┌─────────────┐     ┌─────────────┐
         └─────────────────┘          │  RESULTS    │     │ NO_RESULTS  │
                                      │   FOUND     │     │   FOUND     │
                                      └──────┬──────┘     └─────────────┘
                                             │
                                             │ click user
                                             ▼
                                      ┌─────────────┐
                                      │  NAVIGATE   │
                                      │ TO_PROFILE  │
                                      └─────────────┘
```

### Search Features

| Feature | Description |
|---------|-------------|
| Debounce | 500ms delay before API call |
| Explore Feed | Trending/recommended photos |
| User Search | Search by username |
| Infinite Scroll | Load more explore content |

---

## 8. App Navigation

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              APP_ROUTER                                     │
│                                                                             │
│   ┌─────────────────────────────────────────────────────────────────────┐  │
│   │                       PUBLIC_ROUTES                                  │  │
│   │                                                                      │  │
│   │   LOGIN ◄────────────► REGISTER                                     │  │
│   │     │                      │                                         │  │
│   │     ▼                      │                                         │  │
│   │   FORGOT_PASSWORD          │                                         │  │
│   │     │                      │                                         │  │
│   │     ▼                      │                                         │  │
│   │   RESET_PASSWORD           │                                         │  │
│   │                            │                                         │  │
│   └────────────┬───────────────┴─────────────────────────────────────────┘  │
│                │                                                            │
│                │ login success                                              │
│                ▼                                                            │
│   ┌─────────────────────────────────────────────────────────────────────┐  │
│   │                      PROTECTED_ROUTES                               │  │
│   │                                                                      │  │
│   │   ┌────────────────────────────────────────────────────────────┐   │  │
│   │   │                     WITH_SIDEBAR                            │   │  │
│   │   │                                                             │   │  │
│   │   │    HOME ◄────────► SEARCH ◄────────► CREATE                │   │  │
│   │   │      │                                   │                  │   │  │
│   │   │      ▼                                   ▼                  │   │  │
│   │   │   MESSAGES ◄──────────────────────► PROFILE                │   │  │
│   │   │                                         │                  │   │  │
│   │   │                                         ▼                  │   │  │
│   │   │                                   EDIT_PROFILE             │   │  │
│   │   │                                                             │   │  │
│   │   └─────────────────────────────────────────────────────────────┘   │  │
│   │                                                                      │  │
│   └─────────────────────────────────────────────────────────────────────┘  │
│                │                                                            │
│                │ logout                                                     │
│                ▼                                                            │
│          ┌──────────┐                                                       │
│          │  LOGIN   │                                                       │
│          └──────────┘                                                       │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Route Configuration

| Route | Auth Required | Description |
|-------|---------------|-------------|
| `/login` | No | Login page |
| `/register` | No | Registration page |
| `/forgot-password` | No | Password recovery |
| `/` or `/home` | Yes | Newsfeed |
| `/search` | Yes | Search & Explore |
| `/create` | Yes | Create new post |
| `/messages` | Yes | Direct messages |
| `/profile/:id?` | Yes | User profile |

---

## Legend

| Symbol | Meaning |
|--------|---------|
| `┌─┐` | State box |
| `───►` | Transition |
| `◄───►` | Bidirectional transition |
| `│` | Connection line |
| `▼` | Direction indicator |

---

## 9. Socket.IO Global Connection Lifecycle (NEW - Fixed Architecture)

> **Problem Solved**: Previously, socket was only connected when Messages component was mounted.
> Users had to reload to see new messages because navigating away disconnected the socket.
>
> **Solution**: Global SocketContext maintains connection throughout the entire authenticated session.

### 9.1 App-Level Socket Connection Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         APPLICATION LIFECYCLE                               │
│                                                                             │
│  ┌─────────────┐                                                           │
│  │  APP_START  │                                                           │
│  └──────┬──────┘                                                           │
│         │                                                                   │
│         ▼                                                                   │
│  ┌─────────────────┐                                                       │
│  │  AuthProvider   │                                                       │
│  │  (check token)  │                                                       │
│  └────────┬────────┘                                                       │
│           │                                                                 │
│    ┌──────┴──────┐                                                         │
│    │             │                                                         │
│    ▼             ▼                                                         │
│ ┌──────────┐  ┌──────────────────────────────────────────────────────┐    │
│ │NOT_AUTH  │  │              AUTHENTICATED                           │    │
│ │          │  │  ┌─────────────────────────────────────────────┐    │    │
│ │ (socket  │  │  │           SocketProvider                     │    │    │
│ │  = null) │  │  │   (wrapped inside AuthProvider)              │    │    │
│ │          │  │  │                                               │    │    │
│ │          │  │  │  ┌─────────────┐    ┌────────────────────┐   │    │    │
│ │          │  │  │  │ CONNECTING  │───►│     CONNECTED      │   │    │    │
│ │          │  │  │  │             │    │                    │   │    │    │
│ │          │  │  │  │ connectSocket│   │ - Socket active    │   │    │    │
│ │          │  │  │  │ (userId)    │    │ - In onlineUsers   │   │    │    │
│ │          │  │  │  └─────────────┘    │ - Receives events  │   │    │    │
│ │          │  │  │        ▲            │                    │   │    │    │
│ │          │  │  │        │            └─────────┬──────────┘   │    │    │
│ │          │  │  │        │ reconnect            │              │    │    │
│ │          │  │  │        │            ┌─────────▼──────────┐   │    │    │
│ │          │  │  │        └────────────│   DISCONNECTED     │   │    │    │
│ │          │  │  │                     │  (auto reconnect)  │   │    │    │
│ │          │  │  │                     └────────────────────┘   │    │    │
│ │          │  │  │                                               │    │    │
│ │          │  │  └───────────────────────────────────────────────┘    │    │
│ │          │  │                          │                            │    │
│ │          │  │                          │ logout                     │    │
│ │          │  │                          ▼                            │    │
│ │          │  │                   ┌────────────────┐                  │    │
│ │          │  │                   │ disconnectSocket│                 │    │
│ │◄─────────┼──┼───────────────────│ (cleanup)      │                  │    │
│ │          │  │                   └────────────────┘                  │    │
│ └──────────┘  └───────────────────────────────────────────────────────┘    │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 9.2 Page Navigation - Socket Stays Connected

```
                    ┌─────────────────────────────────────────────────┐
                    │              GLOBAL SOCKET (Always Connected)   │
                    │                                                 │
                    │   User authenticated → Socket connected         │
                    │   Receives: new_message, user_online, etc.      │
                    │                                                 │
                    └─────────────────────────────────────────────────┘
                                          │
                                          │ socket events broadcast
                                          │ to all subscribed listeners
                                          ▼
    ┌────────────────────────────────────────────────────────────────────────┐
    │                        PAGE NAVIGATION                                  │
    │                                                                         │
    │  HOME ──────────► SEARCH ──────────► MESSAGES ──────────► PROFILE      │
    │     │                │                   │                    │         │
    │     │                │                   │                    │         │
    │     ▼                ▼                   ▼                    ▼         │
    │  ┌──────┐        ┌──────┐         ┌──────────────┐      ┌──────┐       │
    │  │Socket│        │Socket│         │ Subscribe to │      │Socket│       │
    │  │stays │        │stays │         │ messages via │      │stays │       │
    │  │active│        │active│         │SocketContext │      │active│       │
    │  └──────┘        └──────┘         └──────────────┘      └──────┘       │
    │                                                                         │
    │        ◄──────────────────────────────────────────────────────►        │
    │              Socket connection maintained across ALL pages              │
    │                                                                         │
    └────────────────────────────────────────────────────────────────────────┘
```

### 9.3 Real-time Message Delivery Flow (Fixed)

```
  User A (on any page)          Backend Server           User B (on any page)
         │                           │                          │
         │                           │                          │
    [Socket Connected]          [Both users in               [Socket Connected]
         │                       onlineUsers map]               │
         │                           │                          │
    ┌────┴────┐                      │                     ┌────┴────┐
    │ Navigate│                      │                     │ Navigate│
    │Messages │                      │                     │  Home   │
    └────┬────┘                      │                     └────┬────┘
         │                           │                          │
         │ send_message ─────────────►                          │
         │ (to: userB)               │                          │
         │                           │                          │
         │                    ┌──────┴──────┐                   │
         │                    │ Check if B  │                   │
         │                    │ in onlineUsers                  │
         │                    └──────┬──────┘                   │
         │                           │                          │
         │                           │ ✓ B is online            │
         │                           │ (socket connected)       │
         │                           │                          │
         │◄──────────new_message─────┤─────new_message─────────►│
         │   (confirmation)          │      (delivery)          │
         │                           │                          │
    ┌────┴────┐                      │                     ┌────┴────┐
    │ Update  │                      │                     │ Update  │
    │Messages │                      │                     │ notif/  │
    │  list   │                      │                     │ badge   │
    └─────────┘                      │                     └─────────┘
```

### 9.4 SocketContext Subscription Pattern

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          SOCKET_CONTEXT                                     │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                    messageListeners (Set)                              │ │
│  │                                                                        │ │
│  │   ┌────────────┐  ┌────────────┐  ┌────────────┐                     │ │
│  │   │ Messages   │  │ Notif      │  │ Any Future │                     │ │
│  │   │ Component  │  │ Component  │  │ Component  │                     │ │
│  │   │ Handler    │  │ Handler    │  │ Handler    │                     │ │
│  │   └─────┬──────┘  └─────┬──────┘  └─────┬──────┘                     │ │
│  │         │               │               │                             │ │
│  │         │ subscribe     │ subscribe     │ subscribe                   │ │
│  │         ▼               ▼               ▼                             │ │
│  │   messageListenersRef.current.add(listener)                          │ │
│  │                                                                        │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
│                                    │                                        │
│                                    │ new_message event                      │
│                                    ▼                                        │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │  messageListenersRef.current.forEach(listener => listener(message))  │ │
│  │                                                                        │ │
│  │           Broadcasts to ALL subscribed components                     │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 9.5 Component Lifecycle Integration

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                     MESSAGES COMPONENT LIFECYCLE                            │
│                                                                             │
│  ┌────────────────┐                                                        │
│  │   COMPONENT    │                                                        │
│  │    MOUNT       │                                                        │
│  └───────┬────────┘                                                        │
│          │                                                                  │
│          │ useEffect with subscribeToMessages                              │
│          ▼                                                                  │
│  ┌────────────────────────────────────────────────────────────────────┐   │
│  │  const unsubscribe = subscribeToMessages(handleNewMessage);        │   │
│  │                                                                     │   │
│  │  // Component is now receiving real-time messages                  │   │
│  │  // Even if navigated from another page                            │   │
│  └────────────────────────────────────────────────────────────────────┘   │
│          │                                                                  │
│          │ cleanup (unmount or deps change)                                │
│          ▼                                                                  │
│  ┌────────────────────────────────────────────────────────────────────┐   │
│  │  return () => unsubscribe();                                        │   │
│  │                                                                     │   │
│  │  // Only removes listener, does NOT disconnect socket              │   │
│  │  // Socket stays connected globally                                 │   │
│  └────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Key Architecture Changes

| Before (Broken) | After (Fixed) |
|-----------------|---------------|
| Socket connected only in Messages.jsx | Socket connected globally in SocketContext |
| Navigating away → disconnectSocket() | Navigation → socket stays connected |
| Both users must be on Messages page | Users can be on ANY page |
| Required reload to see messages | Real-time delivery works everywhere |
| onlineUsers map frequently empty | onlineUsers accurately tracks all users |

### New Event Flow

| Step | Component | Action |
|------|-----------|--------|
| 1 | AuthProvider | User logs in, sets authenticated state |
| 2 | SocketProvider | Detects isAuthenticated=true, calls connectSocket(userId) |
| 3 | Backend | Receives connection, stores in onlineUsers[userId] |
| 4 | Messages.jsx | Mounts, calls subscribeToMessages(handler) |
| 5 | SocketContext | Adds handler to messageListeners Set |
| 6 | Backend | Receives message, looks up receiver in onlineUsers |
| 7 | Backend | Sends new_message event to receiver's socket |
| 8 | SocketContext | Receives event, broadcasts to all listeners |
| 9 | Messages.jsx | Handler updates local messages state |

---

*Last updated: March 2026*
