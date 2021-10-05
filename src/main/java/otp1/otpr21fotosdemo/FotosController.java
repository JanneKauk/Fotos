package otp1.otpr21fotosdemo;

import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class FotosController {
    @FXML
    private BorderPane rootborderpane, settingsBorderPane;
    @FXML
    private Circle profile;
    @FXML
    private StackPane folderMenuHideButton, folderMenu;
    @FXML
    private StackPane folderButtonStackPane;
    @FXML
    private GridPane fotosGridPane;
    @FXML
    private HBox filterMenuHbox;
    @FXML
    ScrollPane scrollp;
    @FXML
    Button omatKuvatButton, julkisetKuvatButton, jaetutKuvatButton, loginButton;
    @FXML
    Label usernameLabel;
    @FXML
    VBox loginVbox, emailVbox;
    @FXML
    TextField usernameField, emailField1, emailField2;
    @FXML
    PasswordField passwordField;
    @FXML
    ImageView bigPicture;
    @FXML
    StackPane centerStackp;
    @FXML
    private Region imageviewBackgroundRegion;
    @FXML
    private StackPane imageViewStackPane, blurringStackPane, addImageButton, testStackPane;
    @FXML
    private ImageView testImageView;
    @FXML
    public Text loginErrorText;

    private Stage mainStage = null;
    private boolean loggedIn = false;
    private Database database = null;
    private Integer privateUserID;

    //Image Grid settings
    private int columns, rows, maxCols = 8;
    private int imageTableCount = 23;//How many images there are in the current location
    RowConstraints rc = new RowConstraints();
    ColumnConstraints cc = new ColumnConstraints();
    File file = new File("src/main/resources/otp1/otpr21fotosdemo/image/noimage.jpg"); //Missing image picture
    File file2 = new File("src/main/resources/otp1/otpr21fotosdemo/image/addition-icon.png");
    Image missingImage = new Image(file.toURI().toString());
    Image additionImage = new Image(file2.toURI().toString());

    @FXML
    private void initialize() {

        logout();
        //Filtermenu piiloon alussa
        filterMenu.setTranslateX(-200);
        filterButtonStackPane.setRotate(180);
        filterMenu.setManaged(false);

        //Login menu piiloo ja sen sisällä rekisteröitymiseen tarvittavat tekstikentät myös.
        loginVbox.setVisible(false);
        emailVbox.setVisible(false);
        emailVbox.setManaged(false);
        settingsBorderPane.setManaged(false);
        settingsBorderPane.setVisible(false);
        imageViewStackPane.setVisible(false);

        database = new Database();

    }
    public void setMainStage(Stage stage){
        mainStage = stage;
        //Adjusts the Igrid when the window size changes TODO: Still ignores maximize and minimize
        centerStackp.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> {
            //System.out.println("Width: " + newSceneWidth);
            adjustGrid(newSceneWidth);
        });
    }

    private void openImageview(){
        blurringStackPane.setEffect(new GaussianBlur());
        imageViewStackPane.setVisible(true);
    }
    @FXML
    private void closeImageview(){
        imageViewStackPane.setVisible(false);
        blurringStackPane.setEffect(null);
    }

    private void setGridConstraints(){
        fotosGridPane.getChildren().clear();
        fotosGridPane.getRowConstraints().clear();
        fotosGridPane.getColumnConstraints().clear();
        //fotosGridPane.setGridLinesVisible(true); //For debug

        //Column constraints
        cc.setMinWidth(150);
        //cc.setHgrow(Priority.ALWAYS);
        //Row constraints
        rc.setMinHeight(150);
        //rc.setVgrow(Priority.ALWAYS);
    }

    private void adjustGrid(Number parentWidth){
        Map<Integer, Pair<String, Image>> images = database.downloadImages(1);
        imageTableCount = images.size();
        //Calc how many columns fit into the parent stackpane
        int cols = Math.max(3 , Math.min(8 , (int)Math.floor(parentWidth.doubleValue()/160)));
        if(cols==columns) return;//Continue only if column count changes
        columns = cols;
        //System.out.println("columns in Igrid: "+columns); DEBUG
        rows = (int)Math.ceil((double)imageTableCount/columns);
        //System.out.println("rows in Igrid: "+rows); DEBUG
        //TODO: getPictureTable, tableCount = getPictureTable.count



        //Reset and recreate the grid
        setGridConstraints();
        if(imageTableCount < 1) return; //Return if there are no pictures in this location
        int t = imageTableCount;

        //Palauttaa Hashmapin jossa key on imageID ja Value on PAIR-rakenne. Pair-rakenteessa taas key on tiedostonimi ja value on imagedata

        /*
        //Esimerkiksi:  luetellaan tiedostonimet konsolii.
        {
            //iteraattori imageID:iden läpikäymiseen
            Iterator<Integer> it = images.keySet().iterator();
            int count = 1;
            while (it.hasNext()) {
                int imageID = it.next();
                Pair<String, Image> filenameAndImage = images.get(imageID);
                //Tällä saa tiedostonimen
                String filename = filenameAndImage.getKey();
                //Tällä saa imagedatan Image-muodossa (javafx.scene...)
                Image image = filenameAndImage.getValue();
                System.out.println("File " + count + " " + filename);
                count++;
            }
        }
           */
        Iterator<Integer> it = images.keySet().iterator();
        //For each row
        for (int i = 0; i < rows; i++) {
            //For each column
            for (int j = 0; j < columns; j++) {
                if(t<=0) return;//Stop when all pictures have been added
                if (!it.hasNext()) return;
                Pane p = new Pane();
                ImageView iv = new ImageView();
                p.getChildren().add(iv);
                p.prefWidthProperty().bind(Bindings.min(fotosGridPane.widthProperty().divide(columns), fotosGridPane.heightProperty().divide(rows)));
                p.prefHeightProperty().bind(Bindings.min(fotosGridPane.widthProperty().divide(columns), fotosGridPane.heightProperty().divide(rows)));
                //ImageView settings

                int imageID = it.next();
                Pair<String, Image> filenameAndImage = images.get(imageID);
                iv.setImage(filenameAndImage.getValue());
                System.out.println("Displaying: " + filenameAndImage.getKey());
                /*
                if(j%2==0)iv.setImage(missingImage);//For testing,TODO: set to next picture in iteration (thumbnail)
                if(j%2==1)iv.setImage(additionImage);
                */

                iv.setSmooth(true);
                iv.setPreserveRatio(true);
                iv.fitWidthProperty().bind(p.widthProperty());
                iv.fitHeightProperty().bind(p.heightProperty());
                iv.setOnMouseClicked(event -> {
                    Image fullImage = database.downloadFullImage(imageID);
                    if (fullImage != null){
                        bigPicture.setImage(fullImage);
                    } else {
                        bigPicture.setImage(iv.getImage());
                    }

                    openImageview();
                });
                //Add the created element p to the grid in pos (j,i)
                fotosGridPane.add(p, j, i);
                //Add column constraints
                if(i < 1) fotosGridPane.getColumnConstraints().add(cc);
                t--;
            }
            //Add row constraints
            fotosGridPane.getRowConstraints().add(rc);
        }
    }

    private void clearLoginFields(){
        usernameField.setText("");
        passwordField.setText("");
        emailField1.setText("");
        emailField2.setText("");
    }
    private void logout(){
        loggedIn = false;
        omatKuvatButton.setVisible(false);
        jaetutKuvatButton.setVisible(false);
        usernameLabel.setText("Kirjaudu/Rekisteröidy");
        switchToDefaultScene();
    }

    public void fetchUserID(int methodUserID) {
        privateUserID = methodUserID;
    }

    @FXML
    private void login() {
        if (Objects.equals(usernameField.getText(), "")) {
            loginErrorText.setText("Syötä käyttäjätunnus");
        } else if (database.userAndPwExists(usernameField.getText(), passwordField.getText()) != 0) {
            loggedIn = true;
            omatKuvatButton.setVisible(true);
            jaetutKuvatButton.setVisible(true);
            usernameLabel.setText("Käyttäjänimi");
            loginVbox.setVisible(false);
            clearLoginFields();
            loginErrorText.setText("");
        } else {
            loginErrorText.setText("Käyttäjänimi tai salasana väärin");
        }
    }

    @FXML
    private void registerMenu() throws UnsupportedEncodingException {
        System.out.println("emailvbox: " + emailVbox.isVisible());
        if (emailVbox.isVisible()) {
            // Lähetetään pyyntö back-end koodin puolelle, jossa toteutetaan tarkistukset ja datan pusku palvelimelle
            if (!database.userExists(usernameField.getText())) {
                database.saltRegister(usernameField.getText(), passwordField.getText(), emailField1.getText(), emailField2.getText(), loginErrorText);
            } else {
                loginErrorText.setText("Tämä käyttäjä on jo olemassa");
            }

            loginButton.setVisible(true);
            loginButton.setManaged(true);
            emailVbox.setManaged(false);
            emailVbox.setVisible(false);

        } else {
            //Näytetään rekisteröitymiseen tarvittavat tekstikentät. Piilotetaan login nappi (avattiin rekisteröitymismenu)
            loginButton.setVisible(false);
            loginButton.setManaged(false);
            emailVbox.setManaged(true);
            emailVbox.setVisible(true);
        }
    }

    @FXML
    private void onMainBorderPaneClick(Event event){
       /* System.out.println("onMainBorderPaneClick: 1 " + loginVbox.isVisible());
        System.out.println("event1: " + event.getSource());
        System.out.println("event2: " + event.getTarget());
        System.out.println("event3: " + event.getTarget().equals(usernameLabel));*/

        //Jos klikattiin muualle kuin profiilipalloon niin suljetaan loginmenu. Tää tarvitaa ettei loginmenu sulkeudu heti auettuaan.
        if (!(event.getTarget().equals(profile))) {
            loginButton.setVisible(true);
            loginButton.setManaged(true);
            emailVbox.setManaged(false);
            emailVbox.setVisible(false);
            loginVbox.setVisible(false);
            clearLoginFields();

        }
    }
    @FXML
    protected void onAddImgButtonClick() {
        //Tähän tullaa ku painetaan sinistä pluspallo-kuvaketta kuvan lisäämiseks.
        System.out.println ("Add image");
        //Varmistetaan että controller on saanut start-metodilta mainStagen
        if (mainStage != null) {
            //Tiedostonvalintaikkuna
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Valitse kuvatiedosto(t)");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
                new FileChooser.ExtensionFilter("All Files", "*.*"
            ));
            List<File> files = fileChooser.showOpenMultipleDialog(mainStage);
            //Valittiinko tiedostoja?
            if (files != null){
                //Rakennetaan varmistuskysymys
                Stage dialog = new Stage();
                StringBuilder kysymys = new StringBuilder();
                if (files.size() == 1){
                    kysymys.append("Haluatko varmasti ladata palveluun seuraavan kuvan?\n");
                } else {
                    kysymys.append("Haluatko varmasti ladata palveluun seuraavat " + files.size() + " kuvaa?\n");
                }
                final int rows_in_confirmation = 10;
                System.out.println("File: " + ((File)files.toArray()[0]).getAbsolutePath());

                System.out.println("File: " + ((File)files.toArray()[0]).getPath());

                System.out.println("File: " + ((File)files.toArray()[0]).toPath());
                System.out.println("File: " + ((File)files.toArray()[0]).toURI());

                if (files.size() <= rows_in_confirmation) {
                    files.forEach(file -> kysymys.append(file.getName() + '\n'));

                } else {
                    Iterator<File> it = files.iterator();
                    for(int i = 0; i < rows_in_confirmation; i++){
                        kysymys.append(it.next().getName() + '\n');
                    }
                    kysymys.append("...\n");
                }

                //Esitetään varmistuskysymys
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, kysymys.toString());
                alert.setTitle("Vahvista");
                alert.setHeaderText(null);

                Optional<ButtonType> vastaus = alert.showAndWait();
                if(vastaus.isPresent() && vastaus.get() == ButtonType.OK){
                    //Upload
                    System.out.println ("Upload");
                    database.uploadImages(1,1, files);
                } else {
                    //No upload
                    System.out.println ("No upload");
                }



            }
        }
    }




    @FXML
    protected void onProfileClick(){
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
                    try {
                        switchToSettingsScene();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
        } else {
            //loginmenu auki
            loginVbox.setVisible(true);
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
    public void switchToSettingsScene() throws IOException {
        //Laitetaan asetusten elementit näkyviin ja poistetaan etusivun elementit pois näkyvistä.
        settingsBorderPane.setManaged(true);
        settingsBorderPane.setVisible(true);
        scrollp.setManaged(false);
        scrollp.setVisible(false);
        filterMenuHbox.setManaged(false);
        filterMenuHbox.setVisible(false);
        folderMenu.setVisible(false);
        folderMenuHideButton.setManaged(false);
        folderMenuHideButton.setVisible(false);

        /*
        Stage stage;
        Scene scene;
        //Vaihdetaan asetukset-näkymään.
        FXMLLoader fxmlLoader = new FXMLLoader(Fotos.class.getResource("Settings.fxml"));
        scene = new Scene(fxmlLoader.load(), 1280, 800);
        stage = (Stage) rootborderpane.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
        */
    }

    @FXML
    public void switchToDefaultScene() {
        //Laitetaan etusivun elementit näkyviin ja poistetaan asetusten elementit pois näkyvistä.
        settingsBorderPane.setManaged(false);
        settingsBorderPane.setVisible(false);
        scrollp.setManaged(true);
        scrollp.setVisible(true);
        filterMenuHbox.setManaged(true);
        filterMenuHbox.setVisible(true);
        folderMenu.setVisible(true);
        folderMenuHideButton.setManaged(true);
        folderMenuHideButton.setVisible(true);

        /*
        //Laitetaan etusivun elementit takaisin näkyviin.
        folderMenu.setVisible(true);
        folderMenuHideButton.setVisible(true);
        fotosGridPane.setVisible(true);
        filterMenuHbox.setVisible(true);
        folderMenu.setManaged(true);
        folderMenuHideButton.setManaged(true);
        fotosGridPane.setManaged(true);
        filterMenuHbox.setManaged(true);
         */
    }

    @FXML
    public void testDownload(){
        /*System.out.println("TestDownload");
        List<javafx.scene.image.Image> images = database.downloadImages(1);
        System.out.println("Number of images: " + images.size());
        if (images.size() > 0){
            System.out.println("Height: " + images.get(0).getHeight());
            System.out.println("Width: " + images.get(0).getWidth());
            testImageView.setImage(missingImage);
        }
*/
    }
}