package app.persistence;

import app.DTO.OrderInfoDTO;
import app.entities.*;
import app.entities.Order;
import app.exceptions.DatabaseException;
import app.enums.OrderStatus;
import app.util.Calculator;
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
            // Sets search_path EVERY time
            stmt.execute("SET search_path TO test");
            // Truncate all tables to clean database
            stmt.execute("TRUNCATE TABLE order_item, orders, users, product_variant, product_description, product, carport_dimension_website RESTART IDENTITY CASCADE");
            // Fills certain tables with data from the public database.
            stmt.execute("INSERT INTO test.carport_dimension_website SELECT * FROM public.carport_dimension_website;");
            stmt.execute("INSERT INTO test.product SELECT * FROM public.product;");
            stmt.execute("INSERT INTO test.product_description SELECT * FROM public.product_description;");
            stmt.execute("INSERT INTO test.product_variant SELECT * FROM public.product_variant;");
            stmt.execute("INSERT INTO test.order_item SELECT * FROM public.order_item;");

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
    void testGetUserById1() throws DatabaseException {
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
        // Check if all inserted lengths are present.
        assertTrue(lengths.containsAll(List.of(240,270,300,330,360,390,420,450,480,510,540,600,630,660,690,720,750,780)), "Returned lengths should contain all inserted values");
        // Optionally, check size exactly matches
        assertEquals(18, lengths.size(), "There should be exactly 3 lengths returned");
    }

    @Test
    void testGetCarportWidth() throws DatabaseException {
        List<Integer> lengths = OrderMapper.getCarportWidth(testConnectionPool);
        // Check if all inserted lengths are present.
        assertTrue(lengths.containsAll(List.of(240,270,300,330,360,390,420,450,480,510,540,600)), "Returned lengths should contain all inserted values");
        // Optionally, check size exactly matches // TODO: Null values in width column
        assertEquals(18, lengths.size(), "There should be exactly 3 lengths returned");
    }

    @Test
    void testInsertOrderItem() throws DatabaseException {
        // Create OrderItem - pass 0 or null for ID if it's auto-generated
        ProductVariant poleVariant = OrderMapper.getVariantsByProductAndLength(300, 1, testConnectionPool);
        OrderItem item = new OrderItem(poleVariant , 8, 1);

        OrderMapper.insertOrderItem(2, item, testConnectionPool);

        try (Connection conn = testConnectionPool.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("SET search_path TO test");

            ResultSet rs = stmt.executeQuery(
                    "SELECT * FROM order_item WHERE order_id = 1 AND product_variant_id = " + poleVariant.getProductVariantId()
            );

            assertTrue(rs.next(), "Order item should exist in the database");
            assertEquals(8, rs.getInt("quantity"), "Quantity should be 8");
            assertEquals(1, rs.getInt("product_description_id"), "Product description ID should be 1");
            assertEquals(poleVariant.getProductVariantId(), rs.getInt("product_variant_id"), "ProductVariant ID should match");

            assertFalse(rs.next(), "Only one matching order item should exist");

        } catch (SQLException e) {
            fail("Query failed: " + e.getMessage());
        }
    }


    @Test
    void testGetOrderByOrderId1() throws DatabaseException {
        int orderId = 1;

        Order order = OrderMapper.getOrderByOrderId(orderId, testConnectionPool);
        assertNotNull(order, "Order should not be null");
        // Checking if the orderId and carport dimensions matches the ones created in setup
        assertEquals(1, order.getOrderId(), "Order id should be 1");
        assertEquals(780, order.getCarportLength(), "Carport width should be 780");
        assertEquals(600, order.getCarportWidth(), "Carport width should be 600");
    }

    @Test
    void testGetOrdersForUserWithId1() throws DatabaseException {
        List<OrderInfoDTO> orders = OrderMapper.getOrdersForUser(1, testConnectionPool);
        // Checking if the orderId and carport dimensions matches the ones created in setup
        assertEquals(1, orders.size(), "Should return exactly one order");
        assertEquals(600, orders.get(0).getCarportWidth(), "Carport width should be 600");
        assertEquals(780, orders.get(0).getCarportLength(), "Carport length should be 780");
    }


    @Test
    void testUpdateOrderStatus() throws DatabaseException {
        int orderId = 1; // Known from @BeforeEach setup

        // Update the order's status
        OrderMapper.updateOrderStatus(orderId, OrderStatus.CONFIRMED, testConnectionPool);

        // Fetch again and assert the status was updated
        Order updated = OrderMapper.getOrderByOrderId(orderId, testConnectionPool);
        System.out.println("Status after update: " + updated.getStatus());
        assertEquals(OrderStatus.CONFIRMED, updated.getStatus(), "Order status should be updated to CONFIRMED");
    }

    @Test
    void testGetProductLengthsForPole() throws DatabaseException {
        int productId = 1;
        try {
            List<Integer> lengths = OrderMapper.getProductLengths(testConnectionPool, productId);
            // Assert the list has expected size and value
            assertNotNull(lengths, "Returned list should not be null");
            assertEquals(1, lengths.size(), "Expected exactly one length");
            assertEquals(300, lengths.get(0), "Expected length 300 for productId 1");

        } catch (DatabaseException e) {
            fail("DatabaseException occurred: " + e.getMessage());
        }
    }

    @Test
    void getProductLengthsForRafter() throws DatabaseException {
        int productId = 2;
        try {
            List<Integer> lengths = OrderMapper.getProductLengths(testConnectionPool, productId);
            assertNotNull(lengths, "Returned list should not be null");
            assertEquals(6, lengths.size(), "Expected 6 different lengths");
            // Checks for all the different rafter lengths
            assertEquals(300, lengths.get(0), "Expected length 300 for the first product_variant_id");
            assertEquals(360, lengths.get(1), "Expected length 300 for the second product_variant_id");
            assertEquals(420, lengths.get(2), "Expected length 300 for the third product_variant_id");
            assertEquals(480, lengths.get(3), "Expected length 300 for the fourth product_variant_id");
            assertEquals(540, lengths.get(4), "Expected length 300 for the fifth product_variant_id");
            assertEquals(600, lengths.get(5), "Expected length 600 for the sixth product_variant_id");

        } catch (DatabaseException e) {
            fail("DatabaseException occurred: " + e.getMessage());
        }
    }

    @Test
    void testGetVariantsByProductAndLengthPost() throws DatabaseException {
        // productId 1 = 'Stolpe' and should only have 1 length which is 300
        int productId = 1;
        int length = 300;

        ProductVariant variant = OrderMapper.getVariantsByProductAndLength(length, productId, testConnectionPool);

        assertNotNull(variant, "ProductVariant should not be null");
        assertEquals(length, variant.getLength(), "Length should match");
        assertEquals(productId, variant.getProduct().getProductId(), "Product ID should match");

        Product product = variant.getProduct();
        assertNotNull(product, "Product should not be null");
        assertEquals("Stolpe", product.getName(), "Product name should match expected");
    }

    @Test
    void testGetVariantsByProductAndLengthRafter() throws DatabaseException {
        // productId 1 = 'Spær' and should multiple potential lengths
        int productId = 2;
        int[] lengths = {300, 360, 420, 480, 540, 600};

        for (int length : lengths) {
            ProductVariant variant = OrderMapper.getVariantsByProductAndLength(length, productId, testConnectionPool);

            assertNotNull(variant, "ProductVariant should not be null for length: " + length);
            assertEquals(length, variant.getLength(), "Length should match for length: " + length);
            assertEquals(productId, variant.getProduct().getProductId(), "Product ID should match for length: " + length);

            Product product = variant.getProduct();
            assertNotNull(product, "Product should not be null for length: " + length);
            assertEquals("Spær", product.getName(), "Product name should match expected for length: " + length);
        }
    }



}

