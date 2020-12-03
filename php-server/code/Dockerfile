FROM ubuntu:20.04
COPY php /var/www/mage/php
COPY install.sh /etc/magento/
COPY init.sh /etc/magento/
COPY xdebug.ini /etc/magento/
EXPOSE 9000 22
USER root
RUN /etc/magento/install.sh
CMD /etc/magento/init.sh
