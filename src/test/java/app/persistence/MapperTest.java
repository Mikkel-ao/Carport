package app.persistence;

import app.entities.*;
import app.exceptions.DatabaseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


//TODO: Refactor to several mapper classes
class MapperTest {

    private static ConnectionPool testConnectionPool;

    @BeforeAll
    static void SetupClass(){
        try(Connection connection = testConnectionPool.getConnection()){

            try(Statement stmt = connection.createStatement()){

                //Deleting all tables and related sequences
                //Using normal Statements (and not "PreparedStatements") because it's only used internally
                stmt.execute("DROP TABLE IF EXISTS test.users");
                stmt.execute("DROP TABLE IF EXISTS test.orders");
                stmt.execute("DROP TABLE IF EXISTS test.product");
                stmt.execute("DROP TABLE IF EXISTS test.product_description");
                stmt.execute("DROP TABLE IF EXISTS test.product_variant");
                stmt.execute("DROP TABLE IF EXISTS test.order_item");
                stmt.execute("DROP SEQUENCE IF EXISTS test.users_user_id_seq");
                stmt.execute("DROP SEQUENCE IF EXISTS test.orders_order_id_seq");
                stmt.execute("DROP SEQUENCE IF EXISTS test.order_item_order_item_id_seq");
                stmt.execute("DROP SEQUENCE IF EXISTS test.product_description_description_id_seq");
                stmt.execute("DROP SEQUENCE IF EXISTS test.product_product_id_seq");
                stmt.execute("DROP SEQUENCE IF EXISTS test.product_variant_product_variant_id_seq");

                //Creating new tables (as a copy of the ones in the production database without the data)
                stmt.execute("CREATE TABLE test.users AS (SELECT * FROM public.users) WITH NO DATA");
                stmt.execute("CREATE TABLE test.orders AS (SELECT * FROM public.orders) WITH NO DATA");
                stmt.execute("CREATE TABLE test.product AS (SELECT * FROM public.product) WITH NO DATA");
                stmt.execute("CREATE TABLE test.product_variant AS (SELECT * FROM public.product_variant) WITH NO DATA");
                stmt.execute("CREATE TABLE test.order_item AS (SELECT * FROM public.order_item) WITH NO DATA");
                stmt.execute("CREATE TABLE test.product_description AS (SELECT * FROM public.product_description) WITH NO DATA");

                //Creating sequences and making them point to the wished columns
                stmt.execute("CREATE SEQUENCE test.users_user_user_id");
                stmt.execute("ALTER TABLE test.users ALTER COLUMN user_id SET DEFAULT nextval('test.users_user_id_seq')");
                stmt.execute("CREATE SEQUENCE test.orders_order_id_seq");
                stmt.execute("ALTER TABLE test.orders ALTER COLUMN order_id SET DEFAULT nextval('test.orders_order_id_seq')");
                stmt.execute("CREATE SEQUENCE test.order_item_order_item_id_seq");
                stmt.execute("ALTER TABLE test.order_item ALTER COLUMN order_item_id SET DEFAULT nextval('test.order_item_order_item_id_seq')");
                stmt.execute("CREATE SEQUENCE test.product_description_description_id_seq");
                stmt.execute("ALTER TABLE test.product_description ALTER COLUMN product_description_id SET DEFAULT nextval('test.product_description_description_id_seq')");
                stmt.execute("CREATE SEQUENCE test.product_product_id_seq");
                stmt.execute("ALTER TABLE test.product ALTER COLUMN product_id SET DEFAULT nextval('test.product_product_id_seq')");
                stmt.execute("CREATE SEQUENCE test.product_variant_product_variant_id_seq");
                stmt.execute("ALTER TABLE test.product_variant ALTER COLUMN product_variant_id SET DEFAULT nextval('test.product_variant_product_variant_id_seq')");


            }
        }
        catch (SQLException e){
            e.printStackTrace();
            fail("Database connection failed");
        }
    }

