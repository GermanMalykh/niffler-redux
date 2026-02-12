#!/bin/bash

docker stop $(docker ps -a -q) 2>/dev/null
docker rm $(docker ps -a -q) 2>/dev/null

# Общая сеть: Kafka и Zookeeper по имени, не по IP
docker network create niffler-net 2>/dev/null || true

docker run --name niffler-all --network niffler-net -p 5432:5432 -e POSTGRES_PASSWORD=secret -v pgdata:/var/lib/postgresql/data -d postgres:15.1
docker run --name zookeeper --network niffler-net -e ZOOKEEPER_CLIENT_PORT=2181 -e ZOOKEEPER_TICK_TIME=2000 -p 2181:2181 -d confluentinc/cp-zookeeper:7.3.2

# Ждём, пока Zookeeper поднимется (иначе Kafka падает с exit 1)
echo "Waiting for Zookeeper (15s)..."
sleep 15

ZK_CONNECT="zookeeper:2181"
echo "Starting Kafka with KAFKA_ZOOKEEPER_CONNECT=$ZK_CONNECT"
# Два listener'а: localhost:9092 для IDE/хоста, kafka:29092 для Docker (Kafka UI и др.)
docker run --name kafka --network niffler-net \
  -e "KAFKA_BROKER_ID=1" \
  -e "KAFKA_ZOOKEEPER_CONNECT=${ZK_CONNECT}" \
  -e "KAFKA_LISTENERS=PLAINTEXT_HOST://0.0.0.0:9092,PLAINTEXT_DOCKER://0.0.0.0:29092" \
  -e "KAFKA_ADVERTISED_LISTENERS=PLAINTEXT_HOST://localhost:9092,PLAINTEXT_DOCKER://kafka:29092" \
  -e "KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT_HOST:PLAINTEXT,PLAINTEXT_DOCKER:PLAINTEXT" \
  -e "KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT_DOCKER" \
  -e "KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1" \
  -e "KAFKA_TRANSACTION_STATE_LOG_MIN_ISR=1" \
  -e "KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1" \
  -p 9092:9092 -p 29092:29092 -d confluentinc/cp-kafka:7.3.2

echo "Done. Check: docker ps && docker logs kafka 2>&1 | tail -5"
