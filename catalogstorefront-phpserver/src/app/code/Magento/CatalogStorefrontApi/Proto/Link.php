<?php
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: catalog.proto

namespace Magento\CatalogStorefrontApi\Proto;

use Google\Protobuf\Internal\GPBType;
use Google\Protobuf\Internal\RepeatedField;
use Google\Protobuf\Internal\GPBUtil;

/**
 * Generated from protobuf message <code>magento.catalogStorefrontApi.proto.Link</code>
 */
class Link extends \Google\Protobuf\Internal\Message
{
    /**
     * Generated from protobuf field <code>string product_id = 1;</code>
     */
    protected $product_id = '';
    /**
     * Generated from protobuf field <code>int32 position = 2;</code>
     */
    protected $position = 0;
    /**
     * Generated from protobuf field <code>string type = 3;</code>
     */
    protected $type = '';
    /**
     * Generated from protobuf field <code>float qty = 4;</code>
     */
    protected $qty = 0.0;

    /**
     * Constructor.
     *
     * @param array $data {
     *     Optional. Data for populating the Message object.
     *
     *     @type string $product_id
     *     @type int $position
     *     @type string $type
     *     @type float $qty
     * }
     */
    public function __construct($data = null)
    {
        \Magento\CatalogStorefrontApi\Metadata\Catalog::initOnce();
        parent::__construct($data);
    }

    /**
     * Generated from protobuf field <code>string product_id = 1;</code>
     * @return string
     */
    public function getProductId()
    {
        return $this->product_id;
    }

    /**
     * Generated from protobuf field <code>string product_id = 1;</code>
     * @param string $var
     * @return $this
     */
    public function setProductId($var)
    {
        GPBUtil::checkString($var, true);
        $this->product_id = $var;

        return $this;
    }

    /**
     * Generated from protobuf field <code>int32 position = 2;</code>
     * @return int
     */
    public function getPosition()
    {
        return $this->position;
    }

    /**
     * Generated from protobuf field <code>int32 position = 2;</code>
     * @param int $var
     * @return $this
     */
    public function setPosition($var)
    {
        GPBUtil::checkInt32($var);
        $this->position = $var;

        return $this;
    }

    /**
     * Generated from protobuf field <code>string type = 3;</code>
     * @return string
     */
    public function getType()
    {
        return $this->type;
    }

    /**
     * Generated from protobuf field <code>string type = 3;</code>
     * @param string $var
     * @return $this
     */
    public function setType($var)
    {
        GPBUtil::checkString($var, true);
        $this->type = $var;

        return $this;
    }

    /**
     * Generated from protobuf field <code>float qty = 4;</code>
     * @return float
     */
    public function getQty()
    {
        return $this->qty;
    }

    /**
     * Generated from protobuf field <code>float qty = 4;</code>
     * @param float $var
     * @return $this
     */
    public function setQty($var)
    {
        GPBUtil::checkFloat($var);
        $this->qty = $var;

        return $this;
    }
}
