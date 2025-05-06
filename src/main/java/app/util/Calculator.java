package app.util;

public class Calculator {


    public static int calcAmountOfPoles(int totalLength, double poleWidth) {

        //100cm overhang in front and 30cm overhang in the back
        int overhang = 130;


        //Subtracting the overhang and the width of one pole (making sure there is space for the last pole)
        double effectiveLength = totalLength - (overhang + poleWidth);

        //Maximum allowed spacing between each of the poles
        int maxGap = 310;

        //Rounding to the smallest integer higher than the result of the calculation. Using the absolute number, to make sure it will never return less than 4 poles!
        int quantity = (int) Math.ceil(Math.abs(effectiveLength / (maxGap + poleWidth)));

        //Adding the last pole (which we made space for earlier) and multiplying with two because we need the same amount in the other side of the carport!
        return (quantity + 1) * 2;
    }

    public static double calcPoleSpacing(int totalLength, double poleWidth) {

        //Getting the number of poles needed for this specific length
        int totalPoleCount = calcAmountOfPoles(totalLength, poleWidth);

        //Dividing by two, because we are only calculating on one side of the carport
        int poleCountPrSide = totalPoleCount / 2;

        //100 cm in front and 30cm in back
        int overhang = 130;

        //Calculating the sum of the space the poles are occupying
        double totalPoleWidth = poleCountPrSide * poleWidth;

        //Subtracting the overhang and the sum of the width of the poles, to get the free space that needs supporting
        double remainingLength = totalLength - (overhang + totalPoleWidth);

        //Subtracting 1 from the amount of poles, because there will always be one gap less than the amount of poles!
        return remainingLength / (poleCountPrSide - 1);

    }


    public static int calcAmountOfRafters(int totalLengthInCm, double rafterWidth) {

        //Making sure that there is spacing for the last rafter!
        double remainingLength = totalLengthInCm - rafterWidth;

        //A section which is 55cm for max space between rafters and 4.5 for width of rafter
        double maxSpacing = 55 + rafterWidth;

        //rounding to smallest integer which is larger than the result of the calculation
        int quantity = (int) Math.ceil((remainingLength / maxSpacing));

        //Adding the last rafter to the quantity before returning (the one we made space for in the beginning)
        return quantity + 1;

    }

    public static double calcRafterSpacing(int carportLength, double rafterWidth) {

        int rafterCount = calcAmountOfRafters(carportLength, rafterWidth);

        double totalRafterWidth = rafterCount * rafterWidth;

        double remainingLength = carportLength - totalRafterWidth;

        //Subtracting 1, because there will always be one less space than rafter
        return remainingLength / (rafterCount - 1);
    }

}

