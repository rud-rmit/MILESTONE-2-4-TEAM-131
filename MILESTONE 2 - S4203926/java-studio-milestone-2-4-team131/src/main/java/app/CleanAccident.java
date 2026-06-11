package app;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class CleanAccident {

    private static final String DB_URL = "jdbc:sqlite:Road_Accidentsv2.db";
    private static final String OUTPUT_CSV = "clean_road_accidents.csv";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            DatabaseMetaData meta = conn.getMetaData();

            String hospitalTable = findTable(meta, "TAKEN_HOSPITAL");
            String ageTable = findTable(meta, "AGE_GROUP");
            String injTable = findTable(meta, "INJ_LEVEL");
            String surfaceTable = findTable(meta, "SURFACE_COND");
            String atmosphTable = findTable(meta, "ATMOSPH_COND");

            if (hospitalTable == null || ageTable == null || injTable == null || surfaceTable == null
                    || atmosphTable == null) {
                return;
            }

            Set<String> tables = new LinkedHashSet<>(
                    Arrays.asList(hospitalTable, ageTable, injTable, surfaceTable, atmosphTable));
            Iterator<String> it = tables.iterator();
            String baseTable = it.next();

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT DISTINCT ").append(baseTable).append(".ACCIDENT_NO, ")
                    .append("LOWER(TRIM(").append(hospitalTable).append(".TAKEN_HOSPITAL)) as HOSPITAL, ")
                    .append("CASE WHEN ").append(ageTable).append(".AGE_GROUP IN ('0-4', '5-12', '13-15') THEN '0-15' ")
                    .append("WHEN ").append(ageTable).append(".AGE_GROUP IN ('16-17', '18-21') THEN '16-21' ")
                    .append("WHEN ").append(ageTable).append(".AGE_GROUP IN ('22-25', '26-29') THEN '22-29' ")
                    .append("WHEN ").append(ageTable).append(".AGE_GROUP IN ('30-39', '40-49', '50-59') THEN ")
                    .append(ageTable).append(".AGE_GROUP ")
                    .append("WHEN ").append(ageTable)
                    .append(".AGE_GROUP IN ('60-64', '65-69', '70+', '70-74', '75+') THEN '60+' ELSE NULL END as AGE_BIN, ")
                    .append("CASE ").append(injTable)
                    .append(".INJ_LEVEL WHEN '1' THEN 'Fatality' WHEN '2' THEN 'Serious Injury' WHEN '3' THEN 'Other Injury' WHEN '4' THEN 'Not Injured' ELSE NULL END as INJURY, ")
                    .append("CASE ").append(surfaceTable)
                    .append(".SURFACE_COND WHEN '1' THEN 'Dry' WHEN '2' THEN 'Wet' WHEN '3' THEN 'Muddy' WHEN '4' THEN 'Snowy' WHEN '5' THEN 'Icy' ELSE NULL END as SURFACE, ")
                    .append("CASE ").append(atmosphTable)
                    .append(".ATMOSPH_COND WHEN '1' THEN 'Clear' WHEN '2' THEN 'Raining' WHEN '3' THEN 'Snowing' WHEN '4' THEN 'Fog' WHEN '5' THEN 'Smoke' WHEN '6' THEN 'Dust' WHEN '7' THEN 'Strong Winds' ELSE NULL END as ATMOSPHERE ")
                    .append("FROM ").append(baseTable);

            while (it.hasNext()) {
                String nextTable = it.next();
                sql.append(" JOIN ").append(nextTable).append(" ON ").append(baseTable).append(".ACCIDENT_NO = ")
                        .append(nextTable).append(".ACCIDENT_NO ");
            }

            sql.append(
                    " WHERE HOSPITAL IN ('yes', 'no') AND AGE_BIN IS NOT NULL AND INJURY IS NOT NULL AND SURFACE IS NOT NULL AND ATMOSPHERE IS NOT NULL;");

            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql.toString());
                    FileWriter csvWriter = new FileWriter(OUTPUT_CSV)) {

                csvWriter.append("ACCIDENT_NO,TAKEN_HOSPITAL,AGE_GROUP,INJ_LEVEL,SURFACE_COND,ATMOSPH_COND\n");

                while (rs.next()) {
                    csvWriter.append(String.format("%s,%s,%s,%s,%s,%s\n",
                            rs.getString("ACCIDENT_NO"),
                            rs.getString("HOSPITAL"),
                            rs.getString("AGE_BIN"),
                            rs.getString("INJURY"),
                            rs.getString("SURFACE"),
                            rs.getString("ATMOSPHERE")));
                }
            }
        } catch (SQLException | IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private static String findTable(DatabaseMetaData meta, String columnName) throws SQLException {
        try (ResultSet rs = meta.getColumns(null, null, "%", columnName)) {
            if (rs.next()) {
                return rs.getString("TABLE_NAME");
            }
        }
        try (ResultSet rs = meta.getColumns(null, null, "%", columnName.toLowerCase())) {
            if (rs.next()) {
                return rs.getString("TABLE_NAME");
            }
        }
        return null;
    }
}