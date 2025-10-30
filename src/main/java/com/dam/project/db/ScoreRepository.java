package com.dam.project.db;

import com.dam.project.score.Score;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScoreRepository {
    private final DatabaseManager db;

    public ScoreRepository(DatabaseManager db) { this.db = db; }

    public void insert(String name, int points, double survivalTime) {
        String sql = ""INSERT INTO scores (name, points, survival_time, created_at) VALUES (?, ?, ?, datetime('now'))"";
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, points);
            ps.setDouble(3, survivalTime);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Score> topN(int n) {
        List<Score> out = new ArrayList<>();
        String sql = ""SELECT name, points, survival_time, created_at FROM scores ORDER BY points DESC LIMIT ?"";
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, n);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Score(
                        rs.getString(""name""),
                        rs.getInt(""points""),
                        rs.getDouble(""survival_time""),
                        rs.getString(""created_at"")
                    ));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return out;
    }
}