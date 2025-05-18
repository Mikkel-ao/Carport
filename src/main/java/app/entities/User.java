package app.entities;

public class User {
    private int userId;
    //We do not use password for now, but keep it as a private instance variable for potential later use!
    private String password;
    private String email;
    private String phoneNumber;
    private String role;
    private String fullName;
    private String address;
    private String zipCode;


    public User(int userId, String email, String phoneNumber, String role, String zipCode, String address, String fullName) {
        this.userId = userId;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.fullName = fullName;
        this.address = address;
        this.zipCode = zipCode;
    }


    public int getUserId() {
        return userId;
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

    public String getFullName() {
        return fullName;
    }

    public String getAddress() {
        return address;
    }

    public String getZipCode() {
        return zipCode;
    }



    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", role='" + role + '\'' +
                ", fullName='" + fullName + '\'' +
                ", address='" + address + '\'' +
                ", zipCode='" + zipCode + '\'' +
                '}';
    }
}