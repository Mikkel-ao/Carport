package app.controllers;

import app.DTO.OrderInfoDTO;
import app.DTO.OrderItemAndPrice;
import app.entities.*;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.OrderMapper;
import app.persistence.ProductMapper;
import app.persistence.UserMapper;
import app.service.EmailService;
import app.util.Calculator;
import app.util.OrderStatus;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.sql.SQLException;
import java.util.*;

public class OrderController {

    public static void addRoutes(Javalin app, ConnectionPool connectionPool) {
        app.get("/index", ctx -> getCarportDimensions(ctx, connectionPool));

        app.post("/add-customer-request", ctx -> {
            createListOfMaterials(ctx, connectionPool);
            ctx.redirect("/add-customer-request?success=true");
        });
        app.get("/add-customer-request", ctx -> ctx.render("index.html"));

        app.get("/orderdetails/{orderId}", ctx -> showListOfMaterials(ctx, connectionPool));

        app.post("/updatePrice", ctx -> updatePrice(ctx, connectionPool));

        app.post("/sendOffer", ctx -> sendOffer(ctx, connectionPool));

        app.post("/payOrder", ctx -> payOrder(ctx, connectionPool));


    }

    private static void getCarportDimensions(Context ctx, ConnectionPool connectionPool) {
        List<Integer> carportLength = OrderMapper.getCarportLength(connectionPool);
        List<Integer> carportWidth = OrderMapper.getCarportWidth(connectionPool);

        ctx.attribute("carportLength", carportLength);
        ctx.attribute("carportWidth", carportWidth);
        ctx.render("index.html");
    }

    public static OrderItemAndPrice getPoleOrderItemAndPrice(int carportLength, ConnectionPool connectionPool) {


        //product length 300 is hard-coded as we do not need to loop through the different lengths as we only have one type of pole
        ProductVariant poleVariant = ProductMapper.getVariantsByProductAndLength(300, 1, connectionPool);
        int poleCount = Calculator.calcAmountOfPoles(carportLength, poleVariant.getProduct().getWidth());
        OrderItem poleOrderItem = new OrderItem(poleVariant, poleCount, 1);

        double price = poleCount * poleVariant.getProduct().getPricePrUnit();

        OrderItemAndPrice poleOrderItemAndPrice = new OrderItemAndPrice(poleOrderItem, price);

        return poleOrderItemAndPrice;

    }

    public static OrderItemAndPrice getRafterOrderItemAndPrice(int carportLength, int carportWidth, ConnectionPool connectionPool) {

        //This is the lengths of each rafter (not to be confused with the width of the carport)
        List<Integer> possibleLengths = OrderMapper.getProductLengths(connectionPool, 2);

        //Making sure that the order is correct and ascending
        Collections.sort(possibleLengths);

        int actualLength = 0;

        for (int possibleLength : possibleLengths) {
            if (possibleLength >= carportWidth) {
                actualLength = possibleLength;
                break;
            }
        }

        ProductVariant rafterVariant = ProductMapper.getVariantsByProductAndLength(actualLength, 2, connectionPool);

        //Caluclating the amount of rafters needed for the wished length of the carport!
        int rafterCount = Calculator.calcAmountOfRafters(carportLength, rafterVariant.getProduct().getWidth());

        OrderItem rafterOrderItem = new OrderItem(rafterVariant, rafterCount,2);

        //Calculating the price with the given length of every single rafter, the count of rafters and the unit price (in meters)
        double price = (double) rafterVariant.getProduct().getPricePrUnit() * rafterCount * rafterVariant.getLength() / 100;

        OrderItemAndPrice orderItemAndPrice = new OrderItemAndPrice(rafterOrderItem, price);

        return orderItemAndPrice;
    }


