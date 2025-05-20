package app.persistence;

import app.DTO.OrderInfoDTO;
import app.entities.*;
import app.exceptions.DatabaseException;
import app.service.OrderStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapperTest {

    private static ConnectionPool testConnectionPool;

    @BeforeAll
    static void SetupClass() {
        testConnectionPool = ConnectionPool.getInstance();

        try (Connection connection = testConnectionPool.getConnection();
             Statement stmt = connection.createStatement()) {

            // Drop dependent views first
            stmt.execute("DROP VIEW IF EXISTS test.complete_order_view_test");

            // Drop all tables
            stmt.execute("DROP TABLE IF EXISTS test.order_item CASCADE");
            stmt.execute("DROP TABLE IF EXISTS test.orders CASCADE");
            stmt.execute("DROP TABLE IF EXISTS test.product_variant CASCADE");
            stmt.execute("DROP TABLE IF EXISTS test.product_description CASCADE");
            stmt.execute("DROP TABLE IF EXISTS test.product CASCADE");
            stmt.execute("DROP TABLE IF EXISTS test.users CASCADE");
            stmt.execute("DROP TABLE IF EXISTS test.carport_dimension_website CASCADE");

            // Drop all sequences
            stmt.execute("DROP SEQUENCE IF EXISTS test.order_item_order_item_id_seq CASCADE");
            stmt.execute("DROP SEQUENCE IF EXISTS test.orders_order_id_seq CASCADE");
            stmt.execute("DROP SEQUENCE IF EXISTS test.product_variant_product_variant_id_seq CASCADE");
            stmt.execute("DROP SEQUENCE IF EXISTS test.product_description_description_id_seq CASCADE");
            stmt.execute("DROP SEQUENCE IF EXISTS test.product_product_id_seq CASCADE");
            stmt.execute("DROP SEQUENCE IF EXISTS test.users_user_id_seq CASCADE");
            stmt.execute("DROP SEQUENCE IF EXISTS test.carport_dimension_website_carport_dimension_id_seq CASCADE");

            // Recreate tables with structure from public schema
            stmt.execute("CREATE TABLE test.users (LIKE public.users INCLUDING CONSTRAINTS INCLUDING INDEXES)");
            stmt.execute("CREATE TABLE test.orders (LIKE public.orders INCLUDING CONSTRAINTS INCLUDING INDEXES)");
            stmt.execute("CREATE TABLE test.product (LIKE public.product INCLUDING CONSTRAINTS INCLUDING INDEXES)");
            stmt.execute("CREATE TABLE test.product_description (LIKE public.product_description INCLUDING CONSTRAINTS INCLUDING INDEXES)");
            stmt.execute("CREATE TABLE test.product_variant (LIKE public.product_variant INCLUDING CONSTRAINTS INCLUDING INDEXES)");
            stmt.execute("CREATE TABLE test.order_item (LIKE public.order_item INCLUDING CONSTRAINTS INCLUDING INDEXES)");
            stmt.execute("CREATE TABLE test.carport_dimension_website (LIKE public.carport_dimension_website INCLUDING CONSTRAINTS INCLUDING INDEXES)");
            stmt.execute("ALTER TABLE test.orders ALTER COLUMN status SET DEFAULT 'pending'");

            // Recreate sequences
            stmt.execute("CREATE SEQUENCE test.users_user_id_seq");
            stmt.execute("ALTER TABLE test.users ALTER COLUMN user_id SET DEFAULT nextval('test.users_user_id_seq')");

            stmt.execute("CREATE SEQUENCE test.orders_order_id_seq");
            stmt.execute("ALTER TABLE test.orders ALTER COLUMN order_id SET DEFAULT nextval('test.orders_order_id_seq')");

            stmt.execute("CREATE SEQUENCE test.product_product_id_seq");
            stmt.execute("ALTER TABLE test.product ALTER COLUMN product_id SET DEFAULT nextval('test.product_product_id_seq')");

            stmt.execute("CREATE SEQUENCE test.product_description_description_id_seq");
            stmt.execute("ALTER TABLE test.product_description ALTER COLUMN description_id SET DEFAULT nextval('test.product_description_description_id_seq')");

            stmt.execute("CREATE SEQUENCE test.product_variant_product_variant_id_seq");
            stmt.execute("ALTER TABLE test.product_variant ALTER COLUMN product_variant_id SET DEFAULT nextval('test.product_variant_product_variant_id_seq')");

            stmt.execute("CREATE SEQUENCE test.order_item_order_item_id_seq");
            stmt.execute("ALTER TABLE test.order_item ALTER COLUMN order_item_id SET DEFAULT nextval('test.order_item_order_item_id_seq')");

            stmt.execute("CREATE SEQUENCE test.carport_dimension_website_carport_dimension_id_seq");
            stmt.execute("ALTER TABLE test.carport_dimension_website ALTER COLUMN carport_dimension_id SET DEFAULT nextval('test.carport_dimension_website_carport_dimension_id_seq')");


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
                 JOIN test.product_description pd ON pd.description_id = oi.product_description_id;
        """);
            // Optionally recreate views (example, if needed)
            // stmt.execute("CREATE VIEW test.complete_order_view_test AS SELECT * FROM ...");

        } catch (SQLException e) {
            e.printStackTrace();
            fail("Database connection/setup failed: " + e.getMessage());
        }
    }


    @BeforeEach
    void setUp() {
        try (Connection connection = testConnectionPool.getConnection();
             Statement stmt = connection.createStatement()) {

            stmt.execute("DELETE FROM test.order_item");
            stmt.execute("DELETE FROM test.orders");
            stmt.execute("DELETE FROM test.users");

            // Insert base user
            stmt.execute("""
                INSERT INTO test.users (user_id, email, password, phone_number, role, zip_code, home_address, full_name)
                VALUES (1, 'email@test.com', '1234', '123456', 'customer', '8000', 'testvej', 'tester Jens')
            """);

            // Insert base order
            stmt.execute("""
                INSERT INTO test.orders (carport_width, carport_length, user_id, customer_price, cost_price, order_date)
                VALUES (600, 780, 1, 2000, 1000, CURRENT_TIMESTAMP)
            """);
            stmt.execute("""
        INSERT INTO test.carport_dimension_website (carport_length, carport_width)
        VALUES (780, 600)
    """);

            // Insert into product
            stmt.execute("""
        INSERT INTO test.product (name, unit, price, width_in_mm)
        VALUES ('Beam', 'pcs', 150, 45)
    """);

            // Insert into product_description
            stmt.execute("""
        INSERT INTO test.product_description (key_word, description, product_id)
        VALUES ('support', 'Support beam for carport roof', 1)
    """);

            // Insert into product_variant
            stmt.execute("""
        INSERT INTO test.product_variant (length, product_id)
        VALUES (4000, 1)
    """);

            // Insert into order_item
            stmt.execute("""
        INSERT INTO test.order_item (order_id, product_variant_id, quantity, product_description_id)
        VALUES (1, 1, 6, 1)
    """);

            // Reset sequences
           // stmt.execute("SELECT setval('test.orders_order_id_seq', COALESCE((SELECT MAX(order_id) + 1 FROM test.orders), 1), false)");
            stmt.execute("SELECT setval('test.users_user_id_seq', COALESCE((SELECT MAX(user_id) + 1 FROM test.users), 1), false)");

        } catch (SQLException e) {
            e.printStackTrace();
            fail("Database setup failed: " + e.getMessage());
        }
    }

    @Test
    void createUser() {
        UserMapper.createUser("newuser@test.com", "password", "11223344", "9000", "Testgade 2", "tester Jens", testConnectionPool);
    }

    @Test
    void getUserById() {
        int userId = 1;
        User user = UserMapper.getUserById(userId,testConnectionPool);

        assertNotNull(user);
        assertEquals(userId, user.getUserId());
        assertEquals("email@test.com", user.getEmail());
        assertEquals("123456", user.getPhoneNumber());
        assertEquals("8000", user.getZipCode());
        assertEquals("testvej", user.getAddress());
        assertEquals("tester Jens", user.getFullName());

    }

    @Test
    void getCarportLength() {
        try (Connection conn = testConnectionPool.getConnection()) {
            String sql = "INSERT INTO test.carport_dimension_website (carport_length) VALUES (500), (600), (700)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.executeUpdate();
            }

            List<Integer> carportLengths = OrderMapper.getCarportLength(testConnectionPool);

            assertNotNull(carportLengths);
            assertEquals(3, carportLengths.size());
            assertTrue(carportLengths.contains(500));
            assertTrue(carportLengths.contains(600));
            assertTrue(carportLengths.contains(700));
        } catch (SQLException e) {
            fail("Failed to set up test data: " + e.getMessage());
        }
    }

    @Test
    void updateOrderStatus() {
        // Arrange: Verify the order exists before the update
        Order existingOrder = OrderMapper.getOrderByOrderId(1, testConnectionPool);
        assertNotNull(existingOrder, "Order should exist before updating status");

        // Act: Call the method to update the order status
        OrderMapper.updateOrderStatus(1, OrderStatus.CONFIRMED, testConnectionPool);

        // Act: Retrieve the updated order from the database
        Order updatedOrder = OrderMapper.getOrderByOrderId(1, testConnectionPool);

        // Assert: Verify that the order status is updated correctly
        assertNotNull(updatedOrder, "Updated order should not be null");
        assertEquals(OrderStatus.CONFIRMED, updatedOrder.getStatus());
    }

    @Test
    void updateOrderStatus1() {
        // Arrange: Insert a test order into the test.orders table
        try (Connection connection = testConnectionPool.getConnection();
             Statement stmt = connection.createStatement()) {
            stmt.execute("""
            INSERT INTO test.orders (carport_width, carport_length, user_id, customer_price, cost_price, order_date)
            VALUES (600, 780, 1, 2000, 1000, CURRENT_TIMESTAMP)
        """);
        } catch (SQLException e) {
            fail("Failed to insert test order: " + e.getMessage());
        }

        // Verify the order exists before the update
        Order existingOrder = OrderMapper.getOrderByOrderId(1, testConnectionPool);
        assertNotNull(existingOrder, "Order should exist before updating status");

        // Act: Call the method to update the order status
        OrderMapper.updateOrderStatus(1, OrderStatus.CONFIRMED, testConnectionPool);

        // Retrieve the updated order from the database
        Order updatedOrder = OrderMapper.getOrderByOrderId(1, testConnectionPool);

        // Assert: Verify that the order status is updated correctly
        assertNotNull(updatedOrder, "Updated order should not be null");
        assertEquals(OrderStatus.CONFIRMED, updatedOrder.getStatus());
    }
    @Test
    void insertOrderItem() {
        Product product = new Product(1, "Test Træ", "stk", 100);
        ProductVariant variant = new ProductVariant(1, 360, product);
        OrderItem orderItem = new OrderItem(variant, 5, 1);

        OrderMapper.insertOrderItem(1, orderItem, testConnectionPool);

        try (Connection conn = testConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM test.order_item WHERE order_id = 1 AND product_variant_id = 1")) {

            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next());
            assertEquals(1, rs.getInt("order_id"));
            assertEquals(1, rs.getInt("product_variant_id"));
            assertEquals(5, rs.getInt("quantity"));
            assertEquals(1, rs.getInt("product_description_id"));

        } catch (SQLException e) {
            fail("Query failed: " + e.getMessage());
        }
    }
    @Test
    void getOrdersForUser_returnsOrdersForGivenUser() {
        try {
            List<OrderInfoDTO> orders = OrderMapper.getOrdersForUser(1, testConnectionPool);

            assertNotNull(orders);
            assertEquals(1, orders.size());

            OrderInfoDTO order = orders.get(0);
            assertEquals(600, order.getCarportWidth());
            assertEquals(780, order.getCarportLength());
            assertEquals(OrderStatus.PENDING, order.getStatus());
            assertEquals(2000, order.getCustomerPrice());
            assertEquals(1000, order.getCostPrice());

        } catch (DatabaseException e) {
            fail("DatabaseException occurred: " + e.getMessage());
        }
    }


    @AfterEach
    void tearDown() {
        try (Connection conn = testConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "TRUNCATE TABLE test.orders, test.users, test.product_variant, test.order_item, test.product RESTART IDENTITY CASCADE")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            fail("Failed to tear down test data: " + e.getMessage());
        }
    }
}
