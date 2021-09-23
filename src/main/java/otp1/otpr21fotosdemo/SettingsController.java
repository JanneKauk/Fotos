package otp1.otpr21fotosdemo;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;

public class SettingsController {
    @FXML
    private Circle profile2;

    @FXML
    protected void onProfileHover() {
        //Kun hiiri viedään proffilikuvan päälle
        System.out.println("Cursor on profile picture.");
        //Tehdään valikko, joka ilmestyy profiilikuvan alle.
        ContextMenu menu = new ContextMenu();
        //Tehdään valikon valinnat ja lisätään niille tarvittavat toiminnot.
        MenuItem settings = new MenuItem("Asetukset");
        MenuItem logout = new MenuItem("Kirjaudu ulos");
        //Lisätään valinnat valikkoon.
        menu.getItems().addAll(settings, logout);
        //Näytetään valikko käyttäjälle.
        double boundsInScenex = profile2.localToScene(profile2.getBoundsInLocal()).getMaxX();
        double boundsInSceney = profile2.localToScene(profile2.getBoundsInLocal()).getMaxY();
        menu.show(profile2, boundsInScenex, boundsInSceney);
    }
}