    public static List<OrderItemAndPrice> getBeamOrderItemAndPrice(int carportLength, ConnectionPool connectionPool) {

        List<OrderItemAndPrice> orderItemAndPriceList = new ArrayList<>();
        List<Integer> possibleLengths = OrderMapper.getProductLengths(connectionPool, 2);


        Collections.sort(possibleLengths);

        int actualLength;

        if (carportLength <= 600) {
            for (int possibleLength : possibleLengths) {
                if (possibleLength >= carportLength) {
                    actualLength = possibleLength;
                    ProductVariant beamVariant = ProductMapper.getVariantsByProductAndLength(actualLength, 2, connectionPool);
                    if (beamVariant != null) {
                        //Hard-coding the quantity to 2, because we are sure we only need one beam in each side of the carport!
                        OrderItem beamOrderItem = new OrderItem(beamVariant, 2, 3);
                        double price = 2 * (double) beamVariant.getProduct().getPricePrUnit() * beamVariant.getLength() / 100;
                        orderItemAndPriceList.add(new OrderItemAndPrice(beamOrderItem, price));
                    }
                    break;
                }

            }
        } else {
            //Hard-coding the length on one of the beams (on each side) to 360, because we then know we will be able to reach all the different lengths up to the maximum length of 780cm!
            int firstBeamLength = 360;
            ProductVariant firstBeamVariant = ProductMapper.getVariantsByProductAndLength(firstBeamLength, 2, connectionPool);
            //If the length of the carport exceeds 600cm, we know we will need two beams on each side of the carport!
            if (firstBeamVariant != null) {
                OrderItem firstBeamOrderItem = new OrderItem(firstBeamVariant, 2, 3);
                double firstBeamPrice = 2 * (double) firstBeamVariant.getProduct().getPricePrUnit() * firstBeamVariant.getLength() / 100;
                orderItemAndPriceList.add(new OrderItemAndPrice(firstBeamOrderItem, firstBeamPrice));
            }
            //Figuring out what variant the second beam needs to be
            for (int secondBeamLength : possibleLengths) {
                if (firstBeamLength + secondBeamLength >= carportLength) {
                    ProductVariant secondBeamVariant = ProductMapper.getVariantsByProductAndLength(secondBeamLength, 2, connectionPool);
                    if (secondBeamVariant != null) {
                        OrderItem secondBeamOrderItem = new OrderItem(secondBeamVariant, 2, 3);
                        double secondBeamPrice = 2 * (double) secondBeamVariant.getProduct().getPricePrUnit() * secondBeamVariant.getLength() / 100;
                        orderItemAndPriceList.add(new OrderItemAndPrice(secondBeamOrderItem, secondBeamPrice));
                    }
                    break;
                }
            }
        }
        return orderItemAndPriceList;
    }


    //TODO: Maybe in ProductMapper instead?
    public static Order createListOfMaterials(Context ctx, ConnectionPool connectionPool) throws SQLException {


        //TODO: Nye navne på disse to ints
        int userLength = Integer.parseInt(ctx.formParam("Længde"));
        int userWidth = Integer.parseInt(ctx.formParam("Bredde"));

        Integer userId = ctx.sessionAttribute("userId");
        if (userId == null) {
            throw new IllegalStateException("User not logged in or session expired");
        }

        User loggedInUser = UserMapper.getUserById(userId, connectionPool);


        List<OrderItem> listOfMaterials = new ArrayList<>();


        //Adding poles to the list of materials
        OrderItemAndPrice poleData = getPoleOrderItemAndPrice(userLength, connectionPool);
        OrderItem poleOrderItem = poleData.getOrderItem();
        listOfMaterials.add(poleOrderItem);
        double poleCostPrice = poleData.getPrice();


        //Adding rafters to the list of materials
        OrderItemAndPrice rafterData = getRafterOrderItemAndPrice(userLength, userWidth, connectionPool);
        //Retrieving the order item from the DTO
        OrderItem rafterOrderItem = rafterData.getOrderItem();
        //Adding rafter order items to the list!
        listOfMaterials.add(rafterOrderItem);
        //Retrieving the price from the DTO
        double rafterCostPrice = rafterData.getPrice();

        //Adding beams to the list of materials
        List<OrderItemAndPrice> beamData = getBeamOrderItemAndPrice(userLength, connectionPool);
        double beamCostPrice = 0;
        for (OrderItemAndPrice beamOrderItemAndPrice : beamData) {
            listOfMaterials.add(beamOrderItemAndPrice.getOrderItem());
            beamCostPrice += beamOrderItemAndPrice.getPrice();
        }

        double totalCostPrice = poleCostPrice + rafterCostPrice + beamCostPrice;
        double totalCustomerPrice = totalCostPrice * 1.39;

        //ctx.sessionAttribute("listOfMaterials", listOfMaterials);
        //TODO: Måske overload konstruktør til Order, så man ikke behøver status? Denne er først relevant, når den bliver sendt til db (som defaulter til "pending")!
        Order currentOrder = new Order(listOfMaterials, userWidth, userLength, OrderStatus.PENDING, loggedInUser, totalCustomerPrice, totalCostPrice);

        saveOrder(currentOrder, connectionPool);

        return currentOrder;
    }


    //TODO: Denne skal kaldes, når brugeren har accepteret de indtastet mål (og evt set SVG), så ordren bliver smidt i databasen
    public static void saveOrder(Order order, ConnectionPool connectionPool) {

        //TODO: Måske noget validering på at denne liste findes?
        List<OrderItem> listOfMaterials = order.getListOfMaterials();

        int orderId = OrderMapper.createOrder(connectionPool, order.getCarportWidth(), order.getCarportLength(), order.getUser().getUserId(), order.getTotalSalesPrice(), order.getCostPrice());

        for (OrderItem orderItem : listOfMaterials) {
            OrderMapper.insertOrderItem(orderId, orderItem, connectionPool);
        }

    }

