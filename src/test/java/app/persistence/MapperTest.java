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
            stmt.execute("ALTER TABLE users ALTER COLUMN role SET DEFAULT 'customer'");
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
                       pd.description_id
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
            // IMPORTANT: set search_path every time!
            stmt.execute("SET search_path TO test");
            // Truncate all tables to clean database
            stmt.execute("TRUNCATE TABLE order_item, orders, users, product_variant, product_description, product, carport_dimension_website RESTART IDENTITY CASCADE");
            // Insert base user
            stmt.execute("""
            INSERT INTO users (user_id, email, password, phone_number, role, zip_code, home_address, full_name)
            VALUES (1, 'email@test.com', '1234', '42424242', 'customer', '2770', 'testvej', 'tester Jens')
        """);
            // Insert base order
            stmt.execute("""
            INSERT INTO orders (order_id, carport_width, carport_length, status, user_id, customer_price, cost_price, order_date)
            VALUES (1, 600, 780, 'pending', 1, 8000, 10000, CURRENT_TIMESTAMP)
        """);
            // Insert carport dimension
            stmt.execute("""
            INSERT INTO carport_dimension_website (carport_dimension_id, carport_length, carport_width)
            VALUES (1, 780, 600)
        """);
            // Insert product
            stmt.execute("""
            INSERT INTO product (product_id, name, unit, price, width_in_mm)
            VALUES (1, 'Stolpe', 'stk', 220, 97)
        """);
            // Insert product description
            stmt.execute("""
            INSERT INTO product_description (description_id, description, product_id)
            VALUES (1, 'Stolper af trykimprægneret egetræ på 97x97mm.', 1)
        """);
            // Insert product variant
            stmt.execute("""
            INSERT INTO product_variant (product_variant_id, length, product_id)
            VALUES (1, 300, 1)
        """);
            // Insert order item
            stmt.execute("""
            INSERT INTO order_item (order_item_id, order_id , product_variant_id, quantity, product_description_id)
            VALUES (1, 1, 1, 6, 1)
        """);
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Database setup failed: " + e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        try (Connection conn = testConnectionPool.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("SET search_path TO test");

            stmt.execute("TRUNCATE TABLE order_item, orders, users, product_variant, product_description, product, carport_dimension_website RESTART IDENTITY CASCADE");

        } catch (SQLException e) {
            e.printStackTrace();
            fail("Test data cleanup failed: " + e.getMessage());
        }
    }


    @Test
    void createUser() throws DatabaseException {
        UserMapper.createUser("tester@gmail.com", "password", "11223344", "9000", "Testgade 2", "tester Jens", testConnectionPool);
        User user = UserMapper.getUserById(2, testConnectionPool); // This assumes the inserted user gets ID 2
        assertEquals("tester@gmail.com", user.getEmail());
    }

    @Test
    void getUserById() throws DatabaseException {
        User user = UserMapper.getUserById(1, testConnectionPool); // This assumes the inserted user gets ID 2
        assertNotNull(user);
        assertEquals("email@test.com", user.getEmail());
    }

    @Test
    void testGetAllOrders() throws DatabaseException {
        List<Order> orders = OrderMapper.getAllOrders(testConnectionPool);

        assertNotNull(orders);
        assertFalse(orders.isEmpty(), "Order list should not be empty");
        assertEquals(1, orders.size());

        Order order = orders.get(0);
        assertEquals(1, order.getOrderId());
        assertEquals(600, order.getCarportWidth());
        assertEquals(780, order.getCarportLength());
        assertEquals(OrderStatus.PENDING, order.getStatus());

        User user = order.getUser();
        assertNotNull(user);
        assertEquals(1, user.getUserId());
        assertEquals("email@test.com", user.getEmail());
    }

    @Test
    void testGetCarportLength() throws DatabaseException {
        List<Integer> lengths = OrderMapper.getCarportLength(testConnectionPool);
        // Check if all inserted lengths are present
        assertTrue(lengths.containsAll(List.of(780)), "Returned lengths should contain all inserted values");
        // Optionally, check size exactly matches
        assertEquals(1, lengths.size(), "There should be exactly 3 lengths returned");
    }

    /*
    @Test
    void insertOrderItem() throws DatabaseException {
        OrderItem item =

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
    */


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

        // Update the order's status
        OrderMapper.updateOrderStatus(orderId, OrderStatus.CONFIRMED, testConnectionPool);

        // Fetch again and assert the status was updated
        Order updated = OrderMapper.getOrderByOrderId(orderId, testConnectionPool);
        System.out.println("Status after update: " + updated.getStatus());
        assertEquals(OrderStatus.CONFIRMED, updated.getStatus(), "Order status should be updated to CONFIRMED");
    }

    @Test
    void getProductLengths_returnsAllLengthsForProduct() {
        int productId = 1;
        try {
            List<Integer> lengths = OrderMapper.getProductLengths(testConnectionPool, productId);
            // Assert the list has expected size and value
            assertNotNull(lengths, "Returned list should not be null");
            assertEquals(1, lengths.size(), "Expected exactly one length");
            assertEquals(300, lengths.get(0), "Expected length 4000 for productId 1");

        } catch (DatabaseException e) {
            fail("DatabaseException occurred: " + e.getMessage());
        }
    }

}

