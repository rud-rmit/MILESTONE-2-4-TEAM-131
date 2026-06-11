package app;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Class for Managing the JDBC Connection to a SQLLite Database.
 * Allows SQL queries to be used with the SQLLite Databse in Java.
 *
 * @author Timothy Wiley, 2023. email: timothy.wiley@rmit.edu.au
 * @author Santha Sumanasekara, 2021. email: santha.sumanasekara@rmit.edu.au
 */
public class JDBCConnection {

    private static final String OUTPUT_CSV = "clean_road_accidents.csv";

    public JDBCConnection() {
        System.out.println("Created JDBC Connection Object");
    }

    public static void main(String[] args) {
        String dbPath = "database/Road_Accidentsv2.db";
        File dbFile = new File(dbPath);

        if (!dbFile.exists() || dbFile.length() == 0) {
            System.out.println("Default path not found. Opening file picker window...");
            JFileChooser chooser = new JFileChooser(System.getProperty("user.home") + "/Desktop");
            chooser.setDialogTitle("Select your Road_Accidentsv2.db file");
            chooser.setFileFilter(new FileNameExtensionFilter("SQLite Database (*.db)", "db"));

            int returnVal = chooser.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                dbFile = chooser.getSelectedFile();
                dbPath = dbFile.getAbsolutePath();
            } else {
                System.err.println("Database selection cancelled. Program stopped.");
                return;
            }
        }

        System.out.println("Using database file at: " + dbFile.getAbsolutePath());
        String connectionUrl = "jdbc:sqlite:" + dbPath;

        try (Connection conn = DriverManager.getConnection(connectionUrl)) {
            DatabaseMetaData meta = conn.getMetaData();

            String hospitalTable = findTable(meta, "TAKEN_HOSPITAL");
            String ageTable = findTable(meta, "AGE_GROUP");
            String injTable = findTable(meta, "INJ_LEVEL");
            String surfaceTable = findTable(meta, "SURFACE_COND");
            String atmosphTable = findTable(meta, "ATMOSPH_COND");

            if (hospitalTable == null || ageTable == null || injTable == null || surfaceTable == null
                    || atmosphTable == null) {
                System.err.println("Error: Required columns are missing inside the database file.");
                return;
            }

            Set<String> tables = new LinkedHashSet<>(
                    Arrays.asList(hospitalTable, ageTable, injTable, surfaceTable, atmosphTable));
            Iterator<String> it = tables.iterator();
            String baseTable = it.next();

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT DISTINCT ").append(baseTable).append(".ACCIDENT_NO, ")
                    // Adjusted to handle 'y' and 'n' values properly
                    .append("CASE LOWER(TRIM(").append(hospitalTable)
                    .append(".TAKEN_HOSPITAL)) WHEN 'y' THEN 'yes' WHEN 'n' THEN 'no' ELSE 'unknown' END as HOSPITAL, ")
                    .append("CASE WHEN ").append(ageTable).append(".AGE_GROUP IN ('0-4', '5-12', '13-15') THEN '0-15' ")
                    .append("WHEN ").append(ageTable).append(".AGE_GROUP IN ('16-17', '18-21') THEN '16-21' ")
                    .append("WHEN ").append(ageTable).append(".AGE_GROUP IN ('22-25', '26-29') THEN '22-29' ")
                    .append("WHEN ").append(ageTable).append(".AGE_GROUP IN ('30-39', '40-49', '50-59') THEN ")
                    .append(ageTable).append(".AGE_GROUP ")
                    .append("WHEN ").append(ageTable)
                    .append(".AGE_GROUP IN ('60-64', '65-69', '70+', '70-74', '75+') THEN '60+' ELSE 'Unknown' END as AGE_BIN, ")
                    // Added CAST safeguard for numerical values mapped as text
                    .append("CASE CAST(").append(injTable)
                    .append(".INJ_LEVEL AS TEXT) WHEN '1' THEN 'Fatality' WHEN '2' THEN 'Serious Injury' WHEN '3' THEN 'Other Injury' WHEN '4' THEN 'Not Injured' ELSE 'Unknown' END as INJURY, ")
                    .append("CASE CAST(").append(surfaceTable)
                    .append(".SURFACE_COND AS TEXT) WHEN '1' THEN 'Dry' WHEN '2' THEN 'Wet' WHEN '3' THEN 'Muddy' WHEN '4' THEN 'Snowy' WHEN '5' THEN 'Icy' ELSE 'Unknown' END as SURFACE, ")
                    .append("CASE CAST(").append(atmosphTable)
                    .append(".ATMOSPH_COND AS TEXT) WHEN '1' THEN 'Clear' WHEN '2' THEN 'Raining' WHEN '3' THEN 'Snowing' WHEN '4' THEN 'Fog' WHEN '5' THEN 'Smoke' WHEN '6' THEN 'Dust' WHEN '7' THEN 'Strong Winds' ELSE 'Unknown' END as ATMOSPHERE ")
                    .append("FROM ").append(baseTable);

            while (it.hasNext()) {
                String nextTable = it.next();
                sql.append(" JOIN ").append(nextTable).append(" ON ").append(baseTable).append(".ACCIDENT_NO = ")
                        .append(nextTable).append(".ACCIDENT_NO ");
            }

            // Cleaned up the filtering constraints to allow 'unknown' rows instead of
            // completely removing them
            sql.append(" WHERE HOSPITAL IN ('yes', 'no') AND AGE_BIN != 'Unknown';");

            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql.toString());
                    FileWriter csvWriter = new FileWriter(OUTPUT_CSV)) {

                csvWriter.append("ACCIDENT_NO,TAKEN_HOSPITAL,AGE_GROUP,INJ_LEVEL,SURFACE_COND,ATMOSPH_COND\n");

                int rows = 0;
                while (rs.next()) {
                    csvWriter.append(String.format("%s,%s,%s,%s,%s,%s\n",
                            rs.getString("ACCIDENT_NO"),
                            rs.getString("HOSPITAL"),
                            rs.getString("AGE_BIN"),
                            rs.getString("INJURY"),
                            rs.getString("SURFACE"),
                            rs.getString("ATMOSPHERE")));
                    rows++;
                }
                System.out.println("SUCCESS: Generated " + OUTPUT_CSV + " with " + rows + " clean data rows!");
                System.out.println("CSV Location: " + new File(OUTPUT_CSV).getAbsolutePath());
            }
        } catch (SQLException | IOException e) {
            System.err.println("Error running extraction: " + e.getMessage());
        }
    }

    private static String findTable(DatabaseMetaData meta, String columnName) throws SQLException {
        List<String> candidates = new ArrayList<>();
        String[] variations = { columnName, columnName.toLowerCase(), columnName.toUpperCase() };
        for (String var : variations) {
            try (ResultSet rs = meta.getColumns(null, null, "%", var)) {
                while (rs.next()) {
                    String table = rs.getString("TABLE_NAME");
                    if (!candidates.contains(table)) {
                        candidates.add(table);
                    }
                }
            }
        }
        for (String tableName : candidates) {
            if (tableHasAccidentNo(meta, tableName)) {
                return tableName;
            }
        }
        return candidates.isEmpty() ? null : candidates.get(0);
    }

    private static boolean tableHasAccidentNo(DatabaseMetaData meta, String tableName) throws SQLException {
        String[] variations = { "ACCIDENT_NO", "accident_no", "Accident_No" };
        for (String var : variations) {
            try (ResultSet rs = meta.getColumns(null, null, tableName, var)) {
                if (rs.next())
                    return true;
            }
        }
        return false;
    }
}