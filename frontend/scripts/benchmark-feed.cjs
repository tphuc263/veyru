const {performance} = require('node:perf_hooks');

const configuredUrl = (process.env.BASE_URL || 'http://localhost:8080').replace(/\/$/, '');
const baseUrl = configuredUrl.endsWith('/api/v1') ? configuredUrl : `${configuredUrl}/api/v1`;
const serverUrl = baseUrl.slice(0, -'/api/v1'.length);
const email = process.env.VEYRU_SEED_EMAIL;
const password = process.env.VEYRU_SEED_PASSWORD;
const runs = Number(process.env.BENCHMARK_RUNS || 20);

if (!email || !password) {
  console.error('Set VEYRU_SEED_EMAIL and VEYRU_SEED_PASSWORD (use the output from npm run seed).');
  process.exit(1);
}
if (!Number.isInteger(runs) || runs < 2) {
  console.error('BENCHMARK_RUNS must be an integer greater than 1.');
  process.exit(1);
}

const cookies = new Map();
let csrf;

async function request(path, options = {}) {
  const headers = new Headers(options.headers);
  if (cookies.size) headers.set('cookie', [...cookies].map(([key, value]) => `${key}=${value}`).join('; '));
  if (csrf && !['GET', 'HEAD'].includes(options.method || 'GET')) headers.set('X-XSRF-TOKEN', csrf);
  const response = await fetch(`${baseUrl}${path}`, {...options, headers});
  for (const value of response.headers.getSetCookie?.() || []) {
    const [pair] = value.split(';');
    const separator = pair.indexOf('=');
    cookies.set(pair.slice(0, separator), pair.slice(separator + 1));
  }
  const text = await response.text();
  if (!response.ok) throw new Error(`${options.method || 'GET'} ${path} -> ${response.status}: ${text}`);
  return text ? JSON.parse(text) : null;
}

function percentile(values, fraction) {
  const sorted = [...values].sort((left, right) => left - right);
  return sorted[Math.ceil(sorted.length * fraction) - 1];
}

async function timedFeed() {
  const start = performance.now();
  await request('/feed/unified?size=20');
  return performance.now() - start;
}

async function metric(name) {
  try {
    const response = await fetch(`${serverUrl}/actuator/metrics/${name}`, {
      headers: {cookie: [...cookies].map(([key, value]) => `${key}=${value}`).join('; ')}
    });
    if (!response.ok) return {unavailable: true, status: response.status};
    return response.json();
  } catch {
    return {unavailable: true};
  }
}

async function main() {
  csrf = (await request('/csrf')).token;
  await request('/sessions', {
    method: 'POST',
    headers: {'content-type': 'application/json'},
    body: JSON.stringify({identifier: email, password})
  });

  const cold = await timedFeed();
  const warm = [];
  for (let index = 1; index < runs; index += 1) warm.push(await timedFeed());
  console.log(JSON.stringify({
    requests: runs,
    latencyMs: {
      cold: Number(cold.toFixed(2)),
      warmP50: Number(percentile(warm, 0.50).toFixed(2)),
      warmP95: Number(percentile(warm, 0.95).toFixed(2))
    },
    metrics: {
      affinityCache: await metric('veyru.feed.affinity.cache'),
      graphFallback: await metric('veyru.feed.graph'),
      totalFeed: await metric('veyru.feed.total')
    }
  }, null, 2));
}

main().catch(error => {
  console.error(error.message);
  process.exitCode = 1;
});
