package app.controllers;

import app.persistence.ConnectionPool;
import app.persistence.OrderMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarportController {
    public static void addRoutes(Javalin app, ConnectionPool connectionPool) {
        app.get("/index",ctx -> getCarportDimension(ctx, connectionPool));
    }
    private static void getCarportDimension(Context ctx, ConnectionPool connectionPool) {
        List<Integer> carportLength = OrderMapper.getCarportLength(connectionPool);
        List<Integer> carportWidth = OrderMapper.getCarportWidth(connectionPool);

        Map<String, Object> model = new HashMap<>();
        model.put("carportLength", carportLength);
        model.put("carportWidth", carportWidth);


        ctx.attribute("carportLength", carportLength);
        ctx.attribute("carportWidth", carportWidth);
        ctx.render("index.html",model);
    }
}
