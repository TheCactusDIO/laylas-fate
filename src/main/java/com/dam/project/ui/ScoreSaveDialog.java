package com.dam.project.ui;

import com.dam.project.db.ScoreRepository;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ScoreSaveDialog {
    public static void show(Stage owner, ScoreRepository repo, int points, double time, Runnable onRetry, Runnable onExit) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Game Over – Save Score");

        Label lblTitle = new Label("Your Score: " + points + "  |  Time: " + String.format(java.util.Locale.US, "%.1f s", time));
        TextField txtName = new TextField();
        txtName.setPromptText("Enter your name");

        Button btnSave = new Button("Save");
        Button btnRetry = new Button("Retry");
        Button btnExit = new Button("Exit");

        Label lblStatus = new Label();

        btnSave.setOnAction(e -> {
            String name = txtName.getText().trim();
            if (name.isEmpty()) {
                lblStatus.setText("Please enter your name");
                return;
            }
            repo.insert(name, points, time);
            lblStatus.setText("Saved!");
        });

        btnRetry.setOnAction(e -> {
            dialog.close();
            if (onRetry != null) onRetry.run();
        });

        btnExit.setOnAction(e -> {
            dialog.close();
            if (onExit != null) onExit.run();
        });

        VBox root = new VBox(10, lblTitle, txtName, btnSave, btnRetry, btnExit, lblStatus);
        root.setPadding(new Insets(15));
        Scene scene = new Scene(root, 320, 220);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}