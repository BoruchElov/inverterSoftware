package org.example.invertersoftware;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class mainClass extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(mainClass.class.getResource("modbusSoftware.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1500, 860);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/images/control-system.png")).toExternalForm()));
        stage.setResizable(false);
        stage.setTitle("Controller Interaction App");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}