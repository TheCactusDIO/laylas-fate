package com.dam.project.db;

import java.sql.*;

public class DatabaseManager {
    private final String url;

    public DatabaseManager(String dbPath) {
        this.url = dbPath; // ej: "jdbc:sqlite:laylas_fate.db"
        init();
    }

    private void init() {
        try (Connection con = DriverManager.getConnection(url);
             Statement st = con.createStatement()) {
            String ddl = "CREATE TABLE IF NOT EXISTS scores (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                         "name TEXT," +
                         "points INTEGER," +
                         "survival_time REAL," +
                         "created_at TEXT)";
            st.execute(ddl);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }
}