#!/usr/bin/env bash

# Create elastic index

curl -XPUT "http://localhost:9200/markets/" -d '{
   "settings" : { "keyspace" : "markets" } },
}'