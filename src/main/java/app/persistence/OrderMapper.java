package app.persistence;

import app.DTO.OrderInfoDTO;
import app.entities.*;
import app.exceptions.DatabaseException;
import app.util.OrderStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderMapper {

    public static List<Order> getOrdersByStatus(int userId, String role, OrderStatus currentStatus, ConnectionPool connectionPool) throws DatabaseException {

        List<Order> orderList = new ArrayList<>();

        User user = null;

        user = UserMapper.getUserById(userId, connectionPool);

        String sql;

        if("admin".equals(role)) {
            sql = "select * from orders where status=?";
        } else if("customer".equals(role)) {
            sql = "select * from orders where status=? and user_id=?";
        } else {
            throw new DatabaseException("Invalid role");
        }
        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);

        ){ if("admin".equals(role)) {
            ps.setString(1, currentStatus.name());
        }else {
            ps.setString(1, currentStatus.name());
            ps.setInt(2, userId);
        }
        ResultSet rs = ps.executeQuery();
            while (rs.next()){
                int orderId = rs.getInt("order_id");
                int carportWidth = rs.getInt("carport_width");
                int carportLength = rs.getInt("carport_length");
                OrderStatus status = OrderStatus.valueOf(rs.getString("status").toUpperCase());
                double customerPrice = rs.getDouble("customer_price");
                double costPrice = rs.getDouble("cost_price");
                Timestamp timestamp = rs.getTimestamp("order_date");

                orderList.add(new Order(orderId, carportWidth, carportLength, status, user, customerPrice, costPrice, timestamp));
            }
        }
        catch (SQLException e){
            throw new DatabaseException("Could not get orders from database!", e.getMessage());
        }
        return orderList;

    }

    public static List<Order> getOrdersByRole(int userId, String role, ConnectionPool connectionPool) throws DatabaseException {

        User user = null;

        user = UserMapper.getUserById(userId, connectionPool);

        List<Order> orderList = new ArrayList<>();
        String sql;

        if("admin".equals(role)) {
            sql = "select * from orders";
        } else if("customer".equals(role)) {
            sql = "select * from orders where user_id = ?";
        } else {
            throw new DatabaseException("Invalid role!");
        }

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);

        ){ if("customer".equals(role)) {
            ps.setInt(1, userId);
        }
        ResultSet rs = ps.executeQuery();
            while (rs.next()){
                int orderId = rs.getInt("order_id");
                int carportWidth = rs.getInt("carport_width");
                int carportLength = rs.getInt("carport_length");
                OrderStatus status = OrderStatus.valueOf(rs.getString("status").toUpperCase());
                double customerPrice = rs.getDouble("customer_price");
                double costPrice = rs.getDouble("cost_price");
                Timestamp timestamp = rs.getTimestamp("order_date");

                orderList.add(new Order(orderId, carportWidth, carportLength, status, user, customerPrice, costPrice, timestamp));
            }
        }
        catch (SQLException e){
            throw new DatabaseException("Could not get orders from database!", e.getMessage());
        }
        return orderList;
    }


    public static List<Order> getAllOrders(ConnectionPool connectionPool)throws DatabaseException {
        List<Order> orderList = new ArrayList<>();
        String sql = "SELECT * FROM orders inner join users using (user_id)";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);

                ResultSet rs = ps.executeQuery()
        ){
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String email = rs.getString("email");
                String password = rs.getString("password");
                String phoneNumber = rs.getString("phone_number");
                String role = rs.getString("role");
                int orderId = rs.getInt("order_id");
                int carportWidth = rs.getInt("carport_width");
                int carportLength = rs.getInt("carport_length");
                OrderStatus status = OrderStatus.valueOf(rs.getString("status").toUpperCase());
                double customerPrice = rs.getDouble("customer_price");
                double costPrice = rs.getDouble("cost_price");
                Timestamp timestamp = rs.getTimestamp("order_date");

                User user = new User(userId, password, email, phoneNumber, role);
                Order order = new Order(orderId, carportWidth, carportLength, status, user, customerPrice, costPrice, timestamp);

                orderList.add(order);
            }
        }catch (SQLException e){
            throw new DatabaseException("Kunne ikke få fat på bruger fra database", e.getMessage());
        }
        return orderList;
    }



    // Used to create the list of materials for the customer.
    public static Order getOrderByOrderId(int orderId, ConnectionPool connectionPool) throws DatabaseException{
        List<OrderItem> orderItemList = new ArrayList<>();
        Order order = null;

        String sql = "SELECT * FROM complete_order_view WHERE order_id = ?";
        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);

        ){
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                if(order == null) {
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
        }catch (SQLException e){
            throw new DatabaseException("Kunne ikke få fat på bruger fra database", e.getMessage());
        }
        if(order != null) {
            order.setListOfMaterials(orderItemList);
        }
        return order;
    }


    public static void insertOrderItem(int orderId, OrderItem orderItem, ConnectionPool connectionPool)throws DatabaseException{
        String sql = "INSERT INTO order_item (order_id, product_variant_id, quantity, product_description_id)" + "VALUES (?,?,?,?)";

        try(Connection connection = connectionPool.getConnection()){
                try (PreparedStatement ps = connection.prepareStatement(sql)){
                    ps.setInt(1, orderId);
                    ps.setInt(2, orderItem.getProductVariant().getProductVariantId());
                    ps.setInt(3, orderItem.getQuantity());
                    ps.setInt(4, orderItem.getDescriptionId());
                    ps.executeUpdate();

            }
        }catch (SQLException e){
            throw new DatabaseException("Kunne ikke lave en orderItem i database", e.getMessage());
        }
    }

    // Måske vi skal lave 3 mapper metoder. En til hver af de 3 stadier status kan have (annulleret, venter, købt)??
    public static void updateOrderStatus(int orderId, OrderStatus newStatus, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            // Set the parameters for the UPDATE query
            ps.setString(1, newStatus.name());  // Set the new status
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
    public static void deleteCancelledOrder(int orderId, ConnectionPool connectionPool) throws DatabaseException{
        String sql = "DELETE FROM orders WHERE order_id = ? AND status = 'cancelled'";

        try(Connection connection = connectionPool.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)){

            ps.setInt(1, orderId);
            int rowsDeleted = ps.executeUpdate();

            if (rowsDeleted == 0){
                throw new DatabaseException("Ingen order slettet. Enten findes ordreren ikke eller den står ikke som cancelled");
            }
        }catch (SQLException e){
            throw new DatabaseException("Fejl ved fjernelsen af cancelled ordre", e.getMessage());
        }
    }
    public static void UpdatePrice(int newPrice, int orderId, ConnectionPool connectionPool)throws DatabaseException{
        String sql = "UPDATE orders SET total_price = ? WHERE order_id = ?";

        try(Connection connection = connectionPool.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)){

            ps.setInt(1, newPrice);
            ps.setInt(2, orderId);

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated == 0){
                throw new DatabaseException("Ingen linjer opdateret for ordrer med id: " + orderId);
            }
        }catch (SQLException e){
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

    public static List<Integer> getCarportLength(ConnectionPool connectionPool)throws DatabaseException{
        String sql = "SELECT carport_length FROM carport_dimension_website";
        List<Integer> carportLengthList = new ArrayList<>();
        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);

                ResultSet rs = ps.executeQuery()
        ){
            while (rs.next()){
               int carportLength = rs.getInt("carport_length");
               carportLengthList.add(carportLength);
            }
        }catch (SQLException e){
            throw new DatabaseException("Fejl ved hentning af længde", e.getMessage());
        }

        return carportLengthList;
    }
    public static List<Integer> getCarportWidth(ConnectionPool connectionPool)throws DatabaseException{
        String sql = "SELECT carport_width FROM carport_dimension_website";
        List<Integer> carportWidthList = new ArrayList<>();
        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);

                ResultSet rs = ps.executeQuery()
        ){
            while (rs.next()){
                int carportWidth = rs.getInt("carport_width");
                carportWidthList.add(carportWidth);
            }
        }catch (SQLException e){
            throw new DatabaseException("Fejl ved hentning af bredde", e.getMessage());
        }
        return carportWidthList;
    }

    public static double getProductPrice(int productId, ConnectionPool connectionPool) throws DatabaseException{
        String sql = "SELECT price FROM public.product WHERE product_id = ?";

        double price = 0;

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();



            if(rs.next()) {
                price = rs.getDouble("price");
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved hentning af bredder", e.getMessage());
        }

        return price;
    }

    //Method for creating an order and retrieve the auto-generated order id
    public static int createOrder(ConnectionPool connectionPool, int width, int length, int userId, double customerPrice, double costPrice) throws DatabaseException {
        String sql = "INSERT INTO orders (carport_width, carport_length, user_id, customer_price, cost_price, order_date) VALUES (?, ?, ?, ?, ?,?)";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) { //Instructs database to return auto-generated keys (user_id) when executing insert statement!

            ps.setInt(1, width);
            ps.setInt(2, length);
            ps.setInt(3, userId);
            ps.setDouble(4, customerPrice);
            ps.setDouble(5, costPrice);
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis())); //Setting "order_date" in sql query (which is datatype TimeStamp) to the current time!
            ps.executeUpdate(); //Executing sql query

            ResultSet rs = ps.getGeneratedKeys(); //Retrieving the auto-generated key from the database
            if (rs.next()) { //Check if they key was in fact returned!
                return rs.getInt(1); //Returning key of the first column in database (which is the order id)
            } else {
                throw new DatabaseException("Kunne ikke oprette ordren, da ingen genereret nøgle blev fundet!");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke oprette ordren!", e.getMessage());
        }
    }

    // Method for getting object with content resembling columns in orders table
    public static OrderInfoDTO getOrderInfo(int orderId, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "SELECT * FROM public.orders WHERE order_id = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int carportWidth = rs.getInt("carport_width");
                int carportLength = rs.getInt("carport_length");
                OrderStatus status = OrderStatus.valueOf(rs.getString("status").toUpperCase());
                double customerPrice = rs.getDouble("customer_price");
                double costPrice = rs.getDouble("cost_price");

                return new OrderInfoDTO(orderId, carportLength, carportWidth, status, customerPrice, costPrice);
            } else {
                throw new DatabaseException("Order with ID " + orderId + " not found.");
            }

        } catch (SQLException e) {
            throw new DatabaseException("Could not retrieve order from database", e);
        }
    }

    // Method for getting user's orders in a way that resembles the columns from orders table
    public static List<OrderInfoDTO> getOrdersForUser(int userId, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "SELECT * FROM orders WHERE user_id = ?";
        List<OrderInfoDTO> orderList = new ArrayList<>();

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int orderId = rs.getInt("order_id");
                    int carportWidth = rs.getInt("carport_width");
                    int carportLength = rs.getInt("carport_length");
                    OrderStatus status = OrderStatus.valueOf(rs.getString("status").toUpperCase());
                    double customerPrice = rs.getDouble("customer_price");
                    double costPrice = rs.getDouble("cost_price");

                    OrderInfoDTO order = new OrderInfoDTO(orderId, carportWidth, carportLength, status, customerPrice, costPrice);
                    orderList.add(order);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not retrieve orders from database", e);
        }

        return orderList;
    }
}
