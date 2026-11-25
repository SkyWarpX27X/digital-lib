package ua.fictionallibrary.digital_lib.filestorage;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ua.fictionallibrary.digital_lib.exception.InvalidUploadException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

@Component
public class FileValidator {
    private static final double TOLERANCE = 0.02;

    public void validatePdf(MultipartFile file) {
        if (file.isEmpty())
            throw new InvalidUploadException("File is empty");

        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[5];
            if (is.read(header) != 5 || !"%PDF-".equals(new String(header, StandardCharsets.US_ASCII)))
                throw new InvalidUploadException("File is not a valid PDF");
        }
        catch (IOException _) {
            throw new InvalidUploadException("An error occurred while trying to upload file");
        }

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            if (document.getNumberOfPages() == 0)
                throw new InvalidUploadException("Document has 0 pages");
        }
        catch (IOException _) {
            throw new InvalidUploadException("File is corrupted");
        }
    }

    public String validateCoverImage(MultipartFile file) {
        try (ImageInputStream in = ImageIO.createImageInputStream(file.getInputStream())) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (!readers.hasNext())
                throw new InvalidUploadException("Image is not valid");
            ImageReader reader = readers.next();
            try {
                reader.setInput(in);
                String format = reader.getFormatName().toLowerCase();
                if (!format.equals("jpeg") && !format.equals("png"))
                    throw new InvalidUploadException("Cover format must be JPEG or PNG");

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (Math.abs(width * 3.0 - height * 2.0) / (height * 2.0) > TOLERANCE)
                    throw new InvalidUploadException("Cover must have a 2:3 aspect ratio");

                return format.equals("png") ? "image/png" : "image/jpeg";
            }
            finally {
                reader.dispose();
            }
        }
        catch (IOException _) {
            throw new InvalidUploadException("An error occurred while trying to upload file");
        }
    }
}
