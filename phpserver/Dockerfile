FROM ubuntu:20.04
COPY install.sh /etc/magento/install.sh
COPY exec.sh /etc/magento/
COPY go.tar.gz /etc/magento/go.tar.gz
COPY xdebug.ini /etc/magento/xdebug.ini
COPY src /var/www/magento
EXPOSE 50051 22
USER root
RUN /etc/magento/install.sh
ENV PATH="${PATH}:/usr/local/go/bin"
CMD /etc/magento/exec.sh