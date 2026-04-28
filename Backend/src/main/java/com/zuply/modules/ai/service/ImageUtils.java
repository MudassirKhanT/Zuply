package com.zuply.modules.ai.service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Utility class for resizing images before sending them to external AI APIs.
 * Groq (and similar) have strict payload limits (~4–10 MB). Resizing to 800 px
 * wide keeps the JPEG well under 1 MB while preserving enough detail for
 * product recognition (colour, material, shape, text on label, etc.).
 */
public class ImageUtils {

    private static final int DEFAULT_TARGET_WIDTH = 800;

    /**
     * Resize {@code inputFile} so its width equals {@code targetWidth}, preserving
     * the aspect ratio, then write it as a JPEG to a temp file.
     *
     * @param inputFile   source image (any format readable by ImageIO)
     * @param targetWidth desired width in pixels (height is auto-calculated)
     * @return temp {@link File} containing the resized JPEG
     * @throws IOException if the source cannot be read or the temp file written
     */
    public static File resizeForAI(File inputFile, int targetWidth) throws IOException {
        BufferedImage original = ImageIO.read(inputFile);
        if (original == null) {
            throw new IOException("Cannot decode image: " + inputFile.getAbsolutePath());
        }

        // If the image is already small enough, skip resizing
        if (original.getWidth() <= targetWidth) {
            return inputFile;
        }

        double ratio       = (double) targetWidth / original.getWidth();
        int    targetHeight = (int) (original.getHeight() * ratio);

        // SCALE_SMOOTH gives the best downscale quality
        Image scaled = original.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage output = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = output.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, targetWidth, targetHeight);
        g.drawImage(scaled, 0, 0, null);
        g.dispose();

        // Write to a temp JPEG — much smaller than PNG for photos
        File tempFile = Files.createTempFile("zuply_ai_resize_", ".jpg").toFile();
        tempFile.deleteOnExit();
        ImageIO.write(output, "jpg", tempFile);
        return tempFile;
    }

    /** Convenience overload using the default 800 px width. */
    public static File resizeForAI(File inputFile) throws IOException {
        return resizeForAI(inputFile, DEFAULT_TARGET_WIDTH);
    }
}
