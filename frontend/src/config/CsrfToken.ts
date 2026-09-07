let token: string | null = null;

export const setCsrfToken = (value: string): void => {
  const normalized = value.trim();
  if (!normalized) {
    throw new Error('The backend returned an empty CSRF token.');
  }
  token = normalized;
};

export const getCsrfToken = (): string | null => token;

export const clearCsrfToken = (): void => {
  token = null;
};
