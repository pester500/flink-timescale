#!/usr/bin/env bash
./gradlew clean build

mv crimes/build/libs/crimes-all.jar crimes/build/libs/crimes.jar
mv logs/build/libs/logs-all.jar logs/build/libs/logs.jar
docker build -t flink-timescale:latest .
