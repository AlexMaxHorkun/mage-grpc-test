<?php
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: catalog.proto

namespace Magento\CatalogStorefrontApi\Proto;

use Google\Protobuf\Internal\GPBType;
use Google\Protobuf\Internal\RepeatedField;
use Google\Protobuf\Internal\GPBUtil;

/**
 * Generated from protobuf message <code>magento.catalogStorefrontApi.proto.OptionSelectionRequest</code>
 */
class OptionSelectionRequest extends \Google\Protobuf\Internal\Message
{
    /**
     * Generated from protobuf field <code>string store = 1;</code>
     */
    protected $store = '';
    /**
     * array of option_values with the following format parent_id:option_id/optionValue.uid
     * &#64;see ProductValue.option_values
     *
     * Generated from protobuf field <code>repeated string values = 2;</code>
     */
    private $values;

    /**
     * Constructor.
     *
     * @param array $data {
     *     Optional. Data for populating the Message object.
     *
     *     @type string $store
     *     @type string[]|\Google\Protobuf\Internal\RepeatedField $values
     *           array of option_values with the following format parent_id:option_id/optionValue.uid
     *           &#64;see ProductValue.option_values
     * }
     */
    public function __construct($data = null)
    {
        \Magento\CatalogStorefrontApi\Metadata\Catalog::initOnce();
        parent::__construct($data);
    }

    /**
     * Generated from protobuf field <code>string store = 1;</code>
     * @return string
     */
    public function getStore()
    {
        return $this->store;
    }

    /**
     * Generated from protobuf field <code>string store = 1;</code>
     * @param string $var
     * @return $this
     */
    public function setStore($var)
    {
        GPBUtil::checkString($var, true);
        $this->store = $var;

        return $this;
    }

    /**
     * array of option_values with the following format parent_id:option_id/optionValue.uid
     * &#64;see ProductValue.option_values
     *
     * Generated from protobuf field <code>repeated string values = 2;</code>
     * @return \Google\Protobuf\Internal\RepeatedField
     */
    public function getValues()
    {
        return $this->values;
    }

    /**
     * array of option_values with the following format parent_id:option_id/optionValue.uid
     * &#64;see ProductValue.option_values
     *
     * Generated from protobuf field <code>repeated string values = 2;</code>
     * @param string[]|\Google\Protobuf\Internal\RepeatedField $var
     * @return $this
     */
    public function setValues($var)
    {
        $arr = GPBUtil::checkRepeatedField($var, \Google\Protobuf\Internal\GPBType::STRING);
        $this->values = $arr;

        return $this;
    }
}
