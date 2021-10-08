package otp1.otpr21fotosdemo;

import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.List;
import java.util.*;

public class Database {
    private String dbUserName = "otpdb";
    private String dbPassword = "Asdfghjkl1234567890";
    private String url = "jdbc:mysql://10.114.32.13:3306/";
    private final int MAX_THUMB_HEIGHT = 200;
    private final int MAX_THUMB_WIDTH = 200;
    private HashMap<Integer, javafx.scene.image.Image> fullImageCache = new HashMap<>();
    private int privateUserId;
    private FotosController controller;

    public Database() {

    }

    public void setController(FotosController c) {
        controller = c;
    }

    public void setPrivateUserId(int i) {
        privateUserId = i;
    }

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

    // For registering
    public void register(String userName, String passWord, String email1, String email2, Text loginErrorText) {
        if (userAndPwExists(userName, passWord) == 0) {
            // Variables
            Connection conn = null;
            String dbUserName = "otpdb";
            String dbPassWord = "Asdfghjkl1234567890";
            String url = "jdbc:mysql://10.114.32.13:3306/Fotos";

            try {
                // Connection statement
                conn = DriverManager.getConnection(url, dbUserName, dbPassWord);
                System.out.println("\nDatabase Connection Established...");

                // USER statement VALUES(userID (int11), frontName (varchar32), surName(varchar32), userLevel(int11)
                // email(varchar32), passWord(varchar64), dbUserName(varchar32)
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO User(frontName, surName, userLevel, email, passWord, userName) VALUES(?,?,?,?,?,?)");
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
        } else {
            System.out.println("BBBBBBBBBBBBB");
            loginErrorText.setText("AAAAA");
        }
    }

    public static void main(String[] args) {
        System.out.println("\n\n***** MySQL JDBC Connection Testing *****");
    }

    public Integer userAndPwExists(String user, String passWord) {
        passWord = saltLogin(passWord);

        int found = 0;
        Connection conn = null;
        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassword);
            System.out.println("\nDatabase Connection Established...");
            PreparedStatement pstmt = null;
            PreparedStatement pstmt2 = null;

            try {
                pstmt = conn.prepareStatement("SELECT userName,userID FROM Fotos.User WHERE userName=?;");
                pstmt.setString(1, user);
                ResultSet result = pstmt.executeQuery();

                pstmt2 = conn.prepareStatement("SELECT COUNT(*) FROM Fotos.User WHERE passWord=?;");
                pstmt2.setString(1, passWord);
                ResultSet result2 = pstmt2.executeQuery();
                result2.next();

                //Tarkistetaan löytyikö yhtään kyseistä usernamea.
                if (!result.next()) {
                    System.err.println("No such username found");
                } else if (Objects.equals(result.getString("userName"), user) && result2.getInt(1) > 0) {
                    found = result.getInt("userID");
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
        controller.fetchUserID(found);
        privateUserId = found;
        return found;
    }

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

    public boolean imageExists(int imageID){
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

    public boolean deleteImage(int imageID){
        boolean deleted = false;
        //boolean deletedFullRes = false;
        Connection conn = null;
        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassword);
            System.out.println("Database Connection Established...");
            PreparedStatement pstmt = null;
            //PreparedStatement pstmt2 = null;
            try {
                pstmt = conn.prepareStatement("DELETE FROM Fotos.Image WHERE  imageID=?;");
                pstmt.setInt(1, imageID);
                pstmt.execute();
                System.out.println("Poistettiin " + pstmt.getUpdateCount() + " riviä taulusta Fotos.Image");
                if (pstmt.getUpdateCount() > 0) {
                    deleted = true;
                }

                /*
                Full_Image taulusta ei tarvitse poistaa erikseen, koska
                tietokanta poistaa tuon entryn, kun se poistetaan Image-taulusta...

                pstmt2 = conn.prepareStatement("DELETE FROM Fotos.Full_Image WHERE  imageID=?;");
                pstmt2.setInt(1, imageID);
                pstmt2.execute();
                System.out.println("Poistettiin " + pstmt2.getUpdateCount() + " riviä taulusta Fotos.Full_Image");
                if (pstmt2.getUpdateCount() > 0) {
                    deletedFullRes = true;
                }
                */

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
                /*
                if (pstmt2 != null) {
                    try {
                        pstmt2.close();

                    } catch (Exception ex) {
                        System.out.println("Error in statement 2 termination!");

                    }
                }*/

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

    //TODO Joku "progress bar" -tyyppinen näkymä mistä näkee miten kuvien uploadaus edistyy
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

                //Rajataan tiedostonimeä jos se on pidempi kuin tietokannan raja
                String filename = originalFile.getName();
                if (filename.length() > 64) {
                    System.out.println("Filename length1: " + filename.length());
                    System.out.println("Filename: " + filename);

                    StringBuilder builder = new StringBuilder();
                    String end = filename.substring(filename.lastIndexOf("."));
                    builder.append(filename.substring(0, (63 - end.length() - 3)));
                    builder.append("---" + end);
                    filename = builder.toString();

                    System.out.println("Filename length2: " + filename.length());
                    System.out.println("Filename: " + filename);
                }

                //Muodostetaan thumbnail InputStream kuvalle
                System.out.println("Thumbthumb... Thumbnailing");
                BufferedImage originalBufferedImage = ImageIO.read(originalFile);
                int origWidth = originalBufferedImage.getWidth();
                int origHeight = originalBufferedImage.getHeight();
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

                try {
                    //Uploadataan thumbnail
                    pstmt = conn.prepareStatement(
                            "INSERT INTO Fotos.Image(viewingRights, fileName, image, date, userID, folderID) values (?, ?, ?, ?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS
                    );

                    pstmt.setInt(1, 0);
                    pstmt.setString(2, filename);
                    pstmt.setBinaryStream(3, is);

                    long dateLong = new java.util.Date().getTime();
                    pstmt.setDate(4, new java.sql.Date(dateLong));

                    pstmt.setInt(5, userId);
                    pstmt.setInt(6, folderId);
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

    //Palauttaa Hashmapin jossa key on imageID ja Value on PAIR-rakenne. Pair-rakenteessa taas key on tiedostonimi ja value on imagedata
    public Map<Integer, Pair<String, javafx.scene.image.Image>> downloadImages(int folderId) {
        Connection conn = null;
        ResultSet result = null;
        Map<Integer, Pair<String, javafx.scene.image.Image>> images = new HashMap<>();


        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassword);
            System.out.println("\nDatabase Connection Established...");

            PreparedStatement pstmt = null;
            try {


                pstmt = conn.prepareStatement(
                        "SELECT imageID, fileName, image FROM Fotos.Image WHERE userID=? AND folderID=?;"
                );

                pstmt.setInt(1, privateUserId);
                pstmt.setInt(2, folderId);
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

    public javafx.scene.image.Image downloadFullImage(int imageID) {
        if (fullImageCache.containsKey(imageID)) {
            System.out.println("Full image found in cache. Showing that instead.");
            return fullImageCache.get(imageID);
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


                pstmt = conn.prepareStatement(
                        "SELECT image FROM Fotos.Full_Image WHERE imageID=?;"
                );
                pstmt.setInt(1, imageID);
                result = pstmt.executeQuery();

                if (result.next()) {
                    image = new javafx.scene.image.Image(result.getBinaryStream("image"));
                    fullImageCache.put(imageID, image);
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

    public ArrayList<String> getUserFolders(int userId) {
        Connection conn = null;
        ResultSet result = null;
        ArrayList<String> folderlist = new ArrayList<String>();

        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassword);
            System.out.println("\nDatabase Connection Established...");

            PreparedStatement myStatement = null;

            try {
                myStatement = conn.prepareStatement(
                        "SELECT name FROM Fotos.Folder WHERE userID=?;"
                );
                myStatement.setInt(1, userId);
                result = myStatement.executeQuery();
                while (result.next()) {
                    String foldername = result.getString("name");
                    folderlist.add(foldername);
                }

            } catch (Exception e) {
                System.err.println("Error in query");
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
        return folderlist;
    }
}