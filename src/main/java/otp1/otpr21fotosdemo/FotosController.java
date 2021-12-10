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
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
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

import java.awt.event.KeyAdapter;
import java.io.*;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import static java.lang.Double.valueOf;

/**
 * Controller class has all the listeners and handlers for the Fotos.fxml ui.
 * @author Kalle Voutilainen, Petri Immonen, Jüri Tihane, Janne Kaukua
 */

public class FotosController {
    @FXML
    private BorderPane rootborderpane, settingsBorderPane, imageViewBorderPane;
    @FXML
    private Circle profile;
    @FXML
    private StackPane folderMenuHideButton, folderMenu, rootStackPane;
    @FXML
    private StackPane folderButtonStackPane;
    @FXML
    private GridPane fotosGridPane, folderGridPane, breadCrumbGridPane, topBarGridPane;
    @FXML
    private HBox filterMenuHbox, pictureInfoBox;
    @FXML
    private Pane bigpicParent;
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
    @FXML
    private ChoiceBox<String> languageChoiceBox;

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
        /*
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
        }
        */
        adminBorderPane.setVisible(false);
        newAdminInfoVbox.setVisible(false);
        displayImages = DisplayImages.PUBLIC;
        publicImagesInView = new ArrayList<>();
        userList = new ArrayList<>();
        imageSelector = new ImageSelector();
        database = new Database();
        database.setController(this);
        logout();
        //Filtermenu hidden at the start.
        filterMenu.setTranslateX(-200);
//        pictureInfoBox.setTranslateX(-230);
        filterButtonStackPane.setRotate(180);
//        pictureInfoArrow.setRotate(180);
        filterMenu.setManaged(false);
//        pictureInfoBox.setManaged(false);
        uploadingStackPane.setVisible(false);

