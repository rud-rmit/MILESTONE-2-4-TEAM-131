package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExportTool {
    public static void main(String[] args) {

        String dbUrl = "jdbc:sqlite:Road_Accidentsv2.db";

        Path outputPath = Paths.get("src/main/resources/tool1_data.json");

        String query = """
                WITH CleanedCrashes AS (
                    SELECT
                        accident_no,
                        CASE
                            WHEN ATMOSPH_COND IN ('Clear') THEN 'Clear'
                            WHEN ATMOSPH_COND IN ('Raining') THEN 'Raining'
                            WHEN ATMOSPH_COND IN ('Snowing') THEN 'Snowing'
                            WHEN ATMOSPH_COND IN ('Fog') THEN 'Fog'
                            WHEN ATMOSPH_COND IN ('Smoke') THEN 'Smoke'
                            WHEN ATMOSPH_COND IN ('Dust') THEN 'Dust'
                            WHEN ATMOSPH_COND IN ('Strong winds') THEN 'Strong winds'
                            ELSE 'Not known'
                        END AS w,
                        CASE
                            WHEN SURFACE_COND IN ('Dry') THEN 'Dry'
                            WHEN SURFACE_COND IN ('Wet') THEN 'Wet'
                            WHEN SURFACE_COND IN ('Muddy') THEN 'Muddy'
                            WHEN SURFACE_COND IN ('Snowy') THEN 'Snowy'
                            WHEN SURFACE_COND IN ('Icy') THEN 'Icy'
                            ELSE 'Unk.'
                        END AS r
                    FROM crashes
                )
                SELECT w, r, COUNT(DISTINCT accident_no) AS c
                FROM CleanedCrashes
                WHERE NOT (w = 'Not known' AND r = 'Unk.')
                GROUP BY w, r
                """;

        try (Connection conn = DriverManager.getConnection(dbUrl);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("Processing accident records and converting IDs to crash counts...");
            StringBuilder json = new StringBuilder("[\n");

            while (rs.next()) {
                String weather = rs.getString("w");
                String road = rs.getString("r");
                int count = rs.getInt("c");

                json.append(String.format("  {\"w\": \"%s\", \"r\": \"%s\", \"c\": %d},\n",
                        weather, road, count));
            }

            if (json.length() > 2) {
                json.setLength(json.length() - 2);
            }
            json.append("\n]");

            Files.writeString(outputPath, json.toString());
            System.out.println("Success! Tool 1 data file written to: " + outputPath.toAbsolutePath());

        } catch (Exception e) {
            System.out.println("Database export failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
