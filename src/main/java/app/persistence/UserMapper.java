package app.persistence;

import app.entities.User;
import app.exceptions.DatabaseException;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper {

    //Method for login that authenticates the user by verifying username and password by comparing them to the ones stored in the database
    /*public static User login(String email, String password, ConnectionPool connectionPool) throws DatabaseException {
        //Not querying for password since it is now hashed and not plain text anymore (email is unique though)
        String sql = "select * from users where email=?";

        //"try-with-resources" block that makes sure to auto close after usage!
        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, email);

            //Ensures ResultSet is properly closed!
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String hashedPassword = rs.getString("password");
                    String role = rs.getString("role");
                    String phoneNumber = rs.getString("phone_number");

                    if (BCrypt.checkpw(password, hashedPassword)) {
                        return new User(userId, email, phoneNumber, role);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke hente brugerens login informationer!", e.getMessage());
        }
        throw new DatabaseException("Kunne ikke hente brugerens login informationer!");
    }*/


    //Authenticates the user by verifying username and password from the database
    public static User login(String email, String password, ConnectionPool connectionPool) throws DatabaseException {
        //Not querying for password since it is now hashed and not plain text anymore
        String sql = "select * from users where email=?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();
            //If a user with the given email is found in the database, it retrieves the user details
            if (rs.next()) {
                int user_id = rs.getInt("user_id");
                //Hashed password
                String hashedPassword = rs.getString("password");
                String role = rs.getString("role");

                //Comparing the entered password with the now hashed password from the database using BCrypt
                if (BCrypt.checkpw(password, hashedPassword)) {
                    return new User(user_id, hashedPassword, email, "12345", role);
                } else {
                    throw new DatabaseException("Invalid email or password.");
                }
            } else {
                throw new DatabaseException("Error on login - please try again.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error", e.getMessage());
        }
    }

        //Creates a new user by inserting username and password into the database
        public static void createUser (String email, String password, String phoneNumber, String zipCode, String
        homeAdress, String fullName, ConnectionPool connectionPool) throws DatabaseException {
            String sql = "INSERT INTO users (email, password, phone_number,role,zip_code,home_address,full_name) VALUES (?, ?, ?, ?,?,?,?)";
            //Encrypts the password and stores it in a variable. Doing the encrypting before storing in database for security
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            System.out.println("Email: " + email);
            System.out.println("Before hashing Password: " + password);
            System.out.println("After hashing Password: " + hashedPassword);

            try (
                    Connection connection = connectionPool.getConnection();
                    PreparedStatement ps = connection.prepareStatement(sql)
            ) {
                ps.setString(1, email);
                ps.setString(2, hashedPassword);
                ps.setString(3, phoneNumber); //Hard-coded values for now
                ps.setString(4, "customer"); //Hard-coded values for now
                ps.setString(5, zipCode);
                ps.setString(6, homeAdress);
                ps.setString(7, fullName);

                //Storing the number of rows modified, if no rows were inserted, exception is thrown
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected != 1) {
                    throw new DatabaseException("An error occurred on attempt to create a user - try again.");
                }
            } catch (SQLException e) {
                String msg = "Error - try again.";
                if (e.getMessage().startsWith("ERROR: duplicate key value ")) {
                    msg = "User " + email + " already exists.";
                }
                throw new DatabaseException(msg, e.getMessage());
            }
        }

        public static User getUserById ( int userId, ConnectionPool connectionPool) throws DatabaseException {
            String sql = "SELECT * FROM public.users WHERE user_id = ?";

            try (
                    Connection connection = connectionPool.getConnection();
                    PreparedStatement ps = connection.prepareStatement(sql)
            ) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String email = rs.getString("email");
                    String phoneNumber = rs.getString("phone_number");
                    String role = rs.getString("role");
                    String zipCode = rs.getString("zip_code");
                    String address = rs.getString("home_address");
                    String fullName = rs.getString("full_name");

                    return new User(userId, email, phoneNumber, role, zipCode, address, fullName);
                }
            } catch (SQLException e) {
                throw new DatabaseException("Could not retrieve user from database", e);
            }
            throw new DatabaseException("Could not retrieve user from database");
        }
    }


