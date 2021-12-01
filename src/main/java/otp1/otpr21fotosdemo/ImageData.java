package otp1.otpr21fotosdemo;

import javafx.scene.image.Image;

import java.sql.Date;

public class ImageData {
    private float fileSize;
    private String fileName, fileOwner, fileResolution, fileType;
    private Date creationDate;
    private javafx.scene.image.Image image;
    public ImageData(float fileSize, String fileName, String fileOwner, String fileResolution, String fileType, Date creationDate, javafx.scene.image.Image image) {
        this.fileSize = fileSize;
        this.fileName = fileName;
        this.fileOwner = fileOwner;
        this.fileResolution = fileResolution;
        this.fileType = fileType;
        this.creationDate = creationDate;
        this.image = image;
    }

    public float fileSize() {
        return fileSize;
    }

    public String fileName() {
        return fileName;
    }

    public String fileOwner() {
        return fileOwner;
    }

    public String fileResolution() {
        return fileResolution;
    }

    public String fileType() {
        return fileType;
    }

    public Date creationDate() {
        return creationDate;
    }

    public Image image() {
        return image;
    }
}
