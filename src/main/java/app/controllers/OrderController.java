package app.controllers;

import app.DTO.OrderInfoDTO;
import app.DTO.OrderItemAndPrice;
import app.entities.*;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.OrderMapper;
import app.persistence.UserMapper;
import app.service.EmailService;
import app.util.Calculator;
import app.util.OrderStatus;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.*;

public class OrderController {

    //Adding routes
    public static void addRoutes(Javalin app, ConnectionPool connectionPool) {
        app.get("/index", ctx -> getCarportDimensions(ctx, connectionPool));
        app.post("/add-customer-request", ctx -> handleCustomerRequest(ctx, connectionPool));
        app.get("/add-customer-request", ctx -> ctx.render("index.html"));
        app.get("/orderdetails/{orderId}", ctx -> showListOfMaterials(ctx, connectionPool));
        app.post("/updatePrice", ctx -> updatePrice(ctx, connectionPool));
        app.post("/sendOffer", ctx -> sendOffer(ctx, connectionPool));
        app.post("/payOrder", ctx -> payOrder(ctx, connectionPool));
        app.get("/customer", ctx -> getOrderDetailsForUser(ctx, connectionPool));
        app.get("/admin", ctx -> showAllOrdersForAdmin(ctx, connectionPool));

    }

    //Retrieving all possible carport lengths and widths from the database, putting them in Lists and saving the Lists in session attributes
    private static void getCarportDimensions(Context ctx, ConnectionPool connectionPool) {
        try {
            List<Integer> carportLength = OrderMapper.getCarportLength(connectionPool);
            List<Integer> carportWidth = OrderMapper.getCarportWidth(connectionPool);

            ctx.attribute("carportLength", carportLength);
            ctx.attribute("carportWidth", carportWidth);
            ctx.render("index.html");
        } catch (DatabaseException e) {
            //Printing stack trace for the developer to locate the bug
            e.printStackTrace();
            //Displaying an error message to the user if the carport dimensions could not be retrieved from the database
            ctx.attribute("errorMessage", "Kunne ikke hente carport dimensioner fra databasen - prøv venligst igen senere!");
            ctx.render("index.html");
        }
    }

    //Method for displaying a confirmation message when a user successfully places an order
    //This method catches the DatabaseException for createListOfMaterials method and all of its supporting methods!
    private static void handleCustomerRequest(Context ctx, ConnectionPool connectionPool) {
        try {
            createListOfMaterials(ctx, connectionPool);
            ctx.redirect("/add-customer-request?success=true");
        } catch (DatabaseException e) {
            //Printing stack trace for the developer to locate the bug
            e.printStackTrace();
            //Displaying an error message to the user if the carport order could not be placed
            ctx.attribute("errorMessage", "Din ordre blev ikke gennemført - prøv igen senere eller kontakt Fog for yderligere information");
            ctx.render("index.html");
        }
    }

    //Private (support) method that returns pole variant, quantity and price (in a DTO) for the given length of carport wished by the user
    private static OrderItemAndPrice getPoleOrderItemAndPrice(int carportLength, ConnectionPool connectionPool) throws DatabaseException {

        //Product length is hard-coded to 300, as we do not need to loop through the different lengths as we only have one type of pole
        ProductVariant poleVariant = OrderMapper.getVariantsByProductAndLength(300, 1, connectionPool);

        //Using the calculator method for deciding how many poles are need for the given length
        int poleCount = Calculator.calcAmountOfPoles(carportLength, poleVariant.getProduct().getWidth());

        //Creating an OrderItem object consisting of a ProductVariant and quantity, and description
        OrderItem poleOrderItem = new OrderItem(poleVariant, poleCount, 1);

        //Calculating the cost price of the poles all together
        double price = poleCount * poleVariant.getProduct().getPricePrUnit();

        //Creating an OrderItemAndPrice object consisting of an OrderItem and a price (double)
        OrderItemAndPrice poleOrderItemAndPrice = new OrderItemAndPrice(poleOrderItem, price);

        return poleOrderItemAndPrice;

    }

