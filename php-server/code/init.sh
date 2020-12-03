#!/usr/bin/env bash

service ssh restart

echo "Initialized PHP server"
while :; do :; done & kill -STOP $! && wait $!
