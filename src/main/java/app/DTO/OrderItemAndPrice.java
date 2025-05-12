package app.DTO;

import app.entities.OrderItem;

public class OrderItemAndPrice {

    OrderItem orderItem;
    double price;

    public OrderItemAndPrice(OrderItem orderItem, double price) {
        this.orderItem = orderItem;
        this.price = price;
    }

    public OrderItem getOrderItem() {
        return orderItem;
    }

    public double getPrice() {
        return price;
    }

    public void setOrderItem(OrderItem orderItem) {
        this.orderItem = orderItem;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
