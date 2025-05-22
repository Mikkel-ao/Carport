package app.persistence;

import app.DTO.OrderInfoDTO;
import app.entities.*;
import app.entities.Order;
import app.exceptions.DatabaseException;
import app.enums.OrderStatus;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapperTest {

    private static ConnectionPool testConnectionPool;
    private static int insertedOrderId;


    @BeforeAll
    static void setupClass() {
        testConnectionPool = ConnectionPool.getInstance();

        try (Connection connection = testConnectionPool.getConnection();
             Statement stmt = connection.createStatement()) {

            stmt.execute("SET search_path TO test");

            // Drop dependent views and tables
            stmt.execute("DROP VIEW IF EXISTS complete_order_view");
            stmt.execute("DROP TABLE IF EXISTS order_item CASCADE");
            stmt.execute("DROP TABLE IF EXISTS orders CASCADE");
            stmt.execute("DROP TABLE IF EXISTS product_variant CASCADE");
            stmt.execute("DROP TABLE IF EXISTS product_description CASCADE");
            stmt.execute("DROP TABLE IF EXISTS product CASCADE");
            stmt.execute("DROP TABLE IF EXISTS users CASCADE");
            stmt.execute("DROP TABLE IF EXISTS carport_dimension_website CASCADE");

            // Drop sequences
            stmt.execute("DROP SEQUENCE IF EXISTS order_item_order_item_id_seq CASCADE");
            stmt.execute("DROP SEQUENCE IF EXISTS orders_order_id_seq CASCADE");
            stmt.execute("DROP SEQUENCE IF EXISTS product_variant_product_variant_id_seq CASCADE");
            stmt.execute("DROP SEQUENCE IF EXISTS product_description_description_id_seq CASCADE");
            stmt.execute("DROP SEQUENCE IF EXISTS product_product_id_seq CASCADE");
            stmt.execute("DROP SEQUENCE IF EXISTS users_user_id_seq CASCADE");
            stmt.execute("DROP SEQUENCE IF EXISTS carport_dimension_website_carport_dimension_id_seq CASCADE");

            // Recreate tables from public schema
            stmt.execute("CREATE TABLE users (LIKE public.users INCLUDING CONSTRAINTS INCLUDING INDEXES)");
            stmt.execute("CREATE TABLE orders (LIKE public.orders INCLUDING CONSTRAINTS INCLUDING INDEXES)");
            stmt.execute("CREATE TABLE product (LIKE public.product INCLUDING CONSTRAINTS INCLUDING INDEXES)");
            stmt.execute("CREATE TABLE product_description (LIKE public.product_description INCLUDING CONSTRAINTS INCLUDING INDEXES)");
            stmt.execute("CREATE TABLE product_variant (LIKE public.product_variant INCLUDING CONSTRAINTS INCLUDING INDEXES)");
            stmt.execute("CREATE TABLE order_item (LIKE public.order_item INCLUDING CONSTRAINTS INCLUDING INDEXES)");
            stmt.execute("CREATE TABLE carport_dimension_website (LIKE public.carport_dimension_website INCLUDING CONSTRAINTS INCLUDING INDEXES)");
            stmt.execute("ALTER TABLE orders ALTER COLUMN status SET DEFAULT 'pending'");

            // Recreate sequences
            stmt.execute("CREATE SEQUENCE users_user_id_seq");
            stmt.execute("ALTER TABLE users ALTER COLUMN user_id SET DEFAULT nextval('users_user_id_seq')");
            stmt.execute("CREATE SEQUENCE orders_order_id_seq");
            stmt.execute("ALTER TABLE orders ALTER COLUMN order_id SET DEFAULT nextval('orders_order_id_seq')");
            stmt.execute("CREATE SEQUENCE product_product_id_seq");
            stmt.execute("ALTER TABLE product ALTER COLUMN product_id SET DEFAULT nextval('product_product_id_seq')");
            stmt.execute("CREATE SEQUENCE product_description_description_id_seq");
            stmt.execute("ALTER TABLE product_description ALTER COLUMN description_id SET DEFAULT nextval('product_description_description_id_seq')");
            stmt.execute("CREATE SEQUENCE product_variant_product_variant_id_seq");
            stmt.execute("ALTER TABLE product_variant ALTER COLUMN product_variant_id SET DEFAULT nextval('product_variant_product_variant_id_seq')");
            stmt.execute("CREATE SEQUENCE order_item_order_item_id_seq");
            stmt.execute("ALTER TABLE order_item ALTER COLUMN order_item_id SET DEFAULT nextval('order_item_order_item_id_seq')");
            stmt.execute("CREATE SEQUENCE carport_dimension_website_carport_dimension_id_seq");
            stmt.execute("ALTER TABLE carport_dimension_website ALTER COLUMN carport_dimension_id SET DEFAULT nextval('carport_dimension_website_carport_dimension_id_seq')");

            // Recreate view
            stmt.execute("""
                CREATE VIEW test.complete_order_view AS
                SELECT pv.product_id,
                       pv.product_variant_id,
                       o.order_id,
                       o.carport_width,
                       o.carport_length,
                       o.status,
                       o.user_id,
                       o.customer_price,
                       o.cost_price,
                       o.order_date,
                       oi.order_item_id,
                       oi.quantity,
                       pv.length,
                       p.name,
                       p.unit,
                       p.price,
                       pd.description,
                       pd.description_id,
                       pd.key_word
                FROM test.orders o
                         JOIN test.order_item oi ON o.order_id = oi.order_id
                         JOIN test.product_variant pv ON oi.product_variant_id = pv.product_variant_id
                         JOIN test.product p ON p.product_id = pv.product_id
                         JOIN test.product_description pd ON pd.description_id = oi.product_description_id
            """);

        } catch (SQLException e) {
            e.printStackTrace();
            fail("Database setup failed: " + e.getMessage());
        }
    }

    @BeforeEach
    void setup() {
        try (Connection connection = testConnectionPool.getConnection();
             Statement stmt = connection.createStatement()) {

            stmt.execute("SET search_path TO test");
            stmt.execute("TRUNCATE TABLE test.order_item, test.orders, test.users, test.product_variant, test.product_description, test.product, test.carport_dimension_website RESTART IDENTITY CASCADE");

            try (Connection conn = testConnectionPool.getConnection()) {
                conn.setSchema("test");  // or execute "SET search_path TO test"
                conn.setAutoCommit(false);

                // Insert user with RETURNING user_id
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO test.users (email, password, phone_number, role, zip_code, home_address, full_name) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING user_id")) {
                    ps.setString(1, "newuser@test.com");
                    ps.setString(2, "hashedpassword");
                    ps.setString(3, "123456");
                    ps.setString(4, "customer");
                    ps.setString(5, "8000");
                    ps.setString(6, "testvej");
                    ps.setString(7, "tester Jens");
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            int userId = rs.getInt(1);

                            // Now insert order using userId
                            try (PreparedStatement ps2 = conn.prepareStatement(
                                    "INSERT INTO test.orders (carport_width, carport_length, user_id, customer_price, cost_price, order_date) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)")) {
                                ps2.setInt(1, 600);
                                ps2.setInt(2, 780);
                                ps2.setInt(3, userId);
                                ps2.setInt(4, 2000);
                                ps2.setInt(5, 1000);
                                ps2.executeUpdate();
                            }
                        }
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                e.printStackTrace();
                fail("Test data setup failed: " + e.getMessage());
            }

            ResultSet rs = stmt.executeQuery("SELECT user_id FROM test.users");
            if (rs.next()) {
                int userId = rs.getInt("user_id");
                System.out.println("Inserted user ID: " + userId);
            }
            ResultSet rs2 = stmt.executeQuery("SELECT order_id FROM test.orders");
            if (rs2.next()) {
                int orderId = rs2.getInt("order_id");
                System.out.println("Inserted order ID: " + orderId); // Add this
            }
            stmt.execute("INSERT INTO test.carport_dimension_website (carport_length, carport_width) VALUES (780, 600)");

            stmt.execute("INSERT INTO test.product (name, unit, price, width_in_mm) VALUES ('Beam', 'pcs', 150, 45)");

            stmt.execute("INSERT INTO test.product_description (key_word, description, product_id) VALUES ('support', 'Support beam for carport roof', 1)");

            stmt.execute("INSERT INTO test.product_variant (length, product_id) VALUES (4000, 1)");

            stmt.execute("INSERT INTO test.order_item (order_id, product_variant_id, quantity, product_description_id) VALUES (1, 1, 5, 1)");

        } catch (SQLException e) {
            e.printStackTrace();
            fail("Test data setup failed: " + e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        try (Connection conn = testConnectionPool.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SET search_path TO test");
            stmt.execute("TRUNCATE TABLE order_item, orders, users, product_variant, product_description, product, carport_dimension_website RESTART IDENTITY CASCADE");
        } catch (SQLException e) {
            fail("Test data cleanup failed: " + e.getMessage());
        }
    }

    @Test
    void createUser() throws DatabaseException {
        UserMapper.createUser("email@.com", "password", "11223344", "9000", "Testgade 2", "tester Jens", testConnectionPool);
    }

    @Test
    void createUserAndGetById() throws DatabaseException {
        //UserMapper.createUser("newuser@test.com", "password", "11223344", "9000", "Testgade 2", "tester Jens", testConnectionPool);
        User user = UserMapper.getUserById(2, testConnectionPool); // This assumes the inserted user gets ID 2
        assertNotNull(user);
        assertEquals("newuser@test.com", user.getEmail());
    }
    @Test
    void getCarportLength() {
        try (Connection conn = testConnectionPool.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SET search_path TO test");
            stmt.execute("INSERT INTO carport_dimension_website (carport_length) VALUES (500), (700), (600)");
        } catch (SQLException e) {
            fail("Setup for getCarportLength failed: " + e.getMessage());
        }

        try {
            List<Integer> carportLengths = OrderMapper.getCarportLength(testConnectionPool);
            assertTrue(carportLengths.containsAll(List.of(500, 600, 700)));
        } catch (DatabaseException e) {
            fail("DatabaseException: " + e.getMessage());
        }
    }


    @Test
    void insertOrderItem() throws DatabaseException {
        Product product = new Product(1, "Test Træ", "stk", 100);
        ProductVariant variant = new ProductVariant(1, 360, product);
        OrderItem item = new OrderItem(variant, 5, 1);

        OrderMapper.insertOrderItem(1, item, testConnectionPool);

        try (Connection conn = testConnectionPool.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SET search_path TO test");
            ResultSet rs = stmt.executeQuery("SELECT * FROM order_item WHERE order_id = 1 AND product_variant_id = 1");
            assertTrue(rs.next());
            assertEquals(5, rs.getInt("quantity"));
        } catch (SQLException e) {
            fail("Query failed: " + e.getMessage());
        }
    }

    @Test
    void getOrdersForUser_returnsOrdersForGivenUser() {
        try {
            List<OrderInfoDTO> orders = OrderMapper.getOrdersForUser(1, testConnectionPool);
            assertEquals(1, orders.size());
            assertEquals(600, orders.get(0).getCarportWidth());
        } catch (DatabaseException e) {
            fail("DatabaseException occurred: " + e.getMessage());
        }
    }
    @Test
    void updateOrderStatus() throws DatabaseException {
        int orderId = 1; // Known from @BeforeEach setup

        try (Connection conn = testConnectionPool.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("SET search_path TO test");
        } catch (SQLException e) {
            fail("Failed to set search_path: " + e.getMessage());
        }

        // Fetch existing order and confirm it exists
        Order existing = OrderMapper.getOrderByOrderId(orderId, testConnectionPool);
        assertNotNull(existing, "Expected order with ID " + orderId + " to exist");
        System.out.println("Status before update: " + existing.getStatus());

        // Update the order's status
        OrderMapper.updateOrderStatus(orderId, OrderStatus.CONFIRMED, testConnectionPool);

        // Fetch again and assert the status was updated
        Order updated = OrderMapper.getOrderByOrderId(orderId, testConnectionPool);
        System.out.println("Status after update: " + updated.getStatus());
        assertEquals(OrderStatus.CONFIRMED, updated.getStatus(), "Order status should be updated to CONFIRMED");
    }
    @Test
    void testGetAllOrders() throws DatabaseException {
        try (Connection conn = testConnectionPool.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("SET search_path TO test");
        } catch (SQLException e) {
            fail("Failed to set search_path: " + e.getMessage());
        }
        List<Order> orders = OrderMapper.getAllOrders(testConnectionPool);

        assertNotNull(orders);
        assertFalse(orders.isEmpty(), "Order list should not be empty");
        assertEquals(1, orders.size());

        Order order = orders.get(0);
        assertEquals(3, order.getOrderId());
        assertEquals(600, order.getCarportWidth());
        assertEquals(780, order.getCarportLength());
        assertEquals(OrderStatus.PENDING, order.getStatus());

        User user = order.getUser();
        assertNotNull(user);
        assertEquals(3, user.getUserId());
        assertEquals("tester Jens", user.getFullName());
        assertEquals("newuser@test.com", user.getEmail());
    }

}
