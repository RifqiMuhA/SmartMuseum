package org.example.smartmuseum.util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.Mat;

import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraQRScanner {
    private OpenCVFrameGrabber grabber;
    private FrameGrabber.ImageMode imageMode;
    private Java2DFrameConverter converter;
    private ExecutorService executor;
    private QRScanCallback callback;
    private boolean isScanning = false;

    public interface QRScanCallback {
        void onQRCodeDetected(String qrCode);
        void onImageUpdate(Image image);
        void onError(String error);
    }

    public CameraQRScanner() {
        this.converter = new Java2DFrameConverter();
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void startScanning(QRScanCallback callback) {
        this.callback = callback;

        executor.submit(() -> {
            try {
                // Initialize camera
                grabber = new OpenCVFrameGrabber(0); // Default camera
                grabber.setImageWidth(640);
                grabber.setImageHeight(480);
                grabber.start();

                isScanning = true;

                // Create QR code reader
                MultiFormatReader reader = new MultiFormatReader();

                System.out.println("✅ Camera started, scanning for QR codes...");

                while (isScanning) {
                    Frame frame = grabber.grab();
                    if (frame != null) {
                        // Convert frame to BufferedImage
                        BufferedImage bufferedImage = converter.convert(frame);
                        if (bufferedImage != null) {
                            // Update UI with camera image
                            Platform.runLater(() -> {
                                Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
                                callback.onImageUpdate(fxImage);
                            });

                            // Try to decode QR code
                            try {
                                LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
                                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                                Result result = reader.decode(bitmap);

                                String qrCode = result.getText();
                                if (qrCode != null && !qrCode.isEmpty()) {
                                    System.out.println("✅ QR Code detected: " + qrCode);
                                    Platform.runLater(() -> callback.onQRCodeDetected(qrCode));

                                    // Stop scanning after successful detection
                                    stopScanning();
                                    break;
                                }
                            } catch (NotFoundException e) {
                                // No QR code found in this frame, continue scanning
                            } catch (Exception e) {
                                System.err.println("Error decoding QR code: " + e.getMessage());
                            }
                        }
                    }

                    // Small delay to prevent excessive CPU usage
                    Thread.sleep(100);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> callback.onError("Camera error: " + e.getMessage()));
            } finally {
                try {
                    if (grabber != null) {
                        grabber.stop();
                        grabber.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void stopScanning() {
        isScanning = false;
        try {
            if (grabber != null) {
                grabber.stop();
                grabber.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("🛑 Camera scanning stopped");
    }

    public boolean isScanning() {
        return isScanning;
    }

    public void shutdown() {
        stopScanning();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}