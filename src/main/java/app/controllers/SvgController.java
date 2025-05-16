package app.controllers;

import app.persistence.CarportSvg;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.Locale;

public class SvgController {

    public static void addRoutes(Javalin app) {
        app.post("/svg", SvgController::showSvg);
    }

    // Method for displaying carport SVG drawing at svg.html
    public static void showSvg(Context ctx) {
        Locale.setDefault(Locale.US);

        String lengthStr = ctx.formParam("Længde");
        String widthStr = ctx.formParam("Bredde");

        int length = Integer.parseInt(lengthStr);
        int width = Integer.parseInt(widthStr);

        CarportSvg svg = new CarportSvg(width, length);

        // Set the svg as an attribute to render it
        ctx.attribute("svg", svg.toString());
        ctx.render("svg.html");
    }
}
