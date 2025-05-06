package app.util;

public class Calculator {


    public static int calcAmountOfPoles(int totalLength) {

        //100cm overhang in front and 30cm overhang in the back
        int overhang = 130;

        //The width of a pole is 9.7cm
        double poleWidth = 9.7;

        //Subtracting the overhang and the width of one pole (making sure there is space for the last pole)
        double effectiveLength = totalLength - (overhang + poleWidth);

        int maxGap = 310;

        int quantity = (int) Math.ceil(Math.abs(effectiveLength / (maxGap + poleWidth)));

        return (quantity + 1) * 2;
    }


    public static int calcAmountOfRafters(int totalLengthInCm) {

        //Making sure that there is spacing for the last rafter!
        double remainingLength = totalLengthInCm - 4.5;

        //A section which is 55cm for max space between rafters and 4.5 for width of rafter
        double maxSpacing = 59.5;

        //rounding to smallest integer which is larger than the result of the calculation
        int quantity = (int) Math.ceil((remainingLength / maxSpacing) + 1);

        return quantity;

    }

    public static double calcRafterSpacing(int carportLength) {

        int rafterCount = calcAmountOfRafters(carportLength);

        double rafterWidth = 4.5;
        double totalRafterWidth = rafterCount * rafterWidth;

        double remainingLength = carportLength - totalRafterWidth;

        //Subtracting 1, because there will always be one less space than rafter
        return remainingLength / (rafterCount - 1);
    }

}

