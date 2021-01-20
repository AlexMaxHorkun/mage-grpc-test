#!/usr/bin/env bash

cd /etc/magento
env TERM=linux
env DEBIAN_FRONTEND=noninteractive
apt update
apt install -yqf apt-utils
apt install -yqf tzdata
ln -fs /usr/share/zoneinfo/America/Chicago /etc/localtime &&\
echo "America/Chicago" > /etc/timezone &&\
dpkg-reconfigure --frontend noninteractive tzdata
apt install -yqf sudo\
    openssh-server\
    curl\
    nano\
    software-properties-common\
    unzip\
    git\
    wget
echo 'root:12345abc' | chpasswd
sed -i 's/\#PasswordAuthentication yes/PasswordAuthentication yes/' /etc/ssh/sshd_config
sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config
sed -i 's/PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config
sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd
echo "export VISIBLE=now" >> /etc/profile
service ssh stop
#export GOPATH="/var/www/magento"
#export GOROOT="/usr/local/go"
export PATH=$PATH:/usr/local/go/bin
wget https://golang.org/dl/go1.15.6.linux-amd64.tar.gz
tar -C /usr/local -xzf go1.15.6.linux-amd64.tar.gz
go version
apt purge php7.2* -y
apt purge php7.3* -y
apt purge php7.1* -y
apt purge php7.4* -y
apt autoremove -y
add-apt-repository ppa:ondrej/php -y
apt install -yqf\
    php7.4\
    php7.4-fpm\
    php7.4-pgsql\
    php7.4-redis\
    php7.4-json\
    php7.4-soap\
    php7.4-xml\
    php7.4-bcmath\
    php7.4-curl\
    php7.4-zip\
    php7.4-mbstring\
    php7.4-gd\
    php7.4-intl\
    php7.4-dev
sed -i 's/memory_limit = 128M/memory_limit = 256M/' /etc/php/7.4/fpm/php.ini
sed -i 's/max_execution_time = 30/max_execution_time = 0/' /etc/php/7.4/fpm/php.ini
sed -i 's/zlib.output_compression = Off/zlib.output_compression = on/' /etc/php/7.4/fpm/php.ini
sed -i 's/max_execution_time = 30/max_execution_time = 0/' /etc/php/7.4/cli/php.ini
#cp /etc/magento/xdebug.ini /etc/php/7.4/mods-available/
echo 'PHP_IDE_CONFIG="serverName=grpc.mage.ua"' >> /etc/environment
php -r "copy('https://getcomposer.org/composer-1.phar', 'composer.phar');" &&\
chmod +x composer.phar
mv composer.phar /usr/local/bin/composer
apt install -yqf redis-tools
#Installing PHP app
cd /var/www/magento
rm go.sum
go mod tidy
go mod vendor
composer install

apt install libprotobuf-dev protobuf-compiler golang-goprotobuf-dev -yfq
#protoc -I proto/ proto/magegrpc.proto --go_out=plugins=grpc:magegrpc