package app.entities;

public class OrderItem {
    int orderItemId;
    ProductVariant productVariant;
    int quantity;
    String description;
    int descriptionId;


    //Overloading the constructor to fit our various needs!
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

    public int getQuantity() {
        return quantity;
    }


    public int getDescriptionId() {
        return descriptionId;
    }


    public ProductVariant getProductVariant() {
        return productVariant;
    }

    public int getOrderItemId() {
        return orderItemId;
    }

    public String getDescription() {
        return description;
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