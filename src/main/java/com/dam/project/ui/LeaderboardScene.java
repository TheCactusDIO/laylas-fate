package com.dam.project.ui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import com.dam.project.db.ScoreRepository;
import com.dam.project.db.DatabaseManager;
import com.dam.project.score.Score;          // <- el modelo real que devuelve el repo

import java.util.List;
import java.util.stream.Collectors;

/**
 * LeaderboardScene muestra las puntuaciones más altas guardadas en la base de datos.
 * Es una escena independiente a la que navegaremos desde el menú o desde Game Over.
 */
public class LeaderboardScene {

    /**
     * Crea y muestra la escena del ranking.
     *
     * @param stage  Stage principal de la aplicación
     * @param onBack Acción a ejecutar cuando el usuario pulse “Volver”
     */
    public static void show(Stage stage, Runnable onBack) {

        // --- Layout principal con fondo oscuro ---
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #121212; -fx-padding: 30;");

        // --- Título ---
        Label title = new Label("🏆 Leaderboard");
        title.setTextFill(Color.WHITE);
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        BorderPane.setAlignment(title, Pos.CENTER);
        root.setTop(title);

        // --- Tabla ---
        TableView<ScoreRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setStyle("-fx-background-color: #1E1E1E; -fx-text-fill: white;");

        // Columnas (mapean contra getters de ScoreRow: getName(), getPoints(), etc.)
        TableColumn<ScoreRow, String> nameCol = new TableColumn<>("Jugador");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<ScoreRow, Integer> pointsCol = new TableColumn<>("Puntuación");
        pointsCol.setCellValueFactory(new PropertyValueFactory<>("points"));

        TableColumn<ScoreRow, Double> timeCol = new TableColumn<>("Tiempo (s)");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("survivalTime"));

        TableColumn<ScoreRow, String> dateCol = new TableColumn<>("Fecha");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        table.getColumns().addAll(nameCol, pointsCol, timeCol, dateCol);

        // --- Datos desde SQLite ---
        // Creamos el repo contra el archivo persistente del juego.
        DatabaseManager dbm = new DatabaseManager("jdbc:sqlite:laylas_fate.db");
        ScoreRepository repo = new ScoreRepository(dbm);

        // 1) Obtenemos List<Score> desde el repositorio
        List<Score> topScores = repo.topN(10);

        // 2) Convertimos cada Score -> ScoreRow (clase para la TableView)
        List<ScoreRow> rows = topScores.stream()
                .map(s -> new ScoreRow(s.name, s.points, s.survivalTime, s.createdAt))
                .collect(Collectors.toList());

        // 3) Poblar la tabla
        table.getItems().setAll(rows);

        root.setCenter(table);

        // --- Footer con botón Volver ---
        Button backBtn = new Button("⬅ Volver");
        backBtn.setOnAction(e -> {
            if (onBack != null) onBack.run();
        });

        HBox footer = new HBox(backBtn);
        footer.setAlignment(Pos.CENTER);
        footer.setSpacing(10);
        footer.setStyle("-fx-padding: 15;");
        root.setBottom(footer);

        // --- Mostrar escena ---
        Scene scene = new Scene(root, 640, 480);
        stage.setScene(scene);
    }

    /**
     * Fila adaptadora para TableView (propiedades simples con getters).
     * OJO: los nombres deben coincidir con los de PropertyValueFactory.
     */
    public static class ScoreRow {
        private final String name;
        private final int points;
        private final double survivalTime;
        private final String createdAt;

        public String getName() { return name; }
        public int getPoints() { return points; }
        public double getSurvivalTime() { return survivalTime; }
        public String getCreatedAt() { return createdAt; }

        public ScoreRow(String name, int points, double survivalTime, String createdAt) {
            this.name = name;
            this.points = points;
            this.survivalTime = survivalTime;
            this.createdAt = createdAt;
        }
    }
}
