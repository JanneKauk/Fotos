package otp1.otpr21fotosdemo;

import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import org.w3c.dom.*;
import java.io.*;
import java.util.*;
import java.util.List;

import static java.lang.Double.valueOf;

public class FotosController {
    @FXML
    private BorderPane rootborderpane, settingsBorderPane;
    @FXML
    private Circle profile;
    @FXML
    private StackPane folderMenuHideButton, folderMenu, rootStackPane;
    @FXML
    private StackPane folderButtonStackPane;
    @FXML
    private GridPane fotosGridPane, folderGridPane, breadCrumbGridPane, topBarGridPane;
    @FXML
    private HBox filterMenuHbox;
    @FXML
    ScrollPane scrollp, adminUsersViewScrollPane, adminViewAdminsScrollPane;
    @FXML
    Group adminsGroup, usersGroup;
    @FXML
    Button omatKuvatButton, julkisetKuvatButton, jaetutKuvatButton, loginButton, cycleBack, cycleForward;
    @FXML
    Label usernameLabel, imageOwner, imageResolution, imageDate, imageSize, imageFileFormat, imageName, tarkennettuHakuLabel;
    @FXML
    Text settingsUserName, settingsUserInfoUpdateResponse, settingsUserPasswordUpdateResponse, settingsSurName, settingsFrontName, settingsEmail;
    @FXML
    VBox loginVbox, emailVbox, newFolderVbox, newAdminInfoVbox, adminViewUsersVBox, adminViewAdminsVBox;
    @FXML
    TextField usernameField, emailField1, emailField2, folderNameField, settingsSurNameTextField, settingsFrontNameTextField, settingsEmailTextField, searchTextField;
    @FXML
    TextField adminUsernameField, adminPasswordField, adminEmailField, adminEmailField2, adminViewSearchUserTextField;
    @FXML
    PasswordField passwordField, settingsOldPassword, settingsNewPassword, settingsNewPasswordAgain;
    @FXML
    ImageView bigPicture;
    @FXML
    StackPane centerStackp;
    @FXML
    private Region imageviewBackgroundRegion;
    @FXML
    private StackPane imageViewStackPane, blurringStackPane, addImageButton, uploadingStackPane;
    @FXML
    private ImageView uploadingRotatingImageview, newFolderButton, addImageButtonImageView;
    @FXML
    public Text loginErrorText, newFolderErrorText, newAdminErrorText;
    @FXML
    private DatePicker dateFilter;
    @FXML
    private Label adminSettingsLabel;
    @FXML
    private BorderPane adminBorderPane;

    private enum DisplayImages {
        OWN, PUBLIC, SHARED
    }

    private DisplayImages displayImages;

    private Stage mainStage = null;
    private boolean loggedIn = false;
    private Database database = null;
    private Integer privateUserID;
    protected int privateUserLevel;
    private String settingsSurNameString, settingsFrontNameString, settingsEmailString, userName;
    private int selectedFolderID;
    private boolean databaseChanged = true;
    //private ArrayList<String> breadCrumbArrayList = new ArrayList<>();
    private int breadCrumbGridPaneCounter;
    private boolean newestToOldestOrder = false;

    //Image Grid settings
    private int currentColumnCount, rows, maxCols = 8;
    private int imageTableCount = 23;//How many images there are in the current location
    RowConstraints rc = new RowConstraints();
    ColumnConstraints cc = new ColumnConstraints();
    Number currentImageID = null;
    int currentImageIndex = 0;
    ArrayList<Integer> imageIdList;
    private ImageSelector imageSelector;
    private ArrayList<Integer> publicImagesInView;
    private ArrayList<FotosUser> userList; //For admin

    private Locale curLocale;
    private ResourceBundle langBundle;
    @FXML
    private void initialize() {
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

            System.out.println("login: " + langBundle.getString("loginText"));

        } catch (Exception e) {
            System.err.println("Error loading properties");
            e.printStackTrace();
        }
        adminBorderPane.setVisible(false);
        newAdminInfoVbox.setVisible(false);
        displayImages = DisplayImages.PUBLIC;
        publicImagesInView = new ArrayList<>();
        userList = new ArrayList<>();
        imageSelector = new ImageSelector();
        database = new Database();
        database.setController(this);
        logout();
        //Filtermenu piiloon alussa
        filterMenu.setTranslateX(-200);
        pictureInfo.setTranslateX(-200);
        filterButtonStackPane.setRotate(180);
        pictureInfoArrow.setRotate(180);
        filterMenu.setManaged(false);
        pictureInfo.setManaged(false);

        /* Ei toimi vielä.....
        //Foldermenu piiloo alussa
        System.out.println("Foldermenu getheight... : " + folderMenu.getHeight() );
        folderMenu.setViewOrder(1);
        folderMenu.setTranslateY(-160);
        System.out.println("Foldermenu translatey... : " + folderMenu.getTranslateY() );
        folderButtonStackPane.setRotate(180);
        folderMenu.setManaged(false);
        BorderPane.setMargin(filterMenuHbox, new Insets(folderMenu.getHeight(), 0, 0, 0));
*/
        uploadingStackPane.setVisible(false);

