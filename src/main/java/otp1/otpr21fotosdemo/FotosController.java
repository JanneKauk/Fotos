package otp1.otpr21fotosdemo;

import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.File;

public class FotosController {
    @FXML
    private BorderPane rootborderpane;
    @FXML
    private Circle profile;
    @FXML
    private StackPane folderMenuHideButton;
    @FXML
    private ScrollPane folderMenu;
    @FXML
    private StackPane folderButtonStackPane;
    @FXML
    private GridPane fotosGridPane;
    @FXML
    private HBox filterMenuHbox;
    private int columns = 5, rows = 5;
    private int imageTableCount = 1;
    File file = new File("src/main/resources/otp1/otpr21fotosdemo/image/noimage.jpg");
    Image image = new Image(file.toURI().toString());
    @FXML
    ScrollPane scrollp;
    @FXML
    Button omatKuvatButton, julkisetKuvatButton, jaetutKuvatButton;
    @FXML
    Label usernameLabel;
    @FXML
    VBox loginVbox;

    private boolean loggedIn;


    @FXML
    private void initialize() {
        System.out.println("Scrollp fit to width? " + scrollp.isFitToWidth());
        logout();
        filterMenu.setTranslateX(-200);
        filterButtonStackPane.setRotate(180);
        filterMenu.setManaged(false);
        loginVbox.setVisible(false);
        createPictureGrid();
    }

