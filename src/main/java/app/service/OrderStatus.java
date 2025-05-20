package app.service;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    REJECTED,
    PAID,
    ACCEPT;


    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
