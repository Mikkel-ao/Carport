package app.controllers;

import app.entities.OrderItem;
import app.entities.Product;
import app.entities.ProductVariant;
import app.persistence.ConnectionPool;
import app.persistence.OrderMapper;
import app.persistence.ProductMapper;
import app.util.Calculator;
import io.javalin.http.Context;

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

        ProductVariant rafterVariant = ProductMapper.getVariantsByProductAndLength(actualLength, 2, connectionPool);

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
                    beamLengths[0] = ProductMapper.getVariantsByProductAndLength(actualLength, 2, connectionPool);
                    return beamLengths;
                }

            }
        }
        if (length > 600) {
            ProductVariant[] beamLengths = new ProductVariant[2];
            int firstBeamLength = 360;
            ProductVariant firstBeam = ProductMapper.getVariantsByProductAndLength(firstBeamLength, 2, connectionPool);
            beamLengths[0] = firstBeam;

            for (int possibleLength : possibleLengths) {
                if (firstBeamLength + possibleLength >= length) {
                    ProductVariant secondBeam = ProductMapper.getVariantsByProductAndLength(possibleLength, 2, connectionPool);
                    beamLengths[1] = secondBeam;
                    return beamLengths;
                }
            }
        }
        //TODO: Proper message for the user on webpage!
        throw new IllegalArgumentException("No suitable length found");
    }

    public static List<OrderItem> createListOfMaterials(int width, int length, ConnectionPool connectionPool) {

        List<OrderItem> orderItems = new ArrayList<>();

        //TODO: Sessions Attributes

        double poleWidth = OrderMapper.getProductWidth(connectionPool, 1);
        int poleLength = 300;  //Hard-coded because we only have one lengths
        int poleCount = Calculator.calcAmountOfPoles(length, poleWidth);
        Product poleProduct = ProductMapper.getProductByProductId(1, connectionPool);
        ProductVariant poleVariant = new ProductVariant(poleLength, poleProduct);
        OrderItem poleOrderItem = new OrderItem(poleVariant, poleCount, "awdawd", poleProduct.getPrice());
        orderItems.add(poleOrderItem);



        double rafterWidth = OrderMapper.getProductWidth(connectionPool, 2);
        int rafterCount = Calculator.calcAmountOfRafters(length, rafterWidth);
        Product rafterProduct = ProductMapper.getProductByProductId(2, connectionPool);
        ProductVariant rafterVariant = selectRafterLength(width, connectionPool);
        OrderItem rafterOrderItem = new OrderItem(rafterVariant, rafterCount, "adwad", rafterProduct.getPrice());
        orderItems.add(rafterOrderItem);


        Product beamProduct = ProductMapper.getProductByProductId(2, connectionPool);

        if(length <= 600){
            ProductVariant beamVariant = selectBeamLength(length, connectionPool)[0];
            OrderItem beamOrderItem = new OrderItem(beamVariant, 2, "adwada", beamProduct.getPrice());
            orderItems.add(beamOrderItem);
        } else {
            ProductVariant beamVariant1 = selectBeamLength(length, connectionPool)[0];
            OrderItem firstBeamOrderItem = new OrderItem(beamVariant1, 2, "adwada", beamProduct.getPrice());
            orderItems.add(firstBeamOrderItem);
            ProductVariant beamVariant2 = selectBeamLength(length, connectionPool)[1];
            OrderItem secondBeamOrderItem = new OrderItem(beamVariant2, 2, "dawdawd", beamProduct.getPrice());
            orderItems.add(secondBeamOrderItem);
        }

        return orderItems;
    }

}
