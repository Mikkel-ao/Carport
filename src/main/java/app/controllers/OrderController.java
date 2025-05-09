package app.controllers;

import app.entities.*;
import app.persistence.ConnectionPool;
import app.persistence.OrderMapper;
import app.persistence.ProductMapper;
import app.util.Calculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderController {


    public static ProductVariant selectRafterLength(int length, ConnectionPool connectionPool) {

        List<Integer> possibleLengths = OrderMapper.getProductLengths(connectionPool, 2);

        //Making sure that the order is correct and ascending
        Collections.sort(possibleLengths);

        int actualLength = 0;

        for (int possibleLength : possibleLengths) {
            if (possibleLength >= length) {
                actualLength = possibleLength;
                break;
            }
        }

        ProductVariant rafterVariant = ProductMapper.getVariantsByProductAndLength(actualLength, 2, "spær", connectionPool);

        return rafterVariant;
    }


    //TODO: Evt hashMap
    public static ProductVariant[] selectBeamLength(int length, ConnectionPool connectionPool) {

        List<Integer> possibleLengths = OrderMapper.getProductLengths(connectionPool, 2);


        Collections.sort(possibleLengths);

        int actualLength = 0;

        if (length <= 600) {
            ProductVariant[] beamLengths = new ProductVariant[1];
            for (int possibleLength : possibleLengths) {
                if (possibleLength >= length) {
                    actualLength = possibleLength;
                    beamLengths[0] = ProductMapper.getVariantsByProductAndLength(actualLength, 2, "rem", connectionPool);
                    return beamLengths;
                }

            }
        }
        if (length > 600) {
            ProductVariant[] beamLengths = new ProductVariant[2];
            int firstBeamLength = 360;
            ProductVariant firstBeam = ProductMapper.getVariantsByProductAndLength(firstBeamLength, 2, "rem", connectionPool);
            beamLengths[0] = firstBeam;

            for (int possibleLength : possibleLengths) {
                if (firstBeamLength + possibleLength >= length) {
                    ProductVariant secondBeam = ProductMapper.getVariantsByProductAndLength(possibleLength, 2, "rem", connectionPool);
                    beamLengths[1] = secondBeam;
                    return beamLengths;
                }
            }
        }
        //TODO: Proper message for the user on webpage!
        throw new IllegalArgumentException("No suitable length found");
    }

    public static List<OrderItem> createListOfMaterials(int width, int length, ConnectionPool connectionPool) {

        //TODO: User kommer fra session attributes, men er blot hard-coded for
        User user = new User(1,"1234", "per@hej.dk", "123123123", "customer");

        List<OrderItem> orderItems = new ArrayList<>();

        //TODO: Sessions Attributes her og Context-objekt i signatur!


        //TODO: Refactor!
        double poleWidth = OrderMapper.getProductWidth(connectionPool, 1);
        int poleLength = 300;  //Hard-coded because we only have one length
        int poleCount = Calculator.calcAmountOfPoles(length, poleWidth);
        Product poleProduct = ProductMapper.getProductByProductId(1, connectionPool);
        ProductVariant poleVariant = ProductMapper.getVariantsByProductAndLength(poleLength, 1, "stolpe", connectionPool);
        OrderItem poleOrderItem = new OrderItem(poleVariant, poleCount);
        orderItems.add(poleOrderItem);



        double rafterWidth = OrderMapper.getProductWidth(connectionPool, 2);
        int rafterCount = Calculator.calcAmountOfRafters(length, rafterWidth);
        Product rafterProduct = ProductMapper.getProductByProductId(2, connectionPool);
        ProductVariant rafterVariant = selectRafterLength(width, connectionPool);
        OrderItem rafterOrderItem = new OrderItem(rafterVariant, rafterCount);
        orderItems.add(rafterOrderItem);


        Product beamProduct = ProductMapper.getProductByProductId(2, connectionPool);

        double poleCostPrice = poleCount * poleProduct.getPricePrUnit();
        double rafterCostPrice = rafterCount * (double) rafterVariant.getLength()/10 *  rafterProduct.getPricePrUnit();
        double beamCostPrice;

        if(length <= 600){
            ProductVariant beamVariant = selectBeamLength(length, connectionPool)[0];
            OrderItem beamOrderItem = new OrderItem(beamVariant, 2);
            beamCostPrice = (double) beamVariant.getLength()/10 * beamProduct.getPricePrUnit() * 2;
            orderItems.add(beamOrderItem);
        } else {
            ProductVariant beamVariant1 = selectBeamLength(length, connectionPool)[0];
            OrderItem firstBeamOrderItem = new OrderItem(beamVariant1, 2);
            orderItems.add(firstBeamOrderItem);
            ProductVariant beamVariant2 = selectBeamLength(length, connectionPool)[1];
            OrderItem secondBeamOrderItem = new OrderItem(beamVariant2, 2);
            orderItems.add(secondBeamOrderItem);
            beamCostPrice = ((double) beamVariant2.getLength()/10 * beamProduct.getPricePrUnit() * 2) + ((double) beamVariant1.getLength()/10 * beamProduct.getPricePrUnit() * 2);
        }

        double totalCostPrice = poleCostPrice + rafterCostPrice + beamCostPrice;
        double totalCustomerPrice = totalCostPrice * 1.39;

        int orderId = OrderMapper.createOrder(connectionPool, width, length, user.getUserId(), totalCustomerPrice, totalCostPrice);

        for (OrderItem orderItem : orderItems) {
            OrderMapper.insertOrderItem(orderId, orderItem, connectionPool);
        }


        /*//TODO: Man kan også lave en order og smide listen i maven og returnerer denne?? 
        Order currentOrder = OrderMapper.getOrderById(orderId, connectionPool);
        currentOrder.setListOfMaterials(orderItems);*/

        /*//TODO: Skal ikke nødvendigvis returneres, men måske gemmes i en attribute? Vi kan altid hente stykliste ud fra orderId senere hen!
        return orderItems;
    }


}
