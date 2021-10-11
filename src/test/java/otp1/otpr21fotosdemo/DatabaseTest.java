package otp1.otpr21fotosdemo;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


// TODO: Connect testing methods with values for correct testing
// As of now, test methods generate their own data

// Without TestInstance resetDbChanges() will not work, or rather this whole class
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DatabaseTest {
    // Variables
    String dbUserName = "otpdb";
    String dbPassWord = "Asdfghjkl1234567890";
    String url = "jdbc:mysql://10.114.32.13:3306/Fotos";
    private Database database = new Database();

    @Test
    @AfterAll
    public void resetDbChanges() {
        System.out.println("Removing test data from DB");
        Connection conn = null;

        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassWord);
            System.out.println("\nDatabase Connection Established...");

            // Deleting entry from Image table
            PreparedStatement pstmtImage = conn.prepareStatement("DELETE FROM Image WHERE userID = 99999;");
            pstmtImage.execute();

            // Deleting entry from Image table
            PreparedStatement pstmtFolder = conn.prepareStatement("DELETE FROM Folder WHERE userID = 99999;");
            pstmtFolder.execute();

            // Deleting entry from Image table
            PreparedStatement pstmtUser = conn.prepareStatement("DELETE FROM User WHERE userID = 99999;");
            pstmtUser.execute();


            System.out.println("Records deleted......");

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

    @Test
    @Order(1)
    public void dbConnectionTest() {
        System.out.println("\n\n***** MySQL JDBC Connection Testing *****");
        Connection conn = null;

        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassWord);
            System.out.println("\nDatabase Connection Established...");

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

    @Test
    @Order(2)
    public void dbUserTest() {
        // Variables
        Connection conn = null;

        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassWord);
            System.out.println("\nDatabase Connection Established...");

            // USER statement VALUES(userID (int11), frontName (varchar32), surName(varchar32), userLevel(int11)
            // email(varchar32), passWord(varchar64), dbUserName(varchar32)
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO User VALUES(?,?,?,?,?,?,?)");
            // userID
            pstmt.setInt(1, 99999);
            // frontName
            pstmt.setString(2, "Frontnametest");
            // surName
            pstmt.setString(3, "Surnametest");
            // userLevel
            pstmt.setInt(4, 1);
            // email
            pstmt.setString(5, "test@test.com");
            // passWord
            pstmt.setString(6, "1234");
            // dbUserName
            pstmt.setString(7, "test");
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
    }

    @Test
    @Order(3)
    public void dbImageTest() {
        // Variables
        Connection conn = null;

        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassWord);
            System.out.println("\nDatabase Connection Established...");

            // Image statement VALUES(imageID (int11), viewingRights (int11), fileName(varchar64), image(blob), date(date)
            // userID(int11), folderID(int11)
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Image VALUES(?,?,?,?,?,?,?)");
            // imageID
            pstmt.setInt(1, 99999);
            // viewingRights
            pstmt.setInt(2, 0);
            // fileNmae
            pstmt.setString(3, "testing1");
            // blob
            InputStream in = new FileInputStream("src/test/resources/image/noimage.jpg");
            pstmt.setBlob(4, in);
            // date
            Date date = Calendar.getInstance().getTime();
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());
            pstmt.setDate(5, sqlDate);
            // userID
            pstmt.setInt(6, 99999);
            // folderID
            pstmt.setInt(7, 99999);
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
    }

    @Test
    @Order(4)
    public void dbFolderTest() {
        // Variables
        Connection conn = null;

        try {
            // Connection statement
            conn = DriverManager.getConnection(url, dbUserName, dbPassWord);
            System.out.println("\nDatabase Connection Established...");

            // Folder statement VALUES(name(varchar32), folderID(int11), editDate(Date), userID(int11)
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Folder VALUES(?,?,?,?)");
            // name
            pstmt.setString(1, "root");
            // folderID
            pstmt.setInt(2, 99999);
            // editDate
            Date date = Calendar.getInstance().getTime();
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());
            pstmt.setDate(3, sqlDate);
            // userID
            pstmt.setInt(4, 99999);
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
    }

    @Test
    @Order(5)
    public void uploadNewFolderTest() {
        database.uploadNewFolder("test1", 99999);
        database.uploadNewFolder("test2", 99999);
        database.uploadNewFolder("test3", 99999);
    }

    @DisplayName("Testataan onko käyttäjän kansioita oikea määrä.")
    @Test
    @Order(6)
    public void folderSizeTest() {
        HashMap<Integer, String> test = database.getUserFolders(99999);
        assertEquals(3, test.size());
    }


    @DisplayName("Testataan userExists metodi useammalla käyttäjätunnuksella.")
    @ParameterizedTest (name="Testataan loytyyko username {0}")
    @CsvSource({"ppouta, false", "1test, true", "noexist, false", "8u34958u342985u89t3hf89ht298t48h, false","-1,false", "NULL, false" })
    @Order(7)
    public void userExistsTest(String userName, boolean result){
        Database base = new Database();
        assertEquals(result, base.userExists(userName), "UserExiststest failed with username " + userName);
    }

    @Test
    @DisplayName("Testataan yhden kuvan uploadaus, etsiminen(imageExists) ja poisto.")
    @Order(8)
    public void uploadImagesTest(){
        Database base = new Database();
        File file = new File("src/main/resources/otp1/otpr21fotosdemo/image/addition-icon.png");
        List<File> fileList = new ArrayList<File>();
        fileList.add(file);

        assertAll(() -> {
            List<Integer> imageIDt = base.uploadImages(1,1,fileList);
            if(imageIDt.size() > 0){
                //Testataan löytyykö lisätty kuva
                assertTrue( base.imageExists(imageIDt.get(0)), "Lisättyä kuvaa ei löytynyt!");
                //Poistetaan lisätty kuva
                assertTrue(base.deleteImage(imageIDt.get(0)), "Kuvan poisto ei onnistunut!");
            }
        });
    }
}
