-- Drop schema if needed (optional and dangerous)
-- DROP SCHEMA public CASCADE;
-- CREATE SCHEMA public;

-- USERS TABLE
CREATE TABLE public.users (
                              user_id SERIAL PRIMARY KEY,
                              email VARCHAR(100),
                              password VARCHAR(100),
                              phone_number VARCHAR(100),
                              role CHARACTER VARYING(50),
                              zip_code VARCHAR(10),
                              home_address VARCHAR(50),
                              full_name VARCHAR(50)
);

-- PRODUCT TABLE
CREATE TABLE public.product (
                                product_id SERIAL PRIMARY KEY,
                                name VARCHAR(100),
                                unit VARCHAR(20),
                                price INTEGER,
                                width_in_mm INTEGER
);

-- PRODUCT_DESCRIPTION TABLE
CREATE TABLE public.product_description (
                                            description_id SERIAL PRIMARY KEY,
                                            description VARCHAR,
                                            product_id INTEGER REFERENCES public.product(product_id) ON DELETE CASCADE
);

-- PRODUCT_VARIANT TABLE
CREATE TABLE public.product_variant (
                                        product_variant_id SERIAL PRIMARY KEY,
                                        length INTEGER,
                                        product_id INTEGER REFERENCES public.product(product_id) ON DELETE CASCADE
);

-- CARPORT_DIMENSION_WEBSITE TABLE
CREATE TABLE public.carport_dimension_website (
                                                  carport_dimension_id SERIAL PRIMARY KEY,
                                                  carport_length INTEGER,
                                                  carport_width INTEGER
);

-- ORDERS TABLE
CREATE TABLE public.orders (
                               order_id SERIAL PRIMARY KEY,
                               carport_width INTEGER,
                               carport_length INTEGER,
                               status VARCHAR,
                               user_id INTEGER REFERENCES public.users(user_id) ON DELETE CASCADE,
                               customer_price INTEGER,
                               cost_price INTEGER,
                               order_date TIMESTAMP WITH TIME ZONE
);

-- ORDER_ITEM TABLE
CREATE TABLE public.order_item (
                                   order_item_id SERIAL PRIMARY KEY,
                                   order_id INTEGER REFERENCES public.orders(order_id) ON DELETE CASCADE,
                                   product_variant_id INTEGER REFERENCES public.product_variant(product_variant_id),
                                   quantity INTEGER,
                                   product_description_id INTEGER REFERENCES public.product_description(description_id)
);