# install

To build image from source file:

1. clone project `git clone https://github.com/magento/catalog-storefront src`

2. install dependencies
* Add Magento authentication keys to access the Magento Commerce repository: 
** with auth.json: copy the contents of `auth.json.dist` to new `auth.json` file and replace placeholders with your credentials  
* Run `composer install`
* Run `php dev/tools/install-dependencies.php`
* Run `bin/command storefront:grpc:init \\Magento\\GrpcTestApi\\Api\\ProductsProxyServer`

3. Build && run image
