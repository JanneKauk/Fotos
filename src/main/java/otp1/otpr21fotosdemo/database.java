package otp1.otpr21fotosdemo;

import java.sql.*;

public class database {
    public static void main(String[] args) {
        System.out.println("\n\n***** MySQL JDBC Connection Testing *****");
        Connection conn = null;
        try {
            String userName = "otpdb";
            String password = "Asdfghjkl1234567890";
            String url = "jdbc:mysql://10.114.32.13:3306/";
            conn = DriverManager.getConnection(url, userName, password);
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
}