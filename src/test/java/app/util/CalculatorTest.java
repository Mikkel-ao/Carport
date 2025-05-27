package app.util;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CalculatorTest {


    @Test
    void testCalcAmountOfPoles100() {
        //Arrange, Act, Assert

        int expected = 4;
        int totalPoles = Calculator.calcAmountOfPoles(100, 9.7);

        assertEquals(expected, totalPoles);
    }

    @Test
    void testCalcAmountOfPoles459() {
        //Arrange, Act, Assert

        int expected = 4;
        int totalPoles = Calculator.calcAmountOfPoles(459, 9.7);

        assertEquals(expected, totalPoles);
    }

    @Test
    void testCalcAmountOfPoles460() {
        //Arrange, Act, Assert

        int expected = 6;
        int totalPoles = Calculator.calcAmountOfPoles(460, 9.7);

        assertEquals(expected, totalPoles);
    }


    @Test
    void testCalcAmountOfPoles779() {
        //Arrange, Act, Assert

        int expected = 6;
        int totalPoles = Calculator.calcAmountOfPoles(779,9.7);

        assertEquals(expected, totalPoles);
    }

    @Test
    void testCalcAmountOfPoles780() {
        //Arrange, Act, Assert

        int expected = 8;
        int totalPoles = Calculator.calcAmountOfPoles(780, 9.7);

        assertEquals(expected, totalPoles);
    }

    @Test
    void testCalcPoleSpacing459() {
        //Arrange, Act, Assert

        double expected = 309.6;
        double poleSpacing = Calculator.calcPoleSpacing(459, 9.7);

        assertEquals(expected, poleSpacing);
    }

    @Test
    void testCalcPoleSpacing460() {
        //Arrange, Act, Assert

        double expected = 150.45;
        double poleSpacing = Calculator.calcPoleSpacing(460, 9.7);

        assertEquals(expected, poleSpacing);
    }


    @Test
    void testCalcPoleSpacing779() {
        //Arrange, Act, Assert

        double expected = 309.95;
        double poleSpacing = Calculator.calcPoleSpacing(779, 9.7);

        assertEquals(expected, poleSpacing);
    }

    @Test
    void testCalcPoleSpacing780() {
        //Arrange, Act, Assert

        double expected = 203.733333333333335;
        double poleSpacing = Calculator.calcPoleSpacing(780, 9.7);

        assertEquals(expected, poleSpacing);
    }


    @Test
    void calcAmountOfRafters580() {

        int expected = 11;
        int totalRafters = Calculator.calcAmountOfRafters(580, 4.5);

        assertEquals(expected, totalRafters);

    }


    @Test
    void calcAmountOfRafters780() {

        int expected = 15;
        int totalRafters = Calculator.calcAmountOfRafters(780, 4.5);

        assertEquals(expected, totalRafters);

    }

    @Test
    void calRafterSpacing580() {

        double expectedSpacing = 53.05;
        double totalSpacing = Calculator.calcRafterSpacing(580, 4.5);

        assertEquals(expectedSpacing, totalSpacing);
    }

    @Test
    void calcRafterSpacing780() {

        double expectedSpacing = 50.892857142857146;
        double totalSpacing = Calculator.calcRafterSpacing(780, 4.5);

        assertEquals(expectedSpacing, totalSpacing);
    }
}