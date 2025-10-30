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
        Set<String> keys = new HashSet<>();

        Player player = new Player(WIDTH / 2.0 - 18, HEIGHT / 2.0 - 18);

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);

        scene.setOnKeyPressed(e -> keys.add(e.getCode().toString()));
        scene.setOnKeyReleased(e -> keys.remove(e.getCode().toString()));

        GameLoop loop = new GameLoop(gc, player, keys, WIDTH, HEIGHT);
        loop.start();

        stage.setTitle("Layla's Fate");
        stage.setScene(scene);
        stage.show();
        canvas.requestFocus();
    }

    public static void main(String[] args) { launch(); }
}