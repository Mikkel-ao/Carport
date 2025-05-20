package app.enums;

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
