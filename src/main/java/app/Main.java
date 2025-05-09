package app;

import app.config.SessionConfig;
import app.config.ThymeleafConfig;
import app.controllers.OrderController;
import app.controllers.SvgController;
import app.controllers.UserController;
import app.entities.Order;
import app.entities.OrderItem;
import app.persistence.ConnectionPool;
import app.persistence.OrderMapper;
import app.persistence.UserMapper;
import app.util.Calculator;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;

import java.util.List;

public class Main {


    private static final ConnectionPool connectionPool = ConnectionPool.getInstance();


    public static void main(String[] args) {
        // Initializing Javalin and Jetty webserver.
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
            config.jetty.modifyServletContextHandler(handler ->  handler.setSessionHandler(SessionConfig.sessionConfig()));
            config.fileRenderer(new JavalinThymeleaf(ThymeleafConfig.templateEngine()));
        }).start(7070);

        //OrderController.createListOfMaterials(400, 750, connectionPool);

        // Routing
        app.get("/", ctx -> ctx.redirect("/index"));

        UserController.addRoutes(app, connectionPool);
        SvgController.addRoutes(app, connectionPool);
        OrderController.addRoutes(app, connectionPool);
    }
}