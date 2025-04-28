package app.entities;

public class User {
    private int userId;
    private String password;
    private String email;
    private String role;

    public User(int userId, String password, String email, String role) {
        this.userId = userId;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    public User(String password, String email, String role) {
        this.password = password;
        this.email = email;
        this.role = role;
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

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
