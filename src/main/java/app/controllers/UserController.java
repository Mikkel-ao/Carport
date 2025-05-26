package app.controllers;

import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.UserMapper;
import app.entities.User;
import io.javalin.Javalin;
import io.javalin.http.Context;

import static app.controllers.OrderController.showAllOrdersForAdmin;

public class UserController {

    //Adding routes
    public static void addRoutes(Javalin app, ConnectionPool connectionPool) {
        app.post("/login", ctx -> login(ctx, connectionPool));
        app.get("/login", ctx -> ctx.render("/login.html"));
        app.get("/createuser", ctx -> ctx.render("/createuser.html"));
        app.post("/createuser", ctx -> createUser(ctx, connectionPool));
        app.get("/logout", ctx -> logout(ctx));
    }

    //Method for logging out by clearing all session attributes
    private static void logout(Context ctx) {
        ctx.req().getSession().invalidate();
        ctx.redirect("/");
    }

    //Method for logging in and storing user information in the session
    private static void login(Context ctx, ConnectionPool connectionPool) {

        //Retrieving log in information from the front end
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");


        //Trying to log in and if succeeded returning the User object
        try {
            User user = UserMapper.login(email, password, connectionPool);

            ctx.sessionAttribute("currentUser", user.getUserId());
            ctx.sessionAttribute("userId", user.getUserId());
            ctx.sessionAttribute("role", user.getRole());
            ctx.sessionAttribute("email", user.getEmail());
            ctx.redirect("/index");
        } catch (DatabaseException e) {
            //Printing stack trace for the developer to locate the bug
            e.printStackTrace();
            //Displaying an error message to the user if the log in failed!
            ctx.attribute("errorMessage", "Forkert email eller kode. Prøv igen!");
            ctx.render("/login.html");
        }
    }

    //Method for creating a new user
    private static void createUser(Context ctx, ConnectionPool connectionPool) {
        //Retrieving user information from the front end
        String email = ctx.formParam("email");
        String password1 = ctx.formParam("password1");
        String password2 = ctx.formParam("password2");
        String phone = ctx.formParam("phone");
        String zipCode = ctx.formParam("zipCode");
        String homeAdress = ctx.formParam("homeAddress");
        String fullName = ctx.formParam("fullName");


        //Making sure that the two passwords are a like
        if (!password1.equals(password2)) {
            ctx.attribute("errorMessage", "Kodeordene er ikke ens. Prøv igen!");
            ctx.render("/createuser.html");
            return;
        }

        //Trying to insert the new user to the database and returning to the login page
        try {
            UserMapper.createUser(email, password1,phone,zipCode,homeAdress,fullName, connectionPool);
            ctx.attribute("message", "Brugeren blev oprettet. Venligst log ind!");
            ctx.render("/login.html");
        } catch (DatabaseException e) {
            //Printing stack trace for the developer to locate the bug
            e.printStackTrace();
            //Displaying an error message to the user if the user could not be created as the user already exists in the database
            ctx.attribute("errorMessage", "Brugeren eksisterer allerede. Log ind eller lav en ny bruger!");
            ctx.render("/createuser.html");
        }
    }
}
