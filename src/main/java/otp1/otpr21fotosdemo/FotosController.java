package otp1.otpr21fotosdemo;

import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class FotosController {
    @FXML
    private Label welcomeText;
    @FXML
    private Circle profile;


    @FXML
    private void initialize() {

    }
    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
    @FXML
    protected void onAddImgButtonClick() {
        //Tähän tullaa ku painetaan sinistä pluspallo-kuvaketta kuvan lisäämiseks.
        System.out.println ("Add image");
    }
    @FXML
    protected void onProfileHover() {
        //Kun hiiri viedään proffilikuvan päälle
        System.out.println("Cursor on profile picture.");
        //Tehdään valikko, joka ilmestyy profiilikuvan alle.
        ContextMenu menu = new ContextMenu();
        //Tehdään valikon valinnat.
        MenuItem settings = new MenuItem("Asetukset");
        MenuItem logout = new MenuItem("Kirjaudu ulos");
        //Lisätään valinnat valikkoon.
        menu.getItems().addAll(settings, logout);
        //Näytetään valikko käyttäjälle.
        double boundsInScenex = profile.localToScene(profile.getBoundsInLocal()).getMaxX();
        double boundsInSceney = profile.localToScene(profile.getBoundsInLocal()).getMaxY();
        menu.show(profile, boundsInScenex, boundsInSceney);
    }

    @FXML
    private Pane filterMenu;
    @FXML
    private StackPane filterButtonStackPane;
    @FXML
    private void onFilterShowHidebuttonClick(){
        TranslateTransition transitionMenu = new TranslateTransition(new Duration(500), filterMenu);
        RotateTransition rotateButton = new RotateTransition(new Duration(500), filterButtonStackPane);
        if (filterMenu.getTranslateX() != 0){
            //Avataan kiinni oleva filtermenu
            System.out.println("Filterit auki!");
            transitionMenu.setToX(0);
            transitionMenu.play();
            rotateButton.setByAngle(180);
            rotateButton.play();
            filterMenu.setManaged(true);
        } else {
            //Suljetaan auki oleva filtermenu
            System.out.println("Filterit kiinni!");
            transitionMenu.setToX(-(filterMenu.getWidth()));
            transitionMenu.play();
            rotateButton.setByAngle(180);
            rotateButton.play();
            transitionMenu.setOnFinished(event -> {
                filterMenu.setManaged(false);
            });

        }
    }
    @FXML
    private ScrollPane folderMenu;
    @FXML
    private StackPane folderButtonStackPane;
    @FXML
    private void onFolderShowHidebuttonClick(){
        TranslateTransition transitionMenu = new TranslateTransition(new Duration(500), folderMenu);
        RotateTransition rotateButton = new RotateTransition(new Duration(500), folderButtonStackPane);
        if (folderMenu.getTranslateY() != 0){
            //Avataan kiinni oleva filtermenu
            System.out.println("Folderit auki!");
            transitionMenu.setToY(0);
            transitionMenu.play();
            rotateButton.setByAngle(180);
            rotateButton.play();
            folderMenu.setManaged(true);
        } else {
            //Suljetaan auki oleva filtermenu
            System.out.println("Folderit kiinni!");
            folderMenu.setViewOrder(1);
            transitionMenu.setToY(-(folderMenu.getHeight()*2));
            transitionMenu.play();
            rotateButton.setByAngle(180);
            rotateButton.play();
            transitionMenu.setOnFinished(event -> {
                folderMenu.setManaged(false);
            });

        }
    }
}