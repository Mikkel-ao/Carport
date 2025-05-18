package app.DTO;

import app.util.OrderStatus;

import java.sql.Timestamp;

public class OrderInfoDTO {
    private int orderId;
    private int carportWidth;
    private int carportLength;
    private OrderStatus status;
    private double customerPrice;
    private double costPrice;
    private Timestamp timeStamp;

    public OrderInfoDTO(int orderId, int carportWidth, int carportLength, OrderStatus status, double customerPrice, double costPrice, Timestamp timeStamp) {
        this.orderId = orderId;
        this.carportWidth = carportWidth;
        this.carportLength = carportLength;
        this.status = status;
        this.customerPrice = customerPrice;
        this.costPrice = costPrice;
        this.timeStamp = timeStamp;
    }

    public double getCostPrice() {
        return costPrice;
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
                ", customerPrice=" + customerPrice +
                ", costPrice=" + costPrice +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
