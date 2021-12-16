package otp1.otpr21fotosdemo;

import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.commons.codec.binary.Hex;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.xml.transform.Result;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.FileTime;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.*;

/**
 * Database class handles all connections and data transfers to and from the mySQL database.
 * @author Kalle Voutilainen, Petri Immonen, J&uuml;ri Tihane, Janne Kaukua
 */

public class Database {
    private String dbUserName = System.getenv("APP_DB_USERNAME");
    private String dbPassword = System.getenv("APP_DB_PASSWORD");
    private String url = System.getenv("APP_DB_URL");
    private final int MAX_THUMB_HEIGHT = 400;
    private final int MAX_THUMB_WIDTH = 400;
    private HashMap<Integer, ImageData> fullImageCache = new HashMap<>();
    private int privateUserId;
    private FotosController controller;
    public static ImageData imageData;

    public Database() {

    }

    /**
     * Set FotosController for communicating with the controller.
     * @param c current FotosController in use.
     */
    public void setController(FotosController c) {
        controller = c;
    }

    /**
     * When user is logging in, store the userID also for database functions.
     * @param i ID of the current user.
     */
    public void setPrivateUserId(int i) {
        privateUserId = i;
    }

    /**
     * Used for adding a new user to database before creating a password hash.
     * @param userName username for the user.
     * @param passWord plaintext password.
     * @param email1 email of the user.
     * @param email2 email of the user again.
     * @param loginErrorText reference to a JavaFX Text -element for displaying error message or other information.
     */
    public void saltRegister(String userName, String passWord, String email1, String email2, Text loginErrorText) {
        String salt = "1234";
        int iterations = 10000;
        int keyLength = 512;
        char[] passwordChars = passWord.toCharArray();
        byte[] saltBytes = salt.getBytes();

        byte[] hashedBytes = hashPassword(passwordChars, saltBytes, iterations, keyLength);
        String hashedString = Hex.encodeHexString(hashedBytes);

        System.out.println(hashedString);
        register(userName, hashedString, email1, email2, loginErrorText);
    }

    /**
     * Creates a hash from plaintext password using hashPassword-method.
     * @param passWord plaintext password
     * @return hash generated from the password.
     */
    public String saltLogin(String passWord) {
        String salt = "1234";
        int iterations = 10000;
        int keyLength = 512;
        char[] passwordChars = passWord.toCharArray();
        byte[] saltBytes = salt.getBytes();

        byte[] hashedBytes = hashPassword(passwordChars, saltBytes, iterations, keyLength);
        String hashedString = Hex.encodeHexString(hashedBytes);

        System.out.println(hashedString);
        return hashedString;
    }

