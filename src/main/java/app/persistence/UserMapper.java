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
    public static User login(String email, String password, ConnectionPool connectionPool) throws DatabaseException {
        //Email is unique in the database, which means we are certain that we get the right user_id and (hashed) password!
        String sql = "select user_id, password from users where email=?";

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

                    if (BCrypt.checkpw(password, hashedPassword)) {
                        User currentUser = getUserById(userId, connectionPool);
                        return currentUser;
                    } else throw new DatabaseException("Forkert email eller kodeord!");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke hente brugerens login informationer!", e.getMessage());
        }
        throw new DatabaseException("Kunne ikke oprette forbindelse til databasen!");
    }


    //Creates a new user by inserting user information from the front end into the database!
    public static void createUser(String email, String password, String phoneNumber, String zipCode, String homeAdress, String fullName, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "INSERT INTO users (email, password, phone_number,zip_code,home_address,full_name) VALUES (?, ?, ?, ?,?,?)";

        //Encrypts the password before storing it in the database for increased security!
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        //"try-with-resources" block that makes sure to auto close after usage!
        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, email);
            ps.setString(2, hashedPassword);
            ps.setString(3, phoneNumber);
            ps.setString(4, zipCode);
            ps.setString(5, homeAdress);
            ps.setString(6, fullName);

            //Storing the number of rows modified, if no rows were affected an exception is thrown
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1) {
                throw new DatabaseException("En fejl opstod under oprettelse af brugeren!");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke oprette forbindelse til databasen!", e.getMessage());
        }

    }

    //Method for retrieving user information by the user id
    public static User getUserById(int userId, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        //"try-with-resources" block that makes sure to auto close after usage!
        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    String email = rs.getString("email");
                    String phoneNumber = rs.getString("phone_number");
                    String role = rs.getString("role");
                    String zipCode = rs.getString("zip_code");
                    String address = rs.getString("home_address");
                    String fullName = rs.getString("full_name");

                    return new User(userId, email, phoneNumber, role, zipCode, address, fullName);

                } else throw new DatabaseException("Kunne ikke hente brugerens user!");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not retrieve user from database", e.getMessage());
        }
    }
}



