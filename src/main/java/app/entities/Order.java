package app.entities;

import app.util.OrderStatus;


import java.sql.Timestamp;
import java.util.List;

public class Order {
    private List<OrderItem> listOfMaterials;
    private int orderId;
    private int carportWidth;
    private int carportLength;
    private OrderStatus status;
    private User user;
    private double totalSalesPrice;
    private double costPrice;
    private Timestamp timeStamp;


    //Overloading the constructor to fit our various needs!
    public Order(int orderId, int carportWidth, int carportLength, OrderStatus status, User user, double totalPrice, double costPrice, Timestamp timeStamp) {
        this.orderId = orderId;
        this.carportWidth = carportWidth;
        this.carportLength = carportLength;
        this.status = status;
        this.user = user;
        this.totalSalesPrice = totalPrice;
        this.costPrice = costPrice;
        this.timeStamp = timeStamp;
    }

    public Order(List<OrderItem> listOfMaterials, int carportWidth, int carportLength, OrderStatus status, User user, double totalSalesPrice, double costPrice) {
        this.listOfMaterials = listOfMaterials;
        this.carportWidth = carportWidth;
        this.carportLength = carportLength;
        this.status = status;
        this.user = user;
        this.totalSalesPrice = totalSalesPrice;
        this.costPrice = costPrice;
    }


    public int getCarportWidth() {
        return carportWidth;
    }


    public int getCarportLength() {
        return carportLength;
    }


    public OrderStatus getStatus() {
        return status;
    }


    public List<OrderItem> getListOfMaterials() {
        return listOfMaterials;
    }


    public double getTotalSalesPrice() {
        return totalSalesPrice;
    }


    public void setListOfMaterials(List<OrderItem> listOfMaterials) {
        this.listOfMaterials = listOfMaterials;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getCostPrice() {
        return costPrice;
    }


    @Override
    public String toString() {
        return "Order{" +
                "listOfMaterials=" + listOfMaterials +
                ", orderId=" + orderId +
                ", carportWidth=" + carportWidth +
                ", carportLength=" + carportLength +
                ", status=" + status +
                ", user=" + user +
                ", totalSalesPrice=" + totalSalesPrice +
                ", costPrice=" + costPrice +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
