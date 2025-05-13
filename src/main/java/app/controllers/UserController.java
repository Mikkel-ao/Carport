package app.controllers;

import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.UserMapper;
import app.entities.User;
import io.javalin.Javalin;
import io.javalin.http.Context;

import static app.controllers.OrderController.getOrderDetails;

public class UserController {

    public static void addRoutes(Javalin app, ConnectionPool connectionPool) {
        app.post("/login", ctx -> login(ctx, connectionPool));
        app.get("/login", ctx -> ctx.render("/login.html"));
        app.get("/createuser", ctx -> ctx.render("/createuser.html"));
        app.post("/createuser", ctx -> createUser(ctx, connectionPool));
        app.get("/logout", ctx -> logout(ctx));
        app.get("/admin", ctx -> {
            getCustomerDetails(ctx, connectionPool);
            ctx.render("/admin.html");
        });
        app.get("/customer", ctx -> {
            getCustomerDetails(ctx, connectionPool);
            getOrderDetails(ctx, connectionPool);
            ctx.render("customer.html");
        });
    }

    private static void logout(Context ctx) {
        ctx.req().getSession().invalidate();
        ctx.redirect("/");
    }

    private static void login(Context ctx, ConnectionPool connectionPool) {
        String username = ctx.formParam("email");
        String password = ctx.formParam("password");


        try {
            User user = UserMapper.login(username, password, connectionPool);

            ctx.sessionAttribute("currentUser", user.getUserId());
            ctx.sessionAttribute("userId", user.getUserId());
            ctx.sessionAttribute("role", user.getRole());
            ctx.sessionAttribute("email", user.getEmail());
            ctx.redirect("/index");
        } catch (DatabaseException e) {
            ctx.attribute("message", "Login failed. Please try again.");
            ctx.render("/login.html");
        }
    }

    private static void createUser(Context ctx, ConnectionPool connectionPool) {
        String email = ctx.formParam("email");
        String password1 = ctx.formParam("password1");
        String password2 = ctx.formParam("password2");
        String phone = ctx.formParam("phone");
        String zipCode = ctx.formParam("zipCode");
        String homeAdress = ctx.formParam("homeAddress");
        String fullName = ctx.formParam("fullName");


        if (!password1.equals(password2)) {
            ctx.attribute("message", "Passwords do not match, try again.");
            ctx.render("/createuser.html");
            return;
        }

        try {
            UserMapper.createUser(email, password1,phone,zipCode,homeAdress,fullName, connectionPool);
            ctx.attribute("message", "User created successfully. Please log in.");
            ctx.render("/login.html");
        } catch (DatabaseException e) {
            ctx.attribute("message", "User already exists. Try again or log in.");
            ctx.render("/createuser.html");
        }
    }

    private static void getCustomerDetails(Context ctx, ConnectionPool connectionPool) {
        Integer userId = ctx.sessionAttribute("userId");

        try {
            User user = UserMapper.getUserById(userId, connectionPool);
            ctx.attribute("user", user);
        } catch (DatabaseException e) {
            ctx.attribute("message", "Could not retrieve user details.");
            ctx.render("/login.html");
        }
    }
}