        //Uuden kansion -ja Login menu piiloo ja sen sisällä rekisteröitymiseen tarvittavat tekstikentät myös.
        loginVbox.setVisible(false);
        emailVbox.setVisible(false);
        emailVbox.setManaged(false);
        settingsBorderPane.setManaged(false);
        settingsBorderPane.setVisible(false);
        imageViewStackPane.setVisible(false);
        newFolderVbox.setVisible(false);
        newFolderButton.setVisible(false);
        //Hakukentälle kuuntelija
        searchTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                    databaseChanged = true;
                    adjustImageGrid();
                }
            }
        });
        //Tämä tarvitaan jotta kuvien valinta saadaan clearattua kun klikataan muualle.
        rootborderpane.setOnMouseClicked(event -> {
            if (event.isControlDown() ||event.isShiftDown() || event.getPickResult().getIntersectedNode().getTypeSelector().equals("ImageView")) {
                //Klikattiin imageviewiin tai CTRL tai SHIFT pohjas
                return;
            } else if (imageSelector.countSelected() > 0) {
                imageSelector.clearSelection();
            }
        });
    }

    public void setMainStage(Stage stage) {
        mainStage = stage;
        setGridConstraints();
        //Adjusts the Igrid when the window size changes
        centerStackp.widthProperty().addListener((observableValue, oldSceneWidth, newSceneW) -> {
            adjustImageGrid();
        });
        centerStackp.heightProperty().addListener((observableValue, oldSceneHeight, newSceneH) -> {
            adjustImageGrid();
        });
        stage.setMaximized(true);
    }

    private void openImageview() {
        blurringStackPane.setEffect(new GaussianBlur());
        imageViewStackPane.setVisible(true);
    }

    @FXML
    private void closeImageview() {
        imageViewStackPane.setVisible(false);
        blurringStackPane.setEffect(null);
        currentImageID = null;
    }

    private void setGridConstraints() {
        fotosGridPane.getChildren().clear();
        fotosGridPane.getRowConstraints().clear();
        fotosGridPane.getColumnConstraints().clear();

        //Column constraints
        cc.setMinWidth(150);
        rc.setMinHeight(150);
    }

    private void refreshImageGrid() {
        //Convenience-method for forcing new imagegrid
        databaseChanged = true;
        adjustImageGrid();
    }

    private void adjustImageGrid() {
        if (loggedIn && privateUserLevel == 1000){
            //Käyttäjä on admin. Ei tarvita imagegridiä
            return;
        }
        //Calc how many columns fit into the parent stackpane
        double parentWidth = centerStackp.getWidth();
        double parentHeight = centerStackp.getHeight();
        fotosGridPane.setMinHeight(parentHeight);
        int columnFitCount = Math.max(3, Math.min(8, (int) Math.floor(parentWidth / (cc.getMinWidth() + fotosGridPane.getHgap()))));
        if (columnFitCount == currentColumnCount && !databaseChanged)
            return;//Continue only if column count OR database changed
        publicImagesInView.clear();
        Map<Integer, Pair<String, Image>> images;
        imageIdList = new ArrayList<>();
        if (privateUserID > 0) {
            //Käyttäjä on kirjautunut.
            if (displayImages == DisplayImages.PUBLIC) {
                //Valittuna "Julkiset kuvat"
                if (!Objects.equals(searchTextField.getText(), "")) {
                    System.out.println("Searching by: " + searchTextField.getText());
                    images = database.downloadPublicImages(searchTextField.getText(), dateFilter.getValue());
                } else {
                    images = database.downloadPublicImages(null, dateFilter.getValue());
                }

            } else if (displayImages == DisplayImages.OWN) {
                //Valittuna "Omat kuvat"
                if (!Objects.equals(searchTextField.getText(), "")) {
                    System.out.println("Searching by: " + searchTextField.getText());
                    images = database.downloadImages(selectedFolderID, searchTextField.getText(), dateFilter.getValue());
                } else {
                    images = database.downloadImages(selectedFolderID, null,  dateFilter.getValue());
                }
            } else {
                //Valittuna "Jaetut kuvat"
                //TODO Jaetut kuvat toteutus
                //Annetaan toistaiseksi vain tyhjä hashmap
                images = new HashMap<Integer, Pair<String, javafx.scene.image.Image>>();
            }
        } else {
            //käyttäjä ei ole kirjautunut.
            images = database.downloadPublicImages(null, dateFilter.getValue());
        }
        StringBuilder b = new StringBuilder();
        b.append("Grids imageID:s: ");
        //Järjestetään lista imageID:n mukaan
        TreeMap<Integer, Pair<String, Image>> sortedImages;
        if (newestToOldestOrder) {
            sortedImages = new TreeMap<>(Comparator.reverseOrder());
        } else {
            sortedImages = new TreeMap<>();
        }
        sortedImages.putAll(images);
        sortedImages.entrySet().stream().forEach(entry -> {
            b.append(entry.getKey() + " ");
        });

        System.out.println(b);
        //Reset and recreate the grid
        setGridConstraints();
        imageTableCount = sortedImages.size();
        System.out.println("IMAGE TABLE COUNT: " + imageTableCount);
        if (imageTableCount < 1) return; //Return if there are no pictures in this location

        //Set rows and columns
        currentColumnCount = columnFitCount;
        //System.out.println("columns in Igrid: "+columns); DEBUG
        rows = (int) Math.ceil((double) imageTableCount / currentColumnCount);
        //System.out.println("rows in Igrid: "+rows); DEBUG
        Iterator<Integer> it = sortedImages.keySet().iterator();

        /*
        //Palauttaa Hashmapin jossa key on imageID ja Value on PAIR-rakenne. Pair-rakenteessa taas key on tiedostonimi ja value on imagedata
        //Esimerkiksi:  luetellaan tiedostonimet konsolii.
        {
            //iteraattori imageID:iden läpikäymiseen
            Iterator<Integer> it = sortedImages.keySet().iterator();
            int count = 1;
            while (it.hasNext()) {
                int imageID = it.next();
                Pair<String, Image> filenameAndImage = sortedImages.get(imageID);
                //Tällä saa tiedostonimen
                String filename = filenameAndImage.getKey();
                //Tällä saa imagedatan Image-muodossa (javafx.scene...)
                Image image = filenameAndImage.getValue();
                System.out.println("File " + count + " " + filename);
                count++;
            }
        }
        */
        imageSelector.clearAll();
        int t = 0;
        System.out.println("Creating imagegrid");
        //For each row
        for (int i = 0; i < rows; i++) {
            //For each column
            for (int j = 0; j < currentColumnCount; j++) {
                if (!it.hasNext()) break;//Stop when all pictures have been added
                int imageDatabaseId = it.next();
                imageIdList.add(imageDatabaseId);//Add the next ImageID to a list
                //New elements
                Pane p = new Pane();
                /*
                p.setStyle("-fx-border-color: red;");
                p.setStyle("-fx-border-width: 10;");
                */
                ImageView iv = new ImageView();
                p.getChildren().add(iv);
                p.prefWidthProperty().bind(Bindings.min(fotosGridPane.widthProperty().divide(currentColumnCount), fotosGridPane.heightProperty().divide(rows)));
                p.prefHeightProperty().bind(Bindings.min(fotosGridPane.widthProperty().divide(currentColumnCount), fotosGridPane.heightProperty().divide(rows)));

                //ImageView settings
                iv.setImage(sortedImages.get(imageIdList.get(t)).getValue());//Gets the ImageID from the list
                //System.out.println("Displaying: " + filenameAndImage.getKey());
                iv.setSmooth(true);
                iv.setPreserveRatio(false);
                //Viewport settings
                double w = iv.getImage().getWidth();
                double h = iv.getImage().getHeight();
                if (w < h) h = w;
                else w = h;
                double x = 0;
                if (w < iv.getImage().getWidth()) x = (iv.getImage().getWidth() / 2) - (w / 2);
                Rectangle2D viewportRect = new Rectangle2D(x, 0, w, h);
                iv.setViewport(viewportRect);
                iv.fitWidthProperty().bind(p.widthProperty());
                iv.fitHeightProperty().bind(p.heightProperty());

                int finalT = t;
                iv.setOnMouseClicked(event -> {

                    if (event.getButton() == MouseButton.PRIMARY) {
                        //Left click
                        if (event.isControlDown()) {
                            //CTRL painettuna
                            if (imageSelector.isSelected(imageDatabaseId))
                                imageSelector.removeFromSelection(imageDatabaseId);
                            else
                                imageSelector.addToSelection(imageDatabaseId);

                        } else if (event.isShiftDown()) {
                            //SHIFT painettuna
                            if (imageSelector.countSelected() == 0) {
                                imageSelector.addToSelection(imageDatabaseId);
                            } else {
                                ArrayList<Integer> lista = imageSelector.getSelectedIds();
                                //Edellinen valittu
                                int lastId = lista.get(lista.size() - 1);
                                int eka, toka;
                                eka = Math.min(lastId, imageDatabaseId);
                                toka = Math.max(lastId, imageDatabaseId);
                                Iterator<Integer> ite = sortedImages.keySet().iterator();
                                while (ite.hasNext()) {
                                    int id = ite.next();
                                    if (id >= eka && id <= toka) {
                                        imageSelector.addToSelection(id);
                                    }

                                }

                            }
                        } else {
                            //EI CTRL EIKÄ SHIFT painettuna
                            //Jos kuvia valittuna niin vain clearataan valinta.

                            if (imageSelector.countSelected() > 0) {
                                imageSelector.clearSelection();
                                return;
                            }
                            currentImageID = imageIdList.get(finalT);//What picture we are looking at
                            System.out.println("ID:" + currentImageID);
                            currentImageIndex = finalT;//What index the picture is in
                            System.out.println("Index:" + currentImageIndex);
                            Image fullImage = database.downloadFullImage(currentImageID.intValue());//Find the original version of the clicked picture
                            if (fullImage != null) {
                                bigPicture.setImage(fullImage);
                            } else {
                                bigPicture.setImage(iv.getImage());
                            }
                            refreshImageData();
                            openImageview();
                        }
                    } else if (event.getButton() == MouseButton.SECONDARY) {
                        //Right click
                        ContextMenu menu = new ContextMenu();
                        if (loggedIn) {
                            MenuItem menuitem1 = new MenuItem("Aseta kaikki julkiseksi");
                            MenuItem menuitem2 = new MenuItem("Aseta kaikki yksityiseksi");
                            menuitem1.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    //Julkiseksi
                                    int count = imageSelector.countSelected();
                                    count = count == 0 ? 1 : count; //Jos valittuna ei ole yhtään kuvaa niin asetetaan count=1, koska tällöin toiminto kohdistuu klikattuun kuvaan.
                                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Haluatko varmasti asettaa valitut " + count + " kuvaa julkiseksi?");
                                    alert.setTitle("Vahvista");
                                    alert.setHeaderText(null);

                                    Optional<ButtonType> vastaus = alert.showAndWait();

                                    if (vastaus.isPresent() && vastaus.get() == ButtonType.OK) {
                                        boolean success = setSelectedImagesPublicity(imageDatabaseId, true);
                                        refreshImageGrid();
                                        if (success) {
                                            Alert info = new Alert(Alert.AlertType.INFORMATION, "Asetettiin valitut " + count + " kuvaa julkiseksi.");
                                            info.setTitle("Julkistettu");
                                            info.setHeaderText(null);
                                            info.showAndWait();
                                        } else {
                                            Alert info = new Alert(Alert.AlertType.ERROR, "Kuvien julkistuksessa tapahtui virhe");
                                            info.setTitle("Virhe");
                                            info.setHeaderText(null);
                                            info.showAndWait();
                                        }
                                    }

                                }
                            });
                            menuitem2.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    //Yksityiseksi
                                    int count = imageSelector.countSelected();
                                    count = count == 0 ? 1 : count; //Jos valittuna ei ole yhtään kuvaa niin asetetaan count=1, koska tällöin toiminto kohdistuu klikattuun kuvaan.
                                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Haluatko varmasti asettaa valitut " + count + " kuvaa yksityiseksi?");
                                    alert.setTitle("Vahvista");
                                    alert.setHeaderText(null);

                                    Optional<ButtonType> vastaus = alert.showAndWait();

                                    if (vastaus.isPresent() && vastaus.get() == ButtonType.OK) {
                                        boolean success = setSelectedImagesPublicity(imageDatabaseId, false);
                                        refreshImageGrid();
                                        if (success) {
                                            Alert info = new Alert(Alert.AlertType.INFORMATION, "Asetettiin valitut " + count + " kuvaa yksityiseksi.");
                                            info.setTitle("Asetettu yksityiseksi");
                                            info.setHeaderText(null);
                                            info.showAndWait();
                                        } else {
                                            Alert info = new Alert(Alert.AlertType.ERROR, "Kuvien asettamisessa yksityiseksi tapahtui virhe");
                                            info.setTitle("Virhe");
                                            info.setHeaderText(null);
                                            info.showAndWait();
                                        }
                                    }
                                }
                            });
                            if (imageSelector.countSelected() > 1) {
                                menu.getItems().addAll(menuitem1);
                                menu.getItems().addAll(menuitem2);
                            } else {
                                //Kuvia on valittuna vain yksi tai right klikattiin yhtä kuvaa valitsematta useampaa.
                                menuitem1.setText("Aseta julkiseksi");
                                menuitem2.setText("Aseta yksityiseksi");
                                //Selvitetään kuvan julkisuus
                                boolean publc;
                                if (imageSelector.countSelected() == 0) {
                                    publc = database.imageIsPublic(imageDatabaseId);
                                } else {
                                    //selected == 1
                                    publc = database.imageIsPublic(imageSelector.getSelectedIds().get(0));
                                }

                                if (publc) {
                                    //Yksityiseksi
                                    menu.getItems().addAll(menuitem2);
                                } else {
                                    //Julkiseksi
                                    menu.getItems().addAll(menuitem1);
                                }
                            }

                            MenuItem menuitem3 = new MenuItem("Poista");
                            menu.getItems().addAll(menuitem3);
                            menuitem3.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    deleteSelectedImages(imageDatabaseId);
                                }
                            });
                        }
                        menu.show(iv, Side.LEFT, event.getX(), event.getY());

                    }
                });
                try {
                    imageSelector.addToAll(imageDatabaseId, iv);
                } catch (Exception e) {
                    System.err.println(e.getMessage() + e);
                }
                //Add the created element p to the grid in pos (j,i)
                StackPane pStack = new StackPane();
                pStack.getChildren().add(p);
                if (loggedIn && publicImagesInView.contains(imageDatabaseId)) {
                    //Jos kirjauduttu ja kuva on julkinen niin lisätään "Julkinen"-label kuvan oikeaan yläreunaan.
                    Label publicLabel = new Label("Julkinen");
                    publicLabel.setTextFill(Color.WHITE);
                    publicLabel.setPadding(new Insets(2.0));
                    publicLabel.setBackground(new Background(new BackgroundFill(Color.web("#5aaaf6", 0.85), new CornerRadii(2.0), Insets.EMPTY)));
                    pStack.setAlignment(Pos.TOP_RIGHT);
                    pStack.getChildren().add(publicLabel);
                }
                //TODO:DRAG TOIMINTO!!!---------------------------------------------------------------------------------------------
                //TODO:DRAG TOIMINTO!!!---------------------------------------------------------------------------------------------
                //TODO:DRAG TOIMINTO!!!---------------------------------------------------------------------------------------------
