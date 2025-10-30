package com.dam.project;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.Set;

import com.dam.project.engine.GameLoop;
import com.dam.project.entities.Player;

public class App extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Input
        Set<String> keys = new HashSet<>();

        // Player centrado
        Player player = new Player(WIDTH / 2.0 - 18, HEIGHT / 2.0 - 18);

        // Escena
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);

        // Gestion de teclas
        scene.setOnKeyPressed(e -> {
            String code = e.getCode().toString();
            keys.add(code);
        });
        scene.setOnKeyReleased(e -> {
            String code = e.getCode().toString();
            keys.remove(code);
        });

        // Game loop
        GameLoop loop = new GameLoop(gc, player, keys);
        loop.start();

        stage.setTitle("Layla's Fate");
        stage.setScene(scene);
        stage.show();

        // Foco para recibir teclas
        canvas.requestFocus();
    }

    public static void main(String[] args) { launch(); }
}