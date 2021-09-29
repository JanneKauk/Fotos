package otp1.otpr21fotosdemo;

import javafx.scene.control.Label;

import org.apache.commons.codec.binary.Hex;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import java.sql.*;

public class Database {
    public Database() {

    }

    public static void saltRegister(String userName, String passWord, String email1, String email2, Label loginErrorLabel) {
        String salt = "1234";
        int iterations = 10000;
        int keyLength = 512;
        char[] passwordChars = passWord.toCharArray();
        byte[] saltBytes = salt.getBytes();

        byte[] hashedBytes = hashPassword(passwordChars, saltBytes, iterations, keyLength);
        String hashedString = Hex.encodeHexString(hashedBytes);

        System.out.println(hashedString);
        new Database(userName, hashedString, email1, email2, loginErrorLabel);
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
    public Database(String userName, String passWord, String email1, String email2, Label loginErrorLabel) {
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
            loginErrorLabel.setText("AAAAA");
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
}