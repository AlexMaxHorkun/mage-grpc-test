<?php
# Generated by the Magento PHP proto generator.  DO NOT EDIT!

/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

declare(strict_types=1);

namespace Magento\GrpcTestApi\Api\Data;

/**
 * Autogenerated description for Option class
 *
 * phpcs:disable Magento2.PHP.FinalImplementation
 * @SuppressWarnings(PHPMD)
 * @SuppressWarnings(PHPCPD)
 */
final class Option implements OptionInterface
{

    /**
     * @var string
     */
    private $id;

    /**
     * @var string
     */
    private $title;

    /**
     * @var float
     */
    private $price;

    /**
     * @var bool
     */
    private $available;
    
    /**
     * @inheritdoc
     *
     * @return string
     */
    public function getId(): string
    {
        return (string) $this->id;
    }
    
    /**
     * @inheritdoc
     *
     * @param string $value
     * @return void
     */
    public function setId(string $value): void
    {
        $this->id = $value;
    }
    
    /**
     * @inheritdoc
     *
     * @return string
     */
    public function getTitle(): string
    {
        return (string) $this->title;
    }
    
    /**
     * @inheritdoc
     *
     * @param string $value
     * @return void
     */
    public function setTitle(string $value): void
    {
        $this->title = $value;
    }
    
    /**
     * @inheritdoc
     *
     * @return float
     */
    public function getPrice(): float
    {
        return (float) $this->price;
    }
    
    /**
     * @inheritdoc
     *
     * @param float $value
     * @return void
     */
    public function setPrice(float $value): void
    {
        $this->price = $value;
    }
    
    /**
     * @inheritdoc
     *
     * @return bool
     */
    public function getAvailable(): bool
    {
        return (bool) $this->available;
    }
    
    /**
     * @inheritdoc
     *
     * @param bool $value
     * @return void
     */
    public function setAvailable(bool $value): void
    {
        $this->available = $value;
    }
}
