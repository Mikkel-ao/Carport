package app.controllers;

import app.persistence.ConnectionPool;
import app.persistence.OrderMapper;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderController {

    //TODO: Skal returnerer et ProductVariant objekt
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


    //TODO: Skal returnere et ProductVariant objekt!
    public static int[] selectBeamLength(int length, ConnectionPool connectionPool) {

        List<Integer> possibleLengths = OrderMapper.getProductLengths(connectionPool, 2);


        Collections.sort(possibleLengths);

        if (length <= 600) {
            int[] beamLengths = new int[1];
            for (int possibleLength : possibleLengths) {
                if (possibleLength >= length) {
                    beamLengths[0] = possibleLength;
                    return beamLengths;
                }

            }
        }
        if (length > 600 && length < 960) {
            int[] beamLengths = new int[2];
            int firstBeamLength = 360;

            for (int possibleLength : possibleLengths) {
                if (firstBeamLength + possibleLength >= length) {
                    beamLengths[0] = 360;
                    beamLengths[1] = possibleLength;
                    return beamLengths;
                }
            }
        }
        //TODO: Proper message for the user on webpage!
        throw new IllegalArgumentException("No suitable length found");
    }
}
