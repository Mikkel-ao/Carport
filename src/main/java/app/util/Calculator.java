package app.util;

public class Calculator {


        public static int calcPoles(int totalLength) {
            int effectiveLength = totalLength - 130;
            int maxGap = 310; // Maximum distance between poles
            int basePoles = 4; // Minimum poles required

            if (effectiveLength <= maxGap) {
                return basePoles;
            }

            int additionalPoles = (int) Math.ceil((effectiveLength - maxGap) / (double) maxGap);
            return basePoles + (additionalPoles * 2);
        }



    }

