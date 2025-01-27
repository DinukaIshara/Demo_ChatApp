package lk.ijse.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.io.*;
import java.net.Socket;

public class ClientController {
    @FXML
    private TextField txtMessage;

    @FXML
    private TextArea txtArea;

    @FXML
    private Button btnSend;

    @FXML
    private Button btnImage;  // Add this button to your FXML

    @FXML
    private VBox imageContainer;  // Add this VBox to your FXML

    Socket socket;
    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;
    String message = "";

    public void initialize() {
            // Start message receiving thread
            new Thread(() -> {
                try {
                socket = new Socket("localhost", 4000);
                txtArea.appendText("Client Connected\n");

                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());


                while (!message.equals("Exit")) {
                    try {
                        message = dataInputStream.readUTF();

                        // Check if the message is an image
                        if (message.startsWith("[IMAGE]")) {
                            // Handle received image
                            String imagePath = message.substring(7); // Remove [IMAGE] prefix
                            Platform.runLater(() -> {
                                displayImage(imagePath);
                                txtArea.appendText("\nServer: [Image Received]\n");
                            });
                        } else {
                            // Handle regular text message
                            Platform.runLater(() -> txtArea.appendText("\nServer: " + message));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();


    }

    @FXML
    void btnSendOnAction(ActionEvent event) {
        try {
            String message = txtMessage.getText();
            dataOutputStream.writeUTF(message);
            dataOutputStream.flush();
            txtArea.appendText("\nClient: " + message);
            txtMessage.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayImage(String imagePath) {
        try {
            File file = new File(imagePath);
            Image image = new Image(file.toURI().toString());
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(150);
            imageView.setFitHeight(150);
            imageView.setPreserveRatio(true);

            imageContainer.getChildren().add(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void btnImageOnAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                // Create image for display
                Image image = new Image(selectedFile.toURI().toString());
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(150);
                imageView.setFitHeight(150);
                imageView.setPreserveRatio(true);

                // Add image to the container
                imageContainer.getChildren().add(imageView);

                // Send image path to server
                dataOutputStream.writeUTF("[IMAGE]" + selectedFile.getPath());
                dataOutputStream.flush();
                txtArea.appendText("\nClient: [Image Sent]\n");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void txtMessageOnAction(ActionEvent event) {
        btnSendOnAction(event);
    }
}