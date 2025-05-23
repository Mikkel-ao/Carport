CREATE VIEW public.complete_order_view AS
SELECT
    pv.product_id,
    pv.product_variant_id,
    o.order_id,
    o.carport_width,
    o.carport_length,
    o.status,
    o.user_id,
    o.customer_price,
    o.cost_price,
    o.order_date,
    oi.order_item_id,
    oi.quantity,
    pv.length,
    p.name,
    p.unit,
    p.price,
    pd.description,
    pd.description_id
FROM public.orders o
         JOIN public.order_item oi ON o.order_id = oi.order_id
         JOIN public.product_variant pv ON oi.product_variant_id = pv.product_variant_id
         JOIN public.product p ON p.product_id = pv.product_id
         JOIN public.product_description pd ON pd.description_id = oi.product_description_id;
