package otp1.otpr21fotosdemo;

import java.sql.*;

public class Database {
    // Variables
    private String userName = "otpdb";
    private String password = "Asdfghjkl1234567890";
    private String url = "jdbc:mysql://10.114.32.13:3306/";

    public Database (){

    }

    public static void main(String[] args) {
        System.out.println("\n\n***** MySQL JDBC Connection Testing *****");
        Database base = new Database();
        String user = "ttesti";
        System.out.println("User " + user + " exists: " + base.userExists(user));
        user = "mr_bean";
        System.out.println("User " + user + " exists: " + base.userExists(user));
    }

    public boolean userExists(String user){
        boolean found = false;
        Connection conn = null;
        try {
            // Connection statement
            conn = DriverManager.getConnection(url, userName, password);
            System.out.println("\nDatabase Connection Established...");

            PreparedStatement pstmt = null;
            try{
                pstmt = conn.prepareStatement("SELECT COUNT(*) FROM Fotos.User WHERE userName=?;");

                pstmt.setString(1, user);
                ResultSet result = pstmt.executeQuery();
                result.next();

                //Tarkistetaan löytyikö yhtään kyseistä usernamea. (Ei pitäisi olla koskaan enempää kuin yksi)
                if (result.getInt(1) > 0){
                    found = true;
                    System.out.println("Found " + result.getInt(1) + " " + user);
                }
            } catch (Exception e) {
                System.err.println("Error in query");
                e.printStackTrace();
            }  finally {
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