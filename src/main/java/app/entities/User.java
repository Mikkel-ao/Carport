package app.entities;

public class User {
    private int userId;
    private String password;
    private String email;
    private String role;
    private double balance;

    public User(int userId, String password, String email, String role, double balance) {
        this.userId = userId;
        this.password = password;
        this.email = email;
        this.role = role;
        this.balance = balance;
    }

    public User(String password, String email, String role, double balance) {
        this.password = password;
        this.email = email;
        this.role = role;
        this.balance = balance;
    }


    public int getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }


    public double getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        return "User{" +
                "user_id=" + userId +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", balance=" + balance +
                '}';
    }
}
