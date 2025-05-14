package app.persistence;

import app.util.Calculator;
// TODO: Where should this be placed, package wise?
public class CarportSvg {
    private int width;
    private int length;
    private Svg carportSvg;
    private String style = "stroke-width:1px; stroke:#000000; fill: #ffffff";
    private double rafterWidth = 4.5;
    private double postWidth = 9.7;


    public CarportSvg(int width, int length) {
        this.width = width;
        this.length = length;
        // TODO: Find a simpler solution for displaying images correctly
        // carportSvg = new Svg(0, 0, "0 0 855 690", "700px");

        String viewBox = "0 0 " + (length+100) + " " + (width+100); // +100 to allow content outside of carport measurements
        carportSvg = new Svg(0, 0, viewBox, "100%");

        drawBase();
        addPosts();
        addBeams();
        addRafters();
        addDimensions();
    }

    private void drawBase() {
        carportSvg.addRectangle(0, 0, width, length, style);
    }

    // Rem
    private void addBeams() {
        String beamStyle = "stroke-width:1px; stroke:#000000; fill: #ffffff";
        carportSvg.addRectangle(0, 35, 4.5, length, beamStyle);
        carportSvg.addRectangle(0, width-35, 4.5, length, beamStyle);
    }

    // Spær
    private void addRafters() {
        //TODO: for now the rafterWidth is hard-coded until we get a mapper method that pulls it from DB
        int rafterCount = Calculator.calcAmountOfRafters(length, rafterWidth);
        double spacing = Calculator.calcRafterSpacing(length, rafterWidth);

        for (int i = 0; i < rafterCount; i++) {
            double x = i * (spacing + rafterWidth);
            carportSvg.addRectangle(x, 0.0, width, rafterWidth, style);

            // Sets arrows and text measurements
            if (i > 0) {
                double previousRafter = (i - 1) * (spacing + rafterWidth);
                double start = previousRafter + rafterWidth;
                double end = x;
                double mid = (start + end) / 2 - 17; // -17 to adjust for SVG starting at mid location.
                carportSvg.addArrow((int) start, width - 20, (int) end, width - 20, style);
                carportSvg.addText((int) mid, (width - 20) + 15, 0, String.format("%.2f", end - start));
            }
        }
    }

    // Stolpe
    private void addPosts() {
        int postCount = Calculator.calcAmountOfPoles(length, postWidth);
        double spacing = Calculator.calcPoleSpacing(length, postWidth);

        // Place first post after 100cm
        double x = 100;

        // Place the first posts
        carportSvg.addRectangle(x, 32.5, postWidth, postWidth, style);    // Upper post
        carportSvg.addRectangle(x, width - 37.5, postWidth, postWidth, style); // Lower post

        // Posts after the first ones
        for (int i = 1; i < postCount; i++) {
            // Calculate the x position for the next post based on spacing
            x = 100 + i * spacing;

            // Ensure the last post is placed within the carport's length
            if (x + postWidth > length - 100) {  // If placing a post exceeds the carport's length
                x = length - postWidth - 100;  // Place the last post at 100cm from the end
            }

            // Placing posts
            carportSvg.addRectangle(x, 32.5, postWidth, postWidth, style);    // Upper post
            carportSvg.addRectangle(x, width - 37.5, postWidth, postWidth, style); // Lower post
        }
    }

    private void addDimensions() {
        // Horizontal dimension of the drawing, length of the carport
        carportSvg.addArrow(0, width+10, length, width+10, style);
        carportSvg.addText(length/2, width+25, 0, length+" cm");

        // Vertical dimension of the drawing, width of the carport
        carportSvg.addArrow(length+40, 0, length+40, width, style);
        carportSvg.addText(length+45, width/2, 90, width+" cm");

        // Inner vertical dimension
        carportSvg.addArrow(length+20, 35, length+20, width-35, style);
        carportSvg.addText(length+25, width/2, 90, width-70+" cm");

    }

    @Override
    public String toString() {
        return carportSvg.toString();
    }
}
