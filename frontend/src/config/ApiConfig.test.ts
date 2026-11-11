import { AxiosError, AxiosHeaders, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios';
import { afterEach, describe, expect, it, vi } from 'vitest';
import api from './ApiConfig';
import { clearCsrfToken, setCsrfToken } from './CsrfToken';

const response = (
  config: InternalAxiosRequestConfig,
  status: number,
  data: Record<string, unknown> = {},
): AxiosResponse => ({
  config,
  data,
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

  it('shares one CSRF sync request and retries concurrent 403 CSRF failures once', async () => {
    let csrfCalls = 0;
    const seenTokens = new Map<string, string[]>();
    setCsrfToken('stale-token');

    api.defaults.adapter = async rawConfig => {
      const config = { ...rawConfig, headers: rawConfig.headers ?? new AxiosHeaders() } as InternalAxiosRequestConfig;
      if (config.url === '/csrf') {
        csrfCalls += 1;
        await Promise.resolve();
        return response(config, 200, { token: 'rotated-token' });
      }
      const token = config.headers.get('X-XSRF-TOKEN')?.toString() ?? '';
      const url = config.url ?? '';
      seenTokens.set(url, [...(seenTokens.get(url) ?? []), token]);
      if (token !== 'rotated-token') {
        throw new AxiosError(
          'Forbidden',
          'ERR_BAD_REQUEST',
          config,
          undefined,
          response(config, 403, { code: 'INVALID_CSRF_TOKEN' }),
        );
      }
      return response(config, 204);
    };

    await Promise.all([
      api.put('/users/me/following/1'),
      api.put('/users/me/following/2'),
    ]);

    expect(csrfCalls).toBe(1);
    expect(seenTokens.get('/users/me/following/1')).toEqual(['stale-token', 'rotated-token']);
    expect(seenTokens.get('/users/me/following/2')).toEqual(['stale-token', 'rotated-token']);
  });

  it('does not resync CSRF or retry when 403 is an ACCESS_DENIED permission error', async () => {
    let csrfCalls = 0;
    let putAttempts = 0;
    setCsrfToken('valid-token');

    api.defaults.adapter = async rawConfig => {
      const config = { ...rawConfig, headers: rawConfig.headers ?? new AxiosHeaders() } as InternalAxiosRequestConfig;
      if (config.url === '/csrf') {
        csrfCalls += 1;
        return response(config, 200, { token: 'new-token' });
      }
      putAttempts += 1;
      throw new AxiosError(
        'Forbidden',
        'ERR_BAD_REQUEST',
        config,
        undefined,
        response(config, 403, { code: 'ACCESS_DENIED' }),
      );
    };

    await expect(api.delete('/photos/other-user-photo')).rejects.toThrow();
    expect(csrfCalls).toBe(0);
    expect(putAttempts).toBe(1);
  });
});
