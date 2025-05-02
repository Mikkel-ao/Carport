package app.controllers;
import app.persistence.CarportSvg;
import java.util.Locale;

import app.persistence.ConnectionPool;
import io.javalin.Javalin;
import io.javalin.http.Context;


// TODO: Consider moving this to OrderController instead once it is implemented.
public class SvgController {

    public static void addRoutes(Javalin app, ConnectionPool connectionPool) {
        app.get("/svg", SvgController::showSvg);  // Correct route definition
    }

    public static void showSvg(Context ctx) {
        Locale.setDefault(Locale.US);
        CarportSvg topView = new CarportSvg(600, 780); // Example dimensions
        ctx.attribute("svg", topView.toString());
        ctx.render("svg.html");
    }
}

