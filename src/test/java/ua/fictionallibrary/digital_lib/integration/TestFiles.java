package ua.fictionallibrary.digital_lib.integration;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TestFiles {
    public static byte[] samplePdf() throws IOException {
        try (PDDocument doc = new PDDocument(); var out = new ByteArrayOutputStream()) {
            doc.addPage(new PDPage());
            doc.save(out);
            return out.toByteArray();
        }
    }

    public static byte[] coverPng() throws IOException {
        var out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(200, 300, BufferedImage.TYPE_INT_RGB), "png", out);
        return out.toByteArray();
    }
}
