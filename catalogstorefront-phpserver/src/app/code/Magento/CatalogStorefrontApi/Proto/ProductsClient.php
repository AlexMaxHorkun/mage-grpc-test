<?php
// GENERATED CODE -- DO NOT EDIT!

namespace Magento\CatalogStorefrontApi\Proto;

/**
 * Services
 */
class ProductsClient extends \Grpc\BaseStub
{

    /**
     * @param string $hostname hostname
     * @param array $opts channel options
     * @param \Grpc\Channel $channel (optional) re-use channel object
     */
    public function __construct($hostname, $opts, $channel = null)
    {
        parent::__construct($hostname, $opts, $channel);
    }

    /**
     * @param \Magento\CatalogStorefrontApi\Proto\GenerateArg $argument input argument
     * @param array $metadata metadata
     * @param array $options call options
     * @return \Magento\CatalogStorefrontApi\Proto\Product
     */
    public function generate(
        \Magento\CatalogStorefrontApi\Proto\GenerateArg $argument,
        $metadata = [],
        $options = []
    )
    {
        return $this->_serverStreamRequest(
            '/magento.catalogStorefrontApi.proto.Products/generate',
            $argument,
            ['\Magento\CatalogStorefrontApi\Proto\Product', 'decode'],
            $metadata,
            $options
        );
    }

    /**
     * @param \Magento\CatalogStorefrontApi\Proto\ClearArg $argument input argument
     * @param array $metadata metadata
     * @param array $options call options
     * @return \Magento\CatalogStorefrontApi\Proto\Cleared
     */
    public function clear(
        \Magento\CatalogStorefrontApi\Proto\ClearArg $argument,
        $metadata = [],
        $options = []
    )
    {
        return $this->_simpleRequest(
            '/magento.catalogStorefrontApi.proto.Products/clear',
            $argument,
            ['\Magento\CatalogStorefrontApi\Proto\Cleared', 'decode'],
            $metadata,
            $options
        );
    }
}
