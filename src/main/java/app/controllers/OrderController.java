package app.controllers;

import app.DTO.OrderItemAndPrice;
import app.entities.*;
import app.persistence.ConnectionPool;
import app.persistence.OrderMapper;
import app.persistence.ProductMapper;
import app.persistence.UserMapper;
import app.util.Calculator;
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
        Order currentOrder = new Order(listOfMaterials, userWidth, userLength, "pending", loggedInUser, totalCustomerPrice, totalCostPrice);

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

}
