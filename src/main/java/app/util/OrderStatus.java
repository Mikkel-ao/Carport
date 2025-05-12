package app.util;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    CANCELLED;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
