package app.controllers;

import app.persistence.ConnectionPool;
import app.persistence.OrderMapper;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderController {

    public static int selectRafterLength(int length, ConnectionPool connectionPool) {

        List<Integer> possibleLengths = OrderMapper.getProductLengths(connectionPool, 2);

        //Making sure that the order is correct and ascending
        Collections.sort(possibleLengths);

        for (int possibleLength : possibleLengths) {
            if (possibleLength >= length) {
                return possibleLength;
            }
        }
        //TODO: Proper message for the user on webpage!
        throw new IllegalArgumentException("No suitable length found");
    }

    /*
    public static int[] selectBeamLength(int length, ConnectionPool connectionPool) {

        List<Integer> possibleLengths = OrderMapper.getProductLengths(connectionPool, 2);
        int[] beamLengths = new int[2];

        //Making sure that the order is correct and ascending
        Collections.sort(possibleLengths);

        if(length <= 600) {
            for (int possibleLength : possibleLengths) {
                if (possibleLength >= length) {
                    return possibleLength;
                }
            }
        }
        //TODO: Proper message for the user on webpage!
        throw new IllegalArgumentException("No suitable length found");
    }
    */



}
