package app.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CalculatorTest {

    @Test
    void testCalPoles440() {
        //Arrange, Act, Assert

        int expected = 4;
        int totalPoles = Calculator.calcPoles(440);

        assertEquals(expected, totalPoles);
    }

    @Test
    void testCalPoles441() {
        //Arrange, Act, Assert

        int expected = 6;
        int totalPoles = Calculator.calcPoles(441);

        assertEquals(expected, totalPoles);
    }

    @Test
    void testCalPoles750() {
        //Arrange, Act, Assert

        int expected = 6;
        int totalPoles = Calculator.calcPoles(750);

        assertEquals(expected, totalPoles);
    }

    @Test
    void testCalPoles751() {
        //Arrange, Act, Assert

        int expected = 8;
        int totalPoles = Calculator.calcPoles(751);

        assertEquals(expected, totalPoles);
    }

}