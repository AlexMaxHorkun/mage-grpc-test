<?php
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: catalog.proto

namespace Magento\CatalogStorefrontApi\Proto;

use Google\Protobuf\Internal\GPBType;
use Google\Protobuf\Internal\RepeatedField;
use Google\Protobuf\Internal\GPBUtil;

/**
 * Generated from protobuf message <code>magento.catalogStorefrontApi.proto.ProductOption</code>
 */
class ProductOption extends \Google\Protobuf\Internal\Message
{
    /**
     * Generated from protobuf field <code>string id = 1;</code>
     */
    protected $id = '';
    /**
     * Generated from protobuf field <code>string label = 2;</code>
     */
    protected $label = '';
    /**
     * Generated from protobuf field <code>int32 sort_order = 3;</code>
     */
    protected $sort_order = 0;
    /**
     * Generated from protobuf field <code>bool required = 4;</code>
     */
    protected $required = false;
    /**
     * Generated from protobuf field <code>string render_type = 5;</code>
     */
    protected $render_type = '';
    /**
     * Generated from protobuf field <code>string type = 6;</code>
     */
    protected $type = '';
    /**
     * Generated from protobuf field <code>repeated .magento.catalogStorefrontApi.proto.ProductOptionValue values = 7;</code>
     */
    private $values;

    /**
     * Constructor.
     *
     * @param array $data {
     *     Optional. Data for populating the Message object.
     *
     *     @type string $id
     *     @type string $label
     *     @type int $sort_order
     *     @type bool $required
     *     @type string $render_type
     *     @type string $type
     *     @type \Magento\CatalogStorefrontApi\Proto\ProductOptionValue[]|\Google\Protobuf\Internal\RepeatedField $values
     * }
     */
    public function __construct($data = null)
    {
        \Magento\CatalogStorefrontApi\Metadata\Catalog::initOnce();
        parent::__construct($data);
    }

    /**
     * Generated from protobuf field <code>string id = 1;</code>
     * @return string
     */
    public function getId()
    {
        return $this->id;
    }

    /**
     * Generated from protobuf field <code>string id = 1;</code>
     * @param string $var
     * @return $this
     */
    public function setId($var)
    {
        GPBUtil::checkString($var, true);
        $this->id = $var;

        return $this;
    }

    /**
     * Generated from protobuf field <code>string label = 2;</code>
     * @return string
     */
    public function getLabel()
    {
        return $this->label;
    }

    /**
     * Generated from protobuf field <code>string label = 2;</code>
     * @param string $var
     * @return $this
     */
    public function setLabel($var)
    {
        GPBUtil::checkString($var, true);
        $this->label = $var;

        return $this;
    }

    /**
     * Generated from protobuf field <code>int32 sort_order = 3;</code>
     * @return int
     */
    public function getSortOrder()
    {
        return $this->sort_order;
    }

    /**
     * Generated from protobuf field <code>int32 sort_order = 3;</code>
     * @param int $var
     * @return $this
     */
    public function setSortOrder($var)
    {
        GPBUtil::checkInt32($var);
        $this->sort_order = $var;

        return $this;
    }

    /**
     * Generated from protobuf field <code>bool required = 4;</code>
     * @return bool
     */
    public function getRequired()
    {
        return $this->required;
    }

    /**
     * Generated from protobuf field <code>bool required = 4;</code>
     * @param bool $var
     * @return $this
     */
    public function setRequired($var)
    {
        GPBUtil::checkBool($var);
        $this->required = $var;

        return $this;
    }

    /**
     * Generated from protobuf field <code>string render_type = 5;</code>
     * @return string
     */
    public function getRenderType()
    {
        return $this->render_type;
    }

    /**
     * Generated from protobuf field <code>string render_type = 5;</code>
     * @param string $var
     * @return $this
     */
    public function setRenderType($var)
    {
        GPBUtil::checkString($var, true);
        $this->render_type = $var;

        return $this;
    }

    /**
     * Generated from protobuf field <code>string type = 6;</code>
     * @return string
     */
    public function getType()
    {
        return $this->type;
    }

    /**
     * Generated from protobuf field <code>string type = 6;</code>
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
     * Generated from protobuf field <code>repeated .magento.catalogStorefrontApi.proto.ProductOptionValue values = 7;</code>
     * @return \Google\Protobuf\Internal\RepeatedField
     */
    public function getValues()
    {
        return $this->values;
    }

    /**
     * Generated from protobuf field <code>repeated .magento.catalogStorefrontApi.proto.ProductOptionValue values = 7;</code>
     * @param \Magento\CatalogStorefrontApi\Proto\ProductOptionValue[]|\Google\Protobuf\Internal\RepeatedField $var
     * @return $this
     */
    public function setValues($var)
    {
        $arr = GPBUtil::checkRepeatedField($var, \Google\Protobuf\Internal\GPBType::MESSAGE, \Magento\CatalogStorefrontApi\Proto\ProductOptionValue::class);
        $this->values = $arr;

        return $this;
    }
}
