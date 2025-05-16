package app.persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionPool {

    private static volatile ConnectionPool instance = null;
    private static HikariDataSource ds = null;
    private static final Logger LOGGER = Logger.getLogger(ConnectionPool.class.getName());


    //Private constructor to enforce Singleton pattern!
    private ConnectionPool() {
        // Prevent instantiation
    }


    //Getting a singleton instance of Hikari Connection Pool with the specific credentials!
    public static ConnectionPool getInstance() {
        if (instance == null) {
            synchronized (ConnectionPool.class) {
                if (instance == null) {  // Double-checked locking
                        ds = createHikariConnectionPool(
                                System.getenv("JDBC_USER"),
                                System.getenv("JDBC_PASSWORD"),
                                System.getenv("JDBC_CONNECTION_STRING"),
                                System.getenv("JDBC_DB"));
                    instance = new ConnectionPool();
                }
            }
        }
        return instance;
    }


    //Getting a live connection from the Hikari Connection Pool - throws a SQLException if failed to do so!
    public Connection getConnection() throws SQLException {
        if (ds == null) {
            throw new SQLException("DataSource is not initialized. Call getInstance() first.");
        }
        return ds.getConnection();
    }

    //Closing the Hikari Connection Pool - not used at the moment, because we use "try-with-resources" blocks instead!
    public void close() {
        if (ds != null) {
            LOGGER.log(Level.INFO, "Shutting down connection pool...");
            ds.close();
            ds = null;
            instance = null;
        }
    }


    //Hikari Connection Pool configuration
    private static HikariDataSource createHikariConnectionPool(String user, String password, String url, String db) {
        LOGGER.log(Level.INFO, "Initializing Connection Pool for database: {0}", db);

        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.postgresql.Driver");
        config.setJdbcUrl(String.format(url, db));
        config.setUsername(user);
        config.setPassword(password);

        // Connection Pool Configurations
        config.setMaximumPoolSize(10); // Default is 10
        config.setMinimumIdle(2);      // Ensures some connections are always available
        config.setIdleTimeout(30000);  // 30 seconds idle timeout
        config.setConnectionTimeout(30000); // Max wait time for a connection
        config.setPoolName("Postgresql-Pool");

        // Optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return new HikariDataSource(config);
    }
}