    // TODO: Routing for this method is currently in UserController - Two get requests
    public static void getOrderDetails(Context ctx, ConnectionPool connectionPool) {
        Integer userId = ctx.sessionAttribute("userId");

        try {
            List<OrderInfoDTO> orders = OrderMapper.getOrdersForUser(userId, connectionPool);
            ctx.attribute("orders", orders);
        } catch (DatabaseException e) {
            ctx.attribute("message", "Could not retrieve order details.");
            ctx.render("/login.html");
        }
    }

    // Method that displays all orders for Admin user
    public static void showAllOrders(Context ctx, ConnectionPool connectionPool) {
        Integer userId = ctx.sessionAttribute("userId");

        try {
            User user = UserMapper.getUserById(userId, connectionPool);
            if (user.getRole().equalsIgnoreCase("admin")) {
                List<Order> orders = OrderMapper.getAllOrders(connectionPool);
                ctx.attribute("orders", orders);
                ctx.render("admin.html");
            } else {
                ctx.status(403).result("Access denied: Admins only.");
            }
        } catch (DatabaseException e) {
            ctx.attribute("message", "Could not retrieve orders.");
            ctx.render("/index.html");
        }
    }

    public static void showListOfMaterials(Context ctx, ConnectionPool connectionPool) {
        int orderId = Integer.parseInt(ctx.pathParam("orderId"));

        Order order = OrderMapper.getOrderByOrderId(orderId, connectionPool);

        List<OrderItem> orderDetails = order.getListOfMaterials();

        ctx.attribute("orderDetails", orderDetails);
        ctx.attribute("orderId", orderId);
        ctx.render("orderdetails.html");
    }

    //TODO: Muligvis overflødge metoder
    //This method is used for updating the status of an order (done by the admin/seller).
    public static void changeStatus(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        String newStatus = ctx.formParam("newStatus");
        int orderId = Integer.parseInt(ctx.formParam("orderId"));

        OrderStatus status = null;
        try {
            if ("confirmed".equalsIgnoreCase(newStatus)) {
                status = OrderStatus.CONFIRMED;
            } else if ("rejected".equalsIgnoreCase(newStatus)) {
                status = OrderStatus.REJECTED;
            }
            OrderMapper.updateOrderStatus(orderId, status, connectionPool);
        } catch (DatabaseException e) {
            ctx.attribute("message","Could not update status on order: " + orderId + "\n" + e.getMessage());
        }
    }
    //This method is used for when admin/seller has confirmed the customers order.
    // Now the customer can accept/buy and the status will update accordingly.
    public static void updateToPaid(Context ctx, ConnectionPool connectionPool) throws DatabaseException{
        String newStatus = ctx.formParam("newStatus");
        int orderId = Integer.parseInt(ctx.formParam("orderId"));

        OrderStatus status = null;
        try {
            if ("paid".equalsIgnoreCase(newStatus)) {
                status = OrderStatus.PAID;
            }
            OrderMapper.updateOrderStatus(orderId, status, connectionPool);
        } catch (DatabaseException e) {
            ctx.attribute("message","Could not update status on order: " + orderId + "\n" + e.getMessage());
        }
    }

    public static void updatePrice(Context ctx, ConnectionPool connectionPool) throws DatabaseException {

        int orderId = Integer.parseInt(ctx.formParam("orderId"));

        double newPrice = Double.parseDouble(ctx.formParam("newPrice"));

        OrderMapper.UpdatePrice(newPrice, orderId, connectionPool);

        //TODO: TRY-CATCH AND BETTER REDIRECT
        ctx.redirect("/admin");
    }

    public static void sendOffer(Context ctx, ConnectionPool connectionPool) throws DatabaseException {

        int orderId = Integer.parseInt(ctx.formParam("orderId"));

        Order order = OrderMapper.getOrderByOrderId(orderId, connectionPool);
        String customerEmail = order.getUser().getEmail();

        OrderMapper.updateOrderStatus(orderId, OrderStatus.CONFIRMED, connectionPool);

        EmailService.sendEmail(customerEmail);



        //TODO: TRY-CATCH AND BETTER REDIRECT
        ctx.redirect("/admin");
    }

    public static void payOrder(Context ctx, ConnectionPool connectionPool) throws DatabaseException {

        int orderId = Integer.parseInt(ctx.formParam("orderId"));

        OrderMapper.updateOrderStatus(orderId, OrderStatus.PAID, connectionPool);

        //TODO: TRY-CATCH AND BETTER REDIRECT
        ctx.redirect("/customer");
    }

}
