package anningtex.controller;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamUtils;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ResourceBundle;

/**
 * desc：拍摄(第二种)
 */
public class CameraController implements Initializable {
    @FXML
    BorderPane webCamPane;
    @FXML
    Button bottomCameraControlPane;

    private static ImageView imgWebCamCapturedImage;
    private Webcam webCam = null;
    private boolean stopCamera = false;
    private BufferedImage grabbedImage;
    private final ObjectProperty<Image> imageProperty = new SimpleObjectProperty<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        imgWebCamCapturedImage = new ImageView();
        webCamPane.setCenter(imgWebCamCapturedImage);
        Platform.runLater(this::setImageViewSize);
        initializeWebCam(0);
    }

    private void initializeWebCam(final int webCamIndex) {
        Task<Void> webCamTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (webCam != null) {
                    disposeWebCamCamera();
                }
                webCam = Webcam.getWebcams().get(webCamIndex);
                webCam.open();
//                Dimension[] nonStandardResolutions = new Dimension[]{
//                        WebcamResolution.UXGA.getSize(),
//                };
//                webCam.setCustomViewSizes(nonStandardResolutions);
//                webCam.setViewSize(WebcamResolution.UXGA.getSize());
                startWebCamStream();
                return null;
            }
        };
        Thread webCamThread = new Thread(webCamTask);
        webCamThread.setDaemon(true);
        webCamThread.start();
        bottomCameraControlPane.setDisable(false);
    }

    private void startWebCamStream() {
        stopCamera = false;
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                while (!stopCamera) {
                    try {
                        if ((grabbedImage = webCam.getImage()) != null) {
                            Platform.runLater(() -> {
                                Image mainImage = SwingFXUtils.toFXImage(grabbedImage, null);
                                imageProperty.set(mainImage);
                            });
                            grabbedImage.flush();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        };
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
        imgWebCamCapturedImage.imageProperty().bind(imageProperty);
    }

    private void disposeWebCamCamera() {
        stopCamera = true;
        webCam.close();
        // Webcam.shutdown();
    }

    private void setImageViewSize() {
        double height = webCamPane.getHeight();
        double width = webCamPane.getWidth();
        imgWebCamCapturedImage.setFitHeight(height);
        imgWebCamCapturedImage.setFitWidth(width);
        imgWebCamCapturedImage.prefHeight(height);
        imgWebCamCapturedImage.prefWidth(width);
        imgWebCamCapturedImage.setPreserveRatio(true);
    }

    @FXML
    protected void takePicture() {
        byte[] bytes = WebcamUtils.getImageBytes(webCam, "jpg");
        System.out.println("Bytes length: " + bytes.length);
        ByteBuffer buffer = WebcamUtils.getImageByteBuffer(webCam, "jpg");
        System.out.println("Buffer length: " + buffer.capacity());
        try {
            byteToFile(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void byteToFile(byte[] bytes) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        BufferedImage bufferedImage = ImageIO.read(bais);
        long lTime = System.currentTimeMillis();
        String filePath = "D:\\" + lTime + ".jpg";
        try {
//            File file = new File("D:\\" + l + 1 + ".jpg");
//            ImageIO.write(bufferedImage, "jpg", file);
            Thumbnails.of(bufferedImage)
                    .size(1200, 900)
                    .toFile(filePath);
            System.out.println("filePath: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            bais.close();
        }
    }
}