CREATE TABLE customers
(
    customer_id   INT PRIMARY KEY,
    customer_name VARCHAR(100) NOT NULL,
    city          VARCHAR(50),
    country       VARCHAR(50)
);

CREATE TABLE orders
(
    order_id     INT PRIMARY KEY,
    customer_id  INT,
    order_date   DATE,
    order_amount DECIMAL(10, 2),
    FOREIGN KEY (customer_id) REFERENCES customers (customer_id)
);

CREATE TABLE products
(
    product_id   INT PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL,
    category     VARCHAR(50)
);

CREATE TABLE order_items
(
    order_item_id INT PRIMARY KEY,
    order_id      INT,
    product_id    INT,
    quantity      INT,
    price         DECIMAL(10, 2),
    FOREIGN KEY (order_id) REFERENCES orders (order_id),
    FOREIGN KEY (product_id) REFERENCES products (product_id)
);

CREATE TABLE customer_order_summary AS
SELECT c.customer_id,
       c.customer_name,
       c.city,
       c.country,
       COUNT(DISTINCT o.order_id) AS total_orders,
       SUM(o.order_amount)        AS total_amount,
       AVG(o.order_amount)        AS avg_order_amount,
       MAX(o.order_date)          AS last_order_date,
       (SELECT p.category
        FROM order_items oi
                 JOIN products p ON oi.product_id = p.product_id
        WHERE oi.order_id IN (SELECT order_id
                              FROM orders
                              WHERE customer_id = c.customer_id)
        GROUP BY p.category
        ORDER BY SUM(oi.quantity) DESC
        LIMIT 1)                  AS most_ordered_category,
       SUM(oi.quantity)           AS total_quantity,
       CASE
           WHEN SUM(o.order_amount) > 10000 THEN 'High Value'
           ELSE 'Regular'
           END                    AS is_high_value
FROM customers c
         LEFT JOIN
     orders o ON c.customer_id = o.customer_id
         LEFT JOIN
     order_items oi ON o.order_id = oi.order_id
GROUP BY c.customer_id, c.customer_name, c.city, c.country;