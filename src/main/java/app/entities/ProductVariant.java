package app.entities;

public class ProductVariant {
    int productVariantId;
    int length;
    String description;
    Product product;

    public ProductVariant(int productVariantId, int length, String description, Product product) {
        this.productVariantId = productVariantId;
        this.length = length;
        this.description = description;
        this.product = product;
    }

    public ProductVariant(int length, String description, Product product){
        this.length = length;
        this.product = product;
        this.description = description;
    }

    public int getProductVariantId() {
        return productVariantId;
    }

    public void setProductVariantId(int productVariantId) {
        this.productVariantId = productVariantId;
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

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "ProductVariant{" +
                "productVariantId=" + productVariantId +
                ", length=" + length +
                ", description='" + description + '\'' +
                ", product=" + product +
                '}';
    }
}