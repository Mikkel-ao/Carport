package app.persistence;

import app.entities.*;
import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderMapper {


    public static List<Order> getAllOrders(ConnectionPool connectionPool) throws DatabaseException {
        List<Order> orderList = new ArrayList<>();
        String sql = "SELECT * FROM orders inner join users using (user_id)";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);

                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String email = rs.getString("email");
                String password = rs.getString("password");
                String phoneNumber = rs.getString("phone_number");
                String role = rs.getString("role");
                int orderId = rs.getInt("order_id");
                int carportWidth = rs.getInt("carport_width");
                int carportLength = rs.getInt("carport_length");
                String status = rs.getString("status");
                int totalPrice = rs.getInt("total_price");

                User user = new User(userId, password, email, phoneNumber, role);
                Order order = new Order(orderId, carportWidth, carportLength, status, user, totalPrice);

                orderList.add(order);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke få fat på bruger fra database", e.getMessage());
        }
        return orderList;
    }

    // Used to create Material List for customer.
    public static List<OrderItem> getOrderItemsByOrderId(int orderId, ConnectionPool connectionPool) throws DatabaseException {
        List<OrderItem> orderItemList = new ArrayList<>();
        String sql = "SELECT * FROM complete_product_view WHERE order_id = ?";
        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);

        ) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                //Order
                int carportWidth = rs.getInt("carport_width");
                int carportLength = rs.getInt("carport_length");
                String status = rs.getString("status");
                int totalPrice = rs.getInt("total_price");
                Order order = new Order(orderId, carportWidth, carportLength, status, null, totalPrice);

                //Product
                int productId = rs.getInt("product_id");
                String name = rs.getString("name");
                String unit = rs.getString("unit");
                int price = rs.getInt("price");
                Product product = new Product(productId, name, unit, price);

                //Product Variant
                int productVariantId = rs.getInt("product_variant_id");
                int length = rs.getInt("length");
                String description = rs.getString("description");
                ProductVariant productVariant = new ProductVariant(productVariantId, length, product);

                //OrderItem
                int orderItemId = rs.getInt("order_item_id");
                int quantity = rs.getInt("quantity");
                OrderItem orderItem = new OrderItem(orderItemId, order, productVariant, quantity, description);
                orderItemList.add(orderItem);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke få fat på bruger fra database", e.getMessage());
        }
        return orderItemList;
    }

    public static Order insertOrder(Order order, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "INSERT INTO orders (carport_width, carport_length, status, user_id, total_price)" + "VALUES (?,?,?,?,?)";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, order.getCarportWidth());
            ps.setInt(2, order.getCarportLength());
            ps.setString(3, order.getStatus());
            ps.setInt(4, order.getUser().getUserId());
            ps.setInt(5, order.getTotalPrice());
            ps.executeUpdate();
            ResultSet keySet = ps.getGeneratedKeys();
            if (keySet.next()) {
                return new Order(keySet.getInt(1), order.getCarportWidth(), order.getCarportLength(), order.getStatus(), order.getUser(), order.getTotalPrice());
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke indsætte order i Database", e.getMessage());
        }
    }

    public static void insertOrderItems(List<OrderItem> orderItems, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "INSERT INTO order_item (order_id, product_variant_id, quantity, description)" + "VALUES (?,?,?,?)";

        try (Connection connection = connectionPool.getConnection()) {
            for (OrderItem orderItem : orderItems) {
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, orderItem.getOrder().getOrderId());
                    ps.setInt(2, orderItem.getProductVariant().getProductVariantId());
                    ps.setInt(3, orderItem.getQuantity());
                    ps.setString(4, orderItem.getDescription());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke lave en orderitem i database", e.getMessage());
        }
    }

    // Måske vi skal lave 3 mapper metoder. En til hver af de 3 stadier status kan have (annulleret, venter, købt)??
    public static void updateOrderStatus(int orderId, String newStatus, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            // Set the parameters for the UPDATE query
            ps.setString(1, newStatus);  // Set the new status
            ps.setInt(2, orderId);  // Set the order_id to target the right record

            // Execute the update
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated == 0) {
                throw new DatabaseException("Ingen linjer opdateret for ordrer med id: " + orderId);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved opdatering af database", e.getMessage());
        }
    }

    public static void deleteCancelledOrder(int orderId, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "DELETE FROM orders WHERE order_id = ? AND status = 'cancelled'";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            int rowsDeleted = ps.executeUpdate();

            if (rowsDeleted == 0) {
                throw new DatabaseException("Ingen order slettet. Enten findes ordreren ikke eller den står ikke som cancelled");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved fjernelsen af cancelled ordre", e.getMessage());
        }
    }

    public static void UpdatePrice(int newPrice, int orderId, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "UPDATE orders SET total_price = ? WHERE order_id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, newPrice);
            ps.setInt(2, orderId);

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated == 0) {
                throw new DatabaseException("Ingen linjer opdateret for ordrer med id: " + orderId);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved opdatering af database", e.getMessage());
        }
    }

    public static List<Integer> getProductLengths(ConnectionPool connectionPool, int productId) throws DatabaseException {


        List<Integer> lengths = new ArrayList<>();
        String sql = "SELECT length FROM public.product p JOIN public.product_variant pv ON p.product_id = pv.product_id WHERE p.product_id = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();


            while (rs.next()) {
                lengths.add(rs.getInt("length"));
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved hentning af længder", e.getMessage());
        }
        return lengths;
    }

    public static double getProductWidth(ConnectionPool connectionPool, int productId) throws DatabaseException {


        double widthInMM = 0;

        String sql = "SELECT width_in_mm FROM public.product WHERE product_id = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();



            if(rs.next()) {
                widthInMM = rs.getDouble("width_in_mm");
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved hentning af bredder", e.getMessage());
        }
        double widthInCm = widthInMM / 10;

        return widthInCm;
    }
}
