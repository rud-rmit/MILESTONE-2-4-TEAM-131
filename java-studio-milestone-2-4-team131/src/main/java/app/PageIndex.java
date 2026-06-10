package app;

import java.util.ArrayList;

import io.javalin.http.Context;
import io.javalin.http.Handler;

/**
 * Example Index HTML class using Javalin
 * <p>
 * Generate a static HTML page using Javalin
 * by writing the raw HTML into a Java String object
 *
 * @author Timothy Wiley, 2023. email: timothy.wiley@rmit.edu.au
 * @author Santha Sumanasekara, 2021. email: santha.sumanasekara@rmit.edu.au
 */
public class PageIndex implements Handler {

    // URL of this page relative to http://localhost:7001/
    public static final String URL = "/";

    @Override
    public void handle(Context context) throws Exception {
        // Create a simple HTML webpage in a String
        String html = "<html>";

        // Add some Header information
        html = html + "<head>" +
                "<title>Homepage</title>";

        // Add some CSS (external file)
        html = html + "<link rel='stylesheet' type='text/css' href='common.css' />";
        html = html + "</head>";

        // Add the body
        html = html + "<body>";

        // Add the topnav
        // This uses a Java v15+ Text Block
        html = html + """
                    <div class='topnav'>
                        <a href='/'>Prog. 2 Sample</a>
                        <a href='Milestone2'>Milestone 2</a>
                    </div>
                """;

        // Add header content block
        html = html + """
                    <div class='header'>
                        <h1>
                            <img src='logo.png' class='top-image' alt='RMIT logo' height='75'>
                            Homepage
                        </h1>
                    </div>
                """;

        // Add Div for page Content
        html = html + """
                <div class='content'>
                    <p>Added the milestone 2 webpage inplace of the prog2x webpage</p>
                </div>
                """;

        // Footer
        html = html + """
                    <div class='footer'>
                        <p><strong>[STUDIO PROJECT 131]
                        RUDRA SHARMA - S4203926</strong></p>
                    </div>
                """;

        // Finish the HTML webpage
        html = html + "</body>" + "</html>";

        // DO NOT MODIFY THIS
        // Makes Javalin render the webpage
        context.html(html);
    }

}