    //Private (support) method that returns rafter variant, quantity and price (in a DTO) for the given length of carport wished by the user
    private static OrderItemAndPrice getRafterOrderItemAndPrice(int carportLength, int carportWidth, ConnectionPool connectionPool) throws DatabaseException {

        //Retrieving all possible rafter lengths from database putting them in a List
        List<Integer> possibleLengths = OrderMapper.getProductLengths(connectionPool, 2);

        //Making sure that the order is correct and ascending so we will get the correct length when looping through the List
        Collections.sort(possibleLengths);

        int actualLength = 0;
        //Deciding the correct length, by iterating through the list finding the first rafter length, that is the same or longer than the carport width!
        for (int possibleLength : possibleLengths) {
            if (possibleLength >= carportWidth) {
                actualLength = possibleLength;
                break;
            }
        }

        //Retrieving the correct product variant with the actual rafter length from the database
        ProductVariant rafterVariant = OrderMapper.getVariantsByProductAndLength(actualLength, 2, connectionPool);

        //Caluclating the amount of rafters needed for the wished length of the carport!
        int rafterCount = Calculator.calcAmountOfRafters(carportLength, rafterVariant.getProduct().getWidth());

        //Creating an OrderItem object consisting of the ProductVariant, quantity and description
        OrderItem rafterOrderItem = new OrderItem(rafterVariant, rafterCount, 2);

        //Calculating the price with the given length of the rafters, the count of rafters and the unit price (in meters)
        double price = (double) rafterVariant.getProduct().getPricePrUnit() * rafterCount * rafterVariant.getLength() / 100;

        //Creating an OrderItemAndPrice object consisting of an OrderItem and a price (double)
        OrderItemAndPrice orderItemAndPrice = new OrderItemAndPrice(rafterOrderItem, price);

        return orderItemAndPrice;
    }

    //Private (support) method that returuns beam variant(s), quantity and price (in a DTO) for the given length of carport wished by the user
    private static List<OrderItemAndPrice> getBeamOrderItemAndPrice(int carportLength, ConnectionPool connectionPool) throws DatabaseException {


        //Initializing a List of DTO ORderItemAndPrice as we might need more than one variant of the beam
        List<OrderItemAndPrice> orderItemAndPriceList = new ArrayList<>();
        //Retrieving all possible rafter lengths from database putting them in a List
        List<Integer> possibleLengths = OrderMapper.getProductLengths(connectionPool, 2);

        //Making sure that the order is correct and ascending so we will get the correct length when looping through the List
        Collections.sort(possibleLengths);

        int actualLength;

        //Looping through the List finding the right beam length, if the wished length is less or equal to 600cm
        if (carportLength <= 600) {
            for (int possibleLength : possibleLengths) {
                if (possibleLength >= carportLength) {
                    actualLength = possibleLength;
                    ProductVariant beamVariant = OrderMapper.getVariantsByProductAndLength(actualLength, 2, connectionPool);
                    if (beamVariant != null) {
                        //Hard-coding the quantity to 2, because we are sure we only need one beam in each side of the carport!
                        OrderItem beamOrderItem = new OrderItem(beamVariant, 2, 3);
                        //Calculating the price with the given length of the beam, and the unit price (in meters) and times two (because we need one beam in each side)
                        double price = 2 * (double) beamVariant.getProduct().getPricePrUnit() * beamVariant.getLength() / 100;
                        //Adding the beam OrderItem and price to a List
                        orderItemAndPriceList.add(new OrderItemAndPrice(beamOrderItem, price));
                    }
                    break;
                }

            }
            //If wished carport length is larger than 600cm we enter this else-block!
        } else {
            //Hard-coding the length on one of the beams (on each side) to 360, because we then know we will be able to reach all the different lengths up to the maximum length of 780cm when adding a second beam!
            int firstBeamLength = 360;
            ProductVariant firstBeamVariant = OrderMapper.getVariantsByProductAndLength(firstBeamLength, 2, connectionPool);
            if (firstBeamVariant != null) {
                OrderItem firstBeamOrderItem = new OrderItem(firstBeamVariant, 2, 3);
                //Calculating the price with the given length of the beam, and the unit price (in meters) and times two (because we need one beam in each side)
                double firstBeamPrice = 2 * (double) firstBeamVariant.getProduct().getPricePrUnit() * firstBeamVariant.getLength() / 100;
                //Adding the beam OrderItem and price to a List
                orderItemAndPriceList.add(new OrderItemAndPrice(firstBeamOrderItem, firstBeamPrice));
            }
            //Selecting the second beam by looping through the list and picking the first beam that (when added with the first beam) reaches the wished length!
            for (int secondBeamLength : possibleLengths) {
                if (firstBeamLength + secondBeamLength >= carportLength) {
                    ProductVariant secondBeamVariant = OrderMapper.getVariantsByProductAndLength(secondBeamLength, 2, connectionPool);
                    if (secondBeamVariant != null) {
                        OrderItem secondBeamOrderItem = new OrderItem(secondBeamVariant, 2, 3);
                        //Calculating the price with the given length of the beam, and the unit price (in meters) and times two (because we need one beam in each side)
                        double secondBeamPrice = 2 * (double) secondBeamVariant.getProduct().getPricePrUnit() * secondBeamVariant.getLength() / 100;
                        //Adding the beam OrderItem and price to a List
                        orderItemAndPriceList.add(new OrderItemAndPrice(secondBeamOrderItem, secondBeamPrice));
                    }
                    break;
                }
            }
        }
        return orderItemAndPriceList;
    }


