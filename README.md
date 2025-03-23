# Shopping Event
### 기간: 2025-03-01 ~

**Shopping Event**는 RESTful API 및 배치 서비스로
Spring Boot, jdbc, Redis, Kafka 등의 기술을 활용하여 이벤트 처리를 제공합니다.

이 프로젝트는 확장성과 유지보수를 고려한 모듈화된 아키텍처로 구성되어 있습니다.

## 주요 기능
- **다수의 Database 동시 사용**
    - 메인 비즈니스 DatasourceConfig [ShopDataSourceConfig.java](src/main/java/com/shop/shoppingevent/config/ShopDataSourceConfig.java)
    - 이벤트 DatasourceConfig [EventDataSourceConfig.java](src/main/java/com/shop/shoppingevent/config/EventDataSourceConfig.java)
- **빠른 DB 처리를 위한 jdbc api 사용**
    - 많은 TPS를 고려하여 jdbc 사용
- **Atomic Count 를 위한 Redis 사용**
    - 동시성 문제를 해결하기 위해 Redis 사용
- **@ConditionalOnProperty 를 사용하여 선택적으로 kafka Consumer 등록**
    - ``@ConditionalOnProperty(name = "feature.point-consumer.enabled", havingValue = "true")`` 설정

## 기술 스택

- **Java 21LTS**
- **Spring Boot 3.4.3**
- **Spring Data Redis**
- **Spring JDBC**
- **Spring for Apache Kafka 3.8.1**


## 부하 테스트 결과 1차
- 예상 사용자 : 500명
- db-max-connection-size: 500
- 수행시간 : 30초
- 준비 수량 : 950개
- 요청 수 : 11327개
- 성공 수량 : 943개
- 실패 수량 : 7개
- 실패 사유 : Duplication key error 발생으로 보아 동일한 사용자가 중복 요청을 보낸 것으로 보이며, 포인트 지급은 되지 않았지만
<br> 발행수에 영향을 준 것으로 확인됨
- 개선 방안 : Lua Script를 활용하여 Atomic 처리 하도록 변경
- 부하 테스트 결과 : 평균 응답 시간: 1.22초 (성공 요청 평균: 4.98초)
```
         /\      Grafana   /‾‾/  
    /\  /  \     |\  __   /  /   
   /  \/    \    | |/ /  /   ‾‾\ 
  /          \   |   (  |  (‾)  |
 / __________ \  |_|\_\  \_____/ 

     execution: local
        script: event-test.js
        output: -

     scenarios: (100.00%) 1 scenario, 500 max VUs, 1m0s max duration (incl. graceful stop):
              * default: 500 looping VUs for 30s (gracefulStop: 30s)


     ✗ status was 200
      ↳  8% — ✓ 943 / ✗ 10384

     checks.........................: 8.32%  943 out of 11327
     data_received..................: 2.4 MB 79 kB/s
     data_sent......................: 2.5 MB 81 kB/s
     http_req_blocked...............: avg=15.01ms min=0s       med=12.59ms  max=57.53ms  p(90)=26.62ms p(95)=29.24ms
     http_req_connecting............: avg=14.94ms min=0s       med=12.58ms  max=53.43ms  p(90)=26.61ms p(95)=29.1ms 
     http_req_duration..............: avg=1.22s   min=242.72ms med=885.31ms max=8.43s    p(90)=1.96s   p(95)=3.04s  
       { expected_response:true }...: avg=4.98s   min=790.08ms med=6.09s    max=8.43s    p(90)=7.91s   p(95)=8.19s  
     http_req_failed................: 91.67% 10384 out of 11327
     http_req_receiving.............: avg=1.03ms  min=4µs      med=18µs     max=133.61ms p(90)=568.2µs p(95)=1.83ms 
     http_req_sending...............: avg=10.34µs min=1µs      med=6µs      max=627µs    p(90)=18µs    p(95)=28µs   
     http_req_tls_handshaking.......: avg=0s      min=0s       med=0s       max=0s       p(90)=0s      p(95)=0s     
     http_req_waiting...............: avg=1.22s   min=242.67ms med=885.16ms max=8.42s    p(90)=1.96s   p(95)=3.04s  
     http_reqs......................: 11327  371.408223/s
     iteration_duration.............: avg=1.33s   min=367.24ms med=1s       max=8.57s    p(90)=2.06s   p(95)=3.14s  
     iterations.....................: 11327  371.408223/s
     vus............................: 500    min=500            max=500
     vus_max........................: 500    min=500            max=500


running (0m30.5s), 000/500 VUs, 11327 complete and 0 interrupted iterations
default ✓ [======================================] 500 VUs  30s
```

