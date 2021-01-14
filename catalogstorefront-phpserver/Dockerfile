FROM rkostiv/php-with-rr-grpc
COPY src /var/www/magento2ce
EXPOSE 9000 22
COPY exec.sh /etc/magento/
COPY install.sh /etc/magento/
USER root
WORKDIR /var/www/magento2ce
RUN /etc/magento/install.sh

CMD ["php-fpm", "-R"]
CMD /etc/magento/exec.sh
ENTRYPOINT ["./vendor/bin/grpc-server"]
