package app.controllers;

import app.persistence.ConnectionPool;
import app.persistence.OrderMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarportController {
    public static void addRoutes(Javalin app, ConnectionPool connectionPool) {
        System.out.println("DEBUG: Registering /index route");
        app.get("/index",ctx -> getCarportDimension(ctx, connectionPool));
    }
    private static void getCarportDimension(Context ctx, ConnectionPool connectionPool) {
        // todo slet senere sout
        System.out.println("DEBUG: Entering getCarportDimension method!");
        List<Integer> carportLength = OrderMapper.getCarportLength(connectionPool);
        List<Integer> carportWidth = OrderMapper.getCarportWidth(connectionPool);
        // todo slet senere sout
        System.out.println("DEBUG: Using test data -> " + carportLength);


        ctx.attribute("carportLength", carportLength);
        ctx.attribute("carportWidth", carportWidth);
        ctx.render("index.html");
    }
}
