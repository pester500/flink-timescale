#!/usr/bin/env bash
./gradlew clean build

mv build/libs/flink-timescale-all.jar build/libs/job.jar
docker-compose build flink-timescale