    @BeforeEach
    void setUp() {
        testConnectionPool = ConnectionPool.getInstance();

        try(Connection conn = testConnectionPool.getConnection()){

            String sql = "TRUNCATE TABLE test.orders, test.users, test.product_variant, test.order_item, test.product RESTART IDENTITY CASCADE";
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.executeUpdate();
            }
        } catch (SQLException e){
            fail(e.getMessage());
        }

    }

    @Test
    void createUser() {
        UserMapper.createUser("1234", "peter@hihi", testConnectionPool);
    }
    @Test
    void getAllOrders_returnsOrdersFromDatabase() {
        try (Connection conn = testConnectionPool.getConnection()) {

            // Insert test user
            String insertUser = "INSERT INTO test.users (email, password, role, phone_number) VALUES (?, ?, ?, ?) RETURNING user_id";
            int userId;
            try (PreparedStatement ps = conn.prepareStatement(insertUser)) {
                ps.setString(1, "test@example.com");
                ps.setString(2, "secret");
                ps.setString(3, "customer");
                ps.setString(4, "12345678");
                ps.execute();
                var rs = ps.getResultSet();
                rs.next();
                userId = rs.getInt("user_id");
            }

            // Insert test order
            String insertOrder = "INSERT INTO test.orders (user_id, carport_width, carport_length, status, total_price) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertOrder)) {
                ps.setInt(1, userId);
                ps.setInt(2, 600);
                ps.setInt(3, 780);
                ps.setString(4, "pending");
                ps.setInt(5, 15000);
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            fail("Test setup failed: " + e.getMessage());
        }

        // Call method and assert
        try {
            var orders = OrderMapper.getAllOrders(testConnectionPool);
            assertEquals(1, orders.size());

            Order order = orders.get(0);
            assertEquals("pending", order.getStatus());
            assertEquals(600, order.getCarportWidth());
            assertEquals(780, order.getCarportLength());
            assertEquals(15000, order.getTotalPrice());

            User user = order.getUser();
            assertEquals("test@example.com", user.getEmail());
            assertEquals("customer", user.getRole());

        } catch (DatabaseException e) {
            fail("getAllOrders failed: " + e.getMessage());
        }
    }
    @Test
    void getOrderItemsByOrderId_returnsCorrectItems() {
        try (Connection conn = testConnectionPool.getConnection()) {
            // Insert product
            String insertProduct = "INSERT INTO test.product (name, unit, price) VALUES ('Beam', 'pcs', 100) RETURNING product_id";
            int productId;
            try (PreparedStatement ps = conn.prepareStatement(insertProduct)) {
                ResultSet rs = ps.executeQuery();
                rs.next();
                productId = rs.getInt("product_id");
            }

            // Insert product_variant
            String insertVariant = "INSERT INTO test.product_variant (product_id, length) VALUES (?, 240) RETURNING product_variant_id";
            int variantId;
            try (PreparedStatement ps = conn.prepareStatement(insertVariant)) {
                ps.setInt(1, productId);
                ResultSet rs = ps.executeQuery();
                rs.next();
                variantId = rs.getInt("product_variant_id");
            }

            // Insert user
            String insertUser = "INSERT INTO test.users (email, password, role, phone_number) VALUES ('itemtest@user.com', 'pass', 'customer', '11111111') RETURNING user_id";
            int userId;
            try (PreparedStatement ps = conn.prepareStatement(insertUser)) {
                ResultSet rs = ps.executeQuery();
                rs.next();
                userId = rs.getInt("user_id");
            }

            // Insert order
            String insertOrder = "INSERT INTO test.orders (user_id, carport_width, carport_length, status, total_price) VALUES (?, 500, 600, 'pending', 3000) RETURNING order_id";
            int orderId;
            try (PreparedStatement ps = conn.prepareStatement(insertOrder)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                rs.next();
                orderId = rs.getInt("order_id");
            }

            // Insert order_item
            String insertItem = "INSERT INTO test.order_item (order_id, product_variant_id, quantity, description) VALUES (?, ?, 5, 'Main support beams')";
            try (PreparedStatement ps = conn.prepareStatement(insertItem)) {
                ps.setInt(1, orderId);
                ps.setInt(2, variantId);
                ps.executeUpdate();
            }

            // ✅ Call method
            List<OrderItem> items = OrderMapper.getOrderItemsByOrderId(orderId, testConnectionPool);

            // 🧪 Assertions
            assertEquals(1, items.size());
            OrderItem item = items.get(0);
            assertEquals("Main support beams", item.getDescription());
            assertEquals(5, item.getQuantity());
            assertEquals("Beam", item.getProductVariant().getProduct().getName());
            assertEquals(240, item.getProductVariant().getLength());
            assertEquals("pcs", item.getProductVariant().getProduct().getUnit());
            assertEquals(100, item.getProductVariant().getProduct().getPrice());
            assertEquals(500, item.getOrder().getCarportWidth());
            assertEquals("pending", item.getOrder().getStatus());

        } catch (SQLException | DatabaseException e) {
            fail("Test setup or execution failed: " + e.getMessage());
        }
    }

    @Test
    void insertOrder_insertsOrderAndReturnsWithGeneratedId() {
        try (Connection conn = testConnectionPool.getConnection()) {
            // Insert test user
            int userId;
            String insertUserSql = "INSERT INTO test.users (email, password, role, phone_number) VALUES (?, ?, ?, ?) RETURNING user_id";
            try (PreparedStatement ps = conn.prepareStatement(insertUserSql)) {
                ps.setString(1, "testingTom@gmail.com");
                ps.setString(2, "1234");
                ps.setString(3, "customer");
                ps.setString(4, "69696969");
                ps.execute();
                try (ResultSet rs = ps.getResultSet()) {
                    rs.next();
                    userId = rs.getInt("user_id");
                }
            }

            // Create User and Order objects
            User user = new User("1234", "testingTom@gmail.com", "69696969", "customer");
            user.setUserId(userId); // assuming setUserId exists
            Order order = new Order(0, 600, 800, "pending", user, 12000);

            // Insert order via mapper
            Order insertedOrder = OrderMapper.insertOrder(order, testConnectionPool);

            // Assertions
            assertNotNull(insertedOrder);
            assertTrue(insertedOrder.getOrderId() > 0);
            assertEquals("pending", insertedOrder.getStatus());
            assertEquals(600, insertedOrder.getCarportWidth());
            assertEquals(800, insertedOrder.getCarportLength());
            assertEquals(12000, insertedOrder.getTotalPrice());
            assertEquals(user.getEmail(), insertedOrder.getUser().getEmail());
        } catch (Exception e) {
            fail("insertOrder test failed: " + e.getMessage());
        }
    }
    @Test
    void insertOrderItems_insertsItemsCorrectly() {
        try (Connection conn = testConnectionPool.getConnection()) {

            // Insert a product
            String insertProduct = "INSERT INTO test.product (name, unit, price) VALUES ('Rafter', 'pcs', 150) RETURNING product_id";
            int productId;
            try (PreparedStatement ps = conn.prepareStatement(insertProduct)) {
                ResultSet rs = ps.executeQuery();
                rs.next();
                productId = rs.getInt("product_id");
            }

            // Insert a product_variant
            String insertVariant = "INSERT INTO test.product_variant (product_id, length) VALUES (?, 300) RETURNING product_variant_id";
            int variantId;
            try (PreparedStatement ps = conn.prepareStatement(insertVariant)) {
                ps.setInt(1, productId);
                ResultSet rs = ps.executeQuery();
                rs.next();
                variantId = rs.getInt("product_variant_id");
            }

            // Insert an order
            String insertOrder = "INSERT INTO test.orders (user_id, carport_width, carport_length, status, total_price) VALUES (1, 300, 400, 'pending', 2500) RETURNING order_id"; // Use a static user_id (e.g., 1) for simplicity
            int orderId;
            try (PreparedStatement ps = conn.prepareStatement(insertOrder)) {
                ResultSet rs = ps.executeQuery();
                rs.next();
                orderId = rs.getInt("order_id");
            }

            // Prepare OrderItem
            Product product = new Product(productId, "Rafter", "pcs", 150);
            ProductVariant variant = new ProductVariant(variantId, 300, product);
            Order order = new Order(orderId, 300, 400, "pending", new User(1, "pass", "test@user.com", "12345678", "customer"), 2500);
            OrderItem item = new OrderItem(0, order, variant, 10, "Roof rafters");

            List<OrderItem> itemList = List.of(item);

            // ✅ Call method
            OrderMapper.insertOrderItems(itemList, testConnectionPool);

            // 🧪 Assert from DB
            String query = "SELECT * FROM test.order_item WHERE order_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, orderId);
                ResultSet rs = ps.executeQuery();

                assertTrue(rs.next(), "Expected order_item to exist");
                assertEquals(orderId, rs.getInt("order_id"));
                assertEquals(variantId, rs.getInt("product_variant_id"));
                assertEquals(10, rs.getInt("quantity"));
                assertEquals("Roof rafters", rs.getString("description"));
            }

        } catch (SQLException | DatabaseException e) {
            fail("Test failed due to: " + e.getMessage());
        }
    }
    @Test
    void testUpdateOrderStatus() {
        try (Connection conn = testConnectionPool.getConnection()) {

            // Insert a test order
            String insertOrder = "INSERT INTO test.orders (user_id, carport_width, carport_length, status, total_price) " +
                    "VALUES (1, 300, 400, 'pending', 2500) RETURNING order_id";
            int orderId;
            try (PreparedStatement ps = conn.prepareStatement(insertOrder)) {
                ResultSet rs = ps.executeQuery();
                rs.next();
                orderId = rs.getInt("order_id");
            }

            // Update the order status to 'completed'
            OrderMapper.updateOrderStatus(orderId, "completed", testConnectionPool);

            // Verify the update
            String query = "SELECT status FROM test.orders WHERE order_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, orderId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    assertEquals("completed", rs.getString("status"));
                } else {
                    fail("Order not found in the database");
                }
            }

        } catch (SQLException | DatabaseException e) {
            fail("Test failed due to: " + e.getMessage());
        }
    }

    @Test
    void deleteCancelledOrder_deletesOnlyCancelledOrders() {
        try (Connection conn = testConnectionPool.getConnection()) {

            // Insert test order with status 'cancelled'
            String insertOrder = "INSERT INTO test.orders (user_id, carport_width, carport_length, status, total_price) " +
                    "VALUES (1, 300, 400, 'cancelled', 2500) RETURNING order_id";
            int orderId;
            try (PreparedStatement ps = conn.prepareStatement(insertOrder)) {
                ResultSet rs = ps.executeQuery();
                rs.next();
                orderId = rs.getInt("order_id");
            }

            // Delete the cancelled order
            OrderMapper.deleteCancelledOrder(orderId, testConnectionPool);

            // Verify deletion
            String select = "SELECT * FROM test.orders WHERE order_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(select)) {
                ps.setInt(1, orderId);
                ResultSet rs = ps.executeQuery();
                assertFalse(rs.next(), "Expected order to be deleted");
            }

        } catch (SQLException | DatabaseException e) {
            fail("Test failed due to: " + e.getMessage());
        }
    }
    @Test
    void getVariantsByProductAndMinLength_returnsCorrectVariants() {
        try (Connection conn = testConnectionPool.getConnection()) {

            // First insert a test product
            String insertProduct = "INSERT INTO test.product (name, unit, price) VALUES ('Spær', 'stk', 100) RETURNING product_id";
            int productId;
            try (PreparedStatement ps = conn.prepareStatement(insertProduct)) {
                ResultSet rs = ps.executeQuery();
                rs.next();
                productId = rs.getInt("product_id");
            }

            // Insert two product_variants, one above and one below the minLength threshold
            String insertVariant = "INSERT INTO test.product_variant (product_id, length) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertVariant)) {
                ps.setInt(1, productId);
                ps.setInt(2, 150); // Below threshold
                ps.executeUpdate();

                ps.setInt(2, 250); // Meets threshold
                ps.executeUpdate();
            }

            int minLength = 200;

            // Call method under test
            List<ProductVariant> variants = ProductMapper.getVariantsByProductAndMinLength(minLength, productId, testConnectionPool);

            // Assertions
            assertNotNull(variants, "List should not be null");
            assertEquals(1, variants.size(), "Only one variant should match the condition");
            assertTrue(variants.get(0).getLength() >= minLength, "Returned variant length should meet minLength condition");

        } catch (SQLException | DatabaseException e) {
            fail("Test failed due to: " + e.getMessage());
        }
    }
    @Test
    void updatePrice_updatesTotalPriceOfOrder() {
        try (Connection conn = testConnectionPool.getConnection()) {

            // Insert test order with initial price
            String insertOrder = "INSERT INTO test.orders (user_id, carport_width, carport_length, status, total_price) " +
                    "VALUES (1, 300, 400, 'pending', 2500) RETURNING order_id";
            int orderId;
            try (PreparedStatement ps = conn.prepareStatement(insertOrder)) {
                ResultSet rs = ps.executeQuery();
                rs.next();
                orderId = rs.getInt("order_id");
            }

            // Update the total price
            int newPrice = 3200;
            OrderMapper.UpdatePrice(newPrice, orderId, testConnectionPool);

            // Verify the update
            String select = "SELECT total_price FROM test.orders WHERE order_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(select)) {
                ps.setInt(1, orderId);
                ResultSet rs = ps.executeQuery();
                assertTrue(rs.next(), "Expected order to exist");
                assertEquals(newPrice, rs.getInt("total_price"), "Total price should be updated");
            }

        } catch (SQLException | DatabaseException e) {
            fail("Test failed due to: " + e.getMessage());
        }
    }


    @AfterEach
    void tearDown() {
        try(Connection conn = testConnectionPool.getConnection()){

            String sql = "TRUNCATE TABLE test.orders, test.users, test.product_variant, test.order_item, test.product RESTART IDENTITY CASCADE";
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.executeUpdate();
            }
        } catch (SQLException e){
            fail(e.getMessage());
        }
    }
}