import { AxiosError, AxiosHeaders, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios';
import { afterEach, describe, expect, it, vi } from 'vitest';
import api from './ApiConfig';
import { clearCsrfToken, setCsrfToken } from './CsrfToken';

const response = (config: InternalAxiosRequestConfig, status: number): AxiosResponse => ({
  config,
  data: {},
  headers: {},
  status,
  statusText: String(status),
});

describe('API client', () => {
  afterEach(() => {
    clearCsrfToken();
    vi.restoreAllMocks();
  });

  it('uses credentialed cookies without reading a cross-subdomain cookie', () => {
    expect(api.defaults.withCredentials).toBe(true);
    expect(api.defaults.withXSRFToken).toBe(false);
  });

  it('adds the in-memory CSRF token to unsafe requests', async () => {
    let receivedToken: string | undefined;
    api.defaults.adapter = async rawConfig => {
      const config = { ...rawConfig, headers: rawConfig.headers ?? new AxiosHeaders() } as InternalAxiosRequestConfig;
      receivedToken = config.headers.get('X-XSRF-TOKEN')?.toString();
      return response(config, 204);
    };
    setCsrfToken('cross-subdomain-token');

    await api.post('/photos', {});

    expect(receivedToken).toBe('cross-subdomain-token');
  });

  it('shares one refresh request and retries each 401 once', async () => {
    let refreshes = 0;
    const attempts = new Map<string, number>();
    api.defaults.adapter = async rawConfig => {
      const config = { ...rawConfig, headers: rawConfig.headers ?? new AxiosHeaders() } as InternalAxiosRequestConfig;
      if (config.url === '/sessions/refresh') {
        refreshes += 1;
        await Promise.resolve();
        return response(config, 204);
      }
      const count = (attempts.get(config.url ?? '') ?? 0) + 1;
      attempts.set(config.url ?? '', count);
      if (count === 1) {
        throw new AxiosError('Unauthorized', 'ERR_BAD_REQUEST', config, undefined, response(config, 401));
      }
      return response(config, 200);
    };

    await Promise.all([api.get('/photos/a'), api.get('/photos/b')]);

    expect(refreshes).toBe(1);
    expect(attempts).toEqual(new Map([['/photos/a', 2], ['/photos/b', 2]]));
  });
});
