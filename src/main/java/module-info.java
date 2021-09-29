module otp1.otpr21fotosdemo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires org.apache.commons.codec;


    opens otp1.otpr21fotosdemo to javafx.fxml;
    exports otp1.otpr21fotosdemo;
}