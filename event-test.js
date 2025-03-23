import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 500,             // 동시에 요청할 가상 사용자 수
    duration: '30s',      // 테스트 지속 시간
};

export default function () {
    const userId = Math.floor(Math.random() * 10000) + 1;

    const payload = JSON.stringify({
        userId: userId,
        ticketNumber: 1,
        reason: "신규 발급"
    });

    const headers = {
        'Content-Type': 'application/json'
    };

    const res = http.post('http://www.ezmartket.store:8090/event/ticket/apply', payload, { headers });

    check(res, {
        'status was 200': (r) => r.status === 200
    });

    sleep(0.1); // Optional: 요청 간 시간 간격
}
