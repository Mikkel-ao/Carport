package app.persistence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class MapperTest {

    private static ConnectionPool testConnectionPool;

    @BeforeEach
    void setUp() {
        testConnectionPool = ConnectionPool.getInstance();

        try(Connection conn = testConnectionPool.getConnection()){

            String sql = "TRUNCATE TABLE test.users RESTART IDENTITY CASCADE";
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.executeUpdate();
            }
        } catch (SQLException e){
            fail(e.getMessage());
        }

    }

    @Test
    void createUser() {
        UserMapper.createUser("1234", "peter@haha", testConnectionPool);
    }

    @AfterEach
    void tearDown() {
        try(Connection conn = testConnectionPool.getConnection()){

            String sql = "TRUNCATE TABLE test.users RESTART IDENTITY CASCADE";
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.executeUpdate();
            }
        } catch (SQLException e){
            fail(e.getMessage());
        }

    }
}