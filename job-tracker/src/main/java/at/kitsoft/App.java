package at.kitsoft;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        // TextField for the Auth Token
        TextField token = new TextField();
        token.setPromptText("Paste Token from Website");
        token.setLayoutX(140);
        token.setLayoutY(64);
        token.setPrefHeight(40);
        token.setPrefWidth(330);
        token.setStyle("-fx-text-fill: #ffffff; -fx-background-color: #2b2b2b; -fx-border-color: #FFEF00 ; -fx-border-radius: 5; -fx-background-radius: 5;");
        token.setAlignment(Pos.CENTER);

        Button install = new Button("Install");
        install.setStyle("-fx-background-radius: 5;");
        install.setLayoutX(532);
        install.setLayoutY(356);
        install.setPrefHeight(40);
        install.setPrefWidth(50);

        Label auth = new Label("Auth Token:");
        auth.setLayoutX(225);
        auth.setLayoutY(46);
        auth.setPrefHeight(18);
        auth.setPrefWidth(152);
        auth.setAlignment(Pos.CENTER);
        auth.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 16;");

        Label ets2id = new Label("Euro Truck Simulator 2 Install Directory");
        ets2id.setLayoutY(147);
        ets2id.setPrefHeight(18);
        ets2id.setPrefWidth(300);
        ets2id.setAlignment(Pos.CENTER);
        ets2id.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 16;");

        TextField ets2InstallDir = new TextField();
        ets2InstallDir.setPromptText("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Euro Truck Simulator 2");
        ets2InstallDir.setLayoutX(134);
        ets2InstallDir.setLayoutY(165);
        ets2InstallDir.setPrefHeight(40);
        ets2InstallDir.setPrefWidth(330);

        Button ets2Browse = new Button("Browse");
        ets2Browse.setStyle("-fx-background-radius: 5;");
        ets2Browse.setLayoutX(474);
        ets2Browse.setLayoutY(165);
        ets2Browse.setPrefHeight(40);
        ets2Browse.setPrefWidth(90);

        AnchorPane root = new AnchorPane();
        root.setStyle("-fx-background-color: #414141ff;");
        root.getChildren().addAll(token, install, auth, ets2id, ets2InstallDir, ets2Browse);
        Scene scene = new Scene(root, 600, 400);

        stage.setScene(scene);
        stage.setTitle("Job Tracker");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}