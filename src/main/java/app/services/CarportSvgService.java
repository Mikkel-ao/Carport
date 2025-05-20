package app.services;

import app.util.Calculator;
import app.util.Svg;

public class CarportSvgService {
    private int width;
    private int length;
    private Svg carportSvg;
    private String style = "stroke-width:1px; stroke:#000000; fill: #ffffff";
    private double rafterWidth = 4.5;
    private double postWidth = 9.7;


    public CarportSvgService(int width, int length) {
        this.width = width;
        this.length = length;

        // viewBox defines the coordinate system and visible area of the SVG canvas
        String viewBox = "0 0 " + (length+100) + " " + (width+100); // +100 allows room for content outside the carport
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

    // Method for placing "remme"
    private void addBeams() {
        carportSvg.addRectangle(0, 35, 4.5, length, style);
        carportSvg.addRectangle(0, width-35, 4.5, length, style);
    }

    // Method for placing "spær" and displaying distance between them
    private void addRafters() {
        int rafterCount = Calculator.calcAmountOfRafters(length, rafterWidth);
        double spacing = Calculator.calcRafterSpacing(length, rafterWidth);

        // Loop for placing rafters
        for (int i = 0; i < rafterCount; i++) {
            double x = i * (spacing + rafterWidth);
            carportSvg.addRectangle(x, 0.0, width, rafterWidth, style);

            // Loop that places the arrows and text displaying the distance between rafters
            if (i > 0) {
                double previousRafter = (i - 1) * (spacing + rafterWidth);
                double start = previousRafter + rafterWidth;
                double end = x;
                double middle = (start + end) / 2 - 17; // -17 to adjust for SVG starting at middle location.
                carportSvg.addArrow((int) start, width - 20, (int) end, width - 20, style);
                carportSvg.addText((int) middle, (width - 20) + 15, 0, String.format("%.2f", end - start));
            }
        }
    }

    // Method for placing "stolper" at the right locations with allowed spacing between them
    private void addPosts() {
        int postCount = Calculator.calcAmountOfPoles(length, postWidth);
        double spacing = Calculator.calcPoleSpacing(length, postWidth);

        // First posts need to be after 100cm.
        double x = 100;

        // Place the first posts - 32.5 & 37.5 is to center posts underneath the beam
        carportSvg.addRectangle(x, 32.5, postWidth, postWidth, style); // Upper post
        carportSvg.addRectangle(x, width - 37.5, postWidth, postWidth, style); // Lower post

        // Posts after the first ones
        for (int i = 1; i < postCount; i++) {
            // Calculate the x position for the next post based on spacing
            x = 100 + i * spacing;

            // Ensure the last post is placed within the carport's length
            if (x + postWidth > length - 100) {  // If placing a post exceeds the carport's length
                x = length - postWidth - 30;  // Place the last post at 30cm from the end
            }

            // Placing posts
            carportSvg.addRectangle(x, 32.5, postWidth, postWidth, style); // Upper post
            carportSvg.addRectangle(x, width - 37.5, postWidth, postWidth, style); // Lower post
        }
    }

    // Method for adding arrows and text informing about the measurements outside the carport drawing
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
