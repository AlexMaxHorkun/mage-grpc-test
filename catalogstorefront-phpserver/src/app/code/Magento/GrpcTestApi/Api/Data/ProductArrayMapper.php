<?php
# Generated by the Magento PHP proto generator.  DO NOT EDIT!

/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

declare(strict_types=1);

namespace Magento\GrpcTestApi\Api\Data;

use Magento\Framework\ObjectManagerInterface;

/**
 * Autogenerated description for Product class
 *
 * @SuppressWarnings(PHPMD.CouplingBetweenObjects)
 * @SuppressWarnings(PHPMD.CyclomaticComplexity)
 * @SuppressWarnings(PHPMD.UnusedPrivateField)
 * @SuppressWarnings(PHPMD.NPathComplexity)
 * @SuppressWarnings(PHPMD.ExcessiveMethodLength)
 */
final class ProductArrayMapper
{
    /**
     * @var mixed
     */
    private $data;

    /**
     * @var ObjectManagerInterface
     */
    private $objectManager;

    public function __construct(ObjectManagerInterface $objectManager)
    {
        $this->objectManager = $objectManager;
    }

    /**
    * Convert the DTO to the array with the data
    *
    * @param Product $dto
    * @return array
    */
    public function convertToArray(Product $dto)
    {
        $result = [];
        $result["id"] = $dto->getId();
        $result["sku"] = $dto->getSku();
        $result["price"] = $dto->getPrice();
        $result["title"] = $dto->getTitle();
        $result["description"] = $dto->getDescription();
        $result["img_url"] = $dto->getImgUrl();
        $result["available"] = $dto->getAvailable();
        /** Convert complex Array field **/
        $fieldArray = [];
        foreach ($dto->getOptions() as $fieldArrayDto) {
            $fieldArray[] = $this->objectManager->get(\Magento\GrpcTestApi\Api\Data\OptionArrayMapper::class)
                ->convertToArray($fieldArrayDto);
        }
        $result["options"] = $fieldArray;
        return $result;
    }
}
