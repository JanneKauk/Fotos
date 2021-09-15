module otp.otpr21fotosdemo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens otp1.otpr21fotosdemo to javafx.fxml;
    exports otp1.otpr21fotosdemo;
}