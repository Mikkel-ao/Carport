package app;

import app.config.SessionConfig;
import app.config.ThymeleafConfig;
import app.controllers.UserController;
import app.persistence.ConnectionPool;
import app.persistence.UserMapper;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;

public class Main {

    private static final String USER = "gruppe10";
    private static final String PASSWORD = "gruppe10";
    private static final String URL = "jdbc:postgresql://207.154.198.27:5433/%s?currentSchema=public";
    private static final String DB = "fog";

    private static final ConnectionPool connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);
    private static UserMapper userMapper = new UserMapper();


    public static void main(String[] args) {
        // Initializing Javalin and Jetty webserver.
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
            config.jetty.modifyServletContextHandler(handler ->  handler.setSessionHandler(SessionConfig.sessionConfig()));
            config.fileRenderer(new JavalinThymeleaf(ThymeleafConfig.templateEngine()));
        }).start(7070);


        // Routing
        app.get("/", ctx -> ctx.render("index.html"));

        UserController.addRoutes(app, connectionPool);
    }
}