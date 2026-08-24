// @vitest-environment jsdom
import '@testing-library/jest-dom/vitest'
import {render, screen} from '@testing-library/react'
import {beforeEach, expect, it, vi} from 'vitest'
import {AuthProvider, useAuthContext} from './AuthContext'

const {currentSession, initializeCsrf} = vi.hoisted(() => ({
  currentSession: vi.fn(),
  initializeCsrf: vi.fn(),
}))

vi.mock('../services/authService', () => ({
  currentSession,
  initializeCsrf,
  login: vi.fn(),
  logout: vi.fn(),
  register: vi.fn(),
}))

const SessionState = () => {
  const {loading, user} = useAuthContext()
  return <div>{loading ? 'loading' : user?.username ?? 'anonymous'}</div>
}

beforeEach(() => {
  vi.clearAllMocks()
  initializeCsrf.mockResolvedValue(undefined)
})

it('bootstraps authentication from the current cookie session after CSRF initialization', async () => {
  currentSession.mockResolvedValue({id: 'u1', username: 'alice', email: 'a@example.com', role: 'ROLE_USER'})

  render(<AuthProvider><SessionState /></AuthProvider>)

  expect(await screen.findByText('alice')).toBeInTheDocument()
  expect(initializeCsrf).toHaveBeenCalledOnce()
  expect(currentSession).toHaveBeenCalledOnce()
  expect(initializeCsrf.mock.invocationCallOrder[0]).toBeLessThan(currentSession.mock.invocationCallOrder[0])
})

it('stays anonymous when no server session exists', async () => {
  currentSession.mockRejectedValue(new Error('Unauthorized'))

  render(<AuthProvider><SessionState /></AuthProvider>)

  expect(await screen.findByText('anonymous')).toBeInTheDocument()
})
