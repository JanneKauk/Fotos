package otp1.otpr21fotosdemo;

import javafx.scene.control.Label;

import javax.imageio.ImageIO;
import javax.xml.transform.Result;
import java.awt.*;
import java.io.*;

import javafx.scene.text.Text;
import org.apache.commons.codec.binary.Hex;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import java.sql.*;
import java.util.Iterator;
import java.util.List;
import java.awt.image.*;

public class Database {
    private String dbUserName = "otpdb";
    private String dbPassword = "Asdfghjkl1234567890";
    private String url = "jdbc:mysql://10.114.32.13:3306/";
    private final int MAX_THUMB_HEIGHT = 200;
    private final int MAX_THUMB_WIDTH = 200;


    public Database (){

    }

    public static void saltRegister(String userName, String passWord, String email1, String email2, Text loginErrorText) {
        String salt = "1234";
        int iterations = 10000;
        int keyLength = 512;
        char[] passwordChars = passWord.toCharArray();
        byte[] saltBytes = salt.getBytes();

        byte[] hashedBytes = hashPassword(passwordChars, saltBytes, iterations, keyLength);
        String hashedString = Hex.encodeHexString(hashedBytes);

        System.out.println(hashedString);
        new Database(userName, hashedString, email1, email2, loginErrorText);
    }

    public static String saltLogin(String passWord) {
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

    public static byte[] hashPassword( final char[] password, final byte[] salt, final int iterations, final int keyLength ) {
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance( "PBKDF2WithHmacSHA512" );
            PBEKeySpec spec = new PBEKeySpec( password, salt, iterations, keyLength );
            SecretKey key = skf.generateSecret( spec );
            byte[] res = key.getEncoded( );
            return res;
        } catch ( NoSuchAlgorithmException | InvalidKeySpecException e ) {
            throw new RuntimeException( e );
        }
    }

    // For registering
    public Database(String userName, String passWord, String email1, String email2, Text loginErrorText) {
        if (!userAndPwExists(userName, passWord)) {
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

    public static boolean userAndPwExists(String user, String passWord) {
        String dbUserName = "otpdb";
        String dbPassword = "Asdfghjkl1234567890";
        String url = "jdbc:mysql://10.114.32.13:3306/";

        passWord = saltLogin(passWord);

        boolean found = false;
        Connection conn = null;
        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassword);
            System.out.println("\nDatabase Connection Established...");
            PreparedStatement pstmt = null;
            PreparedStatement pstmt2 = null;

            try {
                pstmt = conn.prepareStatement("SELECT COUNT(*) FROM Fotos.User WHERE userName=?;");
                pstmt.setString(1, user);
                ResultSet result = pstmt.executeQuery();
                result.next();

                pstmt2 = conn.prepareStatement("SELECT COUNT(*) FROM Fotos.User WHERE passWord=?;");
                pstmt2.setString(1, passWord);
                ResultSet result2 = pstmt2.executeQuery();
                result2.next();

                //Tarkistetaan löytyikö yhtään kyseistä usernamea. (Ei pitäisi olla koskaan enempää kuin yksi)
                if (result.getInt(1) > 0 && result2.getInt(1) > 0) {
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

    public static boolean userExists(String user) {
        String dbUserName = "otpdb";
        String dbPassword = "Asdfghjkl1234567890";
        String url = "jdbc:mysql://10.114.32.13:3306/";

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
    //TODO Koko metodin suoritus omaan threadiin ettei freesaa muuta äppiä.
    //TODO Joku "progress bar" -tyyppinen näkymä mistä näkee miten kuvien uploadaus edistyy
    public void uploadImages(int userId, int folderId, List<File> files){

        Connection conn = null;
        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassword);
            System.out.println("\nDatabase Connection Established...");

            Iterator<File> it = files.iterator();
            //Jokainen tiedosto lähetetään erikseen
            while(it.hasNext()) {
                File originalFile = it.next();

                //Rajataan tiedostonimeä jos se on pidempi kuin tietokannan raja
                String filename = originalFile.getName();
                if (filename.length() > 64){
                    System.out.println("Filename length1: " + filename.length());
                    System.out.println("Filename: " + filename);

                    StringBuilder builder = new StringBuilder();
                    String end = filename.substring(filename.lastIndexOf("."));
                    builder.append(filename.substring(0,(63 - end.length() - 3)));
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
                if (origHeight > origWidth){
                    thumbHeight = MAX_THUMB_HEIGHT;
                    thumbWidth = Math.round((float)thumbHeight/origHeight * origWidth);
                } else {
                    thumbWidth = MAX_THUMB_WIDTH;
                    thumbHeight = Math.round((float)thumbWidth/origWidth * origHeight);
                }

                BufferedImage thumb = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = thumb.createGraphics();
                g.drawImage(originalBufferedImage.getScaledInstance(thumbWidth, thumbHeight, Image.SCALE_SMOOTH),0,0,null);
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

                    pstmt.setInt(1, 0 );
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
                    System.out.println("Sent " + thumbSize + " bytes. New imageID: " + key.getInt(1)+ " Filename: " + filename);

                    //Uploadataan täyden reson kuva
                    FileInputStream fis2 = new FileInputStream(originalFile);
                    pstmt2 = conn.prepareStatement(
                            "INSERT INTO Fotos.Full_Image(imageID, image) values (?, ?)"
                    );
                    //Tähä laitetaa edellisessä insertissä saatu autogeneroitu key
                    pstmt2.setInt(1, key.getInt(1) );
                    pstmt2.setBinaryStream(2, fis2, (int) originalFile.length());
                    System.out.println("Executing statement 2...");
                    pstmt2.execute();
                    System.out.println("Sent " + originalFile.length() + " bytes.");



                } catch (Exception e) {
                    System.err.println("Error in query");
                    e.printStackTrace();

                }  finally {
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
    }

    //Work in progress....
    public List<File> downloadImages(){
        Connection conn = null;
        List<File> files = null;
        int userId = 1;
        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassword);
            System.out.println("\nDatabase Connection Established...");

            PreparedStatement pstmt = null;
            try {


                pstmt = conn.prepareStatement(
                        "SELECT imageID, fileName, image FROM Image WHERE userID=?;"
                );

                pstmt.setInt(1, userId );
                ResultSet result = pstmt.executeQuery();
              //  (Array)result.getArray(3)

            } catch (Exception e) {
                System.err.println("Error in query");
                e.printStackTrace();

            }  finally {
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
        return files;
    }


}