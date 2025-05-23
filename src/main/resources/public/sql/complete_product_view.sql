CREATE VIEW public.complete_product_view AS
SELECT
    pv.product_variant_id,
    pv.product_id,
    pv.length,
    p.width_in_mm,
    p.name,
    p.unit,
    p.price
FROM public.product_variant pv
         JOIN public.product p ON pv.product_id = p.product_id;
