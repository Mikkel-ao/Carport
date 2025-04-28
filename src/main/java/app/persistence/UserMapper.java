package app.persistence;

import app.entities.User;
import app.exceptions.DatabaseException;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper {

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
                double balance = rs.getDouble("balance");

                //Comparing the entered password with the now hashed password from the database using BCrypt
                if (BCrypt.checkpw(password, hashedPassword)) {
                    return new User(user_id, hashedPassword, email, role);
                }else {
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
    public static void createUser(String password, String email, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "insert into users (password, email) values (?,?)";
        //Encrypts the password and stores it in a variable. Doing the encrypting before storing in database for security
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        System.out.println("Email: " + email);
        System.out.println("Before hashing Password: " + password);
        System.out.println("After hashing Password: " + hashedPassword);

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, hashedPassword);
            ps.setString(2, email);

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
}
