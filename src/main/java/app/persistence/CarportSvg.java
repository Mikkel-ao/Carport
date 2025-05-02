package app.persistence;

public class CarportSvg {
    private int width;
    private int length;
    private Svg carportSvg;

    public CarportSvg(int width, int length) {
        this.width = width;
        this.length = length;
        carportSvg = new Svg(0, 0, "0 0 855 690", "700px");

        drawBase();
        addBeams();
        addRafters();
        addDimensions();
    }

    private void drawBase() {
        carportSvg.addRectangle(0, 0, 600, 780, "stroke-width:1px; stroke:#000000; fill: #ffffff");
    }
    // Rem
    private void addBeams() {
        String beamStyle = "stroke-width:1px; stroke:#000000; fill: #ffffff";
        carportSvg.addRectangle(0, 35, 4.5, 780, beamStyle);
        carportSvg.addRectangle(0, 565, 4.5, 780, beamStyle);
    }
    // Spær // TODO: Check does this match Calculator method?
    private void addRafters() {
        String rafterStyle = "stroke:#000000; fill: #ffffff";
        for (double i = 0; i < 780; i += 55.714) {
            carportSvg.addRectangle(i, 0.0, width, 4.5, rafterStyle);
        }
    }

    private void addDimensions() {
        // Horizontal dimension
        carportSvg.addArrow(0, width+10, length, width+10, "stroke:black; stroke-width:1");
        carportSvg.addText(length/2, width+25, 0, "780 cm");

        // Vertical dimension
        carportSvg.addArrow(length+10, 0, length+10, width, "stroke:black; stroke-width:1");
        carportSvg.addText(length+15, width/2, 90, "600 cm");

        // Inner vertical dimension

    }

    @Override
    public String toString() {
        return carportSvg.toString();
    }
}
