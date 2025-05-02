package app.persistence;

public class CarportSvg {
    private int width;
    private int length;
    private Svg carportSvg;

    public CarportSvg(int width, int length) {
        this.width = width;
        this.length = length;
        // carportSvg = new Svg(0, 0, "0 0 855 690", "700px");
        String viewBox = "0 0 " + (length+50) + " " + (width+50); // +50 to allow content outside of carport measurements
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
        // TODO: Check does this match Calculator method? Needs to know how to place the last one correctly.
        for (double i = 0; i < length; i += 55.714) {
            carportSvg.addRectangle(i, 0.0, width, 4.5, rafterStyle);
        }
    }

    private void addDimensions() {
        // Horizontal dimension of the drawing, length of the carport
        carportSvg.addArrow(0, width+10, length, width+10, "stroke:black; stroke-width:1");
        carportSvg.addText(length/2, width+25, 0, length+" cm");

        // Vertical dimension of the drawing, width of the carport
        carportSvg.addArrow(length+20, 0, length+20, width, "stroke:black; stroke-width:1");
        carportSvg.addText(length+25, width/2, 90, width+" cm");

        // Inner vertical dimension

    }

    @Override
    public String toString() {
        return carportSvg.toString();
    }
}
