package com.dam.project;

import com.dam.project.db.DatabaseManager;
import com.dam.project.db.ScoreRepository;
import com.dam.project.score.Score;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class DbSmokeTest {
    @Test
    void canCreateInsertAndReadTop() throws Exception {
        // BD de test en archivo (estable para múltiples conexiones)
        Path dbPath = Path.of("target", "lf_test.sqlite");
        Files.deleteIfExists(dbPath); // limpio
        String url = "jdbc:sqlite:" + dbPath.toString();

        DatabaseManager db = new DatabaseManager(url); // crea tabla en init()
        ScoreRepository repo = new ScoreRepository(db);

        repo.insert("AAA", 15, 10.0);
        repo.insert("BBB", 25, 12.5);
        repo.insert("CCC", 5,  3.2);

        List<Score> top2 = repo.topN(2);
        assertEquals(2, top2.size(), "Debe devolver 2 filas");
        assertEquals("BBB", top2.get(0).name);
        assertTrue(top2.get(0).points >= top2.get(1).points);
    }
}