<?php

declare(strict_types=1);

namespace App\Job;

use App\ProductManager;
use Spiral\Broadcast\BroadcastInterface;
use Spiral\Broadcast\Message;
use Spiral\Jobs\JobHandler;

class ProductsClear extends JobHandler
{
    private ?ProductManager $manager = null;

    public function invoke(string $requestID, BroadcastInterface $broadcast): void
    {
        $manager = $this->getManager();

        $success = true;
        $errorMsg = '';
        try {
            $manager->clear();
        } catch (\Throwable $ex) {
            $success = false;
            $errorMsg = $ex->getMessage();
        }

        $broadcast->publish(new Message($requestID, ['success' => $success, 'error_msg' => $errorMsg]));
    }

    private function getManager(): ProductManager
    {
        if (!$this->manager) {
            $this->manager = new ProductManager();
        }

        return $this->manager;
    }
}