#!/usr/bin/env bash

service ssh start
cd /var/www/magento2ce
./vendor/bin/grpc-server