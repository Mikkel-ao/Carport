package app.entities;

public class OrderItem {
    int orderItemId;
    ProductVariant productVariant;
    int quantity;

    public OrderItem(int orderItemId, ProductVariant productVariant, int quantity) {
        this.orderItemId = orderItemId;
        this.productVariant = productVariant;
        this.quantity = quantity;
    }

    public OrderItem(ProductVariant productVariant, int quantity) {
        this.productVariant = productVariant;
        this.quantity = quantity;
    }

    public int getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(int orderItemId) {
        this.orderItemId = orderItemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }



    public ProductVariant getProductVariant() {
        return productVariant;
    }

    public void setProductVariant(ProductVariant productVariant) {
        this.productVariant = productVariant;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "orderItemId=" + orderItemId +
                ", productVariant=" + productVariant +
                ", quantity=" + quantity +
                '}';
    }
}