    /**
     * Creates a hash from plaintext password.
     * @param password plaintext password
     * @param salt salt.
     * @param iterations number of iterations.
     * @param keyLength key length
     * @return hash generated from the password.
     */
    public byte[] hashPassword(final char[] password, final byte[] salt, final int iterations, final int keyLength) {
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKey key = skf.generateSecret(spec);
            byte[] res = key.getEncoded();
            return res;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Used for adding a new user to database after creating a password hash.
     * @param userName username for the user.
     * @param passWord password hash for the user.
     * @param email1 email of the user.
     * @param email2 email of the user again.
     * @param loginErrorText reference to a JavaFX Text -element for displaying error message or other information.
     */
    public void register(String userName, String passWord, String email1, String email2, Text loginErrorText) {
        if (userAndPwExists(userName, passWord) == 0) {
            // Variables
            Connection conn = null;

            int userId = -1;
            try {
                // Connection statement
                conn = DriverManager.getConnection(url, dbUserName, dbPassword);
                System.out.println("\nDatabase Connection Established...");

                // USER statement VALUES(userID (int11), frontName (varchar32), surName(varchar32), userLevel(int11)
                // email(varchar32), passWord(varchar64), dbUserName(varchar32)
                PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO Fotos.User(frontName, surName, userLevel, email, passWord, userName) VALUES(?,?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS
                );
                // frontName
                pstmt.setString(1, "empty");
                // surName
                pstmt.setString(2, "empty");
                // userLevel
                pstmt.setInt(3, 1);
                // email
                pstmt.setString(4, email1);
                // passWord
                pstmt.setString(5, passWord);
                // userName
                pstmt.setString(6, userName);
                //Executing the statement
                pstmt.execute();
                ResultSet key = pstmt.getGeneratedKeys();
                key.next();
                userId = key.getInt(1);
                System.out.println("Record inserted......");


            } catch (Exception ex) {
                System.err.println("Cannot connect to database server");
                ex.printStackTrace();

            } finally {
                if (conn != null) {
                    try {
                        System.out.println("\n***** Let terminate the Connection *****");
                        conn.close();
                        System.out.println("\nDatabase connection terminated...");
                    } catch (Exception ex) {
                        System.out.println("Error in connection termination!");
                    }
                }
            }
            if (userId > 0){
                uploadNewFolder("root", userId, 0);
            }
        } else {
            System.out.println("BBBBBBBBBBBBB");
            loginErrorText.setText("AAAAA");
        }
    }

    public static void main(String[] args) {
        System.out.println("\n\n***** MySQL JDBC Connection Testing *****");
    }

    /**
     * Checks if username <i>user</i> exists with password <i>passWord</i>. If user and password match, also passes user information to controller with controllers fetchUserInfo-method.
     * @param user username to be searched for.
     * @param passWord password for the user.
     * @return returns the userID of the user if password was correct. If username and password combination was wrong or the user doesnt exist, returns 0.
     */
    public Integer userAndPwExists(String user, String passWord) {
        passWord = saltLogin(passWord);

        int found = 0;
        int userLevel = 0;
        String userSurName = null;
        String userFrontName = null;
        String userEmail = null;
        Connection conn = null;
        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassword);
            System.out.println("\nDatabase Connection Established...");
            PreparedStatement pstmt = null;
            PreparedStatement pstmt2 = null;

            try {
                pstmt = conn.prepareStatement("SELECT userName,userID,userLevel,surName,frontName,email FROM Fotos.User WHERE userName=?;");
                pstmt.setString(1, user);
                ResultSet result = pstmt.executeQuery();

                pstmt2 = conn.prepareStatement("SELECT COUNT(*) FROM Fotos.User WHERE passWord=?;");
                pstmt2.setString(1, passWord);
                ResultSet result2 = pstmt2.executeQuery();
                result2.next();

                //Tarkistetaan löytyikö yhtään kyseistä usernamea.
                if (!result.next()) {
                    System.out.println("No such username found");
                } else if (Objects.equals(result.getString("userName"), user) && result2.getInt(1) > 0) {
                    found = result.getInt("userID");
                    userLevel = result.getInt("userLevel");
                    userSurName = result.getString("surName");
                    userFrontName = result.getString("frontName");
                    userEmail = result.getString("email");
                    System.out.println(userSurName + userFrontName + userEmail);
                    System.out.println("Found " + Objects.equals(result.getString("userName"), user) + " " + user);
                }

            } catch (Exception e) {
                System.err.println("Error in query");
                e.printStackTrace();

            } finally {
                if (pstmt != null) {
                    try {
                        System.out.println("\n***** Close the statement *****");
                        pstmt.close();
                        System.out.println("\nStatement closed...");

                    } catch (Exception ex) {
                        System.out.println("Error in statement termination!");
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Cannot connect to database server");
            ex.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    System.out.println("\n***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("\nDatabase connection terminated...");

                } catch (Exception ex) {
                    System.out.println("Error in connection termination!");
                }
            }
        }
        // Palauttaa userID ja lähettää sen FotosController variableks
        controller.fetchUserInfo(found,userLevel, userSurName, userFrontName, userEmail);
        privateUserId = found;
        return found;
    }

    /**
     * Changes the users password
     * @param userID ID of the user in question
     * @param newPassword new password
     * @return true if password was changed. False if not.
     */
    public boolean changeUserPassword(int userID, String newPassword) {
        Connection conn = null;
        newPassword = saltLogin(newPassword);

        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassword);
            System.out.println("\nDatabase Connection Established...");

            PreparedStatement pstmt = conn.prepareStatement("UPDATE Fotos.User SET passWord = ? WHERE userID = ?;");
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, userID);
            pstmt.executeUpdate();

        } catch (Exception ex) {
            System.err.println("Cannot connect to database server");
            ex.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    System.out.println("\n***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("\nDatabase connection terminated...");
                } catch (Exception ex) {
                    System.out.println("Error in connection termination!");
                }
            }
        }
        return true;
    }

    /**
     * Changes the users information in database.
     * @param userSurname new surname for the user.
     * @param userFrontName new first name for the user.
     * @param userEmail new email for the user.
     * @param userID existing userID of the user.
     * @return true if some information was changed. Otherwise false.
     */
    public boolean changeUserInfoDB(String userSurname, String userFrontName, String userEmail, int userID) {
        Connection conn = null;
        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassword);
            System.out.println("\nDatabase Connection Established...");

            PreparedStatement pstmt = conn.prepareStatement("UPDATE Fotos.User SET surName = ?, frontName = ?, email = ? WHERE userID = ?;");

            pstmt.setString(1, userSurname);
            pstmt.setString(2, userFrontName);
            pstmt.setString(3, userEmail);
            pstmt.setInt(4, userID);

            pstmt.executeUpdate();

        } catch (Exception ex) {
            System.err.println("Cannot connect to database server");
            ex.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    System.out.println("\n***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("\nDatabase connection terminated...");
                } catch (Exception ex) {
                    System.out.println("Error in connection termination!");
                }
            }
        }
        System.out.println("User info changed successfully.");
        return true;
    }

    /**
     * Checks if a user with a username of <i>user</i> exists
     * @param user username of the user to be searched for.
     * @return true if the username is found in the database.
     */
    public boolean userExists(String user) {
        boolean found = false;
        Connection conn = null;
        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassword);
            System.out.println("\nDatabase Connection Established...");
            PreparedStatement pstmt = null;

            try {
                pstmt = conn.prepareStatement("SELECT COUNT(*) FROM Fotos.User WHERE userName=?;");
                pstmt.setString(1, user);
                ResultSet result = pstmt.executeQuery();
                result.next();

                //Tarkistetaan löytyikö yhtään kyseistä usernamea. (Ei pitäisi olla koskaan enempää kuin yksi)
                if (result.getInt(1) > 0) {
                    found = true;
                }
                System.out.println("Found " + result.getInt(1) + " " + user);

            } catch (Exception e) {
                System.err.println("Error in query");
                e.printStackTrace();

            } finally {
                if (pstmt != null) {
                    try {
                        System.out.println("\n***** Close the statement *****");
                        pstmt.close();
                        System.out.println("\nStatement closed...");

                    } catch (Exception ex) {
                        System.out.println("Error in statement termination!");

                    }
                }

            }


        } catch (Exception ex) {
            System.err.println("Cannot connect to database server");
            ex.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    System.out.println("\n***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("\nDatabase connection terminated...");

                } catch (Exception ex) {
                    System.out.println("Error in connection termination!");

                }
            }

        }
        return found;

    }

    /**
     * Checks if an image with the specified imageID exits.
     * @param imageID imageID to be searched for.
     * @return true if an image with <i>imageID</i> is found. Note: will return false if image-thumbnail with imageID is found but full resolution image is missing or vice versa.
     */
    public boolean imageExists(int imageID){
        System.out.println("Database.imageExists");
        boolean foundThumb = false;
        boolean foundFullres = false;
        Connection conn = null;
        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassword);
            System.out.println("Database Connection Established...");
            PreparedStatement pstmt = null;
            PreparedStatement pstmt2 = null;
            try {
                pstmt = conn.prepareStatement("SELECT COUNT(*) FROM Fotos.Image WHERE imageID=?;");
                pstmt.setInt(1, imageID);
                ResultSet result = pstmt.executeQuery();
                result.next();

                //Tarkistetaan löytyikö yhtään kyseistä imageID:tä. (Ei pitäisi olla koskaan enempää kuin yksi)
                if (result.getInt(1) > 0) {
                    foundThumb = true;
                }
                System.out.println("Found imageID " + imageID + " in Fotos.Image");

                pstmt2 = conn.prepareStatement("SELECT COUNT(*) FROM Fotos.Full_Image WHERE imageID=?;");
                pstmt2.setInt(1, imageID);
                ResultSet result2 = pstmt2.executeQuery();
                result2.next();

                //Tarkistetaan löytyikö yhtään kyseistä imageID:tä. (Ei pitäisi olla koskaan enempää kuin yksi)
                if (result2.getInt(1) > 0) {
                    foundFullres = true;
                }
                System.out.println("Found imageID " + imageID + " in Fotos.Full_Image");

            } catch (Exception e) {
                System.err.println("Error in query");
                e.printStackTrace();

            } finally {
                if (pstmt != null) {
                    try {
                        pstmt.close();

                    } catch (Exception ex) {
                        System.out.println("Error in statement 1 termination!");

                    }
                }
                if (pstmt2 != null) {
                    try {
                        pstmt2.close();

                    } catch (Exception ex) {
                        System.out.println("Error in statement 2 termination!");

                    }
                }

            }


        } catch (Exception ex) {
            System.err.println("Cannot connect to database server");
            ex.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    System.out.println("***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("Database connection terminated...");

                } catch (Exception ex) {
                    System.out.println("Error in connection termination!");

                }
            }

        }
        return (foundThumb && foundFullres);
    }

    /**
     * Deletes all the images in database with userID of <i>userID</i>.
     * @param userID users ID
     * @return true if deletion was succesful.
     */
    public boolean deleteAllUserImages(int userID) {
        Connection conn = null;
        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassword);
            System.out.println("\nDatabase Connection Established...");

            PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Fotos.Image WHERE userID = ?;");
            pstmt.setInt(1, userID);
            pstmt.executeUpdate();

        } catch (Exception ex) {
            System.err.println("Cannot connect to database server");
            ex.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    System.out.println("\n***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("\nDatabase connection terminated...");
                } catch (Exception ex) {
                    System.out.println("Error in connection termination!");
                }
            }
        }
        System.out.println("User images deleted successfully.");
        return true;
    }

    /**
     * Deletes a user.
     * @param userID userID of the user to be deleted
     * @return true if deletion was succesful.
     */
    public boolean deleteUser(int userID) {
        Connection conn = null;
        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassword);
            System.out.println("\nDatabase Connection Established...");

            PreparedStatement pstmt3 = conn.prepareStatement("DELETE FROM Fotos.Image WHERE userID = ?;");
            pstmt3.setInt(1, userID);
            pstmt3.executeUpdate();

            PreparedStatement pstmt2 = conn.prepareStatement("DELETE FROM Fotos.Folder WHERE userID = ?;");
            pstmt2.setInt(1, userID);
            pstmt2.executeUpdate();

            PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Fotos.User WHERE userID = ?;");
            pstmt.setInt(1, userID);
            pstmt.executeUpdate();

        } catch (Exception ex) {
            System.err.println("Cannot connect to database server");
            ex.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    System.out.println("\n***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("\nDatabase connection terminated...");
                } catch (Exception ex) {
                    System.out.println("Error in connection termination!");
                }
            }
        }
        System.out.println("User deleted successfully.");
        return true;
    }

    /**
     * Deletes the image with specified imageID.
     * @param imageID ID of the image to be deleted.
     * @return true if rows where deleted from database.
     */
    public boolean deleteImage(int imageID){
        boolean deleted = false;
        Connection conn = null;
        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassword);
            System.out.println("Database Connection Established...");
            PreparedStatement pstmt = null;
            try {
                pstmt = conn.prepareStatement("DELETE FROM Fotos.Image WHERE  imageID=?;");
                pstmt.setInt(1, imageID);
                pstmt.execute();
                System.out.println("Poistettiin " + pstmt.getUpdateCount() + " riviä taulusta Fotos.Image");
                if (pstmt.getUpdateCount() > 0) {
                    deleted = true;
                }

            } catch (Exception e) {
                System.err.println("Error in query");
                e.printStackTrace();

            } finally {
                if (pstmt != null) {
                    try {
                        pstmt.close();

                    } catch (Exception ex) {
                        System.out.println("Error in statement 1 termination!");

                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Cannot connect to database server");
            ex.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    System.out.println("***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("Database connection terminated...");

                } catch (Exception ex) {
                    System.out.println("Error in connection termination!");

                }
            }
        }
        return deleted;
    }

    /**
     * Sets the publicity status of the image with specified imageID.
     * @param imageID ID of the image in question.
     * @param publc publicity of the image. True being public and false being private.
     * @return true if a row was changed in the database.
     */
    public boolean setImagePublicity(int imageID, boolean publc){
        System.out.println("Database.setImagePublicity");
        boolean success = false;
        int viewingRights = publc ? 1 : 0;
        Connection conn = null;
        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassword);
            System.out.println("Database Connection Established...");
            PreparedStatement pstmt = null;
            try {
                pstmt = conn.prepareStatement("UPDATE Fotos.Image SET viewingRights = ? WHERE imageID = ?;");
                pstmt.setInt(1, viewingRights);
                pstmt.setInt(2, imageID);
                pstmt.execute();
                if (publc)
                    System.out.println("Asetettiin " + pstmt.getUpdateCount() + " kuvaa julkiseksi.");
                else
                    System.out.println("Asetettiin " + pstmt.getUpdateCount() + " kuvaa yksityiseksi.");
                if (pstmt.getUpdateCount() > 0) {
                    success = true;
                }


            } catch (Exception e) {
                System.err.println("Error in query");
                e.printStackTrace();

            } finally {
                if (pstmt != null) {
                    try {
                        pstmt.close();

                    } catch (Exception ex) {
                        System.out.println("Error in statement 1 termination!");

                    }
                }
            }


        } catch (Exception ex) {
            System.err.println("Cannot connect to database server");
            ex.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    System.out.println("***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("Database connection terminated...");

                } catch (Exception ex) {
                    System.out.println("Error in connection termination!");

                }
            }

        }
        return success;
    }

    /**
     * Checks whether image is public or not
     * @param imageID ID of the image in question.
     * @return true if image is public and false if it is private.
     */
    public boolean imageIsPublic(int imageID){
        System.out.println("Database.imageIsPublic");
        Connection conn = null;
        ResultSet result = null;
        int viewingRights = -1;
        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassword);
            System.out.println("Database Connection Established...");
            PreparedStatement pstmt = null;
            try {
                pstmt = conn.prepareStatement(
                        "SELECT viewingRights  FROM Fotos.Image WHERE imageID=?;"
                );
                pstmt.setInt(1, imageID);
                result = pstmt.executeQuery();
                if (result.next()) {
                    viewingRights = result.getInt("viewingRights");
                }
            } catch (Exception e) {
                System.err.println("Error in query");
                e.printStackTrace();
            } finally {
                if (pstmt != null) {
                    try {
                        pstmt.close();
                    } catch (Exception ex) {
                        System.out.println("Error in statement 1 termination!");
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Cannot connect to database server");
            ex.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    System.out.println("***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("Database connection terminated...");
                } catch (Exception ex) {
                    System.out.println("Error in connection termination!");

                }
            }
        }
        if (viewingRights > 0){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns files file extension.
     * @param file file in question.
     * @return String containing only the file extension.
     */
    private static String getFileExtension(File file) {
        String fileName = file.getName();
        int lastDot = fileName.lastIndexOf('.');
        return fileName.substring(lastDot + 1);
    }

    /**
     * returns file size of the specified file.
     * @param file file in question.
     * @return file size in megabytes.
     */
    public float getFileSize(File file) {
        Path path = file.toPath();
        try {
            // size of a file (in bytes)
            long bytes = Files.size(path);
            float kB = (float) bytes / 1024;
            float mB = kB/1024;
            mB = (float)Math.round(mB*100)/100;

            System.out.printf("%,d bytes%n", bytes);
            System.out.printf("%,f kilobytes%n", kB);
            System.out.printf("%,f megabytes%n", mB);
            return mB;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Uploads a list of files to database for given userID and folderID.
     * This method creates a smaller thumbnail Image from original image and also extracts image information from the file and saves those to the Database.
     * NOTE!: This method is time-consuming and should be only run in a separate thread.
     * @param userId ID of the user in question.
     * @param folderId folder ID of the folder where the images are uploaded.
     * @param files List&#60;File&#62; containing imagefiles to be uploaded.
     * @return ArrayList&#60;Integer&#62; of image ID:s (generated by the Database) that were uploaded.
     */
    public List<Integer> uploadImages(int userId, int folderId, List<File> files) {

        System.out.println("UploadTask starting.");
        Connection conn = null;
        ArrayList<Integer> newImageIDs = new ArrayList<>();
        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassword);
            System.out.println("\nDatabase Connection Established...");

            Iterator<File> it = files.iterator();
            //Jokainen tiedosto lähetetään erikseen
            while (it.hasNext()) {
                File originalFile = it.next();

                //Rajataan tiedostonimeä jos se on pidempi kuin tietokannan raja (- 5 merkkiä tiedostonimen perään lisättävää numerointia varten)
                System.out.println("------Image Data------");
                BufferedImage originalBufferedImage = ImageIO.read(originalFile);
                int origWidth = originalBufferedImage.getWidth();
                int origHeight = originalBufferedImage.getHeight();
                String filename = originalFile.getName();
                String filetype = getFileExtension(originalFile);
                float filesize = getFileSize(originalFile);
                FileOwnerAttributeView file = Files.getFileAttributeView(originalFile.toPath(), FileOwnerAttributeView.class);
                String fileowner = file.getOwner().getName();
                fileowner = fileowner.substring(fileowner.indexOf("\\")+1);//TODO: change to author or uploader.
                String fileresolution = origWidth+"x"+origHeight;
                Date creationdate = new Date(Files.readAttributes(originalFile.toPath(), BasicFileAttributes.class).creationTime().toMillis());
                //Debug
                System.out.println("Original filename: "+filename);
                System.out.println("Filetype: "+filetype);
                System.out.println("Filesize: "+filesize);
                System.out.println("File owner: "+fileowner);
                System.out.println("File resolution: "+fileresolution);
                System.out.println("File creation date: "+creationdate);

                if (filename.length() > 59) {
                    String end = filename.substring(filename.lastIndexOf("."));
                    String shortenedFilename = filename.substring(0, (59 - end.length() - 3));

                    PreparedStatement statement = conn.prepareStatement("SELECT COUNT(*) FROM Fotos.imagedata WHERE userID=? AND fileName LIKE ?");//todo:change
                    statement.setInt(1, privateUserId);
                    statement.setString(2, shortenedFilename + "%");
                    ResultSet res = statement.executeQuery();
                    res.next();
                    int countDuplicateFilenames = res.getInt(1);
                    System.out.println("DuplicateFilenames: " + countDuplicateFilenames);
                    System.out.println("Filename length1: " + filename.length());
                    System.out.println("Filename: " + filename);

                    StringBuilder builder = new StringBuilder();
                    builder.append(shortenedFilename);
                    builder.append("---");
                    if (countDuplicateFilenames > 0){
                        builder.append("(");
                        builder.append(countDuplicateFilenames);
                        builder.append(")");
                    }
                    builder.append(end);
                    filename = builder.toString();

                    System.out.println("Filename length2: " + filename.length());

                } else {
                    String end = filename.substring(filename.lastIndexOf("."));
                    String filenameNoEnd = filename.substring(0, (filename.length() - end.length()));
                    PreparedStatement statement = conn.prepareStatement("SELECT COUNT(*) FROM Fotos.imagedata WHERE userID=? AND fileName LIKE ?");//TODO: change
                    statement.setInt(1, privateUserId);
                    statement.setString(2, filenameNoEnd + "%");
                    ResultSet res = statement.executeQuery();
                    res.next();
                    int countDuplicateFilenames = res.getInt(1);
                    if (countDuplicateFilenames > 0){
                        StringBuilder builder = new StringBuilder();
                        builder.append(filenameNoEnd);
                        builder.append("(");
                        builder.append(countDuplicateFilenames);
                        builder.append(")");
                        builder.append(end);
                        filename = builder.toString();
                    }
                }
                System.out.println("Filename: " + filename);

                //Muodostetaan thumbnail InputStream kuvalle
                System.out.println("Thumbthumb... Thumbnailing");

                int thumbWidth, thumbHeight;
                if (origHeight > origWidth) {
                    thumbHeight = MAX_THUMB_HEIGHT;
                    thumbWidth = Math.round((float) thumbHeight / origHeight * origWidth);
                } else {
                    thumbWidth = MAX_THUMB_WIDTH;
                    thumbHeight = Math.round((float) thumbWidth / origWidth * origHeight);
                }

                BufferedImage thumb = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = thumb.createGraphics();
                g.drawImage(originalBufferedImage.getScaledInstance(thumbWidth, thumbHeight, Image.SCALE_SMOOTH), 0, 0, null);
                g.dispose();
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(thumb, "jpg", os);
                InputStream is = new ByteArrayInputStream(os.toByteArray());
                int thumbSize = is.available();
                System.out.println("Thumbthumbthumbthumb... Thumbnailed!");
                PreparedStatement pstmt = null;
                PreparedStatement pstmt2 = null;
                PreparedStatement pstmt3 = null;

                try {
                    //Uploadataan thumbnail
                    pstmt = conn.prepareStatement(
                            "INSERT INTO Fotos.Image(viewingRights, image, userID, folderID) values (?, ?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS
                    );
                    pstmt.setInt(1, 0);
                    pstmt.setBinaryStream(2, is);
                    pstmt.setInt(3, userId);
                    pstmt.setInt(4, folderId);
                    System.out.println("Executing statement...");
                    pstmt.execute();

                    ResultSet key = pstmt.getGeneratedKeys();
                    key.next();
                    System.out.println("Sent " + thumbSize + " bytes. New imageID: " + key.getInt(1) + " Filename: " + filename);
                    newImageIDs.add(key.getInt(1));
                    //Uploadataan täyden reson kuva
                    FileInputStream fis2 = new FileInputStream(originalFile);
                    pstmt2 = conn.prepareStatement(
                            "INSERT INTO Fotos.Full_Image(imageID, image) values (?, ?)"
                    );
                    //Tähä laitetaa edellisessä insertissä saatu autogeneroitu key
                    pstmt2.setInt(1, key.getInt(1));
                    pstmt2.setBinaryStream(2, fis2, (int) originalFile.length());
                    System.out.println("Executing statement 2...");
                    pstmt2.execute();
                    System.out.println("Sent " + originalFile.length() + " bytes.");
                    pstmt3 = conn.prepareStatement(
                            "INSERT INTO Fotos.ImageData(imageID, fileSize, fileName, fileOwner, fileResolution, fileType, creationDate) values (?,?,?,?,?,?,?)"
                    );
                    pstmt3.setInt(1, key.getInt(1));
                    pstmt3.setFloat(2, filesize);
                    pstmt3.setString(3, filename);
                    pstmt3.setString(4, fileowner);
                    pstmt3.setString(5, fileresolution);
                    pstmt3.setString(6, filetype);
                    pstmt3.setDate(7, creationdate);
                    pstmt3.execute();

                } catch (Exception e) {
                    System.err.println("Error in query");
                    e.printStackTrace();

                } finally {
                    if (pstmt != null) {
                        try {
                            pstmt.close();
                        } catch (Exception ex) {
                            System.out.println("Error in statement 1 termination!");
                        }
                    }
                    if (pstmt2 != null) {
                        try {
                            pstmt2.close();

                        } catch (Exception ex) {
                            System.out.println("Error in statement 2 termination!");
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Cannot connect to database server");
            ex.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    System.out.println("\n***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("\nDatabase connection terminated...");

                } catch (Exception ex) {
                    System.out.println("Error in connection termination!");
                }
            }
        }
        System.out.println("UploadTask done.");
        return newImageIDs;
    }

    /**
     * Downloads specified images from the Database. If no searchString and uploadDate are specified, all images in folder are returned.
     * @param folderId ID of the folder where to search for the images.
     * @param searchString search-text to match against images filenames. (Optional. Use NULL if not used)
     * @param uploadDate upload date of the images to be selected. (Optional. Use NULL if not used)
     * @return Map&#60;Integer, Pair&#60;String, javafx.scene.image.Image&#62;&#62; -structure where Map-key contains imageID. Map-value is ja Pair containing filename String and actual image data as javafx.scene.image.Image
     */
    public Map<Integer, Pair<String, javafx.scene.image.Image>> downloadImages(int folderId, String searchString, LocalDate uploadDate) {
        //Palauttaa Hashmapin jossa key on imageID ja Value on PAIR-rakenne. Pair-rakenteessa taas key on tiedostonimi ja value on imagedata
        System.out.println("Database.downloadImages");
        Connection conn = null;
        ResultSet result = null;
        Map<Integer, Pair<String, javafx.scene.image.Image>> images = new HashMap<>();
        ArrayList<Integer> publicImageIDs = new ArrayList<>();

        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassword);
            System.out.println("\nDatabase Connection Established...");

            PreparedStatement pstmt = null;
            try {

                if (searchString != null) {
                    if (uploadDate == null) {
                        pstmt = conn.prepareStatement("SELECT imageID, fileName, image, viewingRights FROM Fotos.imagedata WHERE userID=? AND folderID=? AND fileName LIKE ?");
                        pstmt.setInt(1, privateUserId);
                        pstmt.setInt(2, folderId);
                        pstmt.setString(3, "%" + searchString + "%");
                    } else {
                        pstmt = conn.prepareStatement("SELECT imageID, fileName, image, viewingRights FROM Fotos.imagedata WHERE userID=? AND folderID=? AND creationDate=? AND fileName LIKE ?");
                        pstmt.setInt(1, privateUserId);
                        pstmt.setInt(2, folderId);
                        pstmt.setDate(3, Date.valueOf(uploadDate));
                        pstmt.setString(4, "%" + searchString + "%");
                    }
                } else {
                    if (uploadDate == null) {
                        pstmt = conn.prepareStatement(
                                "SELECT imageID, fileName, image, viewingRights FROM Fotos.imagedata WHERE userID=? AND folderID=?;"
                        );
                        pstmt.setInt(1, privateUserId);
                        pstmt.setInt(2, folderId);
                    } else {
                        pstmt = conn.prepareStatement(
                                "SELECT imageID, fileName, image, viewingRights FROM Fotos.imagedata WHERE userID=? AND folderID=? AND creationDate=?;"
                        );
                        pstmt.setInt(1, privateUserId);
                        pstmt.setInt(2, folderId);
                        pstmt.setDate(3, Date.valueOf(uploadDate));
                    }
                }


                result = pstmt.executeQuery();

                while (result.next()) {
                    int id = result.getInt("imageID");
                    String filename = result.getString("filename");
                    javafx.scene.image.Image image = new javafx.scene.image.Image(result.getBinaryStream("image"));
                    images.put(id, new Pair<>(filename, image));
                    if (result.getInt("viewingRights") > 0){
                        publicImageIDs.add(id);
                    }
                }
                controller.setPublicImagesInView(publicImageIDs);

            } catch (Exception e) {
                System.err.println("Error in query");
                e.printStackTrace();

            } finally {
                if (pstmt != null) {
                    try {
                        pstmt.close();

                    } catch (Exception ex) {
                        System.out.println("Error in statement termination!");

                    }
                }
            }

        } catch (Exception ex) {
            System.err.println("Cannot connect to database server");
            ex.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    System.out.println("\n***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("\nDatabase connection terminated...");

                } catch (Exception ex) {
                    System.out.println("Error in connection termination!");

                }
            }

        }
        return images;
    }
    /**
     * Downloads public images from the Database. If no searchString and uploadDate are specified, all public images are returned.
     * @param searchString search-text to match against images filenames. (Optional. Use NULL if not used)
     * @param uploadDate upload date of the images to be selected. (Optional. Use NULL if not used)
     * @return Map&#60;Integer, Pair&#60;String, javafx.scene.image.Image&#62;&#62; -structure where Map-key contains imageID. Map-value is ja Pair containing filename String and actual image data as javafx.scene.image.Image
     */
    public Map<Integer, Pair<String, javafx.scene.image.Image>> downloadPublicImages(String searchString, LocalDate uploadDate) {
        System.out.println("Database.downloadPublicImages");
        Connection conn = null;
        ResultSet result = null;
        Map<Integer, Pair<String, javafx.scene.image.Image>> images = new HashMap<>();


        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassword);
            System.out.println("\nDatabase Connection Established...");

            PreparedStatement pstmt = null;
            try {
                if (searchString != null) {
                    if (uploadDate == null) {
                        pstmt = conn.prepareStatement("SELECT imageID, fileName, image FROM Fotos.imagedata WHERE viewingRights=? AND fileName LIKE ?");
                        pstmt.setString(1, "%" + searchString + "%");
                    } else {
                        pstmt = conn.prepareStatement("SELECT imageID, fileName, image FROM Fotos.imagedata WHERE viewingRights=1 AND creationDate=? AND fileName LIKE ?");
                        pstmt.setDate(1, Date.valueOf(uploadDate));
                        pstmt.setString(2, "%" + searchString + "%");
                    }
                } else {
                    if (uploadDate == null) {
                        pstmt = conn.prepareStatement("SELECT imageID, fileName, image FROM Fotos.imagedata WHERE viewingRights=1;");
                    } else {
                        pstmt = conn.prepareStatement("SELECT imageID, fileName, image FROM Fotos.imagedata WHERE viewingRights=1 AND creationDate=?;");
                        pstmt.setDate(1, Date.valueOf(uploadDate));
                    }
                }
                result = pstmt.executeQuery();

                while (result.next()) {
                    int id = result.getInt("imageID");
                    String filename = result.getString("filename");
                    javafx.scene.image.Image image = new javafx.scene.image.Image(result.getBinaryStream("image"));
                    images.put(id, new Pair<>(filename, image));
                }
            } catch (Exception e) {
                System.err.println("Error in query");
                e.printStackTrace();
            } finally {
                if (pstmt != null) {
                    try {
                        pstmt.close();

                    } catch (Exception ex) {
                        System.out.println("Error in statement termination!");

                    }
                }
            }

        } catch (Exception ex) {
            System.err.println("Cannot connect to database server");
            ex.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    System.out.println("\n***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("\nDatabase connection terminated...");

                } catch (Exception ex) {
                    System.out.println("Error in connection termination!");

                }
            }

        }
        return images;
    }

    /**
     * Downloads a full resolution image with specified image ID
     * @param imageID ID of the image to be downloaded
     * @return image date as javafx.scene.image.Image
     */
    public javafx.scene.image.Image downloadFullImage(int imageID) {
        System.out.println("Database.downloadFullImage");
        if (fullImageCache.containsKey(imageID)) {
            System.out.println("Full image found in cache. Showing that instead.");
            imageData = fullImageCache.get(imageID);
            return fullImageCache.get(imageID).image();
        }
        Connection conn = null;
        ResultSet result = null;
        javafx.scene.image.Image image = null;

        int userId = 1;
        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassword);
            System.out.println("\nDatabase Connection Established...");

            PreparedStatement pstmt = null;
            try {
                System.out.println("Try to retrieve a full image.");
                pstmt = conn.prepareStatement(
                        "SELECT * FROM Fotos.fullimagedata WHERE imageID = ?;"
                );
                pstmt.setInt(1, imageID);
                result = pstmt.executeQuery();

                if (result.next()) {
                    System.out.println("Got here!");
                    image = new javafx.scene.image.Image(result.getBinaryStream("image"));
                    ImageData imagedata = new ImageData(result.getFloat("fileSize"), result.getString("fileName"), result.getString("fileOwner"), result.getString("fileResolution"), result.getString("fileType"), result.getDate("creationDate"), image);
                    imageData = imagedata;
                    System.out.println("ImageData loaded!");
                    String name = result.getString("fileName");
                    System.out.println("FILENAME: "+name);
                    fullImageCache.put(imageID, imagedata);
                    System.out.println("Full Image retrieval successful.");
                }
            } catch (Exception e) {
                System.err.println("Error in query");
                e.printStackTrace();
            } finally {
                if (pstmt != null) {
                    try {
                        pstmt.close();
                    } catch (Exception ex) {
                        System.out.println("Error in statement termination!");
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Cannot connect to database server");
            ex.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    System.out.println("\n***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("\nDatabase connection terminated...");

                } catch (Exception ex) {
                    System.out.println("Error in connection termination!");
                }
            }
        }
        return image;
    }

    /**
     * Returns users folders.
     * @param userId ID of the user in question.
     * @param parentfolderid folder ID of the current folder. 0 if root.
     * @return HashMap&#60;Integer, String&#62; containing folder ID and folder name for each folder.
     */
    public HashMap <Integer, String> getUserFolders(int userId, int parentfolderid) {
        System.out.println("Database.getUserFolders");
        Connection conn = null;
        ResultSet result = null;
        HashMap <Integer, String> folders = new HashMap <Integer, String>();

        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassword);
            System.out.println("\nDatabase Connection Established...");

            PreparedStatement myStatement = null;

            try {
                //Jos parentfolderid parametriksi on asetettu 0, ladataan root-kansion sisällä olevat kansiot.
                //Muuten ladataan annetun kansion sisällä olevat kansiot
                if (parentfolderid == 0) {
                    myStatement = conn.prepareStatement(
                            "SELECT * FROM Fotos.Folder WHERE userID=? AND parentFolderID=?;"
                    );
                    myStatement.setInt(1, userId);
                    myStatement.setInt(2, getRootFolderId(userId));
                    result = myStatement.executeQuery();
                    //
                } else {
                    myStatement = conn.prepareStatement(
                            "SELECT * FROM Fotos.Folder WHERE userID=? AND parentFolderID=?;"
                    );
                    myStatement.setInt(1, userId);
                    myStatement.setInt(2, parentfolderid);
                    result = myStatement.executeQuery();
                }
                while (result.next()) {
                    String foldername = result.getString("name");
                    int folderid = result.getInt("folderID");
                    if (result.getInt("parentFolderID") != 0) {
                        folders.put(folderid, foldername);
                    }
                }

            } catch (Exception e) {
                System.err.println("Error in query");
                e.printStackTrace();
            } finally {
                if (myStatement != null) {
                    try {
                        myStatement.close();
                    } catch (Exception ex) {
                        System.out.println("Error in statement termination!");
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Cannot connect to database server");
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    System.out.println("\n***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("\nDatabase connection terminated...");

                } catch (Exception ex) {
                    System.out.println("Error in connection termination!");

                }
            }

        }
        //return folderlist;
        return folders;
    }

    /**
     * Uploads a new folder.
     * @param name name for the folder.
     * @param userId ID of the current user.
     * @param parentfolderid folder ID of the root folder of current user.
     */
    public void uploadNewFolder(String name, int userId, int parentfolderid) {
        System.out.println("Database.uploadNewFolder");
        Connection conn = null;

        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassword);
            System.out.println("\nDatabase Connection Established...");

            PreparedStatement myStatement = null;
            try {

                if (name.equals("root")) {
                    myStatement = conn.prepareStatement("INSERT INTO Fotos.Folder(NAME, EDITDATE, USERID) VALUES (?, CURDATE(), ?)");
                } else {
                    myStatement = conn.prepareStatement("INSERT INTO Fotos.Folder(NAME, EDITDATE, USERID, PARENTFOLDERID) VALUES (?, CURDATE(), ?, ?)");
                    //Jos parametri parenfolderid on 0, laitetaan uuden kansion parentfolderiksi käyttäjän root-kansio.
                    //Muuten laitetaan parentfolderiksi annettu parentfolderid.
                        if (parentfolderid == 0) {
                            myStatement.setInt(3, getRootFolderId(userId));
                        } else {
                            myStatement.setInt(3, parentfolderid);
                        }
                }
                myStatement.setString(1, name);
                myStatement.setInt(2, userId);
                myStatement.executeUpdate();
                System.out.println("Uusi kansio viety tietokantaan. " + name + " for userID " + userId);
            } catch (Exception e) {
                System.err.println("Error in query");
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println("Cannot connect to database server");
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    System.out.println("\n***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("\nDatabase connection terminated...");

                } catch (Exception ex) {
                    System.out.println("Error in connection termination!");

                }
            }
        }
    }

    /**
     * Deletes a folder. Also deletes all subfolders using deleteChildFolders-method.
     * @param folderid ID of the folder to be deleted.
     */
    public void deleteFolder(int folderid) {
        System.out.println("Database.deleteFolder");
        Connection conn = null;
        ResultSet result;

        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassword);
            System.out.println("\nDatabase Connection Established...");

            PreparedStatement myStatement1 = null;
            PreparedStatement myStatement2 = null;
            try {
                    //Poistetaan ensin kansion sisällä olevat kansiot ja tämän jälkeen pääkansio
                    deleteChildFolders(folderid);
                    myStatement2 = conn.prepareStatement("DELETE FROM Fotos.Image WHERE folderID=?");
                    myStatement2.setInt(1, folderid);
                    myStatement2.executeUpdate();
                    myStatement1 = conn.prepareStatement("DELETE FROM Fotos.Folder WHERE folderID=?");
                    myStatement1.setInt(1, folderid);
                    myStatement1.executeUpdate();
                    System.out.println("Kansio poistettu tietokannasta.");
            } catch (Exception e) {
                System.err.println("Error in query");
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println("Cannot connect to database server");
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    System.out.println("\n***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("\nDatabase connection terminated...");

                } catch (Exception ex) {
                    System.out.println("Error in connection termination!");

                }
            }
        }
    }

    /**
     * Returns the folder ID for users root folder.
     * @param userId ID of the user in question.
     * @return folder ID of the users root folder.
     */
    public int getRootFolderId(int userId) {
        System.out.println("Database.getParentFolderId");
        Connection conn = null;
        ResultSet result = null;
        int parentfolderid = 0;

        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassword);
            System.out.println("\nDatabase Connection Established...");

            PreparedStatement myStatement = null;

            try {
                myStatement = conn.prepareStatement(
                        "SELECT folderID FROM Fotos.Folder WHERE userID=? AND parentFolderID IS NULL;"
                );
                myStatement.setInt(1, userId);
                result = myStatement.executeQuery();
                while (result.next()) {
                    parentfolderid = result.getInt("folderID");
                }

            } catch (Exception e) {
                System.err.println("Error in query");
                e.printStackTrace();
            } finally {
                if (myStatement != null) {
                    try {
                        myStatement.close();
                    } catch (Exception ex) {
                        System.out.println("Error in statement termination!");
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Cannot connect to database server");
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    System.out.println("\n***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("\nDatabase connection terminated...");

                } catch (Exception ex) {
                    System.out.println("Error in connection termination!");

                }
            }

        }
        return parentfolderid;
    }

    /**
     * Changes the userlevel for specified user. Used for changing a free user to premium user of vice versa or setting admin status to user.
     * @param userId ID of the user.
     * @param newUserLevel new user level. 1 being free user, 2 being premium user and 1000 being admin.
     */
    public void changeUserLevel(int userId, int newUserLevel){
        Connection conn = null;
        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassword);
            System.out.println("Database Connection Established...");

            PreparedStatement pstmt = conn.prepareStatement("UPDATE Fotos.User SET userLevel = ? WHERE userID = ?;");
            pstmt.setInt(1, newUserLevel);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
            System.out.println("Userlevel changed");
        } catch (Exception ex) {
            System.err.println("Cannot connect to database server");
            ex.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    System.out.println("***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("Database connection terminated...");
                } catch (Exception ex) {
                    System.out.println("Error in connection termination!");
                }
            }
        }
    }

    /**
     * Deletes all subfolders of the specified folder.
     * @param parentfolderid ID of the folder for which all subfolders will be deleted.
     */
    public void deleteChildFolders(int parentfolderid) {
        System.out.println("Database.deleteParentFolders");
        Connection conn = null;
        ResultSet result = null;

        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassword);
            System.out.println("\nDatabase Connection Established...");
            PreparedStatement myStatement = null;
            try {
                //Etsitään sisällä olevat kansiot ja poistetaan ne.
                myStatement = conn.prepareStatement("SELECT * FROM Fotos.Folder WHERE parentFolderID=?");
                myStatement.setInt(1, parentfolderid);
                result = myStatement.executeQuery();
                while (result.next()) {
                    deleteFolder(result.getInt("folderID"));
                }

            } catch (Exception e) {
                System.err.println("Error in query");
                e.printStackTrace();
            } finally {
                if (myStatement != null) {
                    try {
                        myStatement.close();
                    } catch (Exception ex) {
                        System.out.println("Error in statement termination!");
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Cannot connect to database server");
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    System.out.println("\n***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("\nDatabase connection terminated...");

                } catch (Exception ex) {
                    System.out.println("Error in connection termination!");

                }
            }

        }
    }

    /**
     * Returns an list of users and their user information. Used by adminview.
     * @return ArrayList&#60;FotosUser&#62; containing all users in database.
     */
    public ArrayList<FotosUser> listUsers(){
        Connection conn = null;
        ArrayList<FotosUser> list = new ArrayList<>();
        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassword);
            System.out.println("Database Connection Established...");

            PreparedStatement pstmt = conn.prepareStatement("SELECT userName,userID,userLevel,surName,frontName,email FROM Fotos.User;");
            ResultSet result = pstmt.executeQuery();

            while (result.next()){
                FotosUser user = new FotosUser();
                user.setUserID(result.getInt("userID"));
                user.setUserLevel(result.getInt("userLevel"));
                user.setUserName(result.getString("userName"));
                user.setFirstName(result.getString("frontName"));
                user.setLastName(result.getString("surName"));
                user.setEmail(result.getString("email"));
                list.add(user);
            }

        } catch (Exception ex) {
            System.err.println("Cannot connect to database server");
            ex.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    System.out.println("***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("Database connection terminated...");
                } catch (Exception ex) {
                    System.out.println("Error in connection termination!");
                }
            }
        }
        return list;
    }

    /**
     * Returns the number of admins in database.
     * @return number of admin users in database.
     */
    public int countAdmins(){
        Connection conn = null;
        int count = 0;
        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassword);
            PreparedStatement statement = conn.prepareStatement("SELECT COUNT(*) FROM Fotos.User WHERE userLevel=1000");
            ResultSet res = statement.executeQuery();
            res.next();
            count = res.getInt(1);

        } catch (Exception ex) {
            System.err.println("Cannot connect to database server");
            ex.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    System.out.println("***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("Database connection terminated...");
                } catch (Exception ex) {
                    System.out.println("Error in connection termination!");
                }
            }
        }
        return count;
    }



}