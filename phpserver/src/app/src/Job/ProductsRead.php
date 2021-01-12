<?php

declare(strict_types=1);

namespace App\Job;

use App\ProductManager;
use Spiral\Broadcast\BroadcastInterface;
use Spiral\Broadcast\Message;
use Spiral\Jobs\JobHandler;

class ProductsRead extends JobHandler
{
    private ?ProductManager $manager = null;

    public function invoke(string $requestID, int $limit, BroadcastInterface $broadcast): void
    {
        $manager = $this->getManager();

        $success = true;
        $errorMsg = '';
        $products = [];
        try {
            $found = $manager->find($limit);
            foreach ($found as $product) {
                $data = [
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
                $products[] = $data;
            }
        } catch (\Throwable $ex) {
            $success = false;
            $errorMsg = $ex->getMessage();
            dumprr($ex);
        }

        $broadcast->publish(
            new Message($requestID, ['success' => $success, 'error_msg' => $errorMsg, 'products' => $products])
        );
    }

    private function getManager(): ProductManager
    {
        if (!$this->manager) {
            $this->manager = new ProductManager();
        }

        return $this->manager;
    }
}