        //New folder and Login menu hidden and inside that register textfields as well.
        loginVbox.setVisible(false);
        emailVbox.setVisible(false);
        emailVbox.setManaged(false);
        settingsBorderPane.setManaged(false);
        settingsBorderPane.setVisible(false);
        imageViewStackPane.setVisible(false);
        newFolderVbox.setVisible(false);
        newFolderButton.setVisible(false);
        //Listener for searchfield.
        searchTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                    databaseChanged = true;
                    adjustImageGrid();
                }
            }
        });
        //This is needed for the picture selections to be cleared when clicked somewhere else.
        rootborderpane.setOnMouseClicked(event -> {
            if (event.isControlDown() ||event.isShiftDown() || event.getPickResult().getIntersectedNode().getTypeSelector().equals("ImageView")) {
                //Klikattiin imageviewiin tai CTRL tai SHIFT pohjas
                return;
            } else if (imageSelector.countSelected() > 0) {
                imageSelector.clearSelection();
            }
        });
        imageViewStackPane.setOnKeyPressed(event -> {//TODO: adjust this so it works DX
            if(event.getCode() == KeyCode.RIGHT && imageViewStackPane.isVisible()) {
                cycleImageForward();
                imageViewStackPane.requestFocus();
            }
            if(event.getCode() == KeyCode.LEFT && imageViewStackPane.isVisible()) {
                cycleImageBack();
                imageViewStackPane.requestFocus();
            }
        });
    }

    /**
     * @param stage
     */
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

    /**
     * Adds the language selection menu and adds listeners for changing the language.
     *
     * @param bund  New resource bundle
     * @param loc   New locale
     */
    public void setLangBundleAndCurLocale(ResourceBundle bund, Locale loc){
        langBundle = bund;
        curLocale = loc;
        languageChoiceBox.getItems().addAll("FI", "EN");
        languageChoiceBox.setValue(curLocale.getLanguage().toUpperCase());
        languageChoiceBox.setOnAction(event -> {
            String selection = languageChoiceBox.getValue();
            if (selection.equals("FI") && !curLocale.getLanguage().equals("fi")) {
                changeLanguage("fi", "FI");
            } else if (selection.equals("EN")  && !curLocale.getLanguage().equals("en")) {
                changeLanguage("en", "GB");
            }
        });
    }

    /**
     * Reloads the scene and sets the new language.
     * @param lang      New language
     * @param country   New country
     */
    public void changeLanguage(String lang, String country){
        curLocale = new Locale(lang,country);
        Locale.setDefault(curLocale);
        langBundle = ResourceBundle.getBundle("otp1.otpr21fotosdemo.TextResources", curLocale);
        FXMLLoader fxmlLoader = new FXMLLoader(Fotos.class.getResource("Fotos.fxml"), langBundle);

        try {/*
            Scene scene = new Scene(fxmlLoader.load(), 1280, 800);
            mainStage.setScene(scene);
            */
            StackPane root2 = fxmlLoader.load();
            mainStage.getScene().setRoot(root2);
            FotosController controller = fxmlLoader.getController();
            controller.setLangBundleAndCurLocale(langBundle, curLocale);
            controller.setMainStage(mainStage);

            //Alustustoimenpiteet
            controller.initialize();
            System.out.println("KIELI VAIHDETTU!");

        } catch (IOException e){
            e.printStackTrace();
        }




    }

    private void openImageview() {
        if(!imageViewStackPane.isVisible())
        blurringStackPane.setEffect(new GaussianBlur());
        imageViewStackPane.setVisible(true);
        imageViewStackPane.requestFocus();
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

    /**
     * Convenience-method for forcing new imagegrid
     */
    private void refreshImageGrid() {
        //Convenience-method for forcing new imagegrid
        databaseChanged = true;
        adjustImageGrid();
    }

    /**
     * Downloads images from database and shows them in the application.
     */
    private void adjustImageGrid() {
        if (loggedIn && privateUserLevel == 1000){
            //User is an admin. No need for imagegrid.
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
            //User is logged in.
            if (displayImages == DisplayImages.PUBLIC) {
                //Selected "Public images"
                if (!Objects.equals(searchTextField.getText(), "")) {
                    System.out.println("Searching by: " + searchTextField.getText());
                    images = database.downloadPublicImages(searchTextField.getText(), dateFilter.getValue());
                } else {
                    images = database.downloadPublicImages(null, dateFilter.getValue());
                }

            } else if (displayImages == DisplayImages.OWN) {
                //Selected "Own photos"
                if (!Objects.equals(searchTextField.getText(), "")) {
                    System.out.println("Searching by: " + searchTextField.getText());
                    images = database.downloadImages(selectedFolderID, searchTextField.getText(), dateFilter.getValue());
                } else {
                    images = database.downloadImages(selectedFolderID, null,  dateFilter.getValue());
                }
            } else {
                //Selected "Shared photos"
                //TODO Jaetut kuvat toteutus
                //Giving an empty hashmap for now.
                images = new HashMap<Integer, Pair<String, javafx.scene.image.Image>>();
            }
        } else {
            //käyttäjä ei ole kirjautunut.
            images = database.downloadPublicImages(null, dateFilter.getValue());
        }
        StringBuilder b = new StringBuilder();
        b.append("Grids imageID:s: ");
        //Organizing list by imageID.
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
                            //CTRL clicked
                            if (imageSelector.isSelected(imageDatabaseId))
                                imageSelector.removeFromSelection(imageDatabaseId);
                            else
                                imageSelector.addToSelection(imageDatabaseId);

                        } else if (event.isShiftDown()) {
                            //SHIFT clicked
                            if (imageSelector.countSelected() == 0) {
                                imageSelector.addToSelection(imageDatabaseId);
                            } else {
                                ArrayList<Integer> lista = imageSelector.getSelectedIds();
                                //Previous selected
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
                            //NO CTRL OR SHIFT clicked.
                            //If images selected, just clear choices.

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
                                bigPicture.fitHeightProperty().bind(bigpicParent.heightProperty());
                                bigPicture.fitWidthProperty().bind(bigpicParent.widthProperty());
                            } else {
                                bigPicture.setImage(iv.getImage());
                            }
                            refreshImageData();
                            openImageview();
                        }
                    } else if (event.getButton() == MouseButton.SECONDARY) {
                        //Right click
                        ContextMenu menu = new ContextMenu();
                        if (loggedIn && displayImages == DisplayImages.OWN) {
                            MenuItem menuitem1 = new MenuItem(langBundle.getString("imgMenuSetAllPublicText"));
                            MenuItem menuitem2 = new MenuItem(langBundle.getString("imgMenuSetAllPrivateText"));
                            menuitem1.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    //Set to public
                                    int count = imageSelector.countSelected();
                                    count = count == 0 ? 1 : count; //If no images selected, set count=1, because then function is aimed at selected image
                                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, MessageFormat.format(langBundle.getString("publishMultipleImgConfirmationText"),count));
                                    alert.setTitle(langBundle.getString("generalConfirmationTitle"));
                                    alert.setHeaderText(null);

                                    Optional<ButtonType> vastaus = alert.showAndWait();

                                    if (vastaus.isPresent() && vastaus.get() == ButtonType.OK) {
                                        boolean success = setSelectedImagesPublicity(imageDatabaseId, true);
                                        refreshImageGrid();
                                        if (success) {
                                            Alert info = new Alert(Alert.AlertType.INFORMATION, MessageFormat.format(langBundle.getString("publishMultipleImgSuccessText"),count));
                                            info.setTitle(langBundle.getString("publishMultipleImgSuccessTitle"));
                                            info.setHeaderText(null);
                                            info.showAndWait();
                                        } else {
                                            Alert info = new Alert(Alert.AlertType.ERROR, langBundle.getString("publishMultipleImgFailureText"));
                                            info.setTitle(langBundle.getString("generalErrorTitle"));
                                            info.setHeaderText(null);
                                            info.showAndWait();
                                        }
                                    }

                                }
                            });
                            menuitem2.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    //To private
                                    int count = imageSelector.countSelected();
                                    count = count == 0 ? 1 : count; //If no images selected, set count=1, because then function is aimed at selected image
                                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, MessageFormat.format(langBundle.getString("setPrivateMultipleImgConfirmationText"),count));
                                    alert.setTitle(langBundle.getString("generalConfirmationTitle"));
                                    alert.setHeaderText(null);

                                    Optional<ButtonType> vastaus = alert.showAndWait();

                                    if (vastaus.isPresent() && vastaus.get() == ButtonType.OK) {
                                        boolean success = setSelectedImagesPublicity(imageDatabaseId, false);
                                        refreshImageGrid();
                                        if (success) {
                                            Alert info = new Alert(Alert.AlertType.INFORMATION, MessageFormat.format(langBundle.getString("setPrivateMultipleImgSuccessText"),count));
                                            info.setTitle(langBundle.getString("setPrivateMultipleImgSuccessTitle"));
                                            info.setHeaderText(null);
                                            info.showAndWait();
                                        } else {
                                            Alert info = new Alert(Alert.AlertType.ERROR, langBundle.getString("setPrivateMultipleImgFailureText"));
                                            info.setTitle(langBundle.getString("generalErrorTitle"));
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
                                //Only one image selected or one image was right-clicked without selecting other images.
                                menuitem1.setText(langBundle.getString("imgMenuSetOnePublicText"));
                                menuitem2.setText(langBundle.getString("imgMenuSetOnePrivateText"));
                                //Figuring out images publicity
                                boolean publc;
                                if (imageSelector.countSelected() == 0) {
                                    publc = database.imageIsPublic(imageDatabaseId);
                                } else {
                                    //selected == 1
                                    publc = database.imageIsPublic(imageSelector.getSelectedIds().get(0));
                                }

                                if (publc) {
                                    //To private
                                    menu.getItems().addAll(menuitem2);
                                } else {
                                    //To public
                                    menu.getItems().addAll(menuitem1);
                                }
                            }

                            MenuItem menuitem3 = new MenuItem(langBundle.getString("imgMenuDeleteText"));
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
                    //If logged in and image is public, add "public"-label to images top-right corner
                    Label publicLabel = new Label(langBundle.getString("imgThumbnailPublicLabel"));
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

    /**
     * Deletes selected images or if nothing is selected, deletes where right-clicked.
     *
     * @param clickedImageDatabaseId    Image id in database.
     */
    private void deleteSelectedImages(int clickedImageDatabaseId) {
        ArrayList<Integer> selectedImageIds = imageSelector.getSelectedIds();

        //If nothing is selected, add right-clicked image to the to-be-deleted list.
        if (selectedImageIds.size() == 0) {
            selectedImageIds.add(clickedImageDatabaseId);
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, MessageFormat.format(langBundle.getString("deleteMultipleImgConfirmationText"),selectedImageIds.size()));
        alert.setTitle(langBundle.getString("generalConfirmationTitle"));
        alert.setHeaderText(null);

        Optional<ButtonType> vastaus = alert.showAndWait();
        boolean success = true;
        if (vastaus.isPresent() && vastaus.get() == ButtonType.OK) {

            for (Integer i : selectedImageIds) {
                //Success stays false if even one image deletion fails.
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
                Alert info = new Alert(Alert.AlertType.INFORMATION, MessageFormat.format(langBundle.getString("deleteMultipleImgSuccessText"),selectedImageIds.size()));
                info.setTitle(langBundle.getString("deleteMultipleImgSuccessTitle"));
                info.setHeaderText(null);
                info.showAndWait();
            } else {
                Alert info = new Alert(Alert.AlertType.ERROR, langBundle.getString("deleteMultipleImgFailureText"));
                info.setTitle(langBundle.getString("generalErrorTitle"));
                info.setHeaderText(null);
                info.showAndWait();
            }

        } else {
            System.out.println("Nothing deleted");
        }

    }

    /**
     *
     * @param clickedImageId    Clicked image
     * @param publc             true=public and false=private
     * @return                  true=success and false=failed
     */
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

    /**
     * Refreshes image info panel.
     */
    private void refreshImageData(){
        //TODO: insert values
        try {
//            bigpicParent.maxHeight(bigPicture.getImage().getHeight());
//            System.out.println("picture height"+bigPicture.getImage().getHeight());
//            System.out.println(bigpicParent.getHeight());
//            bigpicParent.maxWidth(bigPicture.getImage().getWidth());
            ImageData data = Database.imageData;
            imageName.setText(data.fileName());
            imageOwner.setText(data.fileOwner());
            imageDate.setText(""+data.creationDate());
            imageResolution.setText(data.fileResolution());
            imageSize.setText(""+data.fileSize()+"MB");
            imageFileFormat.setText(data.fileType());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows previous image when viewing images in full screen mode.
     */
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
        imageViewStackPane.requestFocus();//TODO: cycle images with arrow keys.
    }

    /**
     * Shows next image when viewing images in full screen mode.
     */
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
        imageViewStackPane.requestFocus();
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

    /**
     * Performs actions that need to be performed when logging out.
     */
    private void logout() {
        if (privateUserLevel == 1000){
            //User was admin
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
        if (langBundle != null) //This is still null, when on startup initialize calls (logout)
            usernameLabel.setText(langBundle.getString("usernameLabelNotLoggedinText"));
        folderGridPane.getChildren().clear();
        newFolderButton.setVisible(false);
        switchToDefaultScene();
        resetBreadCrumbs();
        resetFilters();
        adjustImageGrid();
    }

    /**
     * Method for passing information to the controller
     *
     * @param methodUserID
     * @param methodUserLevel
     * @param userSurName
     * @param userFrontName
     * @param userEmail
     */
    public void fetchUserInfo(int methodUserID, int methodUserLevel, String userSurName, String userFrontName, String userEmail) {
        if (!loggedIn) {
            privateUserID = methodUserID;
            privateUserLevel = methodUserLevel;
            settingsSurNameString = userSurName;
            settingsFrontNameString = userFrontName;
            settingsEmailString = userEmail;
        }
    }

    /**
     * Performs actions that need to be performed when logged in.
     */
    @FXML
    private void login() {
        if (Objects.equals(usernameField.getText(), "")) {
            loginErrorText.setText(langBundle.getString("loginErrorUsernamePasswordEmpty"));
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
            if (!settingsSurNameString.equals("empty")) {
                settingsSurNameTextField.setText(settingsSurNameString);
            }
            if (!settingsFrontNameString.equals("empty")) {
                settingsFrontNameTextField.setText(settingsFrontNameString);
            }
            settingsEmailTextField.setText(settingsEmailString);
            userName = usernameField.getText();
            loginVbox.setVisible(false);

            clearLoginFields();
            loginErrorText.setText("");
            databaseChanged = true;
            resetFilters();

        } else {
            loginErrorText.setText(langBundle.getString("loginErrorWrongUsernamePassword"));
        }
    }

    /**
     * Validates registering fields.
     *
     * @throws UnsupportedEncodingException
     */
    @FXML
    private void registerMenu() throws UnsupportedEncodingException {
        System.out.println("emailvbox: " + emailVbox.isVisible());
        if (Objects.equals(usernameField.getText(), "") || passwordField.getText().equals("")) {
            loginErrorText.setText(langBundle.getString("loginErrorUsernamePasswordEmpty"));
        } else if (emailVbox.isVisible()) {
            // Lähetetään pyyntö back-end koodin puolelle, jossa toteutetaan tarkistukset ja datan pusku palvelimelle
            if (!database.userExists(usernameField.getText())) {
                database.saltRegister(usernameField.getText(), passwordField.getText(), emailField1.getText(), emailField2.getText(), loginErrorText);
                //Tehdään root-kansio uudelle käyttäjälle
                int userid = database.userAndPwExists(usernameField.getText(), passwordField.getText());
            } else {
                loginErrorText.setText(langBundle.getString("loginErrorRegisterUsernameAlreadyExists"));
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

    /**
     * Performs actions that need to be performed when clicked on the main stack pane.
     *
     * @param event Used for locating where the user clicked.
     */
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

    /**
     * Performs the image selection process when uploading new images.
     */
    @FXML
    protected void onAddImgButtonClick() {
        if (loggedIn) {
            //When clicked on blue plus button
            System.out.println("Add image");
            //Confirming that controller got the mainStage from start-method
            if (mainStage != null) {
                //File selection window
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle(langBundle.getString("uploadImgFileChooserTitle"));
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter(langBundle.getString("uploadImgFileFormatFilterText"), "*.png", "*.jpg", "*.gif")
                        // new FileChooser.ExtensionFilter("All Files", "*.*")
                );
                List<File> files = fileChooser.showOpenMultipleDialog(mainStage);
                //Were any files selected?
                if (files != null) {
                    //Rakennetaan varmistuskysymys
                    Stage dialog = new Stage();
                    StringBuilder kysymys = new StringBuilder();
                    if (files.size() == 1) {
                        kysymys.append(langBundle.getString("uploadImgConfirmOneText"));
                        kysymys.append("\n");
                    } else {
                        kysymys.append(MessageFormat.format(langBundle.getString("uploadImgConfirmMultipleText"), files.size()));
                        kysymys.append("\n");
                    }
                    final int rows_in_confirmation = 10;

                    if (files.size() <= rows_in_confirmation) {
                        files.forEach(file -> kysymys.append(file.getName() + '\n'));
                    } else {
                        Iterator<File> it = files.iterator();
                        for (int i = 0; i < rows_in_confirmation; i++) {
                            kysymys.append(it.next().getName());
                            kysymys.append("\n");
                        }
                        kysymys.append("...\n");
                    }

                    //Showing confirmation message
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, kysymys.toString());
                    alert.setTitle(langBundle.getString("generalConfirmationTitle"));
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
                                //System.out.println("adjusted?");
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


    /**
     * Shows profile menu.
     */
    @FXML
    protected void onProfileClick() {
        if (loggedIn) {
            //When clicked on profile photo.
            System.out.println("Cursor on profile picture.");
            //Making a menu that appears under the profile picture.
            ContextMenu menu = new ContextMenu();
            //Making options for the menu and functions for them.
            MenuItem settings = new MenuItem(langBundle.getString("settingsMenuItemText"));
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
            MenuItem logout = new MenuItem(langBundle.getString("logOutMenuItemText"));
            logout.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    System.out.println("Kirjauduttu ulos.");
                    logout();
                }
            });
            //Adding menu options to the menu.
            menu.getItems().addAll(settings, logout);
            //Showing the menu to the user.
            double boundsInScenex = profile.localToScene(profile.getBoundsInLocal()).getMaxX();
            double boundsInSceney = profile.localToScene(profile.getBoundsInLocal()).getMaxY();
            menu.show(profile, boundsInScenex, boundsInSceney);
        } else {
            //Login menu open.
            loginVbox.setVisible(true);
            usernameField.requestFocus();
        }
    }

    @FXML
    private Pane filterMenu, pictureInfo;
    @FXML
    private StackPane filterButtonStackPane, pictureInfoArrow;

    /**
     * Shows or hides the filter menu depending on if it's open or closed.
     */
    @FXML
    private void onFilterShowHidebuttonClick() {
        TranslateTransition transitionMenu = new TranslateTransition(new Duration(500), filterMenu);
        RotateTransition rotateButton = new RotateTransition(new Duration(500), filterButtonStackPane);
        if (filterMenu.getTranslateX() != 0) {
            //Opening filter menu that was closed
            System.out.println("Filterit auki!");
            transitionMenu.setToX(0);
            transitionMenu.play();
            rotateButton.setByAngle(180);
            rotateButton.play();
            filterMenu.setManaged(true);
            System.out.println("LayoutX: " + filterMenu.getLayoutX());
            System.out.println("TranslateX: " + filterMenu.getTranslateX());
        } else {
            //Closing filter menu that was open
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

    /**
     *  Shows or hides the picture info menu depending on if it's open or closed.
     */
    @FXML
    private void onPictureInfoShowHideButtonClick() {
        TranslateTransition transitionMenu = new TranslateTransition(new Duration(500), pictureInfoBox);
        RotateTransition rotateButton = new RotateTransition(new Duration(500), pictureInfoArrow);
        if (pictureInfoBox.getTranslateX() != 0) {
            //Opening menu that was closed
            System.out.println("Kuva infot auki!");
            transitionMenu.setToX(0);
            transitionMenu.play();
            rotateButton.setByAngle(180);
            rotateButton.play();
            pictureInfoBox.setManaged(true);
            System.out.println("LayoutX: " + pictureInfoBox.getLayoutX());
            System.out.println("TranslateX: " + pictureInfoBox.getTranslateX());
        } else {
            //Closing menu that was open
            System.out.println("Kuva infot kiinni!");
            transitionMenu.setToX(-(pictureInfo.getWidth()));
            transitionMenu.play();
            rotateButton.setByAngle(180);
            rotateButton.play();
            transitionMenu.setOnFinished(event -> {
                pictureInfoBox.setManaged(false);
                System.out.println("LayoutX: " + pictureInfoBox.getLayoutX());
                System.out.println("TranslateX: " + pictureInfoBox.getTranslateX());
            });
        }
    }

    /**
     *  Shows or hides the folder menu depending on if it's open or closed.
     */
    @FXML
    private void onFolderShowHidebuttonClick() {
        TranslateTransition transitionMenu = new TranslateTransition(new Duration(500), folderMenu);
        RotateTransition rotateButton = new RotateTransition(new Duration(500), folderButtonStackPane);
        System.out.println("Folder translateY: " + folderMenu.getTranslateY());
        if (folderMenu.getTranslateY() != 0) {
            //Opening menu that was closed
            System.out.println("Folderit auki!");
            transitionMenu.setToY(0);
            transitionMenu.play();
            rotateButton.setByAngle(180);
            rotateButton.play();
            folderMenu.setManaged(true);
            //filterMenuHbox.setPadding(Insets.EMPTY);
            rootborderpane.setMargin(filterMenuHbox, Insets.EMPTY);
        } else {
            //Closing menu that was open
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

    /**
     * Turns on the settings scene.
     *
     * @throws IOException
     */
    @FXML
    public void switchToSettingsScene() throws IOException {
        //Showing settings elements and hiding main scene elements.
        if (privateUserLevel == 1000){
            //Admin privileges
            adminSettingsLabel.setVisible(true);
            adminSettingsLabel.setText(langBundle.getString("adminStatusLabelText"));
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

    /**
     * Updates user data in database.
     */
    @FXML
    public void changeUserInfo() {
        String userSurName = settingsSurNameTextField.getText();
        String userFrontName = settingsFrontNameTextField.getText();
        String userEmail = settingsEmailTextField.getText();
        if (database.changeUserInfoDB(userSurName, userFrontName, userEmail, privateUserID)) {
            settingsUserInfoUpdateResponse.setText(langBundle.getString("successfulUserInfoUpdate"));
            settingsUserInfoUpdateResponse.setStyle("-fx-text-fill: black");
        } else {
            settingsUserInfoUpdateResponse.setText(langBundle.getString("unsuccessfulUserInfoUpdate"));
            settingsUserInfoUpdateResponse.setStyle("-fx-text-fill: red");
        }
    }

    /**
     * Updates users password in database.
     */
    @FXML
    public void changeUserPassword() {
        String oldPassword = settingsOldPassword.getText();
        String newPassword = settingsNewPassword.getText();
        String newPasswordAgain = settingsNewPasswordAgain.getText();

        if (!Objects.equals(newPassword, newPasswordAgain)) {
            settingsUserPasswordUpdateResponse.setText(langBundle.getString("samePasswordInBothFieldsText"));
            settingsUserPasswordUpdateResponse.setStyle("-fx-text-fill: red");
        } else if (database.userAndPwExists(userName, oldPassword) != 0) {
            if (database.changeUserPassword(privateUserID, newPassword)) {
                settingsUserPasswordUpdateResponse.setText(langBundle.getString("passwordChangeSuccessfulText"));
                settingsUserPasswordUpdateResponse.setStyle("-fx-text-fill: black");
            }
        } else {
            settingsUserPasswordUpdateResponse.setText(langBundle.getString("passwordChangeUnsuccessfulText"));
            settingsUserPasswordUpdateResponse.setStyle("-fx-text-fill: red");
        }
    }

    /**
     * Deletes all user images in database.
     */
    @FXML
    public void deleteUserImages() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, langBundle.getString("deleteUserImagesAlertText"));
        alert.setTitle(langBundle.getString("deleteUserImagesAlertTitle"));
        alert.setHeaderText(null);

        Optional<ButtonType> vastaus = alert.showAndWait();
        if (vastaus.isPresent() && vastaus.get() == ButtonType.OK) {
            if (database.deleteAllUserImages(privateUserID)) {
                Alert alert2 = new Alert(Alert.AlertType.INFORMATION, langBundle.getString("photosDeletedAlertText"));
                alert2.setTitle(langBundle.getString("photosDeletedAlertTitle"));
                alert2.setHeaderText(null);
                alert2.showAndWait();
            } else {
                Alert alert3 = new Alert(Alert.AlertType.INFORMATION, langBundle.getString("photosDeletedErrorAlertText"));
                alert3.setTitle(langBundle.getString("photosDeletedErrorAlertTitle"));
                alert3.setHeaderText(null);
                alert3.showAndWait();
            }
        } else {
            System.out.println("Abort user image deletion");
        }
    }

    /**
     * Deletes user from database.
     */
    @FXML
    public void deleteUser() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, langBundle.getString("deleteAccountAlertText"));
        alert.setTitle(langBundle.getString("deleteAccountAlertTitle"));
        alert.setHeaderText(null);

        Optional<ButtonType> vastaus = alert.showAndWait();
        if (vastaus.isPresent() && vastaus.get() == ButtonType.OK) {
            if (database.deleteUser(privateUserID)) {
                Alert alert2 = new Alert(Alert.AlertType.INFORMATION, langBundle.getString("accountDeletedAlertText"));
                alert2.setTitle(langBundle.getString("accountDeletedAlertTitle"));
                alert2.setHeaderText(null);
                alert2.showAndWait();

                logout();
            } else {
                Alert alert3 = new Alert(Alert.AlertType.INFORMATION, langBundle.getString("accountDeletedErrorAlertText"));
                alert3.setTitle(langBundle.getString("accountDeletedErrorAlertTitle"));
                alert3.setHeaderText(null);
                alert3.showAndWait();
            }
        } else {
            System.out.println("Abort user deletion");
        }
    }

    /**
     * Turns on default scene.
     */
    @FXML
    public void switchToDefaultScene() {
        //Showing main scene elements and hiding settings scene elements.

        settingsBorderPane.setManaged(false);
        settingsBorderPane.setVisible(false);
        scrollp.setManaged(true);
        scrollp.setVisible(true);
        filterMenuHbox.setManaged(true);
        filterMenuHbox.setVisible(true);
        folderMenu.setVisible(true);
        folderMenuHideButton.setManaged(true);
        folderMenuHideButton.setVisible(true);
        if (loggedIn) {
            newFolderButton.setVisible(true);
            if (privateUserLevel == 1000) {
                adminBorderPane.setVisible(true);
            } else {
                loadUserFolders(privateUserID, 0);
                loadUserRootFolder();
                resetBreadCrumbs();
                updateBreadCrumbs(selectedFolderID, "root");
            }
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

    /**
     * Loads and shows user folders.
     *
     * @param userId Users id, used for database query.
     * @param parentfolder Needed for showing inner folders.
     */
    @FXML
    public void loadUserFolders(int userId, int parentfolder) {
        //Searching from database
        System.out.println("Ladataan kansioita...");
        HashMap<Integer, String> folderinfo;
        folderinfo = database.getUserFolders(userId, parentfolder);

        int i = 0;
        //Showing the folders in interface
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

            //For folder deletion
            vbox.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                        ContextMenu menu = new ContextMenu();
                        MenuItem menuitem1 = new MenuItem(langBundle.getString("foldersContextMenuDeleteFolder"));
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

    /**
     * Shows menu for creating a new folder.
     */
    @FXML
    public void onNewFolderButtonClick() {
        System.out.println("Uuden kansion kuvaketta painettu");
        newFolderVbox.setVisible(true);
        newFolderErrorText.setVisible(false);
        newFolderErrorText.setManaged(false);
    }

    /**
     * Validates text-fields and uploads the new folder to database.
     */
    @FXML
    public void onNewFolderReadyButtonClick() {
        String newfoldername;

        if (folderNameField.getText().equals("")) {
            newFolderErrorText.setText(langBundle.getString("newFolderNoFolderNameErrorText"));
            newFolderErrorText.setVisible(true);
            newFolderErrorText.setManaged(true);
        } else if (folderNameField.getText().equals("root") || folderNameField.getText().equals("ROOT")) {
            newFolderErrorText.setText(langBundle.getString("newFolderFolderNameIsRootErrorText"));
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

    /**
     * Deletes the folder and its images from database.
     *
     * @param folderid Folder to be deleted.
     */
    @FXML
    public void onDeleteFolderButtonClick(Integer folderid) {
        database.deleteFolder(folderid);
        folderGridPane.getChildren().clear();
        loadUserFolders(privateUserID, selectedFolderID);
    }

    /**
     * Shows images from the clicked folder from database.
     *
     * @param folderid      Folder to be shown.
     * @param foldername    Folders name.
     */
    @FXML
    public void onFolderClick(Integer folderid, String foldername) {
        selectedFolderID = folderid;
        System.out.println("Näytetään folderid: " + selectedFolderID);
        updateBreadCrumbs(folderid, foldername);
        folderGridPane.getChildren().clear();
        loadUserFolders(privateUserID, folderid);
        databaseChanged = true;
        adjustImageGrid();
    }

    /**
     * Loads users root folder from database and shows the images in it.
     */
    @FXML
    public void loadUserRootFolder() {
        selectedFolderID = database.getRootFolderId(privateUserID);
        databaseChanged = true;
        adjustImageGrid();
        updateBreadCrumbs(selectedFolderID, "root");
    }

    /**
     * Adds a new folder breadcrumb to the interface.
     *
     * @param folderid      for setting an id to the new breadcrumb element
     * @param foldername    folders name to be shown in the breadcrumb.
     */
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

    /**
     * Clears the breadcrumbs.
     */
    public void resetBreadCrumbs() {
        //breadCrumbArrayList.clear();
        breadCrumbGridPane.getChildren().clear();
        breadCrumbGridPaneCounter = 0;
    }

    /**
     * Deletes previous breadcrumbs before the clicked breadcrumb and shows images from the clicked folder.
     *
     * @param folderid      Clicked folder id.
     * @param node          Clicked node.
     * @param foldername    Name of the folder that was clicked.
     */
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
    /**
     * At the moment changes the view to own images
     */
    public void onFotosLogoClick(){
        onOwnImagesButtonClick();
        omatKuvatButton.requestFocus();
    }
    /**
     * Changes the view to own images
     */
    public void onOwnImagesButtonClick() {
        switchToDefaultScene();
        resetBreadCrumbs();
        loadUserRootFolder();
        displayImages = DisplayImages.OWN;
        refreshImageGrid();
    }
    /**
     * Changes the view to public images
     */
    public void onPublicImagesButtonClick() {
        displayImages = DisplayImages.PUBLIC;
        folderGridPane.getChildren().clear();
        newFolderButton.setVisible(false);
        resetBreadCrumbs();
        refreshImageGrid();
    }
    /**
     * (TODO) Changes the view to shared images
     */
    public void onSharedImagesButtonClick() {
        //displayImages = DisplayImages.SHARED;
    }
    /**
     * Sets the list of public images in current view (usually folder). Needed for "public"-labels
     */
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

    /**
     * Turns on admin view scene.
     */
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
            deleteBtn.setText(langBundle.getString("adminDeleteUserBtnText"));
            deleteBtn.setOnAction(event -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, MessageFormat.format(langBundle.getString("adminDeleteUserConfirmationText"), usr.getUserName()));
                alert.setTitle(langBundle.getString("generalConfirmationTitle"));
                alert.setHeaderText(null);
                Optional<ButtonType> vastaus = alert.showAndWait();
                if (vastaus.isPresent() && vastaus.get() == ButtonType.OK) {
                    if (usr.getUserLevel() == 1000 && database.countAdmins() == 1){
                        Alert i = new Alert(Alert.AlertType.INFORMATION, langBundle.getString("adminLastAdminDeleteWarningText"));
                        i.setTitle(langBundle.getString("adminLastAdminDeleteWarningTitle"));
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
                deleteImagesBtn.setText(langBundle.getString("adminDeleteImagesBtnText"));
                deleteImagesBtn.setOnAction(event -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, MessageFormat.format(langBundle.getString("adminDeleteUserImagesConfirmationText"), usr.getUserName()));
                    alert.setTitle(langBundle.getString("generalConfirmationTitle"));
                    alert.setHeaderText(null);
                    Optional<ButtonType> vastaus = alert.showAndWait();
                    if (vastaus.isPresent() && vastaus.get() == ButtonType.OK) {
                        database.deleteAllUserImages(usr.getUserID());
                        Alert i = new Alert(Alert.AlertType.INFORMATION, MessageFormat.format(langBundle.getString("adminDeleteUserImagesSuccessText"),usr.getUserName()));
                        i.setTitle(langBundle.getString("adminDeleteUserImagesSuccessTitle"));
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

    /**
     * Searches for users in the user list in adminview.
     */
    @FXML
    public void onAdminViewUserSearchTextTyped(){
        String searchString = adminViewSearchUserTextField.getText();
        Iterator<Node>it = adminViewUsersVBox.getChildren().iterator();
        while (it.hasNext()){
            VBox row = (VBox)it.next();
            HBox info = (HBox)row.getChildren().get(0);
            String username = ((Label)info.getChildren().get(0)).getText();
            if (username.toLowerCase().contains(searchString.toLowerCase())){
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
    /**
     * Opens the view for adding new admin-user
     */
    @FXML
    public void onAddNewAdminButtonClick(){
        newAdminInfoVbox.setVisible(!newAdminInfoVbox.isVisible());

    }
    /**
     * Attepmts to register new admin with information given in new admin view textfields.
     */
    @FXML
    public void onFinalAddNewAdminButtonClick(){
        if (adminUsernameField.getText().equals("") || adminPasswordField.getText().equals("") ){
            newAdminErrorText.setText(langBundle.getString("adminNewAdminErrorUserEmpty"));

        } else {
            if (database.userExists(adminUsernameField.getText())){
                newAdminErrorText.setText(langBundle.getString("adminNewAdminErrorUserExists"));
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