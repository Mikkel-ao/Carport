package app.entities;

public class ProductVariant {
    int productVariantId;
    int length;
    Product product;

    public ProductVariant(int productVariantId, int length, Product product) {
        this.productVariantId = productVariantId;
        this.length = length;
        this.product = product;
    }

    public ProductVariant(int length, Product product){
        this.length = length;
        this.product = product;
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


    @Override
    public String toString() {
        return "ProductVariant{" +
                "productVariantId=" + productVariantId +
                ", length=" + length +
                ", product=" + product +
                '}';
    }
}