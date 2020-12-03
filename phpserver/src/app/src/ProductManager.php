<?php
declare(strict_types=1);

namespace App;

use App\Dto\Product;

class ProductManager
{
    private const PROD_INSERT_SQL = 'INSERT INTO prods(id, sku, price, title, description, img_url, available) '
        .'VALUES (:id, :sku, :price, :title, :description, :img_url, :available)';

    private const OPTION_INSERT_SQL = 'INSERT INTO prod_options(id, prod_id, title, price, available) '
        .'VALUES (:id, :prod_id, :title, :price, :available)';

    private const DELETE_SQL = 'DELETE FROM prods';

    private const COUNT_SQL = 'SELECT count(*) cnt FROM prods p LEFT JOIN prod_options o on o.prod_id = p.id';

    private \PDO $pdo;

    /**
     * ProductManager constructor.
     */
    public function __construct()
    {
        $conStr = sprintf(
            "pgsql:host=%s;port=%d;dbname=%s;user=%s;password=%s",
            'mage-grpc-phpserver-db',
            5432,
            'magento',
            'admin',
            '12345aBc'
        );

        $pdo = new \PDO($conStr);
        $pdo->setAttribute(\PDO::ATTR_ERRMODE, \PDO::ERRMODE_EXCEPTION);
        $this->pdo = $pdo;
    }

    /**
     * @param Product[] $products
     * @return Product[]|iterable
     */
    public function create(array $products): iterable
    {
        $gen = function () use (&$products) {
            $this->pdo->beginTransaction();
            try {
                foreach ($products as $product) {
                    $product->setId($this->generateUuid());
                    $insertStmt = $this->pdo->prepare(self::PROD_INSERT_SQL);
                    $insertStmt->bindValue(':id', $product->getId());
                    $insertStmt->bindValue(':sku', $product->getSku());
                    $insertStmt->bindValue(':price', $product->getPrice());
                    $insertStmt->bindValue(':title', $product->getTitle());
                    $insertStmt->bindValue(':description', $product->getDescription());
                    $insertStmt->bindValue(':img_url', $product->getImgUrl());
                    $insertStmt->bindValue(':available', $product->isAvailable());
                    $insertStmt->execute();

                    foreach ($product->getOptions() as $option) {
                        $option->setId($this->generateUuid());
                        $insertStmt = $this->pdo->prepare(self::OPTION_INSERT_SQL);
                        $insertStmt->bindValue(':id', $option->getId());
                        $insertStmt->bindValue(':prod_id', $product->getId());
                        $insertStmt->bindValue(':price', $option->getPrice());
                        $insertStmt->bindValue(':title', $option->getTitle());
                        $insertStmt->bindValue(':available', $option->isAvailable());
                        $insertStmt->execute();
                    }

                    yield $product;
                }

                $this->pdo->commit();
            } catch (\Throwable $ex) {
                $this->pdo->rollBack();
                dumprr($ex->getMessage());
                throw $ex;
            }
        };

        return $gen();
    }

    public function clear(): void
    {
        $this->pdo->exec(self::DELETE_SQL);
    }

    public function count(): int
    {
        return (int)$this->pdo->query(self::COUNT_SQL)->fetch()['cnt'];
    }

    private function generateUuid(): string
    {
        return sprintf( '%04x%04x-%04x-%04x-%04x-%04x%04x%04x',
            // 32 bits for "time_low"
            mt_rand( 0, 0xffff ), mt_rand( 0, 0xffff ),

            // 16 bits for "time_mid"
            mt_rand( 0, 0xffff ),

            // 16 bits for "time_hi_and_version",
            // four most significant bits holds version number 4
            mt_rand( 0, 0x0fff ) | 0x4000,

            // 16 bits, 8 bits for "clk_seq_hi_res",
            // 8 bits for "clk_seq_low",
            // two most significant bits holds zero and one for variant DCE1.1
            mt_rand( 0, 0x3fff ) | 0x8000,

            // 48 bits for "node"
            mt_rand( 0, 0xffff ), mt_rand( 0, 0xffff ), mt_rand( 0, 0xffff )
        );
    }
}