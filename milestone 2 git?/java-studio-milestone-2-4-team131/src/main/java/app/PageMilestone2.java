package app;

import io.javalin.http.Context;
import io.javalin.http.Handler;

public class PageMilestone2 implements Handler {
    public static final String URL = "Milestone2";

    @Override
    public void handle(Context context) throws Exception {
        // Redirect the user to the actual HTML file
        context.redirect("/1-home.html");
    }
}