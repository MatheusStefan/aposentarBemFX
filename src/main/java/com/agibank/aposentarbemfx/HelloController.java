package com.agibank.aposentarbemfx;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class HelloController {

    @FXML
    private Label welcomeText;

    @FXML
    private ImageView imageView;

    @FXML
    public void initialize() {
        // Load the image
        Image image = new Image("file:assets/aposentarBem.JPG");
        imageView.setImage(image);

    }

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Hello, World!");
    }
}
