<?php
/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
declare(strict_types=1);

namespace Magento\GrpcTest\Model;

use Magento\CatalogStorefront\Model\CatalogRepository;
use Magento\GrpcTestApi\Api\Data\ClearArgInterface;
use Magento\GrpcTestApi\Api\Data\Cleared;
use Magento\GrpcTestApi\Api\Data\ClearedInterface;
use Magento\GrpcTestApi\Api\Data\GenerateArgInterface;
use Magento\GrpcTestApi\Api\Data\ProductArrayMapper;
use Magento\GrpcTestApi\Api\Data\ProductMapper;
use Magento\GrpcTestApi\Api\Data\ReadRequestInterface;
use Magento\GrpcTestApi\Api\Data\ReadResponse;
use Magento\GrpcTestApi\Api\Data\ReadResponseInterface;
use Magento\GrpcTestApi\Api\ProductsServerInterface;

/**
 * Class for retrieving catalog data
 *
 * @SuppressWarnings(PHPMD.CouplingBetweenObjects)
 * @SuppressWarnings(PHPMD.CyclomaticComplexity)
 * @SuppressWarnings(PHPMD.ExcessiveClassComplexity)
 * @SuppressWarnings(PHPMD.NPathComplexity)
 */
class GrpcTestService implements ProductsServerInterface
{
    private const STORE_CODE = 'default';
    private const PRODUCT_ENTITY = 'product';

    /**
     * @var CatalogRepository
     */
    private $catalogRepository;
    /**
     * @var ProductMapper
     */
    private $productBuilder;
    /**
     * @var \Magento\CatalogStorefront\Model\Storage\State
     */
    private $storageState;
    /**
     * @var \Magento\CatalogStorefront\Model\Storage\Client\ConnectionPull
     */
    private $connectionPull;
    /**
     * @var \Magento\CatalogStorefront\DataProvider\ProductDataProvider
     */
    private $dataProvider;

    private $productsCount;

    /**
     * @param CatalogRepository $catalogRepository
     */
    public function __construct(
        ProductMapper $productBuilder,
        CatalogRepository $catalogRepository,
        \Magento\CatalogStorefront\Model\Storage\State $storageState,
        \Magento\CatalogStorefront\Model\Storage\Client\ConnectionPull $connectionPull,
        \Magento\CatalogStorefront\DataProvider\ProductDataProvider $dataProvider
    ) {
        $this->catalogRepository = $catalogRepository;
        $this->productBuilder = $productBuilder;
        $this->storageState = $storageState;
        $this->connectionPull = $connectionPull;
        $this->dataProvider = $dataProvider;
    }

    private function getProductCounts()
    {
        file_put_contents('/tmp/sss', "1\n", FILE_APPEND);
        try {
            $sourceName = $this->storageState->getCurrentDataSourceName([self::STORE_CODE, self::PRODUCT_ENTITY]);
            $count = $this->connectionPull->getConnection()->count(['index' => $sourceName]);
            return isset($count['count']) ? ($count['count'] + 1) : 0;
        } catch (\Throwable $e) {
        }
        return 0;
    }

    /**
     * Use similar to \Magento\CatalogStorefront\Model\CatalogService::importProducts approach to compare apple to apple
     *
     * @param GenerateArgInterface $request
     * @return \Magento\GrpcTestApi\Api\Data\ProductInterface
     * @throws \Exception
     */
    public function generate(GenerateArgInterface $request): \Magento\GrpcTestApi\Api\Data\ProductInterface
    {
        $number = $request->getNumber();
        $storeCode = self::STORE_CODE;
        $productsInElasticFormat = [];
        $productResponse = null;
        // hack to get Next product id to simplify "read" testing
        $nextProductId = $this->getProductCounts();
        $number+= $nextProductId;


        try {
            for ($i = $nextProductId; $i < $number; $i++) {
                $randId = random_int(1, PHP_INT_MAX);
                $product = [
                    'sku' => sprintf('sku_%d', $randId),
                    'id' => (string)$i,
                    'price' => (float)random_int(1, 10000),
                    'title' => sprintf('Product Number #%d', $randId),
                    'description' => sprintf('This is a generated product number #%d', $randId),
                    'image_url' => sprintf('/media/prod_%d.jpg', $randId),
                    'available' => true,
                ];
                $options = [];
                for ($j = 1; $j <= 10; $j++) {
                    $option = [
                        'title' => sprintf('Option #%d', $j),
                        'price' => (float)random_int(1, 10000),
                        'available' => true
                    ];
                    $options[] = $option;
                }
                $product['options'] = $options;
                $productsInElasticFormat[self::PRODUCT_ENTITY][$storeCode][CatalogRepository::SAVE][] = $product;
                if (!$productResponse) {
                    $productResponse = $this->productBuilder->setData($product)->build();
                }
            }

            // TODO: use mapper
            // $product = $this->productArrayMapper->convertToArray($productData->getProduct());
            $this->catalogRepository->saveToStorage($productsInElasticFormat);
        } catch (\Throwable $e) {
            $message = \sprintf('Cannot process product import, error: "%s"', $e->getMessage());
            throw new \Exception($message);
        }

        // return only one product instead of strem, for testing
        return $productResponse;
    }

    public function clear(ClearArgInterface $request): ClearedInterface
    {
        $sourceName = $this->storageState->getCurrentDataSourceName([self::STORE_CODE, self::PRODUCT_ENTITY]);
        exec('curl -XDELETE mage-grpc-elastic:9200/' . $sourceName);

        return new Cleared();
    }

    public function read(ReadRequestInterface $request): ReadResponseInterface
    {
        $productsN = $request->getN();

        // randomize ids
        if (!$this->productsCount) {
            $this->productsCount = $this->getProductCounts();
        }
        $startMax = $this->productsCount > $productsN ? max(0, $this->productsCount - $productsN) : 0;
        $start = random_int(0, $startMax);

        $rawItems = $this->dataProvider->fetch(
            range($start, $start + $productsN - 1),
            ['sku', 'id', 'price', 'title', 'description', 'image_url', 'available', 'options'],
            ['store' => self::STORE_CODE]
        );

        $products = [];
        foreach ($rawItems as $item) {
            $products[] = $this->productBuilder->setData($item)->build();
        }

        $response = new ReadResponse();
        $response->setItems($products);

        return $response;
    }
}
