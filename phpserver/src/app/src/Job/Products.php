<?php

declare(strict_types=1);

namespace App\Job;

use App\Dto\Option;
use App\Dto\Product;
use App\ProductManager;
use Spiral\Broadcast\BroadcastInterface;
use Spiral\Broadcast\Message;
use Spiral\Jobs\JobHandler;

class Products extends JobHandler
{
    private ?ProductManager $manager = null;

    public function invoke(string $requestID, int $number, BroadcastInterface $broadcast)
    {
        $manager = $this->getManager();
        dumprr("Streaming generated products for: {$requestID}, n = " .$number);
        try {
            $products = [];
            for ($i = 0; $i < $number; $i++) {
                $randId = random_int(1, PHP_INT_MAX);
                $product = new Product();
                $product->setSku(sprintf('sku_%d', $randId));
                $product->setPrice((float)random_int(1, 10000));
                $product->setTitle(sprintf('Product Number #%d', $randId));
                $product->setDescription(sprintf('This is a generated product number #%d', $randId));
                $product->setImgUrl(sprintf('/media/prod_%d.jpg', $randId));
                $product->setAvailable(true);
                $options = [];
                for ($j = 1; $j <= 10; $j++) {
                    $option = new Option();
                    $option->setTitle(sprintf('Option #%d', $j));
                    $option->setPrice((float)random_int(1, 10000));
                    $option->setAvailable(true);
                    $options[] = $option;
                }
                $product->setOptions($options);
                $products[] = $product;
            }

            $success = true;
            $errorMsg = '';
            try {
                $products = $manager->create($products);
                foreach ($products as $product) {
                    $data = [
                        'finished' => false,
                        'success' => true,
                        'error_msg' => '',
                        'id' => $product->getId(),
                        'sku' => $product->getSku(),
                        'price' => $product->getPrice(),
                        'title' => $product->getTitle(),
                        'description' => $product->getDescription(),
                        'img_url' => $product->getImgUrl(),
                        'available' => $product->isAvailable(),
                        'options' => []
                    ];
                    foreach ($product->getOptions() as $option) {
                        $data['options'][] = [
                            'id' => $option->getId(),
                            'title' => $option->getTitle(),
                            'price' => $option->getPrice(),
                            'available' => $option->isAvailable()
                        ];
                    }

                    $broadcast->publish(new Message($requestID, $data));
                }
            } catch (\Throwable $ex) {
                $success = false;
                $errorMsg = $ex->getMessage();
            }

            // the stream is over
            dumprr(sprintf("Finished streaming %d generated products", $number));
            $broadcast->publish(
                new Message(
                    $requestID,
                    [
                        'finished' => true,
                        'success' => $success,
                        'error_msg' => $errorMsg,
                        'id' => '',
                        'sku' => '',
                        'price' => 0.0,
                        'title' => '',
                        'description' => '',
                        'img_url' => '',
                        'available' => false,
                        'options' => []
                    ]
                )
            );

            dumprr(sprintf('%d total products persisted', $manager->count()));
        } catch (\Throwable $ex) {
            dumprr($ex->getMessage());
            throw $ex;
        }
    }

    private function getManager(): ProductManager
    {
        if (!$this->manager) {
            $this->manager = new ProductManager();
        }

        return $this->manager;
    }
}