    //Method that retrieves the wished length and width from front end and then uses different support methods to create an order and save it in the database!
    public static Order createListOfMaterials(Context ctx, ConnectionPool connectionPool) throws DatabaseException {


        //Retrieving information from the front end
        int chosenLength = Integer.parseInt(ctx.formParam("Længde"));
        int chosenWidth = Integer.parseInt(ctx.formParam("Bredde"));

        Integer userId = ctx.sessionAttribute("userId");


        User loggedInUser = UserMapper.getUserById(userId, connectionPool);


        //Initializing a list consisting of OrderItems which will end up being the list of materials
        List<OrderItem> listOfMaterials = new ArrayList<>();


        //Adding poles to the list of materials using the private support method
        OrderItemAndPrice poleData = getPoleOrderItemAndPrice(chosenLength, connectionPool);
        OrderItem poleOrderItem = poleData.getOrderItem();
        listOfMaterials.add(poleOrderItem);
        double poleCostPrice = poleData.getPrice();


        //Adding rafters to the list of materials using the private support method
        OrderItemAndPrice rafterData = getRafterOrderItemAndPrice(chosenLength, chosenWidth, connectionPool);
        //Retrieving the order item from the DTO
        OrderItem rafterOrderItem = rafterData.getOrderItem();
        //Adding rafter order items to the list!
        listOfMaterials.add(rafterOrderItem);
        //Retrieving the price from the DTO
        double rafterCostPrice = rafterData.getPrice();

        //Adding beams to the list of materials
        List<OrderItemAndPrice> beamData = getBeamOrderItemAndPrice(chosenLength, connectionPool);
        double beamCostPrice = 0;
        //Looping through the list of beams adding every beam order item to the list of materials
        for (OrderItemAndPrice beamOrderItemAndPrice : beamData) {
            listOfMaterials.add(beamOrderItemAndPrice.getOrderItem());
            //Adding up the price of the beams to get to total cost price of the beams
            beamCostPrice += beamOrderItemAndPrice.getPrice();
        }

        //Adding the cost price of poles, rafters and beams
        double totalCostPrice = poleCostPrice + rafterCostPrice + beamCostPrice;
        //Multiplying the cost price with 1.39, to get the default sales price, which is 39% higher than the cost price!
        double totalCustomerPrice = totalCostPrice * 1.39;

        //Initializing an order with all of the above information to save in the database!
        Order currentOrder = new Order(listOfMaterials, chosenWidth, chosenLength, OrderStatus.PENDING, loggedInUser, totalCustomerPrice, totalCostPrice);

        //Calling the saveOrder method, that saves the order in the database
        saveOrder(currentOrder, connectionPool);

        return currentOrder;

    }


    public static void saveOrder(Order order, ConnectionPool connectionPool) throws DatabaseException {


        //Initializing list of materials
        List<OrderItem> listOfMaterials = order.getListOfMaterials();

        //Creating an order and retrieving the newly made order id
        int orderId = OrderMapper.createOrder(connectionPool, order.getCarportWidth(), order.getCarportLength(), order.getUser().getUserId(), order.getTotalSalesPrice(), order.getCostPrice());

        //Looping through the list of OrderItems and saving them line for line in the database "order_item" table
        if (!listOfMaterials.isEmpty()) {
            for (OrderItem orderItem : listOfMaterials) {
                OrderMapper.insertOrderItem(orderId, orderItem, connectionPool);
            }
        }
    }

    //Method for retrieving and displaying the order details from the database
    public static void getOrderDetailsForUser(Context ctx, ConnectionPool connectionPool) {

        Integer userId = ctx.sessionAttribute("userId");

        //Retrieving the order and user details (DTO because we do not want to show all details to the user) and passing them along to the next page!
        try {
            User user = UserMapper.getUserById(userId, connectionPool);
            List<OrderInfoDTO> orders = OrderMapper.getOrdersForUser(userId, connectionPool);
            ctx.attribute("user", user);
            ctx.attribute("orders", orders);
            ctx.render("customer.html");
        } catch (DatabaseException e) {
            //Printing stack trace for the developer to locate the bug
            e.printStackTrace();
            //Sending an error message to the user if the order list could not be retrieved
            ctx.attribute("errorMessage", "Kunne ikke hente ordrer");
            ctx.render("/index.html");
        }
    }

