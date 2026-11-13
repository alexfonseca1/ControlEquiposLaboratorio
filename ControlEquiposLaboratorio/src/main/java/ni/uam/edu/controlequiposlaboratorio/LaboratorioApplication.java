package ni.uam.edu.controlequiposlaboratorio;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ni.uam.edu.controlequiposlaboratorio.database.JPAUtil;

import java.io.IOException;
import java.net.URL;

public class LaboratorioApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        URL vista = LaboratorioApplication.class.getResource("equipo-view.fxml");

        if (vista == null) {
            throw new IOException("No se encontró equipo-view.fxml en los recursos.");
        }

        FXMLLoader loader = new FXMLLoader(vista);
        Scene scene = new Scene(loader.load(), 950, 620);

        stage.setTitle("Control de equipos de laboratorio");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        JPAUtil.cerrar();
    }
}