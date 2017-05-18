package com.skyworx.iso8583;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.jpos.q2.Q2;

public class MainApplication extends Application{

    public static final Q2 Q_2 = new Q2(new String[]{"-dq2-deployment","-r"});

    static {
        Q_2.start();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("ISO8583 Client App");
        GridPane mainUI = FXMLLoader.load(getClass().getResource("/com/skyworx/iso8583/ui/MainUI.fxml"));
        Scene scene = new Scene(mainUI);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            Q_2.stop();
            primaryStage.close();
            System.exit(0);
        });
    }

}
