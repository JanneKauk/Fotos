package otp1.otpr21fotosdemo;

import java.sql.*;

public class database {
    public static void main(String[] args) {
        String url = "jdbc:mysql://10.114.32.13:22/";
        String db = "testi";
        String dbuser = "otpdb";
        String dbpass = "Asdfghjkl1234567890";
        Connection connection;

        {
            try {
                connection = DriverManager.getConnection(url + db, dbuser, dbpass);
                Statement statement = connection.createStatement();
                String sql = "INSERT INTO Testi1" +
                        "VALUES ('gasgadbbcxbrw')";
                int update = statement.executeUpdate(sql);
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("SQL statement is not executed!");
            }
        }
    }
}
