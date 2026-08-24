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
