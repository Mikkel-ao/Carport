package app.entities;

public class ProductVariant {
    private int productVariantId;
    private int length;
    private Product product;

    public ProductVariant(int productVariantId, int length, Product product) {
        this.productVariantId = productVariantId;
        this.length = length;
        this.product = product;
    }

    public int getProductVariantId() {
        return productVariantId;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public Product getProduct() {
        return product;
    }


    @Override
    public String toString() {
        return "ProductVariant{" +
                "productVariantId=" + productVariantId +
                ", length=" + length +
                ", product=" + product +
                '}';
    }
}