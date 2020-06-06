#!/usr/bin/env bash
./gradlew clean build

mv crimes/build/libs/crimes-all.jar crimes/build/libs/job.jar
docker build -t flink-timescale:latest .
