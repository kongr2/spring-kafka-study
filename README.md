# Getting Started

## 카프카 시작
> docker-compose up -d

## 스프링 부트 시작 후 토픽 생성 확인
> newtopic 으로 생성 하도록 빈으로 등록 함

## 토픽 리스트 확인 (topic)
> docker exec -it <컨테이너 아이디> kafka-topics --bootstrap-server localhost:9092 --list

## 토픽의 파티션 확인
> docker exec -it <컨테이너 아이디> kafka-topics --bootstrap-server localhost:9092 --describe --topic spring-topic

## payload publish test
> docker exec -it <컨테이너 아이디> kafka-console-producer --topic spring-topic --bootstrap-server localhost:9092
> payload 입력
> 스프링 부트 콘솔 리스너 메시지 확인

## 카프카 컨슈밍
> docker exec -it <컨테이너 아이디> kafka-console-consumer --bootstrap-server localhost:9092 --topic spring-topic

## 그룹 정보 보기
> docker exec -it <컨테이너 아이디> kafka-consumer-groups --group first-group --bootstrap-server localhost:9092 --describe
> 

## DLT 컨슈머 메시지 보기
> docker exec -it <컨테이너 아이디> kafka-console-consumer --bootstrap-server localhost:9092 --topic kbus-retryable-topic-dlt --from-beginning

## 메시지 발행 테스트
> http://localhost:8080/<토픽명>/event?item=<메시지 내용>
### 테스트 토픽 리스트
> kbus-topic
> kbus-error-topic
> kbus-failed-topic