import {expect, test} from '@playwright/test'

test.beforeEach(async ({page}) => {
  await page.route('**/api/v1/csrf', route => route.fulfill({json: {token: 'csrf'}}))
  await page.route('**/api/v1/sessions/current', route => route.fulfill({status: 401, json: {code: 'AUTHENTICATION_REQUIRED'}}))
  await page.route('**/api/v1/sessions/refresh', route => route.fulfill({status: 401, json: {code: 'AUTHENTICATION_REQUIRED'}}))
})

test('anonymous session bootstrap reaches login', async ({page}) => {
  await page.goto('/login')
  await expect(page.getByRole('button', {name: 'Log in', exact: true})).toBeVisible()
})

test('reset page renders directly without a validation endpoint', async ({page}) => {
  const validationCalls: string[] = []
  page.on('request', request => {
    if (request.url().includes('password-reset-validations')) validationCalls.push(request.url())
  })

  await page.goto('/reset-password?token=one-time-reset-token')

  await expect(page.getByRole('button', {name: /reset password/i})).toBeVisible()
  expect(validationCalls).toEqual([])
})
