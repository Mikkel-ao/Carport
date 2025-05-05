package app.util;

public class Calculator {


        public static int calcAmountOfPoles(int totalLength) {
            int effectiveLength = totalLength - 130;
            int maxGap = 310; // Maximum distance between poles
            int basePoles = 4; // Minimum poles required

            if (effectiveLength <= maxGap) {
                return basePoles;
            }

            int additionalPoles = (int) Math.ceil((effectiveLength - maxGap) / (double) maxGap);
            return basePoles + (additionalPoles * 2);
        }


        public static int calcAmountOfRafters(int totalLengthInCm) {

            //TODO: Find ud af hvad vi gør med rafter width! (4,5cm er de normalt)!
            //TODO: Refactor til global variabler
            double rafterWidth = 4.5;
            int spacing = 55; //maximum spacing allowed

            //Subtracting two times rafter width because of the starting and ending rafter is defaulted (4,5 cm each)
            double remainingLength = totalLengthInCm - (2 * rafterWidth);

            int numIntermediateRafters = 0;
            double usedLength = 0;

            while(usedLength + spacing + rafterWidth <= remainingLength) {
                numIntermediateRafters++;
                usedLength += spacing;
            }

            return numIntermediateRafters + 2;

        }

        public static double getRafterSpacing(int carportLength){

            int rafterCount = calcAmountOfRafters(carportLength);

            double rafterWidth = 4.5;
            double totalRafterWidth = rafterCount * rafterWidth;

            double remainingLength = carportLength - totalRafterWidth;

            //Subtracting 1, because there will always be one less space than rafter
            return remainingLength / (rafterCount - 1);
        }

    }

