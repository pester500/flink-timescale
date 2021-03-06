# Running Locally
* Add an entry for Kafka to your `/etc/hosts`: `127.0.0.1 kafka`
* Download Kafka and extract archive: https://kafka.apache.org/downloads
* `./scripts/build.sh && ./scripts/launch-dependencies.sh`
* Sample data is in `data/` folder, extract the archive and post it to the Kafka topic: `input`. Or download as CSV from: https://data.cityofchicago.org/Public-Safety/Crimes-2001-to-present/ijzp-q8t2
* Ex: `cat /<path_to_repo>/data/crimes.csv | /<path_to_kafka_dir>/bin/kafka-console-producer.sh --topic input --broker-list localhost:9092`

# Scripts
* `scripts/build.sh` - builds Flink job jar and Docker image
* `scripts/launch-dependencies.sh` - Start Zookeeper, TimescaleDB, Kafka, Job Manager, Task Manager, and Flink job
* `scripts/kill-containers.sh` - Stop and remove all docker containers


# Performance considerations
The Flink Task Manager instance count defaults to the number of cores available to the Docker engine.
If you want to see how much parallelism impacts processing speed, change `TASK_MANAGER_NUMBER_OF_TASK_SLOTS` in the task-manager container,

AND

Change `KAFKA_CREATE_TOPICS: "input:10:1"` to `KAFKA_CREATE_TOPICS: "input:<TASK_MANAGER_NUMBER_OF_TASK_SLOTS>:1"` so Kafka can partition the data properly for the number of consumers.

AND

Change `PARALLELISM: 10` to `PARALLELISM:<TASK_MANAGER_NUMBER_OF_TASK_SLOTS>`. If `PARALLELISM` exceeds `TASK_MANAGER_NUMBER_OF_TASK_SLOTS`, the job will be rejected by the Job Manager.
