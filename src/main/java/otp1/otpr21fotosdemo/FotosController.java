package otp1.otpr21fotosdemo;

import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;

import java.io.*;
import java.util.*;

import static java.lang.Double.valueOf;

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
    private GridPane fotosGridPane, folderGridPane;
    @FXML
    private HBox filterMenuHbox;
    @FXML
    ScrollPane scrollp;
    @FXML
    Button omatKuvatButton, julkisetKuvatButton, jaetutKuvatButton, loginButton;
    @FXML
    Label usernameLabel;
    @FXML
    VBox loginVbox, emailVbox, newFolderVbox;
    @FXML
    TextField usernameField, emailField1, emailField2, folderNameField;
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
    private ImageView testImageView, newFolderButton;
    @FXML
    public Text loginErrorText, newFolderErrorText;

    private Stage mainStage = null;
    private boolean loggedIn = false;
    private Database database = null;
    private Integer privateUserID;
    private int selectedFolder;
    private boolean databaseChanged = true;

    //Image Grid settings
    private int currentColumnCount, rows, maxCols = 8;
    private int imageTableCount = 23;//How many images there are in the current location
    RowConstraints rc = new RowConstraints();
    ColumnConstraints cc = new ColumnConstraints();
    File file = new File("src/main/resources/otp1/otpr21fotosdemo/image/noimage.jpg"); //Missing image picture
    File file2 = new File("src/main/resources/otp1/otpr21fotosdemo/image/addition-icon.png");
    Image missingImage = new Image(file.toURI().toString());
    Image additionImage = new Image(file2.toURI().toString());
    @FXML
    private void deleteTest(){
        database.deleteImage(118);
    }
    @FXML
    private void initialize() {
        database = new Database();
        database.setController(this);
        logout();
        //Filtermenu piiloon alussa
        filterMenu.setTranslateX(-200);
        filterButtonStackPane.setRotate(180);
        filterMenu.setManaged(false);

        //Uuden kansion -ja Login menu piiloo ja sen sisällä rekisteröitymiseen tarvittavat tekstikentät myös.
        loginVbox.setVisible(false);
        emailVbox.setVisible(false);
        emailVbox.setManaged(false);
        settingsBorderPane.setManaged(false);
        settingsBorderPane.setVisible(false);
        imageViewStackPane.setVisible(false);
        newFolderVbox.setVisible(false);
    }

    public void setMainStage(Stage stage){
        mainStage = stage;
        setGridConstraints();
        //Adjusts the Igrid when the window size changes
        centerStackp.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> {
            //System.out.println("Width: " + newSceneWidth);
            adjustImageGrid();
        });
        stage.maximizedProperty().addListener(((observableValue, oldVal, newVal) -> {
            //System.out.println("isMaxed?"+newVal);
            adjustImageGrid();
        }));
        stage.setMaximized(true);
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

        //Column constraints
        cc.setMinWidth(150);
        //cc.setHgrow(Priority.ALWAYS);
        //Row constraints
        rc.setMinHeight(150);
        //rc.setVgrow(Priority.ALWAYS);
    }

    private void adjustImageGrid(){
        //Calc how many columns fit into the parent stackpane
        double parentWidth = fotosGridPane.widthProperty().getValue();
        System.out.println("pwidth:"+parentWidth);
        System.out.println();
        int columnFitCount = Math.max(3 , Math.min(8 , (int)Math.floor(parentWidth/(cc.getMinWidth()+fotosGridPane.getHgap()))));
        if(columnFitCount == currentColumnCount && !databaseChanged) return;//Continue only if column count OR database changed

        Map<Integer, Pair<String, Image>> images;
        if (privateUserID > 0) {
            //Käyttäjä on kirjautunut.
            images = database.downloadImages(1);
        } else {
            //käyttäjä ei ole kirjautunut.
            //TODO lataa julkiset kuvat
            images = new HashMap<Integer, Pair<String, javafx.scene.image.Image>>();
        }
        imageTableCount = images.size();
        if(imageTableCount < 1) return; //Return if there are no pictures in this location

        //Set rows and columns
        currentColumnCount = columnFitCount;
        //System.out.println("columns in Igrid: "+columns); DEBUG
        rows = (int)Math.ceil((double)imageTableCount / currentColumnCount);
        //System.out.println("rows in Igrid: "+rows); DEBUG

        Iterator<Integer> it = images.keySet().iterator();
        /*
        //Palauttaa Hashmapin jossa key on imageID ja Value on PAIR-rakenne. Pair-rakenteessa taas key on tiedostonimi ja value on imagedata
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
        //Reset and recreate the grid
        setGridConstraints();
        System.out.println("Creating imagegrid");
        //For each row
        for (int i = 0; i < rows; i++) {
            //For each column
            for (int j = 0; j < currentColumnCount; j++) {
                if (!it.hasNext()) break;//Stop when all pictures have been added
                //New elements
                Pane p = new Pane();
                ImageView iv = new ImageView();
                p.getChildren().add(iv);
                p.prefWidthProperty().bind(Bindings.min(fotosGridPane.widthProperty().divide(currentColumnCount), fotosGridPane.heightProperty().divide(rows)));
                p.prefHeightProperty().bind(Bindings.min(fotosGridPane.widthProperty().divide(currentColumnCount), fotosGridPane.heightProperty().divide(rows)));
                //ImageView settings
                int imageID = it.next();
                Pair<String, Image> filenameAndImage = images.get(imageID);
                iv.setImage(filenameAndImage.getValue());
                //System.out.println("Displaying: " + filenameAndImage.getKey());
                iv.setSmooth(true);
                iv.setPreserveRatio(false);
                double w = iv.getImage().getWidth();
                double h = iv.getImage().getHeight();
                if(w<h) h=w;
                else w=h;
                double x = 0;
                if(w < iv.getImage().getWidth()) x = (iv.getImage().getWidth()/2)-(w/2);
                Rectangle2D viewportRect = new Rectangle2D(x , 0, w, h);
                iv.setViewport(viewportRect);
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
            }
            //Add row constraints
            fotosGridPane.getRowConstraints().add(rc);
        }
        fotosGridPane.setGridLinesVisible(false); //For debug
        fotosGridPane.setGridLinesVisible(true); //For debug
        databaseChanged = false;
    }

    private void clearLoginFields(){
        usernameField.setText("");
        passwordField.setText("");
        emailField1.setText("");
        emailField2.setText("");
    }

    private void clearNewFolderMenuFields() {
        folderNameField.setText("");
        newFolderErrorText.setText("");
    }

    private void logout(){
        loggedIn = false;
        privateUserID = -1;
        database.setPrivateUserId(-1);
        databaseChanged = true;
        adjustImageGrid();
        omatKuvatButton.setVisible(false);
        jaetutKuvatButton.setVisible(false);
        usernameLabel.setText("Kirjaudu/Rekisteröidy");
        folderGridPane.getChildren().clear();
        newFolderButton.setVisible(false);
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
            int userid = database.userAndPwExists(usernameField.getText(), passwordField.getText());
            loadUserFolders(userid);
            loggedIn = true;
            omatKuvatButton.setVisible(true);
            jaetutKuvatButton.setVisible(true);
            usernameLabel.setText(usernameField.getText());
            loginVbox.setVisible(false);
            newFolderButton.setVisible(true);
            clearLoginFields();
            loginErrorText.setText("");
            databaseChanged = true;
            adjustImageGrid();
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
                //Tehdään root-kansio uudelle käyttäjälle
                int userid = database.userAndPwExists(usernameField.getText(), passwordField.getText());
                database.uploadNewFolder("root", userid);
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

        if (!(event.getTarget().equals(newFolderButton))) {
            newFolderVbox.setVisible(false);
            newFolderErrorText.setVisible(false);
            newFolderErrorText.setManaged(false);
            clearNewFolderMenuFields();
        }
    }
    @FXML
    protected void onAddImgButtonClick() {
        if (loggedIn){
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
                        //Upload on another thread
                        Runnable uploadTask = () -> {
                            System.out.println ("Upload for userID " + privateUserID);
                            database.uploadImages(privateUserID,1, files);
                            System.out.println("Uploaded.");
                            databaseChanged = true;
                            Platform.runLater(() -> {
                                adjustImageGrid();
                                System.out.println("adjusted?");
                            });

                        };
                        Thread uploadThread = new Thread(uploadTask);
                        uploadThread.setDaemon(true);
                        uploadThread.start();

                    } else {
                        //No upload
                        System.out.println ("No upload");
                    }



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

    @FXML
    //Käyttäjän kansioiden lataamiseen
    public void loadUserFolders(int userId) {
        //Haetaan tietokannasta
        System.out.println("Ladataan kansioita...");
        HashMap <Integer, String> folderinfo;
        folderinfo = database.getUserFolders(userId);
        int i = 0;
        //Asetetaan kansiot käyttöliittymään
        for (Integer folder: folderinfo.keySet()) {
            Image img = new Image("file:src/main/resources/otp1/otpr21fotosdemo/image/folder-1484.png");
            ImageView imgview = new ImageView(img);
            imgview.setFitWidth(54);
            imgview.setFitHeight(47);
            Label label = new Label(folderinfo.get(folder));
            VBox vbox = new VBox(imgview, label);
            label.setFont(new Font("System", 12));
            vbox.setPrefWidth(80);
            vbox.setPrefHeight(72);
            vbox.setAlignment(Pos.CENTER);
            VBox.setMargin(imgview, new Insets(7, 0, 0, 0));
            folderGridPane.add(vbox, i, 0, 1, 1);
            i++;

            //Kansion poistamiseen
            vbox.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                        ContextMenu menu = new ContextMenu();
                        MenuItem menuitem1 = new MenuItem("Poista");
                        menu.getItems().addAll(menuitem1);
                        menuitem1.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                onDeleteFolderButtonClick(folder);
                            }
                        });
                        menu.show(vbox, mouseEvent.getScreenX(), mouseEvent.getScreenY());
                    }
                }
            });
        }
    }

    @FXML
    //Uuden kansion luontiin
    public void onNewFolderButtonClick() {
        System.out.println("Uuden kansion kuvaketta painettu");
        newFolderVbox.setVisible(true);
        newFolderErrorText.setVisible(false);
        newFolderErrorText.setManaged(false);
    }

    @FXML
    //Kun käyttäjä klikkaa valmis-nappia kun ollaan tekemässä uutta kansiota.
    public void onNewFolderReadyButtonClick() {
        String newfoldername;

        if (folderNameField.getText().equals("")) {
            newFolderErrorText.setText("Anna kansiolle nimi");
            newFolderErrorText.setVisible(true);
            newFolderErrorText.setManaged(true);
        } else if (folderNameField.getText().equals("root") || folderNameField.getText().equals("ROOT")) {
            newFolderErrorText.setText("Kansiolle ei voi antaa nimeksi root");
            newFolderErrorText.setVisible(true);
            newFolderErrorText.setManaged(true);
        } else {
            newfoldername = folderNameField.getText();
            database.uploadNewFolder(newfoldername, privateUserID);
            newFolderVbox.setVisible(false);
            clearNewFolderMenuFields();
            folderGridPane.getChildren().clear();
            loadUserFolders(privateUserID);
        }
    }

    @FXML
    //Kansioiden poistaminen
    public void onDeleteFolderButtonClick(Integer folderid) {
        database.deleteFolder(folderid);
        folderGridPane.getChildren().clear();
        loadUserFolders(privateUserID);
    }
}