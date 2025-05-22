package app.enums;

public enum OrderStatus {
    //We use enums for static constants (variables, in this case "status"), it allows us to centralize them in one place and protect against typos.
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
