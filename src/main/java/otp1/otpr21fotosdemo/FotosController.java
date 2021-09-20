package otp1.otpr21fotosdemo;

import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Objects;

public class FotosController {
    @FXML
    private Label welcomeText;
    @FXML
    private Circle profile;
    @FXML
    private BorderPane rootborderpane;

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
        //Tehdään valikon valinnat ja lisätään niille tarvittavat toiminnot.
        MenuItem settings = new MenuItem("Asetukset");
        settings.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Menty asetuksiin.");
                try {
                    switchToSettingsScene(event);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
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
    @FXML
    public void switchToSettingsScene(ActionEvent event) throws IOException {
        Stage stage;
        Scene scene;
        Parent root;
        //Vaihdetaan asetukset-näkymään.
        BorderPane borderpane = FXMLLoader.load(getClass().getResource("Settings.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(borderpane);
        stage.setScene(scene);
        stage.show();
    }
}