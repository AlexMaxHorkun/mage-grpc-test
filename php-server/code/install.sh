#!/usr/bin/env bash

env TERM=linux
env DEBIAN_FRONTEND=noninteractive
apt update
apt install -yqf apt-utils
apt install -yqf tzdata
ln -fs /usr/share/zoneinfo/America/Chicago /etc/localtime &&\
echo "America/Chicago" > /etc/timezone &&\
dpkg-reconfigure --frontend noninteractive tzdata
##Installation
apt install -yqf sudo\
    openssh-server\
    curl\
    nano\
    software-properties-common\
    unzip
echo 'root:12345abc' | chpasswd
sed -i 's/\#PasswordAuthentication yes/PasswordAuthentication yes/' /etc/ssh/sshd_config
sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config
sed -i 's/PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config
sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd
echo "export VISIBLE=now" >> /etc/profile
service ssh stop

apt purge php7.2* -y
apt purge php7.3* -y
apt purge php7.1* -y
apt purge php7.4* -y
apt purge php8.0* -y
apt autoremove -y
add-apt-repository ppa:ondrej/php -y
apt install -yqf\
    php7.4\
    php7.4-fpm\
    php7.4-json\
    php7.4-soap\
    php7.4-xml\
    php7.4-bcmath\
    php7.4-xdebug\
    php7.4-curl\
    php7.4-zip\
    php7.4-mbstring\
    php7.4-gd\
    php7.4-intl\
    php7.4-dev\
    php7.4-pgsql
sed -i 's/memory_limit = 128M/memory_limit = 4096M/' /etc/php/7.4/fpm/php.ini
sed -i 's/max_execution_time = 30/max_execution_time = 18000/' /etc/php/7.4/fpm/php.ini
sed -i 's/zlib.output_compression = Off/zlib.output_compression = on/' /etc/php/7.4/fpm/php.ini
sed -i 's/max_execution_time = 30/max_execution_time = 18000/' /etc/php/7.4/cli/php.ini
cp /etc/magento/xdebug.ini /etc/php/7.4/mods-available/
echo 'PHP_IDE_CONFIG="serverName=grpc.magento.ua"' >> /etc/environment
php -r "copy('https://getcomposer.org/composer-1.phar', 'composer.phar');" &&\
chmod +x composer.phar
sudo mv composer.phar /usr/local/bin/composer
pecl install protobuf
echo "extension=protobuf.so" >> /etc/php/7.4/fpm/php.ini
echo "extension=protobuf.so" >> /etc/php/7.4/cli/php.ini
