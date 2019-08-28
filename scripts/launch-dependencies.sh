#!/usr/bin/env bash


function startDocker {
  printf "\nStarting $1...\n"
  docker-compose stop $1
  docker-compose kill $1
  docker-compose rm -f $1
  docker-compose -f docker-compose.yml up -d $1
  echo "done."
}

rm -rf /tmp/flink/
mkdir -p /tmp/flink

startDocker "zookeeper"
startDocker "timescaledb"
startDocker "kafka"
startDocker "jobmanager"
startDocker "taskmanager"
startDocker "flink-timescale"
