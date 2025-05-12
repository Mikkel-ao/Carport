package app.entities;

public class User {
    private int userId;
    private String password;
    private String email;
    private String phoneNumber;
    private String role;

    public User(int userId, String password, String email, String phoneNumber, String role) {
        this.userId = userId;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public User(String password, String email, String phoneNumber, String role) {
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public User(int userId, String email, String phoneNumber, String role) {
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.userId = userId;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getRole() {
        return role;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}