package app.controllers;
import app.persistence.CarportSvg;
import java.util.Locale;

import app.persistence.ConnectionPool;
import io.javalin.Javalin;
import io.javalin.http.Context;


// TODO: Consider moving this to OrderController instead once it is implemented.
public class SvgController {

    public static void addRoutes(Javalin app, ConnectionPool connectionPool) {
        app.get("/svg", SvgController::showSvg);
    }

    public static void showSvg(Context ctx) {
        Locale.setDefault(Locale.US);
        CarportSvg topView = new CarportSvg(600, 780); // Example dimensions
        CarportSvg topView2 = new CarportSvg(400, 580); // Second smaller one
        CarportSvg topView3 = new CarportSvg(200, 459); // Second smaller one


        ctx.attribute("svg", topView.toString());
        ctx.attribute("svg2", topView2.toString());
        ctx.attribute("svg3", topView3.toString());
        ctx.render("svg.html");
    }
}

