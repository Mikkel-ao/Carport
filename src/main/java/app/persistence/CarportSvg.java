package app.persistence;

import app.util.Calculator;
// TODO: Where should this be placed, package wise?
public class CarportSvg {
    private int width;
    private int length;
    private Svg carportSvg;
    private double rafterWidth = 4.5;


    public CarportSvg(int width, int length) {
        this.width = width;
        this.length = length;
        // TODO: Find a simpler solution for displaying images correctly
        // carportSvg = new Svg(0, 0, "0 0 855 690", "700px");
        String viewBox = "0 0 " + (length+100) + " " + (width+100); // +100 to allow content outside of carport measurements
        carportSvg = new Svg(0, 0, viewBox, "100%");


        drawBase();
        addBeams();
        addRafters();
        addDimensions();
    }

    private void drawBase() {
        carportSvg.addRectangle(0, 0, width, length, "stroke-width:1px; stroke:#000000; fill: #ffffff");
    }
    // Rem
    private void addBeams() {
        String beamStyle = "stroke-width:1px; stroke:#000000; fill: #ffffff";
        carportSvg.addRectangle(0, 35, 4.5, length, beamStyle);
        carportSvg.addRectangle(0, width-35, 4.5, length, beamStyle);
    }
    // Spær
    private void addRafters() {
        String rafterStyle = "stroke:#000000; fill: #ffffff";
        int rafterCount = Calculator.calcAmountOfRafters(length, 4.5);
        double spacing = Calculator.calcRafterSpacing(length, 4.5);

        for (int i = 0; i < rafterCount; i++) {
            double x = i * (spacing + rafterWidth);
            carportSvg.addRectangle(x, 0.0, width, rafterWidth, rafterStyle);
        }
    }

    private void addDimensions() {
        // Horizontal dimension of the drawing, length of the carport
        carportSvg.addArrow(0, width+10, length, width+10, "stroke:black; stroke-width:1");
        carportSvg.addText(length/2, width+25, 0, length+" cm");

        // Vertical dimension of the drawing, width of the carport
        carportSvg.addArrow(length+40, 0, length+40, width, "stroke:black; stroke-width:1");
        carportSvg.addText(length+45, width/2, 90, width+" cm");

        // Inner vertical dimension
        carportSvg.addArrow(length+20, 35, length+20, width-35, "stroke:black; stroke-width:1");
        carportSvg.addText(length+25, width/2, 90, width-70+" cm");

    }

    @Override
    public String toString() {
        return carportSvg.toString();
    }
}
