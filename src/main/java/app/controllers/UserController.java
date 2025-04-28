package app.controllers;

import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.UserMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.eclipse.jetty.server.Authentication;

public class UserController {

    public static void addRoutes(Javalin app, ConnectionPool connectionPool) {
        app.post("/login", ctx -> login(ctx, connectionPool));
        app.get("/login", ctx -> ctx.render("/login.html"));
        app.get("/index", ctx -> ctx.render("index.html"));
        app.get("/createuser", ctx -> ctx.render("createuser.html"));
        app.post("/createuser", ctx -> createUser(ctx, connectionPool));
        app.get("/logout", ctx -> logout(ctx));
    }

    //Method for logging out
    private static void logout(Context ctx) {
        //Gets the current session and destroys it. Removing all stored attributes.
        ctx.req().getSession().invalidate();
        //After destroying the session it returns to the index(main page)
        ctx.redirect("/");
    }

    //Method for logging in
    private static void login(Context ctx, ConnectionPool connectionPool) {
        //Retrieving the value of the form field "email" and "password" from the login.html page
        String username = ctx.formParam("email");
        String password = ctx.formParam("password");

        //Validating user credentials (email and password), storing in a User object
        try {
            Authentication.User user = UserMapper.login(username, password, connectionPool);

            //If successful, user details are stored in the session
            ctx.sessionAttribute("currentUser", user.getUserId());
            ctx.sessionAttribute("userId", user.getUserId());
            ctx.sessionAttribute("role", user.getRole());
            ctx.sessionAttribute("email", user.getEmail());
            ctx.redirect("/index");
        } catch (DatabaseException e) {
            //If login fails, error message shown to user and login page re-rendered
            ctx.attribute("message", "Login failed. Please try again.");
            ctx.render("/login.html");
        }
    }

    //Method for creating a user/customer
    private static void createUser(Context ctx, ConnectionPool connectionPool) {
        //Retrieving the value of the form field "email", "password1" and "password2 from the createuser.html page
        String email = ctx.formParam("email");
        String password1 = ctx.formParam("password1");
        String password2 = ctx.formParam("password2");

        //If passwords don't match, message sent to User to let them know
        if (!password1.equals(password2)) {
            ctx.attribute("message", "Passwords do not match, try again.");
            ctx.render("/createuser.html");
            return;
        }

        //Checking if a user already exists or not
        try {
            //If email is unique, user is created, success message sent to user and user is sent to login page
            UserMapper.createUser(password1, email, connectionPool);
            ctx.attribute("message", "User created successfully. Please log in.");
            ctx.render("/login.html");
        } catch (DatabaseException e) {
            //If user already exists, message sent to say that a user with those credentials already exists, try again
            ctx.attribute("message", "User already exists. Try again or log in.");
            ctx.render("/createuser.html");
        }
    }
}
