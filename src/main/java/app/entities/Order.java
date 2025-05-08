package app.entities;

import java.util.List;

public class Order {
    private List<OrderItem> orderItemList;
    int orderId;
    int carportWidth;
    int carportLength;
    String status;
    User user;
    double totalSalesPrice;

    public Order(int orderId, int carportWidth, int carportLength, String status, User user, double totalPrice) {
        this.orderId = orderId;
        this.carportWidth = carportWidth;
        this.carportLength = carportLength;
        this.status = status;
        this.user = user;
        this.totalSalesPrice = totalPrice;
    }

    public Order(int orderId, int carportWidth, int carportLength, String status, User user) {
        this.orderId = orderId;
        this.carportWidth = carportWidth;
        this.carportLength = carportLength;
        this.status = status;
        this.user = user;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCarportWidth() {
        return carportWidth;
    }

    public void setCarportWidth(int carportWidth) {
        this.carportWidth = carportWidth;
    }

    public int getCarportLength() {
        return carportLength;
    }

    public void setCarportLength(int carportLength) {
        this.carportLength = carportLength;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OrderItem> getOrderItemList() {
        return orderItemList;
    }

    public double getTotalSalesPrice() {
        return totalSalesPrice;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", carportWidth=" + carportWidth +
                ", carportLength=" + carportLength +
                ", status='" + status + '\'' +
                ", user=" + user +
                ", totalPrice=" + totalSalesPrice +
                '}';
    }
}