    // Method for retrieving and displaying all orders based on privilege (admin)
    public static void showAllOrdersForAdmin(Context ctx, ConnectionPool connectionPool) {

        Integer userId = ctx.sessionAttribute("userId");

        try {
            User user = UserMapper.getUserById(userId, connectionPool);
            if (user.getRole().equalsIgnoreCase("admin")) {
                List<Order> orders = OrderMapper.getAllOrders(connectionPool);
                ctx.attribute("orders", orders);
                ctx.attribute("user", user);
                ctx.render("admin.html");
            }
        } catch (DatabaseException e) {
            //Printing stack trace for the developer to locate the bug
            e.printStackTrace();
            //Displaying an error message to the user if the order list could not be retrieved
            ctx.attribute("errorMessage", "Kunne ikke hente ordrer!");
            ctx.render("/index.html");
        }
    }

    //Method for displaying the list of materials of a particular order
    public static void showListOfMaterials(Context ctx, ConnectionPool connectionPool) {

        int orderId = Integer.parseInt(ctx.pathParam("orderId"));

        //Trying to retrieve an order from database and save the list in an attribute and pass it along to next page
        try {
            Order order = OrderMapper.getOrderByOrderId(orderId, connectionPool);

            List<OrderItem> orderDetails = order.getListOfMaterials();

            ctx.attribute("orderDetails", orderDetails);
            ctx.attribute("orderId", orderId);
            ctx.render("orderdetails.html");
        } catch (DatabaseException e) {
            //Printing stack trace for the developer to locate the bug
            e.printStackTrace();
            //Displaying an error message to the user if the list of materials could not be retrieved from database
            ctx.attribute("errorMessage", "Kunne ikke hente stykliste");
            ctx.render("/index.html");
        }
    }


    //Method for updating and saving a new sales price in the database
    public static void updatePrice(Context ctx, ConnectionPool connectionPool){

        int orderId = Integer.parseInt(ctx.formParam("orderId"));

        double newPrice = Double.parseDouble(ctx.formParam("newPrice"));

        //Trying to update price in the database
        try {
            OrderMapper.UpdatePrice(newPrice, orderId, connectionPool);
            ctx.redirect("/admin");
        } catch (DatabaseException e) {
            //Printing stack trace for the developer to locate the bug
            e.printStackTrace();
            //Displaying an error message to the admin if the price could not be updated!
            ctx.attribute("errorMessage", "Kunne ikke opdatere prisen!");
            ctx.render("/admin.html");
        }
    }

    //Method for sending an order to the user by updating the order status and using the EmailService
    public static void sendOffer(Context ctx, ConnectionPool connectionPool) {

        int orderId = Integer.parseInt(ctx.formParam("orderId"));

        //Retrieving the order from the database and getting the email from the order's user
        try {
            Order order = OrderMapper.getOrderByOrderId(orderId, connectionPool);
            String customerEmail = order.getUser().getEmail();

            //Updating status
            OrderMapper.updateOrderStatus(orderId, OrderStatus.CONFIRMED, connectionPool);

            //Sending email to customer
            EmailService.sendEmail(customerEmail);
            ctx.redirect("/admin");
        } catch (DatabaseException e) {
            //Printing stack trace for the developer to locate the bug
            e.printStackTrace();
            //Displaying an error message to the admin if the order could not be sent!
            ctx.attribute("errorMessage", "Kunne ikke afsende ordren!");
            ctx.render("/admin.html");
        }
    }

    //Method for paying order (customer) by changing order status
    public static void payOrder(Context ctx, ConnectionPool connectionPool) {

        int orderId = Integer.parseInt(ctx.formParam("orderId"));

        //Trying to update the status of the order in the database
        try {
            OrderMapper.updateOrderStatus(orderId, OrderStatus.PAID, connectionPool);
            ctx.redirect("/customer");
        } catch (DatabaseException e) {
            //Printing stack trace for the developer to locate the bug
            e.printStackTrace();
            //Displaying an error message to the admin if the order could not be paid!
            ctx.attribute("errorMessage", "Kunne ikke afsende ordren!");
            ctx.render("/customer.html");
        }
    }
}
