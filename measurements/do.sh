#!/usr/bin/env bash

mkdir results
mkdir results/reqs${1}_conns${3}_items${2}
for (( i=1; i <= $1; i++))
do
  sleep 0.001 && curl -X GET "http://${4}/invoke-read?number=$2&connections=$3" -o "results/reqs${1}_conns${3}_items${2}/result_${i}.json" & disown;
done
