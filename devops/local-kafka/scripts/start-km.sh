#!/bin/sh

rm -f $KM_HOME/RUNNING_PID

# Run Kafka Manager
$KM_HOME/bin/kafka-manager -Dconfig.file=$KM_HOME/conf/application.conf