//                p.setOnDragDetected(event -> {
//                    /* drag was detected, start a drag-and-drop gesture*/
//                    /* allow any transfer mode */
//                    Dragboard db = p.startDragAndDrop(TransferMode.ANY);
//
//                    /* Put a string on a dragboard */
//                    ClipboardContent content = new ClipboardContent();
//                    content.putString(source.getText());
//                    db.setContent(content);
//
//                    event.consume();
//                });
                fotosGridPane.add(pStack, j, i);
                t++;
                //Add column constraints
                if (i < 1) fotosGridPane.getColumnConstraints().add(cc);
            }
            //Add row constraints
            fotosGridPane.getRowConstraints().add(rc);
        }
//        fotosGridPane.setGridLinesVisible(false); //For debug
//        fotosGridPane.setGridLinesVisible(true); //For debug
        databaseChanged = false;
        System.out.println("Grid done");
    }

    //Poistaa valitut kuvat tai jos ei mitään valittuna niin sen mistä klikattiin juuri oikealla hiirellä.
    private void deleteSelectedImages(int clickedImageDatabaseId) {
        ArrayList<Integer> selectedImageIds = imageSelector.getSelectedIds();

        //Jos mitään ei ollut valittuna niin lisätään poistettavien listaan se kuva josta klikattiin juuri oikealla.
        if (selectedImageIds.size() == 0) {
            selectedImageIds.add(clickedImageDatabaseId);
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Haluatko varmasti poistaa valitut " + selectedImageIds.size() + " kuvaa?");
        alert.setTitle(langBundle.getString("deleteImageConfirmationTitle"));
        alert.setHeaderText(null);

        Optional<ButtonType> vastaus = alert.showAndWait();
        boolean success = true;
        if (vastaus.isPresent() && vastaus.get() == ButtonType.OK) {

            for (Integer i : selectedImageIds) {
                //success jää falseksi jos yksikin kuvanpoisto epäonnistuu.
                success = success && database.deleteImage(i);
            }
            imageSelector.clearSelection();
            databaseChanged = true;
            adjustImageGrid();
            StringBuilder b = new StringBuilder();
            for (Integer i : selectedImageIds) {
                b.append(i + " ");
            }

            System.out.println("Deleted images with ids: " + b);
            if (success) {
                Alert info = new Alert(Alert.AlertType.INFORMATION, "Poistettiin valitut " + selectedImageIds.size() + " kuvaa.");
                info.setTitle("Poistettu");
                info.setHeaderText(null);
                info.showAndWait();
            } else {
                Alert info = new Alert(Alert.AlertType.ERROR, "Kuvan poistossa tapahtui virhe");
                info.setTitle("Virhe");
                info.setHeaderText(null);
                info.showAndWait();
            }

        } else {
            System.out.println("Nothing deleted");
        }

    }

    private boolean setSelectedImagesPublicity(int clickedImageId, boolean publc) {
        boolean success = true;
        int count = 0;
        if (imageSelector.countSelected() == 0) {
            success = success && database.setImagePublicity(clickedImageId, publc);
            count++;
        } else {
            ArrayList<Integer> selected = imageSelector.getSelectedIds();
            for (Integer i : selected) {
                success = success && database.setImagePublicity(i, publc);
                count++;
            }
        }


        if (success) {
            System.out.println("Set " + count + " images " + (publc ? "public" : "private"));
        } else {
            System.out.println("Error in setting some images publicity");
        }
        return success;
    }

    private void refreshImageData(){
        Image image = bigPicture.getImage();

        imageName.setText("filename");
        imageOwner.setText("uploader");
        imageDate.setText("upload date.");
        imageResolution.setText((int)image.getWidth()+"x"+(int)image.getHeight());
        imageSize.setText("size");
        imageFileFormat.setText("Get filename ending.");
    }

    @FXML//Cycle pictures back when viewing them
    private void cycleImageBack() {
        if (currentImageIndex < 1) {
            System.out.println("Start reached.");
            return;
        }
        try {
            currentImageIndex = currentImageIndex - 1;
            currentImageID = imageIdList.get(currentImageIndex);
            bigPicture.setImage(database.downloadFullImage(currentImageID.intValue()));
            refreshImageData();
        } catch (Error e) {
            System.out.println("Full picture not found!:" + e);
        }
    }

    @FXML//Cycle pictures forward when viewing them
    private void cycleImageForward() {
        if (currentImageIndex == imageIdList.size() - 1) {
            System.out.println("End reached.");
            return;
        }
        try {
            currentImageIndex = currentImageIndex + 1;
            currentImageID = imageIdList.get(currentImageIndex);
            bigPicture.setImage(database.downloadFullImage(currentImageID.intValue()));
            refreshImageData();
        } catch (Error e) {
            System.out.println("Full picture not found!:" + e);
        }
    }

    private void clearLoginFields() {
        usernameField.setText("");
        passwordField.setText("");
        emailField1.setText("");
        emailField2.setText("");
    }

    private void clearNewFolderMenuFields() {
        folderNameField.setText("");
        newFolderErrorText.setText("");
    }

    private void logout() {
        if (privateUserLevel == 1000){
            //Käyttäjä oli admin
            julkisetKuvatButton.setVisible(true);
            searchTextField.setVisible(true);
            tarkennettuHakuLabel.setVisible(true);
            adminBorderPane.setVisible(false);
            System.out.println("Admin kirjautui ulos");
        }

        loggedIn = false;
        privateUserID = -1;
        privateUserLevel = -1;
        database.setPrivateUserId(-1);
        databaseChanged = true;
        omatKuvatButton.setVisible(false);
        jaetutKuvatButton.setVisible(false);
        addImageButton.setVisible(false);
        usernameLabel.setText("Kirjaudu/Rekisteröidy");
        folderGridPane.getChildren().clear();
        newFolderButton.setVisible(false);
        switchToDefaultScene();
        resetBreadCrumbs();
        resetFilters();
        adjustImageGrid();
    }

    public void fetchUserInfo(int methodUserID, int methodUserLevel, String userSurName, String userFrontName, String userEmail) {
        if (!loggedIn) {
            privateUserID = methodUserID;
            privateUserLevel = methodUserLevel;
            settingsSurNameString = userSurName;
            settingsFrontNameString = userFrontName;
            settingsEmailString = userEmail;
        }
    }

    @FXML
    private void login() {
        if (Objects.equals(usernameField.getText(), "")) {
            loginErrorText.setText("Syötä käyttäjätunnus");
        } else if (database.userAndPwExists(usernameField.getText(), passwordField.getText()) != 0) {
            int userid = database.userAndPwExists(usernameField.getText(), passwordField.getText());
            loggedIn = true;
            if (privateUserLevel == 1000){
                //Käyttäjä on admin
                openAdminView();
                addImageButton.setVisible(false);
                julkisetKuvatButton.setVisible(false);
                searchTextField.setVisible(false);
                tarkennettuHakuLabel.setVisible(false);
            } else {
                loadUserFolders(userid, 0);
                omatKuvatButton.setVisible(true);
                jaetutKuvatButton.setVisible(true);
                addImageButton.setVisible(true);
                newFolderButton.setVisible(true);
                omatKuvatButton.requestFocus();
                displayImages = DisplayImages.OWN;
                loadUserRootFolder();
            }


            usernameLabel.setText(usernameField.getText());
            settingsUserName.setText(usernameField.getText());
            settingsSurNameTextField.setText(settingsSurNameString);
            settingsFrontNameTextField.setText(settingsFrontNameString);
            settingsEmailTextField.setText(settingsEmailString);
            userName = usernameField.getText();
            loginVbox.setVisible(false);

            clearLoginFields();
            loginErrorText.setText("");
            databaseChanged = true;
            resetFilters();

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
    private void onMainBorderPaneClick(Event event) {
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
        if (loggedIn) {
            //Tähän tullaa ku painetaan sinistä pluspallo-kuvaketta kuvan lisäämiseks.
            System.out.println("Add image");
            //Varmistetaan että controller on saanut start-metodilta mainStagen
            if (mainStage != null) {
                //Tiedostonvalintaikkuna
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Valitse kuvatiedosto(t)");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
                        // new FileChooser.ExtensionFilter("All Files", "*.*")
                );
                List<File> files = fileChooser.showOpenMultipleDialog(mainStage);
                //Valittiinko tiedostoja?
                if (files != null) {
                    //Rakennetaan varmistuskysymys
                    Stage dialog = new Stage();
                    StringBuilder kysymys = new StringBuilder();
                    if (files.size() == 1) {
                        kysymys.append("Haluatko varmasti ladata palveluun seuraavan kuvan?\n");
                    } else {
                        kysymys.append("Haluatko varmasti ladata palveluun seuraavat " + files.size() + " kuvaa?\n");
                    }
                    final int rows_in_confirmation = 10;

                    if (files.size() <= rows_in_confirmation) {
                        files.forEach(file -> kysymys.append(file.getName() + '\n'));
                    } else {
                        Iterator<File> it = files.iterator();
                        for (int i = 0; i < rows_in_confirmation; i++) {
                            kysymys.append(it.next().getName() + '\n');
                        }
                        kysymys.append("...\n");
                    }

                    //Esitetään varmistuskysymys
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, kysymys.toString());
                    alert.setTitle("Vahvista");
                    alert.setHeaderText(null);

                    Optional<ButtonType> vastaus = alert.showAndWait();
                    if (vastaus.isPresent() && vastaus.get() == ButtonType.OK) {
                        //Upload on another thread
                        uploadingStackPane.setVisible(true);
                        addImageButtonImageView.setVisible(false);
                        RotateTransition rotateLoadingImage = new RotateTransition(new Duration(2000), uploadingRotatingImageview);
                        rotateLoadingImage.setByAngle(360);
                        rotateLoadingImage.setCycleCount(1000);
                        rotateLoadingImage.play();

                        Runnable uploadTask = () -> {
                            System.out.println("Upload for userID " + privateUserID);
                            database.uploadImages(privateUserID, selectedFolderID, files);
                            System.out.println("Uploaded.");
                            databaseChanged = true;
                            Platform.runLater(() -> {
                                rotateLoadingImage.stop();
                                uploadingStackPane.setVisible(false);
                                addImageButtonImageView.setVisible(true);
                                adjustImageGrid();
                                System.out.println("adjusted?");
                            });

                        };
                        Thread uploadThread = new Thread(uploadTask);
                        uploadThread.setDaemon(true);
                        uploadThread.start();

                    } else {
                        //No upload
                        System.out.println("No upload");
                    }


                }
            }
        }
    }


    @FXML
    protected void onProfileClick() {
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
            usernameField.requestFocus();
        }
    }

    @FXML
    private Pane filterMenu, pictureInfo;
    @FXML
    private StackPane filterButtonStackPane, pictureInfoArrow;

    @FXML
    private void onFilterShowHidebuttonClick() {
        TranslateTransition transitionMenu = new TranslateTransition(new Duration(500), filterMenu);
        RotateTransition rotateButton = new RotateTransition(new Duration(500), filterButtonStackPane);
        if (filterMenu.getTranslateX() != 0) {
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
    private void onPictureInfoShowHideButtonClick() {
        TranslateTransition transitionMenu = new TranslateTransition(new Duration(500), pictureInfo);
        RotateTransition rotateButton = new RotateTransition(new Duration(500), pictureInfoArrow);
        if (pictureInfo.getTranslateX() != 0) {
            //Avataan kiinni oleva menu
            System.out.println("Kuva infot auki!");
            transitionMenu.setToX(0);
            transitionMenu.play();
            rotateButton.setByAngle(180);
            rotateButton.play();
            pictureInfo.setManaged(true);
            System.out.println("LayoutX: " + pictureInfo.getLayoutX());
            System.out.println("TranslateX: " + pictureInfo.getTranslateX());
        } else {
            //Suljetaan auki oleva menu
            System.out.println("Kuva infot kiinni!");
            transitionMenu.setToX(-(pictureInfo.getWidth()));
            transitionMenu.play();
            rotateButton.setByAngle(180);
            rotateButton.play();
            transitionMenu.setOnFinished(event -> {
                pictureInfo.setManaged(false);
                System.out.println("LayoutX: " + pictureInfo.getLayoutX());
                System.out.println("TranslateX: " + pictureInfo.getTranslateX());
            });
        }
    }

    @FXML
    private void onFolderShowHidebuttonClick() {
        TranslateTransition transitionMenu = new TranslateTransition(new Duration(500), folderMenu);
        RotateTransition rotateButton = new RotateTransition(new Duration(500), folderButtonStackPane);
        System.out.println("Folder translateY: " + folderMenu.getTranslateY());
        if (folderMenu.getTranslateY() != 0) {
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
            transitionMenu.setToY(-(folderMenu.getHeight() * 2));
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
        if (privateUserLevel == 1000){
            //Adminoikeudet
            adminSettingsLabel.setVisible(true);
            adminSettingsLabel.setText("Ylläpitäjä");
            adminBorderPane.setVisible(false);

        } else {
            adminSettingsLabel.setVisible(false);
        }
        resetBreadCrumbs();
        settingsBorderPane.setManaged(true);
        settingsBorderPane.setVisible(true);
        scrollp.setManaged(false);
        scrollp.setVisible(false);
        filterMenuHbox.setManaged(false);
        filterMenuHbox.setVisible(false);
        folderMenu.setVisible(false);
        folderMenuHideButton.setManaged(false);
        folderMenuHideButton.setVisible(false);
        settingsUserInfoUpdateResponse.setText("");
        settingsUserPasswordUpdateResponse.setText("");
        settingsOldPassword.setText("");
        settingsNewPassword.setText("");
        settingsNewPasswordAgain.setText("");


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
    public void changeUserInfo() {
        String userSurName = settingsSurNameTextField.getText();
        String userFrontName = settingsFrontNameTextField.getText();
        String userEmail = settingsEmailTextField.getText();
        if (database.changeUserInfoDB(userSurName, userFrontName, userEmail, privateUserID)) {
            settingsUserInfoUpdateResponse.setText("Käyttäjän tiedot päivitetty onnistuneesti");
            settingsUserInfoUpdateResponse.setStyle("-fx-text-fill: black");
        } else {
            settingsUserInfoUpdateResponse.setText("Käyttäjän tietojen päivittäminen epäonnistui");
            settingsUserInfoUpdateResponse.setStyle("-fx-text-fill: red");
        }
    }

    @FXML
    public void changeUserPassword() {
        String oldPassword = settingsOldPassword.getText();
        String newPassword = settingsNewPassword.getText();
        String newPasswordAgain = settingsNewPasswordAgain.getText();

        if (!Objects.equals(newPassword, newPasswordAgain)) {
            settingsUserPasswordUpdateResponse.setText("Uuden salasanan pitää olla samanlainen molemmassa kentässä");
            settingsUserPasswordUpdateResponse.setStyle("-fx-text-fill: red");
        } else if (database.userAndPwExists(userName, oldPassword) != 0) {
            if (database.changeUserPassword(privateUserID, newPassword)) {
                settingsUserPasswordUpdateResponse.setText("Uuden salasanan uusiminen onnistui");
                settingsUserPasswordUpdateResponse.setStyle("-fx-text-fill: black");
            }
        } else {
            settingsUserPasswordUpdateResponse.setText("Salasanan uusimisessa tapahtui virhe");
            settingsUserPasswordUpdateResponse.setStyle("-fx-text-fill: red");
        }
    }

    @FXML
    public void deleteUserImages() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Haluatko varmasti poistaa kaikki kuvat");
        alert.setTitle("Vahvista");
        alert.setHeaderText(null);

        Optional<ButtonType> vastaus = alert.showAndWait();
        if (vastaus.isPresent() && vastaus.get() == ButtonType.OK) {
            if (database.deleteAllUserImages(privateUserID)) {
                Alert alert2 = new Alert(Alert.AlertType.INFORMATION, "Kaikki kuvat poistettu onnistuneesti");
                alert2.setTitle("Kuvat poistettu");
                alert2.setHeaderText(null);
                alert2.showAndWait();
            } else {
                Alert alert3 = new Alert(Alert.AlertType.INFORMATION, "Kuvien poisto epäonnistui, yritä uudestan");
                alert3.setTitle("Kuvat eivät poistunut");
                alert3.setHeaderText(null);
                alert3.showAndWait();
            }
        } else {
            System.out.println("Abort user image deletion");
        }
    }

    @FXML
    public void deleteUser() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Haluatko varmasti poistaa tilin? Huom. myös kaikki tallennetut kuvat poistetaan!");
        alert.setTitle("Vahvista");
        alert.setHeaderText(null);

        Optional<ButtonType> vastaus = alert.showAndWait();
        if (vastaus.isPresent() && vastaus.get() == ButtonType.OK) {
            if (database.deleteUser(privateUserID)) {
                Alert alert2 = new Alert(Alert.AlertType.INFORMATION, "Tili poistettu onnistuneesti");
                alert2.setTitle("Tili poistettu");
                alert2.setHeaderText(null);
                alert2.showAndWait();

                logout();
            } else {
                Alert alert3 = new Alert(Alert.AlertType.INFORMATION, "Tilin poisto epäonnistui, yritä uudestan");
                alert3.setTitle("Tili ei poistunut");
                alert3.setHeaderText(null);
                alert3.showAndWait();
            }
        } else {
            System.out.println("Abort user deletion");
        }
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
        newFolderButton.setVisible(true);
        if (privateUserLevel == 1000){
            adminBorderPane.setVisible(true);
        } else {
            loadUserFolders(privateUserID, 0);
            loadUserRootFolder();
            resetBreadCrumbs();
            updateBreadCrumbs(selectedFolderID, "root");
        }

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
    public void testDownload() {
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
    public void loadUserFolders(int userId, int parentfolder) {
        //Haetaan tietokannasta
        System.out.println("Ladataan kansioita...");
        HashMap<Integer, String> folderinfo;
        folderinfo = database.getUserFolders(userId, parentfolder);

        int i = 0;
        //Asetetaan kansiot käyttöliittymään
        for (Integer folder : folderinfo.keySet()) {
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
                    } else if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                        onFolderClick(folder, folderinfo.get(folder));
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
            database.uploadNewFolder(newfoldername, privateUserID, selectedFolderID);
            newFolderVbox.setVisible(false);
            clearNewFolderMenuFields();
            folderGridPane.getChildren().clear();
            loadUserFolders(privateUserID, selectedFolderID);
        }
    }

    @FXML
    //Kansioiden poistaminen
    public void onDeleteFolderButtonClick(Integer folderid) {
        database.deleteFolder(folderid);
        folderGridPane.getChildren().clear();
        loadUserFolders(privateUserID, selectedFolderID);
    }

    @FXML
    //Kun kansiota klikataan
    public void onFolderClick(Integer folderid, String foldername) {
        selectedFolderID = folderid;
        System.out.println("Näytetään folderid: " + selectedFolderID);
        updateBreadCrumbs(folderid, foldername);
        folderGridPane.getChildren().clear();
        loadUserFolders(privateUserID, folderid);
        databaseChanged = true;
        adjustImageGrid();
    }

    @FXML
    //käyttäjän root-kansion näyttämiseen
    public void loadUserRootFolder() {
        selectedFolderID = database.getRootFolderId(privateUserID);
        databaseChanged = true;
        adjustImageGrid();
        updateBreadCrumbs(selectedFolderID, "root");
    }

    //Breadcrumbssien päivitykseen
    public void updateBreadCrumbs(Integer folderid, String foldername) {

        //breadCrumbArrayList.add(foldername);
        System.out.println("Added folderid: " + folderid + " and name: " + foldername);
        //breadCrumbGridPane.getChildren().clear();

        Label label1 = new Label(foldername);
        Label label2 = new Label(">");
        label1.setFont(new Font(14));
        label1.setAlignment(Pos.CENTER_LEFT);
        label1.setId(String.valueOf(folderid));
        breadCrumbGridPane.add(label2, breadCrumbGridPaneCounter, 0, 1, 1);
        breadCrumbGridPane.add(label1, breadCrumbGridPaneCounter + 1, 0, 1, 1);
        breadCrumbGridPaneCounter += 2;

       /* int j = 0;
        for (int i = 0; i < breadCrumbArrayList.size(); i++) {
            Label label1 = new Label(breadCrumbArrayList.get(i));
            Label label2 = new Label(">");
            label1.setFont(new Font(14));
            label1.setAlignment(Pos.CENTER_LEFT);
            label1.setId(String.valueOf(folderid));
            breadCrumbGridPane.add(label2, j, 0, 1, 1);
            breadCrumbGridPane.add(label1, j + 1, 0, 1, 1);

            int finalI = i;*/
        System.out.println("Label id: " + label1.getId());
        label1.setOnMouseEntered(mouseEvent1 -> {
            label1.setUnderline(true);

            label1.setOnMouseExited(mouseEvent3 -> label1.setUnderline(false));
        });
        label1.setOnMouseClicked(mouseEvent2 -> onBreadCrumbClick(folderid, label1, foldername));


        //j += 2;

    }

//}

    //Breadcrumbssien resetointiin
    public void resetBreadCrumbs() {
        //breadCrumbArrayList.clear();
        breadCrumbGridPane.getChildren().clear();
        breadCrumbGridPaneCounter = 0;
    }

    //Kun jotain breadcrumbia klikataan, poistetaan edellä olevat breadcrumbit
    public void onBreadCrumbClick(Integer folderid, Node node, String foldername) {

        //System.out.println("BREADCRUMB SIZE: " + breadCrumbArrayList.size());
        System.out.println("CLICKED BREADCRUMB FOLDERID: " + folderid + " AND NAME :" + foldername);
        int clickedBreadCrumbIndex = GridPane.getColumnIndex(node);
        for (int i = breadCrumbGridPane.getChildren().size() - 1; i >= 0; i--) {
            if (i >= clickedBreadCrumbIndex - 1) {
                Node node2 = breadCrumbGridPane.getChildren().get(i);
                breadCrumbGridPane.getChildren().remove(node2);
                breadCrumbGridPaneCounter--;
            }
        }

        /*for (int i = breadCrumbArrayList.size() - 1; i >= 0; i--) {
            if (i >= arrayindex) {
                System.out.println("REMOVED INDEX: " + i);
                breadCrumbArrayList.remove(i);
            }
        }*/
        //onFolderClick(folderid, foldername);

        onFolderClick(folderid, foldername);
    }

    public void onOwnImagesButtonClick() {
        displayImages = DisplayImages.OWN;
        refreshImageGrid();
    }

    public void onPublicImagesButtonClick() {
        displayImages = DisplayImages.PUBLIC;
        folderGridPane.getChildren().clear();
        newFolderButton.setVisible(false);
        resetBreadCrumbs();
        refreshImageGrid();
    }

    public void onSharedImagesButtonClick() {
        //displayImages = DisplayImages.SHARED;
    }

    public void setPublicImagesInView(ArrayList<Integer> list) {
        publicImagesInView = list;

    }

    public void onDatepickerClick() {
        refreshImageGrid();
    }

    public void onNewestToOldestImageOrderButtonClick() {
        newestToOldestOrder = true;
        refreshImageGrid();
    }

    public void onOldestToNewestImageOrderButtonClick() {
        newestToOldestOrder = false;
        refreshImageGrid();
    }

    public void resetFilters() {
        searchTextField.setText("");
        dateFilter.setValue(null);
        newestToOldestOrder = false;
        refreshImageGrid();
    }

    public void openAdminView(){
        userList = database.listUsers();
        String userSearchText = adminViewSearchUserTextField.getText();
        adminViewUsersVBox.getChildren().clear();
        adminViewAdminsVBox.getChildren().clear();
        StackPane.setMargin(adminBorderPane, new Insets(topBarGridPane.getHeight(),0,0,0));
        adminBorderPane.setVisible(true);
        //Sort according to username
        userList.sort((usr1,usr2) -> usr1.getUserName().compareTo(usr2.getUserName()));
        Iterator<FotosUser> it = userList.iterator();
        int userCount = 0;
        int adminCount = 0;
        while (it.hasNext()){
            FotosUser usr = it.next();
            VBox row = new VBox();
            HBox info = new HBox();
            HBox controls = new HBox();
            controls.setVisible(false);
            controls.setManaged(false);
            info.setOnMouseClicked(event-> {
                if (!controls.isVisible()){
                    controls.setVisible(true);
                    controls.setManaged(true);
                } else {
                    controls.setVisible(false);
                    controls.setManaged(false);
                }

            });
            row.getChildren().addAll(info,controls);

            Button deleteBtn = new Button();
            deleteBtn.setText("Poista");
            deleteBtn.setOnAction(event -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Haluatko varmasti poistaa käyttäjän " + usr.getUserName() + "?");
                alert.setTitle("Vahvista");
                alert.setHeaderText(null);
                Optional<ButtonType> vastaus = alert.showAndWait();
                if (vastaus.isPresent() && vastaus.get() == ButtonType.OK) {
                    if (usr.getUserLevel() == 1000 && database.countAdmins() == 1){
                        Alert i = new Alert(Alert.AlertType.INFORMATION, "Varoitus! Viimeistä adminia ei voi poistaa tätä kautta.");
                        i.setTitle("Varoitus");
                        i.setHeaderText(null);
                        i.showAndWait();
                    } else {
                        database.deleteUser(usr.getUserID());
                        openAdminView();
                        if (usr.getUserID() == privateUserID){
                            //Admin poisti itsensä... Kirjaudutaan ulos
                            logout();
                        }
                    }

                }
            });
            controls.getChildren().add(deleteBtn);

            if (usr.getUserLevel() == 1000){
                Label userNameLabel = new Label(usr.getUserName());
                userNameLabel.setPrefWidth(adminViewAdminsVBox.getWidth());
                userNameLabel.setAlignment(Pos.CENTER_LEFT);
                info.getChildren().add(userNameLabel);
                switch (adminCount%2){
                    case 0:
                        row.setBackground(new Background(new BackgroundFill(Color.web("#ebebeb"),CornerRadii.EMPTY,Insets.EMPTY)));
                        break;
                    case 1:
                        row.setBackground(new Background(new BackgroundFill(Color.web("#cbcbcb"),CornerRadii.EMPTY,Insets.EMPTY)));
                        break;
                }
                adminViewAdminsVBox.getChildren().add(row);
                adminCount++;
            } else {
                Label userNameLabel = new Label(usr.getUserName());
                Label firstNameLabel = new Label(usr.getFirstName());
                Label lastNameLabel = new Label(usr.getLastName());
                info.getChildren().addAll(userNameLabel,firstNameLabel,lastNameLabel);
                info.setSpacing(10);

                switch (userCount%2){
                    case 0:
                        row.setBackground(new Background(new BackgroundFill(Color.web("#ebebeb"),CornerRadii.EMPTY,Insets.EMPTY)));
                    break;
                    case 1:
                        row.setBackground(new Background(new BackgroundFill(Color.web("#cbcbcb"),CornerRadii.EMPTY,Insets.EMPTY)));
                    break;
                }
                //hbox.setMaxWidth(Double.MAX_VALUE);
                double hboxPrefWidth = adminUsersViewScrollPane.getWidth();
                //hbox.setPrefWidth(hboxPrefWidth);
                userNameLabel.setPrefWidth(hboxPrefWidth/3);
                firstNameLabel.setPrefWidth(hboxPrefWidth/3);
                lastNameLabel.setPrefWidth(hboxPrefWidth/3);
                userNameLabel.setAlignment(Pos.CENTER_LEFT);
                firstNameLabel.setAlignment(Pos.CENTER_LEFT);
                lastNameLabel.setAlignment(Pos.CENTER_LEFT);
                Button deleteImagesBtn = new Button();
                deleteImagesBtn.setText("Poista kuvat");
                deleteImagesBtn.setOnAction(event -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Haluatko varmasti poistaa kaikki käyttäjän " + usr.getUserName() + " kuvat?");
                    alert.setTitle("Vahvista");
                    alert.setHeaderText(null);
                    Optional<ButtonType> vastaus = alert.showAndWait();
                    if (vastaus.isPresent() && vastaus.get() == ButtonType.OK) {
                        database.deleteAllUserImages(usr.getUserID());
                        Alert i = new Alert(Alert.AlertType.INFORMATION, "Poistettiin kaikki käyttäjän " + usr.getUserName() + " kuvat?");
                        i.setTitle("Poistettu");
                        i.setHeaderText(null);
                        i.showAndWait();
                        openAdminView();
                    }
                });
                controls.getChildren().add(deleteImagesBtn);

                adminViewUsersVBox.setMaxWidth(Double.MAX_VALUE);
                adminViewUsersVBox.getChildren().add(row);
                userCount++;


            }

        }

    }
    @FXML
    public void onAdminViewUserSearchTextTyped(){
        String searchString = adminViewSearchUserTextField.getText();
        Iterator<Node>it = adminViewUsersVBox.getChildren().iterator();
        while (it.hasNext()){
            VBox row = (VBox)it.next();
            HBox info = (HBox)row.getChildren().get(0);
            String username = ((Label)info.getChildren().get(0)).getText();
            if (username.contains(searchString)){
                row.setVisible(true);
                row.setManaged(true);
                System.out.print(username + ", ");
            } else {
                row.setVisible(false);
                row.setManaged(false);
            }

        }
        System.out.println(".");

    }

    @FXML
    public void onAddNewAdminButtonClick(){
        newAdminInfoVbox.setVisible(true);
    }
    @FXML
    public void onFinalAddNewAdminButtonClick(){
        if (adminUsernameField.getText().equals("") || adminPasswordField.getText().equals("") ){
            newAdminErrorText.setText("Käyttäjätunnus tai salasana ei voi olla tyhjä");

        } else {
            if (database.userExists(adminUsernameField.getText())){
                newAdminErrorText.setText("Käyttäjätunnus on jo olemassa.");
            } else {
                newAdminErrorText.setText("");
                database.saltRegister(adminUsernameField.getText(), adminPasswordField.getText(), adminEmailField.getText(), adminEmailField2.getText(), newAdminErrorText);
                int userId = database.userAndPwExists(adminUsernameField.getText(), adminPasswordField.getText());
                if (userId > 0){
                    database.changeUserLevel(userId, 1000);
                    adminUsernameField.clear();
                    adminPasswordField.clear();
                    adminEmailField.clear();
                    adminEmailField2.clear();
                    newAdminInfoVbox.setVisible(false);
                    openAdminView();
                }
            }

        }


    }

    public int getPrivateUserLevel() {
        //For tests
        return privateUserLevel;
    }





}