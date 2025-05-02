package app.persistence;

public class CarportSvg {
    private int width;
    private int length;
    private Svg carportSvg;

    public CarportSvg(int width, int length) {
        this.width = width;
        this.length = length;
        carportSvg = new Svg(0, 0, "0 0 855 690", "75%");

        drawBase();
        addBeams();
        addRafters();
        addDimensions();
    }

    private void drawBase() {
        carportSvg.addRectangle(0, 0, 600, 780, "stroke-width:1px; stroke:#000000; fill: #ffffff");
    }

    private void addBeams() {
        String beamStyle = "stroke-width:1px; stroke:#000000; fill: #cfcfcf";
        carportSvg.addRectangle(0, 35, 4.5, 780, beamStyle);
        carportSvg.addRectangle(0, 565, 4.5, 780, beamStyle);
    }

    private void addRafters() {
        String rafterStyle = "stroke:#000000; fill: #aaaaaa";
        for (double i = 0; i < 780; i += 55.714) {
            carportSvg.addRectangle(i, 0.0, 600, 4.5, rafterStyle);
        }
    }

    private void addDimensions() {
        // Horizontal dimension (width)
        carportSvg.addArrow(0, 610, 780, 610, "stroke:black; stroke-width:1");
        carportSvg.addText(370, 625, 0, "780 cm");

        // Vertical dimension (height)
        carportSvg.addArrow(790, 0, 790, 600, "stroke:black; stroke-width:1");
        carportSvg.addText(795, 300, 90, "600 cm");
    }

    @Override
    public String toString() {
        return carportSvg.toString();
    }
}
