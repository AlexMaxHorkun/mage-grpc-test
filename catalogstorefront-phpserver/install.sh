#!/usr/bin/env bash

composer install
rm -f ./auth.json
# php dev/tools/install-dependencies.php
bin/command storefront:grpc:init \\Magento\\GrpcTestApi\\Api\\ProductsProxyServer
