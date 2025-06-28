package org.example.smartmuseum.util;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

public class CameraQRScanner {

    public interface QRScanCallback {
        void onQRCodeDetected(String qrCode);
        void onImageUpdate(Image image);
        void onError(String error);
    }

    private OpenCVFrameGrabber grabber;
    private OpenCVFrameConverter.ToMat converterToMat;
    private Java2DFrameConverter converterToBufferedImage;
    private MultiFormatReader qrReader;
    private QRScanCallback callback;
    private AtomicBoolean isScanning;
    private Thread scanningThread;

    public CameraQRScanner() {
        converterToMat = new OpenCVFrameConverter.ToMat();
        converterToBufferedImage = new Java2DFrameConverter();
        qrReader = new MultiFormatReader();
        isScanning = new AtomicBoolean(false);

        System.out.println("✅ CameraQRScanner initialized");
    }

    public void startScanning(QRScanCallback callback) throws Exception {
        if (isScanning.get()) {
            throw new IllegalStateException("Scanner is already running");
        }

        this.callback = callback;

        try {
            // Initialize camera
            grabber = new OpenCVFrameGrabber(0); // Default camera
            grabber.setImageWidth(640);
            grabber.setImageHeight(480);
            grabber.setFrameRate(30);
            grabber.start();

            isScanning.set(true);

            // Start scanning thread
            scanningThread = new Thread(this::scanningLoop);
            scanningThread.setDaemon(true);
            scanningThread.start();

            System.out.println("✅ Camera scanning started");

        } catch (Exception e) {
            isScanning.set(false);
            throw new Exception("Failed to start camera: " + e.getMessage(), e);
        }
    }

    public void stopScanning() {
        isScanning.set(false);

        if (scanningThread != null) {
            scanningThread.interrupt();
        }

        if (grabber != null) {
            try {
                grabber.stop();
                grabber.release();
                grabber = null;
            } catch (Exception e) {
                System.err.println("Error stopping camera: " + e.getMessage());
            }
        }

        System.out.println("🛑 Camera scanning stopped");
    }

    private void scanningLoop() {
        while (isScanning.get() && !Thread.currentThread().isInterrupted()) {
            try {
                Frame frame = grabber.grab();
                if (frame == null) continue;

                // Convert frame to BufferedImage
                BufferedImage bufferedImage = converterToBufferedImage.convert(frame);
                if (bufferedImage == null) continue;

                // Convert to JavaFX Image and update UI
                Image fxImage = convertToFXImage(bufferedImage);
                if (callback != null) {
                    Platform.runLater(() -> callback.onImageUpdate(fxImage));
                }

                // Try to detect QR code
                try {
                    LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
                    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                    Result result = qrReader.decode(bitmap);

                    if (result != null && result.getText() != null && !result.getText().isEmpty()) {
                        String qrCode = result.getText();
                        System.out.println("🔍 QR Code detected: " + qrCode);

                        if (callback != null) {
                            Platform.runLater(() -> callback.onQRCodeDetected(qrCode));
                        }

                        // Brief pause after detection
                        Thread.sleep(1000);
                    }
                } catch (NotFoundException e) {
                    // No QR code found in this frame, continue
                } catch (Exception e) {
                    System.err.println("QR decode error: " + e.getMessage());
                }

                // Control frame rate
                Thread.sleep(100);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Camera error: " + e.getMessage());
                if (callback != null) {
                    Platform.runLater(() -> callback.onError(e.getMessage()));
                }
                break;
            }
        }
    }

    private Image convertToFXImage(BufferedImage bufferedImage) {
        try {
            // Convert BufferedImage to JavaFX Image
            WritableImage writableImage = new WritableImage(
                    bufferedImage.getWidth(),
                    bufferedImage.getHeight()
            );

            javafx.scene.image.PixelWriter pixelWriter = writableImage.getPixelWriter();

            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                for (int y = 0; y < bufferedImage.getHeight(); y++) {
                    int rgb = bufferedImage.getRGB(x, y);
                    pixelWriter.setArgb(x, y, rgb);
                }
            }

            return writableImage;
        } catch (Exception e) {
            System.err.println("Error converting image: " + e.getMessage());
            return null;
        }
    }

    public boolean isScanning() {
        return isScanning.get();
    }

    public void shutdown() {
        stopScanning();

        if (converterToMat != null) {
            try {
                converterToMat.close();
            } catch (Exception e) {
                System.err.println("Error closing converter: " + e.getMessage());
            }
        }

        System.out.println("🔧 CameraQRScanner shutdown complete");
    }
}
