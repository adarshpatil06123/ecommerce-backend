-- Clean development/test data templates (run manually in your MySQL client).
-- NOTE: Review before executing. Do not run in production.

-- Auth Service DB (ecommerce_auth_db)
-- DELETE FROM users WHERE email LIKE '%@example.com';
-- OPTIMIZE TABLE users;

-- Product Service DB (ecommerce_product_db)
-- DELETE FROM products WHERE name LIKE 'Test%';
-- OPTIMIZE TABLE products;

-- Order Service DB (ecommerce_order_db)
-- DELETE FROM orders WHERE created_at < DATE_SUB(NOW(), INTERVAL 90 DAY) AND status IN ('CANCELLED');
-- OPTIMIZE TABLE orders;

-- Payment Service DB (ecommerce_payment_db)
-- DELETE FROM payments WHERE timestamp < DATE_SUB(NOW(), INTERVAL 90 DAY) AND status IN ('FAILED');
-- OPTIMIZE TABLE payments;

-- Redis: flush specific caches (run in redis-cli)
-- DEL products::*
-- DEL productList

-- Kafka: No direct clean—topics are retained as per broker config.
