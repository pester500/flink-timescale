#!/bin/bash

# If unspecified, the hostname of the container is taken as the JobManager address
JOB_MANAGER_RPC_ADDRESS=${JOB_MANAGER_RPC_ADDRESS:-$(hostname -f)}
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
STATE_BACKEND_ASYNC=${STATE_BACKEND_ASYNC:-false}
WEB_CHECKPOINTS_HISTORY=${WEB_CHECKPOINTS_HISTORY:100}
JOBSTORE_EXPIRATION_TIME=${JOBSTORE_EXPIRATION_TIME:604800}

export FLINK_CONFIG_FILE="$FLINK_HOME/conf/flink-conf.yaml"

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

    #Any Job Manager config changes should be made here, following `sed` examples below
    #config reference: https://ci.apache.org/projects/flink/flink-docs-stable/ops/config.html#jobmanager
    JOB_MANAGER_HEAP_SIZE=${JOB_MANAGER_HEAP_SIZE:-1024m}

    sed -i -e "s/jobmanager.heap.size: 1024m/jobmanager.heap.size: $JOB_MANAGER_HEAP_SIZE/g" "$FLINK_CONFIG_FILE"

    echo "Starting Job Manager"
    echo "config file: " && grep '^[^\n#]' "$FLINK_CONFIG_FILE"
    gosu flink "$FLINK_HOME/bin/jobmanager.sh" start-foreground $JOB_MANAGER_RPC_ADDRESS

elif [ "$COMMAND" = "taskmanager" ]; then

    #Any Task Manager should be made here, following same `sed` syntax
    #config reference: https://ci.apache.org/projects/flink/flink-docs-stable/ops/config.html#taskmanager
    TASK_MANAGER_PROCESS_SIZE=${TASK_MANAGER_PROCESS_SIZE:-1024m}
    TASK_MANAGER_NUMBER_OF_TASK_SLOTS=${TASK_MANAGER_NUMBER_OF_TASK_SLOTS:-$(grep -c ^processor /proc/cpuinfo)}
    TASK_MANAGER_NUMBER_OF_TASK_MANAGERS=${TASK_MANAGER_NUMBER_OF_TASK_MANAGERS:-1}

    sed -i -e "s/taskmanager.numberOfTaskSlots: 1/taskmanager.numberOfTaskSlots: $TASK_MANAGER_NUMBER_OF_TASK_SLOTS/g" "$FLINK_CONFIG_FILE"
    sed -i -e "s/taskmanager.memory.process.size: 1728m/taskmanager.memory.process.size: $TASK_MANAGER_PROCESS_SIZE/g" "$FLINK_CONFIG_FILE"
    echo "taskmanager.exit-on-fatal-akka-error: true" >> "$FLINK_CONFIG_FILE"
    echo "taskmanager.jvm-exit-on-oom: true" >> "$FLINK_CONFIG_FILE"

    echo "Starting Task Manager"
    echo "config file: " && grep '^[^\n#]' "$FLINK_CONFIG_FILE"
    echo "log file configs"
    cat $LOG_CONFIG_FILE

    gosu flink "$FLINK_HOME/bin/taskmanager.sh" start-foreground
elif [ "$COMMAND" = "local" ]; then
    echo "Starting local cluster"
    exec $(drop_privs_cmd) flink "$FLINK_HOME/bin/jobmanager.sh" start-foreground local
elif [ "$COMMAND" = "startJob" ]; then
    echo "Stopping Existing Job If currently running"
    /stop_job.sh ${JOB_NAME}
    echo "Stopped Existing Job"
    echo "Starting crimes job"
    flink run -p ${CRIMES_PARALLELISM} -d -c ${CRIMES_MAIN_CLASS} ${CRIMES_JAR_FILE_NAME}
    echo "Starting logs job"
    flink run -p ${LOGS_PARALLELISM} -d -c ${LOGS_MAIN_CLASS} ${LOGS_JAR_FILE_NAME}
    sleep 31536000
fi

exec "$@"