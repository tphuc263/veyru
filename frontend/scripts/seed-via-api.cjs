const configuredUrl = (process.env.BASE_URL || 'http://localhost:8080').replace(/\/$/, '');
const baseUrl = configuredUrl.endsWith('/api/v1') ? configuredUrl : `${configuredUrl}/api/v1`;
const runId = (process.env.SEED_RUN_ID || Date.now().toString(36)).replace(/[^a-z0-9]/gi, '').slice(-10);
const password = process.env.SEED_PASSWORD || `Veyru-${runId}-Pass1!`;
const png = Buffer.from(
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=',
  'base64'
);

class Session {
  constructor() {
    this.cookies = new Map();
    this.csrf = null;
  }

  async request(path, options = {}) {
    const headers = new Headers(options.headers);
    if (this.cookies.size) {
      headers.set('cookie', [...this.cookies].map(([key, value]) => `${key}=${value}`).join('; '));
    }
    if (this.csrf && !['GET', 'HEAD'].includes(options.method || 'GET')) {
      headers.set('X-XSRF-TOKEN', this.csrf);
    }
    const response = await fetch(`${baseUrl}${path}`, {...options, headers});
    const setCookies = response.headers.getSetCookie?.() || [];
    for (const value of setCookies) {
      const [pair] = value.split(';');
      const separator = pair.indexOf('=');
      this.cookies.set(pair.slice(0, separator), pair.slice(separator + 1));
    }
    const text = await response.text();
    const body = text ? JSON.parse(text) : null;
    if (!response.ok) {
      throw new Error(`${options.method || 'GET'} ${path} -> ${response.status}: ${text}`);
    }
    return body;
  }

  async initializeCsrf() {
    this.csrf = (await this.request('/csrf')).token;
  }

  async json(path, method, body) {
    return this.request(path, {
      method,
      headers: {'content-type': 'application/json'},
      body: body === undefined ? undefined : JSON.stringify(body)
    });
  }
}

async function registerAndLogin(index) {
  const session = new Session();
  const username = `seed_${runId}_${index}`.slice(0, 30);
  const email = `${username}@example.test`;
  await session.initializeCsrf();
  await session.json('/users', 'POST', {username, email, password});
  const user = await session.json('/sessions', 'POST', {identifier: email, password});
  return {session, user, email};
}

async function uploadPhoto(account, index) {
  const form = new FormData();
  form.append('image', new Blob([png], {type: 'image/png'}), `seed-${runId}-${index}.png`);
  form.append('caption', `Seed photo ${index} (${runId})`);
  form.append('tags', 'portfolio');
  form.append('tags', `seed-${runId}`);
  try {
    return await account.session.request('/photos', {method: 'POST', body: form});
  } catch (error) {
    throw new Error(
      `${error.message}\nPhoto upload failed. Start the backend with valid Cloudinary credentials before running npm run seed.`
    );
  }
}

async function main() {
  const accounts = [];
  for (let index = 0; index < 5; index += 1) accounts.push(await registerAndLogin(index));

  // Viewer follows two users; both lead to the same two-hop suggestion.
  await accounts[0].session.request(`/users/me/following/${accounts[1].user.id}`, {method: 'PUT'});
  await accounts[0].session.request(`/users/me/following/${accounts[2].user.id}`, {method: 'PUT'});
  await accounts[1].session.request(`/users/me/following/${accounts[3].user.id}`, {method: 'PUT'});
  await accounts[2].session.request(`/users/me/following/${accounts[3].user.id}`, {method: 'PUT'});
  await accounts[3].session.request(`/users/me/following/${accounts[4].user.id}`, {method: 'PUT'});

  const photos = [];
  for (let index = 0; index < accounts.length; index += 1) {
    photos.push(await uploadPhoto(accounts[index], index));
  }
  await accounts[0].session.request(`/photos/${photos[1].id}/likes/me`, {method: 'PUT'});
  await accounts[0].session.json(`/photos/${photos[1].id}/comments`, 'POST', {
    text: `Seed comment ${runId}`,
    mentionedUsernames: []
  });
  await accounts[0].session.json(`/photos/${photos[2].id}/shares`, 'POST', {
    caption: `Seed share ${runId}`
  });

  console.log(JSON.stringify({
    runId,
    viewer: {email: accounts[0].email, password},
    users: accounts.map(({user}) => ({id: user.id, username: user.username})),
    photoIds: photos.map(({id}) => id)
  }, null, 2));
}

main().catch(error => {
  console.error(error.message);
  process.exitCode = 1;
});
