package app.DTO;

import app.util.OrderStatus;

import java.sql.Timestamp;

public class OrderInfoDTO {
    private int orderId;
    private int carportWidth;
    private int carportLength;
    private OrderStatus status;
    private double totalSalesPrice;
    private double costPrice;
    private Timestamp timeStamp;

    public OrderInfoDTO(int orderId, int carportWidth, int carportLength, OrderStatus status, double totalSalesPrice, double costPrice, Timestamp timeStamp) {
        this.orderId = orderId;
        this.carportWidth = carportWidth;
        this.carportLength = carportLength;
        this.status = status;
        this.totalSalesPrice = totalSalesPrice;
        this.costPrice = costPrice;
        this.timeStamp = timeStamp;
    }

    public double getCostPrice() {
        return costPrice;
    }

    public double getTotalSalesPrice() {
        return totalSalesPrice;
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

    public Timestamp getTimeStamp() {
        return timeStamp;
    }

    @Override
    public String toString() {
        return "OrderInfoDTO{" +
                "orderId=" + orderId +
                ", carportWidth=" + carportWidth +
                ", carportLength=" + carportLength +
                ", status=" + status +
                ", totalSalesPrice=" + totalSalesPrice +
                ", costPrice=" + costPrice +
                ", timeStamp=" + timeStamp +
                '}';
    }
}