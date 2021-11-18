package anningtex.controller;

import anningtex.manger.Constants;
import anningtex.utils.FileUtil;
import com.github.sarxos.webcam.*;
import com.github.sarxos.webcam.util.jh.JHFlipFilter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * desc：拍摄(第一种)
 */
public class MainController {
    private WebcamPicker webcamPicker = null;
    private Webcam webcam = null;
    private WebcamPanel panel = null;

    @FXML
    public void mainOneOnclick(ActionEvent actionEvent) {
        openWebCam();
    }

    private void openWebCam() {
        JFrame window = new JFrame("webcam");
        webcamPicker = new WebcamPicker();
        // 自定义分辨率
        Dimension[] nonStandardResolutions = new Dimension[]{
                WebcamResolution.UXGA.getSize(),
                new Dimension(900, 1200),
        };
        Webcam.addDiscoveryListener(new WebcamDiscoveryListener() {
            @Override
            public void webcamFound(WebcamDiscoveryEvent event) {
                if (webcamPicker != null) {
                    webcamPicker.addItem(event.getWebcam());
                }
            }

            @Override
            public void webcamGone(WebcamDiscoveryEvent event) {
                if (webcamPicker != null) {
                    webcamPicker.removeItem(event.getWebcam());
                }
            }
        });

        // 查找HD USB CAMERA 1 并设置为默认 camera
        for (int i = 0; i < webcamPicker.getItemCount(); i++) {
            Webcam itemAt = (Webcam) webcamPicker.getItemAt(i);
            if (itemAt.getName().contains("HD USB CAMERA")) {
                webcamPicker.setSelectedIndex(i);
            }
        }

        // 下拉列表item点击事件
        webcamPicker.addItemListener(e -> {
            if (e.getItem() != webcam) {
                if (webcam != null) {
                    panel.stop();
                    window.remove(panel);
                    webcam.close();
                    webcam = (Webcam) e.getItem();
                    webcam.setCustomViewSizes(nonStandardResolutions);
                    webcam.setViewSize(WebcamResolution.UXGA.getSize());
                    panel = new WebcamPanel(webcam, false);
                    panel.setMirrored(true);
                    panel.setFPSDisplayed(true);
                    window.add(panel, BorderLayout.CENTER);
                    window.pack();
                    Thread t = new Thread(() -> panel.start());
                    t.setName("example-stoper");
                    t.setDaemon(true);
                    t.setUncaughtExceptionHandler((t1, e1) -> {
                        System.err.println(String.format("Exception in thread %s", t1.getName()));
                        e1.printStackTrace();
                    });
                    t.start();
                    window.setSize(800, 900);
                }
            }
        });
        // 获取当前选择的camera
        webcam = webcamPicker.getSelectedWebcam();
        boolean open = webcam.isOpen();
        if (!open) {
            // 设置分辨率
            webcam.setCustomViewSizes(nonStandardResolutions);
            webcam.setViewSize(WebcamResolution.UXGA.getSize());
        }
        panel = new WebcamPanel(webcam);
        // 旋转显示的图像
        WebcamPanel.Painter painter = panel.new DefaultPainter() {
            final JHFlipFilter rotate = new JHFlipFilter(JHFlipFilter.FLIP_180);

            @Override
            public void paintImage(WebcamPanel owner, BufferedImage image, Graphics2D g2) {
                super.paintImage(owner, rotate.filter(image, null), g2);
            }
        };
        panel.setPainter(painter);
        panel.setFPSDisplayed(true);
        panel.setDisplayDebugInfo(true);
        panel.setImageSizeDisplayed(true);
        // 翻转图像(镜像)
        panel.setMirrored(false);
        JButton jButton = new JButton("tack");
        window.add(webcamPicker, BorderLayout.NORTH);
        window.add(jButton, BorderLayout.SOUTH);
        window.add(panel);
        window.setResizable(true);
        window.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setSize(800, 900);
        window.setVisible(true);
        jButton.addActionListener(e -> {
            BufferedImage image = webcam.getImage();
            System.out.println(image.getWidth() + " x " + image.getHeight());
            String path = "D:\\Camera图片\\";
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdir();
            }
            File orderDir = new File(path + "saw" + "\\");
            if (!orderDir.exists()) {
                orderDir.mkdir();
            }
            long lTime = System.currentTimeMillis();
            try {
                File file = new File(path + lTime + ".png");
                boolean isExist = ImageIO.write(webcam.getImage(), "PNG", file);
                if (isExist) {
                    Thumbnails.of(file)
                            .rotate(90)
                            .outputQuality(1.0f)
                            .size(image.getWidth(), 900)
                            .toFile(path + lTime + "90.png");
//                    FileUtil.deleteFile(file.getPath());
                    System.out.println("path: " + path + lTime + "90.png");
                }
                window.dispose();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    @FXML
    void mainTwoOnclick(ActionEvent event) {
        Stage stage = new Stage();
        Parent root;
        try {
            root = FXMLLoader.load(getClass().getResource(Constants.CAMERA_VIEW_PATH));
            Scene scene = new Scene(root);
            stage.setTitle("第二种");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}