    @FXML
    private void createPictureGrid(){
        //getPictureTable tableCount = getPictureTable.count
        //adjust gridHeight and width gridWidth/table.count
        if(imageTableCount < 1) return;
        fotosGridPane.getChildren().clear();
        fotosGridPane.getRowConstraints().clear();
        fotosGridPane.getColumnConstraints().clear();
        RowConstraints rc = new RowConstraints();
        ColumnConstraints cc = new ColumnConstraints();
        rc.setMinHeight(200);
        //rc.setPrefHeight(200);
        rc.setMaxHeight(500);
        rc.setValignment(VPos.CENTER);
        rc.setVgrow(Priority.ALWAYS);
        cc.setMinWidth(200);
        cc.setMaxWidth(500);
        cc.setHalignment(HPos.CENTER);
        cc.setHgrow(Priority.ALWAYS);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                Pane pane = new Pane();
                ImageView iv = new ImageView();
                iv.setImage(image);
                iv.setSmooth(true);
                iv.setPreserveRatio(true);
                pane.getChildren().add(iv);
                iv.fitWidthProperty().bind(pane.widthProperty());
                iv.fitHeightProperty().bind(pane.heightProperty());
                fotosGridPane.add(pane, j, i);
                if(i < 1) fotosGridPane.getColumnConstraints().add(cc);
            }
            fotosGridPane.getRowConstraints().add(rc);
        }
        System.out.println(fotosGridPane.getRowCount());
        System.out.println(fotosGridPane.getColumnCount());
        fotosGridPane.setGridLinesVisible(true);
    }

    private void logout(){
        loggedIn = false;
        omatKuvatButton.setVisible(false);
        jaetutKuvatButton.setVisible(false);
        usernameLabel.setText("Kirjaudu/Rekisteröidy");

    }
    @FXML
    private void login(){
        loggedIn = true;
        omatKuvatButton.setVisible(true);
        jaetutKuvatButton.setVisible(true);
        usernameLabel.setText("Käyttäjä");
        loginVbox.setVisible(false);
    }
    @FXML
    protected void onAddImgButtonClick() {
        //Tähän tullaa ku painetaan sinistä pluspallo-kuvaketta kuvan lisäämiseks.
        System.out.println ("Add image");
    }
    @FXML
    protected void onProfileClick(){
        loginVbox.setVisible(true);
    }
    @FXML
    protected void onProfileHover() {
        if (loggedIn) {
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
                    switchToSettingsScene();
                }
            });
            MenuItem logout = new MenuItem("Kirjaudu ulos");
            logout.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    System.out.println("Kirjauduttu ulos.");
                    logout();
                }
            });
            //Lisätään valinnat valikkoon.
            menu.getItems().addAll(settings, logout);
            //Näytetään valikko käyttäjälle.
            double boundsInScenex = profile.localToScene(profile.getBoundsInLocal()).getMaxX();
            double boundsInSceney = profile.localToScene(profile.getBoundsInLocal()).getMaxY();
            menu.show(profile, boundsInScenex, boundsInSceney);
        }
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
            System.out.println("LayoutX: " + filterMenu.getLayoutX());
            System.out.println("TranslateX: " + filterMenu.getTranslateX());
        } else {
            //Suljetaan auki oleva filtermenu
            System.out.println("Filterit kiinni!");
            transitionMenu.setToX(-(filterMenu.getWidth()));
            transitionMenu.play();
            rotateButton.setByAngle(180);
            rotateButton.play();
            transitionMenu.setOnFinished(event -> {
                filterMenu.setManaged(false);
                System.out.println("LayoutX: " + filterMenu.getLayoutX());
                System.out.println("TranslateX: " + filterMenu.getTranslateX());
            });

        }
    }

    @FXML
    private void onFolderShowHidebuttonClick(){
        TranslateTransition transitionMenu = new TranslateTransition(new Duration(500), folderMenu);
        RotateTransition rotateButton = new RotateTransition(new Duration(500), folderButtonStackPane);
        if (folderMenu.getTranslateY() != 0){
            //Avataan kiinni oleva foldermenu
            System.out.println("Folderit auki!");
            transitionMenu.setToY(0);
            transitionMenu.play();
            rotateButton.setByAngle(180);
            rotateButton.play();
            folderMenu.setManaged(true);
            //filterMenuHbox.setPadding(Insets.EMPTY);
            rootborderpane.setMargin(filterMenuHbox, Insets.EMPTY);
        } else {
            //Suljetaan auki oleva foldermenu
            System.out.println("Folderit kiinni!");
            folderMenu.setViewOrder(1);
            transitionMenu.setToY(-(folderMenu.getHeight()*2));
            transitionMenu.play();
            rotateButton.setByAngle(180);
            rotateButton.play();
            transitionMenu.setOnFinished(event -> {
                folderMenu.setManaged(false);
                rootborderpane.setMargin(filterMenuHbox, new Insets(folderMenu.getHeight(), 0, 0, 0));
                //filterMenuHbox.setPadding(new Insets(folderMenu.getHeight(), 0, 0, 0));
            });

        }
    }
    @FXML
    public void switchToSettingsScene() {
        //Laitetaan etusivun elementit pois näkyvistä.
            folderMenu.setVisible(false);
            folderMenuHideButton.setVisible(false);
            fotosGridPane.setVisible(false);
            filterMenuHbox.setVisible(false);
            folderMenu.setManaged(false);
            folderMenuHideButton.setManaged(false);
            fotosGridPane.setManaged(false);
            filterMenuHbox.setManaged(false);

        /*Stage stage;
        Scene scene;
        Parent root;
        //Vaihdetaan asetukset-näkymään.
        FXMLLoader fxmlLoader = new FXMLLoader(Fotos.class.getResource("Settings.fxml"));
        scene = new Scene(fxmlLoader.load(), 1280, 800);
        stage = (Stage) rootborderpane.getScene().getWindow();
        stage.setScene(scene);
        stage.show();*/
    }

    @FXML
    public void switchToDefaultScene() {
        //Laitetaan etusivun elementit takaisin näkyviin.
        folderMenu.setVisible(true);
        folderMenuHideButton.setVisible(true);
        fotosGridPane.setVisible(true);
        filterMenuHbox.setVisible(true);
        folderMenu.setManaged(true);
        folderMenuHideButton.setManaged(true);
        fotosGridPane.setManaged(true);
        filterMenuHbox.setManaged(true);
    }
}