## 부하 테스트 결과 2차
- 예상 사용자 : 500명
- db-max-connection-size: 500
- 수행시간 : 30초
- 준비 수량 : 1,000개
- 요청 수 : 11,703개
- 성공 수량 : 1000개
- 실패 수량 : 0개
- Redis + Lua Script(원자적 처리)를 활용하여 1,000명까지 이벤트 참여 성공
- 테스트를 통해 이벤트 참가 제한 기능이 정확히 동작함을 검증
- 개선 방안 : 건당 1회 db 처리 및 kafka 처리에서 50건씩 묶어 batch 처리로 변경 (I/O 감소 및 네트워크 비용감소) 
- 부하 테스트 결과 : 평균 응답 시간: 1.17초 (성공 요청 평균: 4.37초)
```
         /\      Grafana   /‾‾/  
    /\  /  \     |\  __   /  /   
   /  \/    \    | |/ /  /   ‾‾\ 
  /          \   |   (  |  (‾)  |
 / __________ \  |_|\_\  \_____/ 

     execution: local
        script: event-test.js
        output: -

     scenarios: (100.00%) 1 scenario, 500 max VUs, 1m0s max duration (incl. graceful stop):
              * default: 500 looping VUs for 30s (gracefulStop: 30s)

     ✗ status was 200
      ↳  8% — ✓ 1000 / ✗ 10703

     checks.........................: 8.54%  1000 out of 11703
     data_received..................: 2.5 MB 81 kB/s
     data_sent......................: 2.5 MB 84 kB/s
     http_req_blocked...............: avg=16.76ms min=0s       med=12.17ms  max=105.9ms  p(90)=26.46ms p(95)=28.89ms
     http_req_connecting............: avg=14.69ms min=0s       med=12.16ms  max=77.04ms  p(90)=26.44ms p(95)=28.52ms
     http_req_duration..............: avg=1.17s   min=22.04ms  med=814.7ms  max=7.89s    p(90)=2.05s   p(95)=2.58s  
       { expected_response:true }...: avg=4.37s   min=910.82ms med=2.72s    max=7.89s    p(90)=7.11s   p(95)=7.37s  
     http_req_failed................: 91.45% 10703 out of 11703
     http_req_receiving.............: avg=1.33ms  min=0s       med=16µs     max=332.14ms p(90)=295µs   p(95)=2.41ms 
     http_req_sending...............: avg=8.98µs  min=1µs      med=6µs      max=1.59ms   p(90)=15µs    p(95)=23µs   
     http_req_tls_handshaking.......: avg=0s      min=0s       med=0s       max=0s       p(90)=0s      p(95)=0s     
     http_req_waiting...............: avg=1.17s   min=22.03ms  med=814ms    max=7.69s    p(90)=2.04s   p(95)=2.57s  
     http_reqs......................: 11703  384.019852/s
     iteration_duration.............: avg=1.29s   min=227.3ms  med=930.65ms max=8.09s    p(90)=2.15s   p(95)=2.68s  
     iterations.....................: 11703  384.019852/s
     vus............................: 500    min=500            max=500
     vus_max........................: 500    min=500            max=500


running (0m30.5s), 000/500 VUs, 11703 complete and 0 interrupted iterations
default ✓ [======================================] 500 VUs  30s
```
