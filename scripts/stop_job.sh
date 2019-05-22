#!/bin/bash
JOB_NAME=$1

[ -z "${JOB_NAME}" ] && echo "error: unknown JOB_NAME" && exit 1

echo "Job Name: $JOB_NAME"

echo "Listing jobs"
JOB_IDS=($(flink list | grep -i ${JOB_NAME} | grep 'RUNNING\|RESTARTING' |  awk '{print $4}'))
if [ -z $JOB_IDS ]; then
    echo "No active jobs found"
    exit 1
fi

echo "Active Jobs: $JOB_IDS"
for JOB_ID in ${JOB_IDS[@]}; do
     echo "Starting to Cancel Job ${JOB_ID}"
     flink cancel $JOB_ID
done
