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
