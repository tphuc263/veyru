# Veyru Frontend

React client for Veyru. See the [project README](../README.md) for the full-stack quick start and architecture.

## Stack

- React 19, TypeScript and Vite
- React Router and Axios
- STOMP WebSocket messaging and notifications
- Vitest, Testing Library and Playwright
- Nginx for the production container and backend proxy

## Local development

Start the backend, then run:

```bash
npm ci
npm run dev
```

Vite serves the application at http://localhost:5173 and defaults to the backend at http://localhost:8080.

## Production on Vercel

Configure these build-time variables and redeploy the frontend:

```text
VITE_API_BASE_URL=https://api.veyru.dev/api/v1
VITE_SOCKET_URL=https://api.veyru.dev
VITE_OAUTH_URL=https://api.veyru.dev/oauth2/authorization/google
```

The API client obtains the CSRF token from `GET /api/v1/csrf`, stores it only in memory and sends
it in `X-XSRF-TOKEN` for state-changing REST requests and STOMP `CONNECT`. It does not attempt to
read the host-only cookie belonging to `api.veyru.dev`.

## Checks

```bash
npm run api:check
npm run lint
npm test
npm run build
npm run test:e2e
docker build -t veyru-frontend .
```

`openapi/openapi.json` is the backend release contract used to generate `src/types/generated-api.ts`. Run `npm run api:generate` only after an intentional contract update.
