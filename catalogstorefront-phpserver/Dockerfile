FROM rkostiv/php-with-rr-grpc
COPY src /var/www/magento2ce
EXPOSE 9000 22
COPY exec.sh /etc/magento/
USER root

CMD ["php-fpm", "-R"]

CMD /etc/magento/exec.sh
