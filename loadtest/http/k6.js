// k6 run: API=http://localhost:18092 k6 run k6.js
// Rampa di carico sugli endpoint "pesanti" segnalati dal pentest DoS.
import http from 'k6/http';
import { check, sleep } from 'k6';

const API = __ENV.API || 'http://localhost:18092';
if (/asteriacore\.online/.test(API) || !/^https?:\/\/(localhost|127\.0\.0\.1|10\.|192\.168\.|172\.(1[6-9]|2\d|3[01])\.)/.test(API)) {
  throw new Error(`Target non locale: ${API}. Questo test punta SOLO a un'istanza usa-e-getta.`);
}

export const options = {
  scenarios: {
    ramp: {
      executor: 'ramping-arrival-rate',
      startRate: 20, timeUnit: '1s',
      preAllocatedVUs: 100, maxVUs: 500,
      stages: [
        { target: 100, duration: '30s' },
        { target: 300, duration: '30s' },
        { target: 600, duration: '30s' },
        { target: 0,   duration: '10s' },
      ],
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<800', 'p(99)<2000'],
    http_req_failed: ['rate<0.05'],
  },
};

const heavy = [
  '/api/v2/community/stats',
  '/api/v2/community/rooms?limit=60&offset=0',
  '/api/v2/community/rooms?limit=60&offset=5000000', // deep offset (cap OFFSET)
  '/api/v2/profile/habbo',                            // fan-out ~13 query
];

export default function () {
  const path = heavy[Math.floor(Math.random() * heavy.length)];
  const res = http.get(`${API}${path}`);
  check(res, { 'status ok o 404/429 gestito': r => [200, 404, 429].includes(r.status) });
  sleep(0.1);
}
