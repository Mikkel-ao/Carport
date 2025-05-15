package app.entities;

public class OrderItem {
    int orderItemId;
    ProductVariant productVariant;
    int quantity;
    String description;
    int descriptionId;

    public OrderItem(int orderItemId, ProductVariant productVariant, int quantity, String description, int descriptionId) {
        this.orderItemId = orderItemId;
        this.productVariant = productVariant;
        this.quantity = quantity;
        this.description = description;
        this.descriptionId = descriptionId;
    }


    public OrderItem(ProductVariant productVariant, int quantity, int descriptionId) {
        this.productVariant = productVariant;
        this.quantity = quantity;
        this.descriptionId = descriptionId;
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

    public int getDescriptionId() {
        return descriptionId;
    }

    public String getDescription() {
        return description;
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
                ", description='" + description + '\'' +
                ", descriptionId=" + descriptionId +
                '}';
    }
}