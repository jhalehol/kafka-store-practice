#!/usr/bin/env bash

docker exec -it kafka1 kafka-topics --bootstrap-server kafka1:29092 --list

echo -e 'Creating kafka topics'
docker exec -it kafka1 kafka-topics --bootstrap-server kafka1:29092 --create --if-not-exists --topic sales --replication-factor 1 --partitions 1
docker exec -it kafka1 kafka-topics --bootstrap-server kafka1:29092 --create --if-not-exists --topic salesmen --replication-factor 1 --partitions 1
docker exec -it kafka1 kafka-topics --bootstrap-server kafka1:29092 --create --if-not-exists --topic products --replication-factor 1 --partitions 1
docker exec -it kafka1 kafka-topics --bootstrap-server kafka1:29092 --create --if-not-exists --topic salesman-sales-average --replication-factor 1 --partitions 1
docker exec -it kafka1 kafka-topics --bootstrap-server kafka1:29092 --create --if-not-exists --topic high-sales-product --replication-factor 1 --partitions 1

echo -e 'Successfully created the following topics:'
docker exec -it kafka1 kafka-topics --bootstrap-server kafka2:29093 --list