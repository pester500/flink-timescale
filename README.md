# Running
* `./scripts/build.sh && ./scripts/launch-dependencies.sh`
* Sample data is in `data/` folder, post it to the Kafka topic: `input`
* Ex: `./bin/kafka-console-producer.sh --topic input --broker-list localhost:9092 < ./flink-timescale/data/passwords.txt`

# Scripts
* `scripts/build.sh` - builds Flink job jar and Docker image
* `scripts/launch-dependencies.sh` - Start Zookeeper, TimescaleDB, Kafka, Job Manager, Task Manager, and Flink job
* `scripts/kill-containers.sh` - Stop and remove all docker containers

# Caveats
To run the Docker containers, add this entry to `/etc/hosts`:
127.0.0.1 kafka

# Performance considerations
The Flink Task Manager instance count defaults to the number of cores available to the Docker engine.
If you have more than 4 cores available and want to see how much greater parallelism impacts processing speed, change `MAX PARALLELISM` to however many cores you have,

AND

Change `KAFKA_CREATE_TOPICS: "input:4:1"` to `KAFKA_CREATE_TOPICS: "input:<number of cores>:1"` so Kafka can partition the data properly for the number of consumers.