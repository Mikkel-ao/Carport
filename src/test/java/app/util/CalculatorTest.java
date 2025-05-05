package app.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CalculatorTest {

    @Test
    void testCalcAmountOfPoles440() {
        //Arrange, Act, Assert

        int expected = 4;
        int totalPoles = Calculator.calcAmountOfPoles(440);

        assertEquals(expected, totalPoles);
    }

    @Test
    void testCalcAmountOfPoles441() {
        //Arrange, Act, Assert

        int expected = 6;
        int totalPoles = Calculator.calcAmountOfPoles(441);

        assertEquals(expected, totalPoles);
    }

    @Test
    void testCalcAmountOfPoles750() {
        //Arrange, Act, Assert

        int expected = 6;
        int totalPoles = Calculator.calcAmountOfPoles(750);

        assertEquals(expected, totalPoles);
    }

    @Test
    void testCalcAmountOfPoles751() {
        //Arrange, Act, Assert

        int expected = 8;
        int totalPoles = Calculator.calcAmountOfPoles(751);

        assertEquals(expected, totalPoles);
    }


    @Test
    void calcAmountOfRafters780() {

        int expected = 15;
        int totalRafters = Calculator.calcAmountOfRafters(780);

        assertEquals(expected, totalRafters);

    }

    @Test
    void getRafterSpacing780() {

        double expectedSpacing = 55;
        double totalSpacing = Calculator.getRafterSpacing(780);

        assertEquals(expectedSpacing, totalSpacing);
    }
}