package app.DTO;

import app.util.OrderStatus;

public class OrderInfoDTO {
    private int orderId;
    private int carportWidth;
    private int carportLength;
    private OrderStatus status;
    private double customerPrice;
    private double costPrice;

    public OrderInfoDTO(int orderId, int carportWidth, int carportLength, OrderStatus status, double customerPrice, double costPrice) {
        this.orderId = orderId;
        this.carportWidth = carportWidth;
        this.carportLength = carportLength;
        this.status = status;
        this.customerPrice = customerPrice;
        this.costPrice = costPrice;
    }

    public double getCostPrice() {
        return costPrice;
    }

    public double getTotalSalesPrice() {
        return customerPrice;
    }

    public double getCustomerPrice() {
        return customerPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public int getCarportLength() {
        return carportLength;
    }

    public int getCarportWidth() {
        return carportWidth;
    }

    public int getOrderId() {
        return orderId;
    }

    @Override
    public String toString() {
        return "OrderColumns{" +
                "orderId=" + orderId +
                ", carportWidth=" + carportWidth +
                ", carportLength=" + carportLength +
                ", status=" + status +
                ", customerPrice=" + customerPrice +
                ", costPrice=" + costPrice +
                '}';
    }
}
