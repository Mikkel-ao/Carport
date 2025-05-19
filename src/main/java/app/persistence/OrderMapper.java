package app.persistence;

import app.DTO.OrderInfoDTO;
import app.entities.*;
import app.exceptions.DatabaseException;
import app.util.OrderStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderMapper {


    //Method for retrieving all orders from the database and returning them in a List of Order object
    public static List<Order> getAllOrders(ConnectionPool connectionPool) throws DatabaseException {
        List<Order> orderList = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY order_id ASC";

        //"try-with-resources" block that makes sure to auto close after usage!
        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            //Retrieving all data needed to create an instance of a User and Order by iterating through each row of the database!
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                int orderId = rs.getInt("order_id");
                int carportWidth = rs.getInt("carport_width");
                int carportLength = rs.getInt("carport_length");
                OrderStatus status = OrderStatus.valueOf(rs.getString("status").toUpperCase());
                double customerPrice = rs.getDouble("customer_price");
                double costPrice = rs.getDouble("cost_price");
                Timestamp timestamp = rs.getTimestamp("order_date");

                User user = UserMapper.getUserById(userId, connectionPool);
                Order order = new Order(orderId, carportWidth, carportLength, status, user, customerPrice, costPrice, timestamp);

                orderList.add(order);
            }
            //Catching the SQLException and converting it to a DatabaseException and throw it to the caller of this method
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke hente ordrerne fra databasen!", e.getMessage());
        }
        return orderList;
    }


    //Method used for getting a specific Order object (which contains the "list of materials" and much more!)
    public static Order getOrderByOrderId(int orderId, ConnectionPool connectionPool) throws DatabaseException {

        List<OrderItem> orderItemList = new ArrayList<>();
        Order order = null;

        String sql = "SELECT * FROM complete_order_view WHERE order_id = ?";

        //"try-with-resources" block that makes sure to auto close after usage!
        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)

        ) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    //Making sure to only create one Order object, as the data retrieved from each iteration will always be the same
                    if (order == null) {
                        int userId = rs.getInt("user_id");
                        int carportWidth = rs.getInt("carport_width");
                        int carportLength = rs.getInt("carport_length");
                        OrderStatus status = OrderStatus.valueOf(rs.getString("status").toUpperCase());
                        double customerPrice = rs.getDouble("customer_price");
                        double costPrice = rs.getDouble("cost_price");
                        Timestamp timestamp = rs.getTimestamp("order_date");

                        User user = UserMapper.getUserById(userId, connectionPool);
                        order = new Order(orderId, carportWidth, carportLength, status, user, customerPrice, costPrice, timestamp);
                    }

                    //Iterating throw each row of the database first creating a Product, then a ProductVariant and at last an OrderItem
                    //Product
                    int productId = rs.getInt("product_id");
                    String name = rs.getString("name");
                    String unit = rs.getString("unit");
                    int pricePrUnit = rs.getInt("price");
                    Product product = new Product(productId, name, unit, pricePrUnit);

                    //Product Variant
                    int productVariantId = rs.getInt("product_variant_id");
                    int length = rs.getInt("length");
                    ProductVariant productVariant = new ProductVariant(productVariantId, length, product);

                    //OrderItem
                    int orderItemId = rs.getInt("order_item_id");
                    int quantity = rs.getInt("quantity");
                    String description = rs.getString("description");
                    int descriptionId = rs.getInt("description_id");
                    OrderItem orderItem = new OrderItem(orderItemId, productVariant, quantity, description, descriptionId);
                    orderItemList.add(orderItem);
                }
            }
            //Setting the list of materials (which is a list of OrderItem objects) to the specific Order before returning it!
            if (order != null) {
                order.setListOfMaterials(orderItemList);
            }
            return order;
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke få fat på ordren fra databasen", e.getMessage());
        }
    }


    //Method for inserting the "list of materials" into the database, by inserting one OrderItem at a time in the "order_item" table with a reference to the specific order_id!
    public static void insertOrderItem(int orderId, OrderItem orderItem, ConnectionPool connectionPool) throws DatabaseException {

        String sql = "INSERT INTO order_item (order_id, product_variant_id, quantity, product_description_id)" + "VALUES (?,?,?,?)";

        //"try-with-resources" block that makes sure to auto close after usage!
        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)

        ) {
            ps.setInt(1, orderId);
            ps.setInt(2, orderItem.getProductVariant().getProductVariantId());
            ps.setInt(3, orderItem.getQuantity());
            ps.setInt(4, orderItem.getDescriptionId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke indsætte det ønskede data i database", e.getMessage());
        }
    }

    //Method for updating the order status of a specific order
    public static void updateOrderStatus(int orderId, OrderStatus newStatus, ConnectionPool connectionPool) throws DatabaseException {

        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";

        //"try-with-resources" block that makes sure to auto close after usage!
        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {

            ps.setString(1, newStatus.name());  // Set the new status
            ps.setInt(2, orderId);  // Set the order_id to target the right record
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved opdatering af ordrestatus i database", e.getMessage());
        }
    }


    //Method for updating the price of a specific order
    public static void UpdatePrice(double newPrice, int orderId, ConnectionPool connectionPool) throws DatabaseException {

        String sql = "UPDATE orders SET customer_price = ? WHERE order_id = ?";

        //"try-with-resources" block that makes sure to auto close after usage!
        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {

            ps.setDouble(1, newPrice);
            ps.setInt(2, orderId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved opdatering af pris i databasen", e.getMessage());
        }
    }

    //Method for getting all available lengths of a specific Product and returning them in a List!
    public static List<Integer> getProductLengths(ConnectionPool connectionPool, int productId) throws DatabaseException {
        List<Integer> lengths = new ArrayList<>();

        String sql = "SELECT length FROM public.product p JOIN public.product_variant pv ON p.product_id = pv.product_id WHERE p.product_id = ?";

        //"try-with-resources" block that makes sure to auto close after usage!
        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {

            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {

                //Iterating through the ResultSet (each row of the database table) and adding the length to the List
                while (rs.next()) {
                    lengths.add(rs.getInt("length"));
                }

            }
            return lengths;
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke hente produktlængder fra databasen!", e.getMessage());
        }
    }

    //Method for getting the all available lengths of carports for the customer to choose between on the web page, and returning them in a List!
    public static List<Integer> getCarportLength(ConnectionPool connectionPool) throws DatabaseException {
        String sql = "SELECT carport_length FROM carport_dimension_website";
        List<Integer> carportLengthList = new ArrayList<>();

        //"try-with-resources" block that makes sure to auto close after usage!
        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            //Iterating through the ResultSet (each row of the database table) and adding the carport length to the List
            while (rs.next()) {
                int carportLength = rs.getInt("carport_length");
                carportLengthList.add(carportLength);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke hente carport dimensioner!", e.getMessage());
        }

        return carportLengthList;
    }

    //Method for getting the all available widths of carports for the customer to choose between on the web page, and returning them in a List!
    public static List<Integer> getCarportWidth(ConnectionPool connectionPool) throws DatabaseException {
        String sql = "SELECT carport_width FROM carport_dimension_website";
        List<Integer> carportWidthList = new ArrayList<>();

        //"try-with-resources" block that makes sure to auto close after usage!
        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            //Iterating through the ResultSet (each row of the database table) and adding the carport width to the List
            while (rs.next()) {
                int carportWidth = rs.getInt("carport_width");
                carportWidthList.add(carportWidth);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke hente carport dimensioner!", e.getMessage());
        }
        return carportWidthList;
    }


    //Method for creating an order and retrieve the auto-generated order id from the database!
    public static int createOrder(ConnectionPool connectionPool, int width, int length, int userId, double customerPrice, double costPrice) throws DatabaseException {

        String sql = "INSERT INTO orders (carport_width, carport_length, user_id, customer_price, cost_price, order_date) VALUES (?, ?, ?, ?, ?,?)";

        //"try-with-resources" block that makes sure to auto close after usage!
        try (
                Connection connection = connectionPool.getConnection();
                //Making the auto-generated id ready for retrieval!
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            ps.setInt(1, width);
            ps.setInt(2, length);
            ps.setInt(3, userId);
            ps.setDouble(4, customerPrice);
            ps.setDouble(5, costPrice);
            //Setting "order_date" in the table to the current time!
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();

            //Retrieving the auto-generated id from the database
            try (ResultSet rs = ps.getGeneratedKeys()) {
                //Check if the id was in fact retrieved!
                if (rs.next()) {
                    //Returning the id, which is in the first column of the table!
                    return rs.getInt(1);
                } else {
                    throw new DatabaseException("Kunne ikke oprette ordren, da ingen genereret nøgle blev fundet!");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke oprette ordren!", e.getMessage());
        }
    }


    //Method for getting all orders for a specific user returned in a List of OrderInfoDTO, as we do not want the customer to be able to see all information (at least before they have paid)!
    public static List<OrderInfoDTO> getOrdersForUser(int userId, ConnectionPool connectionPool) throws
            DatabaseException {
        String sql = "SELECT * FROM public.orders WHERE user_id = ? ORDER BY order_id ASC";
        List<OrderInfoDTO> orderList = new ArrayList<>();

        //"try-with-resources" block that makes sure to auto close after usage!
        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                //Iterating through the ResultSet (each row of the database table) and adding the DTO width to the List
                while (rs.next()) {
                    int orderId = rs.getInt("order_id");
                    int carportWidth = rs.getInt("carport_width");
                    int carportLength = rs.getInt("carport_length");
                    OrderStatus status = OrderStatus.valueOf(rs.getString("status").toUpperCase());
                    double customerPrice = rs.getDouble("customer_price");
                    double costPrice = rs.getDouble("cost_price");
                    Timestamp timestamp = rs.getTimestamp("order_date");

                    OrderInfoDTO order = new OrderInfoDTO(orderId, carportWidth, carportLength, status, customerPrice, costPrice, timestamp);
                    orderList.add(order);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke hente ordrerne fra databasen!", e.getMessage());
        }
        return orderList;
    }

    //Method for getting the correct Product Variant
    public static ProductVariant getVariantsByProductAndLength(int productLength, int productId, ConnectionPool connectionPool) throws DatabaseException {

        String sql = "SELECT * FROM public.complete_product_view\n" +
                "WHERE product_id = ? AND length = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, productId);
            ps.setInt(2, productLength);

            try (ResultSet rs = ps.executeQuery()) {
                //Checking whether a row in the database exists and if "true" returns the ProductVariant!
                if (rs.next()) {
                    int variantId = rs.getInt("product_variant_id");
                    int product_id = rs.getInt("product_id");
                    int length = rs.getInt("length");
                    String name = rs.getString("name");
                    String unit = rs.getString("unit");
                    int price = rs.getInt("price");
                    double width = rs.getDouble("width_in_mm") / 10;


                    Product product = new Product(product_id, name, unit, width, price);
                    ProductVariant productVariant = new ProductVariant(variantId, length, product);
                    return productVariant;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke hente produkt variant fra databasen!", e.getMessage());
        }
        return null;
    }
}
