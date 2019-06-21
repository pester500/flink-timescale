#!/bin/bash

###############################################################################
#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
# limitations under the License.
###############################################################################

# If unspecified, the hostname of the container is taken as the JobManager address
JOB_MANAGER_RPC_ADDRESS=${JOB_MANAGER_RPC_ADDRESS:-$(hostname -f)}
#if unspecified, default command is help
COMMAND=${command}

# print all environment variables for debugging
printenv

# Akka config
AKKA_ASK_TIMEOUT=${AKKA_ASK_TIMEOUT:-60 s}
AKKA_TCP_TIMEOUT=${AKKA_TCP_TIMEOUT:-60 s}
AKKA_FRAMESIZE=${AKKA_FRAMESIZE:-208857600b}
AKKA_WATCH_HEARTBEAT_INTERVAL=${AKKA_WATCH_HEARTBEAT_INTERVAL:-5s}
AKKA_WATCH_HEARTBEAT_PAUSE=${AKKA_WATCH_HEARTBEAT_PAUSE:-120s}

# Max jar size
REST_MAX_CONTENT_LENGTH=${REST_MAX_CONTENT_LENGTH:-208857600}

# Job Manager Information
JOB_MANAGER_REST_PORT=${JOB_MANAGER_REST_PORT}
JOB_MANAGER_RPC_PORT=${JOB_MANAGER_RPC_PORT}
JOB_MANAGER_BLOB_PORT=${JOB_MANAGER_BLOB_PORT}
JOB_MANAGER_QUERY_PORT=${JOB_MANAGER_QUERY_PORT}
FLINK_SCRUTINY_REPORTING_INTERVAL=${FLINK_SCRUTINY_REPORTING_INTERVAL:-5 MINUTES}
STATE_BACKEND_ASYNC=${STATE_BACKEND_ASYNC:-false}
WEB_CHECKPOINTS_HISTORY=${WEB_CHECKPOINTS_HISTORY:100}
JOBSTORE_EXPIRATION_TIME=${JOBSTORE_EXPIRATION_TIME:604800}

export FLINK_CONFIG_FILE="$FLINK_HOME/conf/flink-conf.yaml"

echo "akka.framesize: $AKKA_FRAMESIZE" >> "$FLINK_CONFIG_FILE"
echo "akka.ask.timeout: $AKKA_ASK_TIMEOUT" >> "$FLINK_CONFIG_FILE"
echo "akka.tcp.timeout: $AKKA_TCP_TIMEOUT" >> "$FLINK_CONFIG_FILE"
echo "akka.watch.heartbeat.interval: $AKKA_WATCH_HEARTBEAT_INTERVAL" >> "$FLINK_CONFIG_FILE"
echo "akka.watch.heartbeat.pause: $AKKA_WATCH_HEARTBEAT_PAUSE" >> "$FLINK_CONFIG_FILE"

echo "rest.client.max-content-length: $REST_MAX_CONTENT_LENGTH" >> "$FLINK_CONFIG_FILE"
echo "rest.server.max-content-length: $REST_MAX_CONTENT_LENGTH" >> "$FLINK_CONFIG_FILE"


echo "blob.server.port: $JOB_MANAGER_BLOB_PORT" >> "$FLINK_CONFIG_FILE"
echo "query.server.port: $JOB_MANAGER_QUERY_PORT" >> "$FLINK_CONFIG_FILE"
sed -i -e "s/jobmanager.rpc.address: localhost/jobmanager.rpc.address: ${JOB_MANAGER_RPC_ADDRESS}/g" "$FLINK_CONFIG_FILE"
sed -i -e "s/jobmanager.rpc.port: 6123/jobmanager.rpc.port: $JOB_MANAGER_RPC_PORT/g" "$FLINK_CONFIG_FILE"
sed -i -e "s/rest.port: 8081/rest.port: $JOB_MANAGER_REST_PORT/g" "$FLINK_CONFIG_FILE"
echo "rest.address: $JOB_MANAGER_RPC_ADDRESS" >> "$FLINK_CONFIG_FILE"


if [ "$COMMAND" = "help" ]; then
    echo "Usage: $(basename "$0") (jobmanager|taskmanager|startJob|local|help)"
    exit 0
elif [ "$COMMAND" = "jobmanager" ]; then
    JOB_MANAGER_HEAP_SIZE=${JOB_MANAGER_HEAP_SIZE:-1024m}

    sed -i -e "s/jobmanager.heap.size: 1024m/jobmanager.heap.size: $JOB_MANAGER_HEAP_SIZE/g" "$FLINK_CONFIG_FILE"
    echo "web.checkpoints.history: $WEB_CHECKPOINTS_HISTORY" >> "$FLINK_CONFIG_FILE"
    echo "jobstore.expiration-time: $JOBSTORE_EXPIRATION_TIME" >> "$FLINK_CONFIG_FILE"

    echo "Starting Job Manager"
    echo "config file: " && grep '^[^\n#]' "$FLINK_CONFIG_FILE"
    gosu flink "$FLINK_HOME/bin/jobmanager.sh" start-foreground $JOB_MANAGER_RPC_ADDRESS

elif [ "$COMMAND" = "taskmanager" ]; then
    TASK_MANAGER_HEAP_SIZE=${TASK_MANAGER_HEAP_SIZE:-1024m}
    TASK_MANAGER_NUMBER_OF_TASK_SLOTS=${TASK_MANAGER_NUMBER_OF_TASK_SLOTS:-$(grep -c ^processor /proc/cpuinfo)}
    TASK_MANAGER_NUMBER_OF_TASK_MANAGERS=${TASK_MANAGER_NUMBER_OF_TASK_MANAGERS:-1}


    sed -i -e "s/taskmanager.numberOfTaskSlots: 1/taskmanager.numberOfTaskSlots: $TASK_MANAGER_NUMBER_OF_TASK_SLOTS/g" "$FLINK_CONFIG_FILE"
    sed -i -e "s/taskmanager.heap.size: 1024m/taskmanager.heap.size: $TASK_MANAGER_HEAP_SIZE/g" "$FLINK_CONFIG_FILE"
    echo "taskmanager.exit-on-fatal-akka-error: true" >> "$FLINK_CONFIG_FILE"
    echo "taskmanager.jvm-exit-on-oom: true" >> "$FLINK_CONFIG_FILE"

    echo "Starting Task Manager"
    echo "config file: " && grep '^[^\n#]' "$FLINK_CONFIG_FILE"
    echo "log file configs"
    cat $LOG_CONFIG_FILE

    export HOSTNAME=$HOST # set hostname of task manager to be the HOST in mesos
    gosu flink "$FLINK_HOME/bin/taskmanager.sh" start-foreground
elif [ "$COMMAND" = "local" ]; then
    echo "Starting local cluster"
    exec $(drop_privs_cmd) flink "$FLINK_HOME/bin/jobmanager.sh" start-foreground local
elif [ "$COMMAND" = "startJob" ]; then
    echo "Stopping Existing Job If currently running"
    /stop_job.sh ${JOB_NAME}
    echo "Stopped Existing Job"
    echo "Starting new job"
    flink run -p ${PARALLELISM} -d -c ${MAIN_CLASS} ${JAR_FILE_NAME}
    sleep 31536000
fi

exec "$@"
