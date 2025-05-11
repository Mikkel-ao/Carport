package app.controllers;

import app.persistence.CarportSvg;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.Locale;

public class SvgController {

    public static void addRoutes(Javalin app) {
        app.get("/svg", SvgController::showSvg);
    }

    public static void showSvg(Context ctx) {
        Locale.setDefault(Locale.US);

        // Retrieves user input from url, "længde" could return "780".
        String lengthStr = ctx.queryParam("Længde");
        String widthStr = ctx.queryParam("Bredde");

        // From string to int to work as parameters for CarportSvg()
        int length = Integer.parseInt(lengthStr);
        int width = Integer.parseInt(widthStr);
        
        CarportSvg svg = new CarportSvg(width, length);

        // Pass the generated SVG to the template
        ctx.attribute("svg", svg.toString());

        // Render the SVG page
        ctx.render("svg.html");
    }
}
