package otp1.otpr21fotosdemo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

public class Fotos extends Application {
    /**
     * Start point of the javafx app. Sets initial UI language and loads UI from fxml-file.
     * @param stage stage
     * @throws IOException
     */
    @Override
    public void start(Stage stage) throws IOException {

        //System.out.println("Env url: " + System.getenv("APP_DB_URL"));
        Locale curLocale = null;
        ResourceBundle langBundle = null;
        Properties properties = new Properties();
        try {
            String configPath = "src/main/resources/otp1/otpr21fotosdemo/Fotos.properties";
            properties.load(new FileInputStream(configPath));
            String lang = properties.getProperty("language");
            String country = properties.getProperty("country");
            curLocale = new Locale(lang,country);
            Locale.setDefault(curLocale);
            System.out.println("lang: " + lang + " country: " + country);

            langBundle = ResourceBundle.getBundle("otp1.otpr21fotosdemo.TextResources", curLocale);



        } catch (Exception e) {
            System.err.println("Error loading properties");
            e.printStackTrace();
            System.exit(0);
        }
        if (langBundle == null){
            System.exit(0);
        }
        FXMLLoader fxmlLoader = new FXMLLoader(Fotos.class.getResource("Fotos.fxml"), langBundle);
        Scene scene = new Scene(fxmlLoader.load(), 1280, 800);
        stage.setTitle("Fotos!");
        stage.setScene(scene);
        FotosController controller = fxmlLoader.getController();
        controller.setLangBundleAndCurLocale(langBundle, curLocale);
        controller.setMainStage(stage);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}