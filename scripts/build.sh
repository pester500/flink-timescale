#!/usr/bin/env bash
./gradlew clean build

mv build/libs/flink-timescale*.jar build/libs/job.jar
docker build -t flink-timescale:latest .
