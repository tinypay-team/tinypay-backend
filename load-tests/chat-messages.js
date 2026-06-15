import http from 'k6/http';
import { check, sleep } from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://host.docker.internal:8080';
const token = __ENV.ACCESS_TOKEN;
const sessionId = __ENV.SESSION_ID;
const smoke = __ENV.SMOKE === 'true';
const stress = __ENV.STRESS === 'true';

const loadScenario = {
  executor: 'ramping-vus',
  startVUs: 1,
  stages: [
    { duration: '20s', target: 10 },
    { duration: '40s', target: 10 },
    { duration: '20s', target: 30 },
    { duration: '40s', target: 30 },
    { duration: '20s', target: 0 },
  ],
  gracefulRampDown: '10s',
};

const smokeScenario = {
  executor: 'constant-vus',
  vus: 5,
  duration: '30s',
};

const stressScenario = {
  executor: 'ramping-vus',
  startVUs: 1,
  stages: [
    // 기준 성능 확인
    { duration: '30s', target: 10 },
    { duration: '30s', target: 10 },

    // 부하를 단계적으로 높여 성능 저하 시작점을 찾는다.
    { duration: '30s', target: 30 },
    { duration: '30s', target: 30 },
    { duration: '30s', target: 50 },
    { duration: '30s', target: 50 },
    { duration: '30s', target: 100 },
    { duration: '30s', target: 100 },

    // 부하 제거 후 서버가 정상 상태로 회복하는지 확인한다.
    { duration: '30s', target: 0 },
  ],
  gracefulRampDown: '30s',
};

export const options = {
  scenarios: {
    chat_message_reads: stress
      ? stressScenario
      : smoke
        ? smokeScenario
        : loadScenario,
  },
  thresholds: {
    // 스트레스 테스트에서는 실패가 발생하는 한계점도 관찰 대상이다.
    http_req_failed: [stress ? 'rate<0.05' : 'rate<0.01'],
    http_req_duration: stress
      ? ['p(95)<1000', 'p(99)<2000']
      : ['p(95)<500', 'p(99)<1000'],
  },
};

export function setup() {
  if (!token || !sessionId) {
    throw new Error('ACCESS_TOKEN and SESSION_ID are required');
  }
}

export default function () {
  const response = http.get(`${baseUrl}/api/chat/sessions/${sessionId}/messages`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
    tags: {
      name: 'GET /api/chat/sessions/{sessionId}/messages',
    },
  });

  check(response, {
    'status is 200': (res) => res.status === 200,
    'response contains data': (res) => {
      try {
        return Array.isArray(res.json('data'));
      } catch (_) {
        return false;
      }
    },
  });

  sleep(0.2);
}
