# Veyru Frontend

React 19 client for Veyru, built with TypeScript/JavaScript and Vite. React Router manages navigation, Axios handles REST requests, and STOMP carries real-time messages and notifications.

[Live demo](https://veyru.dev) · [Project overview](../README.md) · [Backend guide](../backend/README.md)

## Local development

Use **Node.js 22** and npm, matching CI. Start the backend using its [local development guide](../backend/README.md#local-development), then run the following from `frontend/`:

```bash
npm ci
```

Create `frontend/.env.local` with these public development settings:

```dotenv
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_SOCKET_URL=http://localhost:8080
VITE_OAUTH_URL=http://localhost:8080/oauth2/authorization/google
```

```bash
npm run dev
```

Open [localhost:5173](http://localhost:5173). Use `localhost` consistently so cookies and configured browser origins match.

REST and OAuth default to the local backend, but the WebSocket service defaults to the browser's origin when its variable is absent. Vite has no `/ws` proxy, so explicitly setting `VITE_SOCKET_URL` is necessary for this setup. Restart Vite after changing environment variables. The local backend allows ports 5173 and 5174 by default; another port needs a matching backend CORS origin.

## Runtime environments

| Environment | Frontend delivery | Backend connection |
| --- | --- | --- |
| Local development | Vite on port 5173 | Browser calls port 8080 directly using the variables above |
| Local Compose | Nginx serves the built app on port 5173 | Nginx proxies `/api`, OAuth routes and `/ws` to the backend container |
| Deployed application | Vercel serves `veyru.dev` | Browser calls `api.veyru.dev` on Heroku directly |

The Nginx configuration belongs to the Docker frontend image. Vercel uses [vercel.json](vercel.json) to rewrite SPA routes to `index.html`.

## Vercel deployment

Use `frontend/` as the project root, `npm run build` as the build command, and `dist` as the output directory. Set these build-time variables:

```dotenv
VITE_API_BASE_URL=https://api.veyru.dev/api/v1
VITE_SOCKET_URL=https://api.veyru.dev
VITE_OAUTH_URL=https://api.veyru.dev/oauth2/authorization/google
```

| Variable | Meaning |
| --- | --- |
| `VITE_API_BASE_URL` | REST base URL including `/api/v1` |
| `VITE_SOCKET_URL` | Backend origin; the client converts HTTPS to WSS and appends `/ws` |
| `VITE_OAUTH_URL` | Backend endpoint that starts Google sign-in |

Vite includes these values in browser assets, so they must contain no secrets. Redeploy after changing them. Backend CORS, cookies and OAuth callbacks must match the [backend production configuration](../backend/README.md#production-deployment).

## Sessions and real-time connections

[`AuthContext`](src/context/AuthContext.jsx) initializes CSRF before loading the current session. On the OAuth redirect route, the exchange flow handles sign-in instead of the usual session lookup.

The [`API client`](src/config/ApiConfig.ts) sends cookies with requests and keeps the CSRF token in memory. It obtains the token from `GET /api/v1/csrf`, then adds `X-XSRF-TOKEN` to state-changing requests. It does not read the API subdomain's cookies or store access/refresh tokens in browser storage.

Eligible `401` responses share a refresh request while it is in flight and retry once. An `INVALID_CSRF_TOKEN` response triggers a CSRF sync and one retry. When refresh fails, an authentication-expired event clears the client session.

The [`WebSocket service`](src/services/socketService.ts) connects to `/ws`, sends the CSRF token in STOMP `CONNECT`, and subscribes to user messages and presence events. Outgoing chat events carry client message IDs for backend duplicate suppression. Reconnection and pending messages are held in browser memory; they are not a durable offline queue.

## API contract

The backend commits its REST contract in [`backend/openapi/openapi.json`](../backend/openapi/openapi.json). This directory holds a release copy at [`openapi/openapi.json`](openapi/openapi.json), used to generate [`src/types/generated-api.ts`](src/types/generated-api.ts).

After an intentional backend contract update, run from `frontend/`:

```bash
cp ../backend/openapi/openapi.json openapi/openapi.json
npm run api:generate
npm run api:check
```

Review and commit the contract changes and generated types together. `api:check` checks generated types against the frontend contract copy; it does not fetch the running backend or verify that both committed JSON copies match. Do not edit generated types by hand.

## Checks

After `npm ci`, run from `frontend/`:

```bash
npm run api:check
npm run lint
npm test
npm run build
```

- **Vitest and Testing Library:** API/session behavior and component tests, including feed pagination.
- **Build:** TypeScript checking followed by Vite asset generation.
- **ESLint:** uses the repository's configured warning allowance; it is not a zero-warning policy.

Install Chromium before running the browser smoke tests:

```bash
npx playwright install chromium
npm run test:e2e
```

[Playwright](playwright.config.ts) starts a local Vite server on port 4173. The [smoke tests](e2e/auth-smoke.spec.ts) check the login and password-reset screens with mocked authentication API responses. They do not test Google sign-in, live messaging or the full deployed stack.

To check the optional Docker packaging separately:

```bash
docker build -t veyru-frontend .
```

For a working container stack with the backend proxy, use the [root Compose setup](../README.md#run-locally).
