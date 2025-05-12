package app.controllers;

import app.entities.*;
import app.persistence.ConnectionPool;
import app.persistence.OrderMapper;
import app.persistence.ProductMapper;
import app.persistence.UserMapper;
import app.util.Calculator;
import io.javalin.Javalin;
import io.javalin.http.Context;

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

    public static HashMap<OrderItem, Double> getPoleOrderItemAndPrice(int carportLength, ConnectionPool connectionPool) {

        HashMap<OrderItem, Double> OrderItemAndPrice = new HashMap<>();

        //product length 300 is hard-coded as we do not need to loop through the different lengths as we only have one type of pole
        ProductVariant poleVariant = ProductMapper.getVariantsByProductAndLength(300, 1, "stolpe", connectionPool);
        int poleCount = Calculator.calcAmountOfPoles(carportLength, poleVariant.getProduct().getWidth());
        OrderItem poleOrderItem = new OrderItem(poleVariant, poleCount);

        double price = poleCount * poleVariant.getProduct().getPricePrUnit();

        OrderItemAndPrice.put(poleOrderItem, price);

        return OrderItemAndPrice;

    }

    public static HashMap<OrderItem, Double> getRafterOrderItemAndPrice(int carportLength, int carportWidth, ConnectionPool connectionPool) {


        HashMap<OrderItem, Double> orderItemAndPrice = new HashMap<>();

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

        ProductVariant rafterVariant = ProductMapper.getVariantsByProductAndLength(actualLength, 2, "spær", connectionPool);

        //Caluclating the amount of rafters needed for the wished length of the carport!
        int rafterCount = Calculator.calcAmountOfRafters(carportLength, rafterVariant.getProduct().getWidth());

        OrderItem rafterOrderItem = new OrderItem(rafterVariant, rafterCount);

        //Calculating the price with the given length of every single rafter, the count of rafters and the unit price (in meters)
        double price = (double) rafterVariant.getProduct().getPricePrUnit() * rafterCount * rafterVariant.getLength() / 100;

        orderItemAndPrice.put(rafterOrderItem, price);

        return orderItemAndPrice;
    }


    public static HashMap<OrderItem, Double> getBeamOrderItemAndPrice(int carportLength, ConnectionPool connectionPool) {

        HashMap<OrderItem, Double> orderItemAndPrice = new HashMap<>();

        List<Integer> possibleLengths = OrderMapper.getProductLengths(connectionPool, 2);

        Collections.sort(possibleLengths);

        int actualLength;

        if (carportLength <= 600) {
            for (int possibleLength : possibleLengths) {
                if (possibleLength >= carportLength) {
                    actualLength = possibleLength;
                    ProductVariant beamVariant = ProductMapper.getVariantsByProductAndLength(actualLength, 2, "rem", connectionPool);
                    //Hard-coding the quantity to 2, because we are sure we only need one beam in each side of the carport!
                    OrderItem beamOrderItem = new OrderItem(beamVariant, 2);
                    double price = 2 * (double) beamVariant.getProduct().getPricePrUnit() * beamVariant.getLength() / 100;
                    orderItemAndPrice.put(beamOrderItem, price);
                    break;
                }

            }
        }
        //If the length of the carport exceeds 600cm, we know we will need two beams on each side of the carport!
        if (carportLength > 600) {
            //Hard-coding the length on one of the beams (on each side) to 360, because we then know we will be able to reach all the different lengths up to the maximum length of 780cm!
            int firstBeamLength = 360;
            ProductVariant firstBeamVariant = ProductMapper.getVariantsByProductAndLength(firstBeamLength, 2, "rem", connectionPool);
            OrderItem firstBeamOrderItem = new OrderItem(firstBeamVariant, 2);
            double firstBeamPrice = 2 * (double) firstBeamVariant.getProduct().getPricePrUnit() * firstBeamVariant.getLength() / 100;
            orderItemAndPrice.put(firstBeamOrderItem, firstBeamPrice);
            //Figuring out what variant the second beam needs to be
            for (int secondBeamLength : possibleLengths) {
                if (firstBeamLength + secondBeamLength >= carportLength) {
                    ProductVariant secondBeamVariant = ProductMapper.getVariantsByProductAndLength(secondBeamLength, 2, "rem", connectionPool);
                    OrderItem secondBeamOrderItem = new OrderItem(secondBeamVariant, 2);
                    double secondBeamPrice = 2 * (double) secondBeamVariant.getProduct().getPricePrUnit() * secondBeamVariant.getLength() / 100;
                    orderItemAndPrice.put(secondBeamOrderItem, secondBeamPrice);
                }
            }
        }
        //TODO: Proper message for the user on webpage!
        throw new IllegalArgumentException("No suitable length found");
    }


    //TODO: Maybe in ProductMapper instead?
    public static Order createListOfMaterials(Context ctx, ConnectionPool connectionPool) {


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
        HashMap<OrderItem, Double> poleData = getPoleOrderItemAndPrice(userLength, connectionPool);
        OrderItem poleOrderItem = poleData.keySet().iterator().next();
        listOfMaterials.add(poleOrderItem);
        double poleCostPrice = poleData.get(poleOrderItem);


        //Adding rafters to the list of materials
        HashMap<OrderItem, Double> rafterData = getRafterOrderItemAndPrice(userLength, userWidth, connectionPool);
        //Using this approach as we only expect to find one entry in the hashMap
        OrderItem rafterOrderItem = rafterData.keySet().iterator().next();
        //Adding rafter order items to the list!
        listOfMaterials.add(rafterOrderItem);
        //Retrieving the price from the hash map
        double rafterCostPrice = rafterData.get(rafterOrderItem);

        //Adding beams to the list of materials
        HashMap<OrderItem, Double> beamData = getBeamOrderItemAndPrice(userLength, connectionPool);
        double beamCostPrice = 0;
        for (Map.Entry<OrderItem, Double> entry : beamData.entrySet()) {
            listOfMaterials.add(entry.getKey());
            beamCostPrice += entry.getValue();
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
