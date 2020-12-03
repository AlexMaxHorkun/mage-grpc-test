#!/usr/bin/env bash

service ssh start
redis-server &>/dev/null & disown
cd /var/www/magento
php app.php configure -vv
/usr/local/go/bin/go run main.go serve -d -v
