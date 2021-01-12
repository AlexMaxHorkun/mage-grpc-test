<?php
declare(strict_types=1);

namespace App;

use App\Dto\Option;
use App\Dto\Product;

class ProductManager
{
    private const PROD_INSERT_SQL = 'INSERT INTO prods(id, sku, price, title, description, img_url, available) '
        .'VALUES (:id, :sku, :price, :title, :description, :img_url, :available)';

    private const OPTION_INSERT_SQL = 'INSERT INTO prod_options(id, prod_id, title, price, available) '
        .'VALUES (:id, :prod_id, :title, :price, :available)';

    private const DELETE_SQL = 'DELETE FROM prods';

    private const COUNT_SQL = 'SELECT count(*) cnt FROM prods p';

    private const SELECT_SQL = 'SELECT * FROM prods p ORDER BY p.id ASC LIMIT :lim OFFSET :off';

    private const SELECT_OPTIONS_SQL = 'SELECT * FROM prod_options o WHERE o.prod_id IN (%ids%) ORDER BY o.prod_id ASC';

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

    /**
     * @param int $limit
     * @return Product[]
     */
    public function find(int $limit): array
    {
        if ($limit < 1) {
            throw new \InvalidArgumentException('Invalid limit provided');
        }
        $count = $this->count();
        if ($count <= $limit) {
            $offset = 0;
        } else {
            $offset = random_int(0, $count - $limit);
        }

        $stmt = $this->pdo->prepare(self::SELECT_SQL);
        $stmt->bindValue(':lim', $limit, \PDO::PARAM_INT);
        $stmt->bindValue(':off', $offset, \PDO::PARAM_INT);
        $stmt->execute();
        $rows = $stmt->fetchAll();
        $products = [];
        foreach ($rows as $row) {
            $product = new Product();
            $products[$row['id']] = $product;
            $product->setId($row['id']);
            $product->setTitle($row['title']);
            $product->setDescription($row['description']);
            $product->setSku($row['sku']);
            $product->setImgUrl($row['img_url']);
            $product->setPrice((float)$row['price']);
            $product->setAvailable((bool)$row['available']);
        }

        if ($products) {
            $rows = $this->pdo->query(
                str_replace('%ids%',
                    implode(', ', array_map(function ($id) {
                        return $this->pdo->quote($id);
                    }, array_keys($products))),
                    self::SELECT_OPTIONS_SQL
                )
            )->fetchAll();
            foreach ($rows as $row) {
                $product = $products[$row['prod_id']];
                $option = new Option();
                $option->setId($row['id']);
                $option->setTitle($row['title']);
                $option->setPrice((float)$row['price']);
                $option->setAvailable((bool)$row['available']);
                $product->setOptions(array_merge($product->getOptions(), [$option]));
            }
        }

        return array_values($products);
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