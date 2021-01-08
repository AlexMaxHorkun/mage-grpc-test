<?php
// GENERATED CODE -- DO NOT EDIT!

namespace Magento\GrpcTestApi\Proto;

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
     * @param \Magento\GrpcTestApi\Proto\GenerateArg $argument input argument
     * @param array $metadata metadata
     * @param array $options call options
     * @return \Magento\GrpcTestApi\Proto\Product
     */
    public function generate(
        \Magento\GrpcTestApi\Proto\GenerateArg $argument,
        $metadata = [],
        $options = []
    )
    {
        return $this->_serverStreamRequest(
            '/magento.grpcTestApi.proto.Products/generate',
            $argument,
            ['\Magento\GrpcTestApi\Proto\Product', 'decode'],
            $metadata,
            $options
        );
    }

    /**
     * @param \Magento\GrpcTestApi\Proto\ReadRequest $argument input argument
     * @param array $metadata metadata
     * @param array $options call options
     * @return \Magento\GrpcTestApi\Proto\ReadResponse
     */
    public function read(
        \Magento\GrpcTestApi\Proto\ReadRequest $argument,
        $metadata = [],
        $options = []
    )
    {
        return $this->_simpleRequest(
            '/magento.grpcTestApi.proto.Products/read',
            $argument,
            ['\Magento\GrpcTestApi\Proto\ReadResponse', 'decode'],
            $metadata,
            $options
        );
    }

    /**
     * @param \Magento\GrpcTestApi\Proto\ClearArg $argument input argument
     * @param array $metadata metadata
     * @param array $options call options
     * @return \Magento\GrpcTestApi\Proto\Cleared
     */
    public function clear(
        \Magento\GrpcTestApi\Proto\ClearArg $argument,
        $metadata = [],
        $options = []
    )
    {
        return $this->_simpleRequest(
            '/magento.grpcTestApi.proto.Products/clear',
            $argument,
            ['\Magento\GrpcTestApi\Proto\Cleared', 'decode'],
            $metadata,
            $options
        );
    }
}
