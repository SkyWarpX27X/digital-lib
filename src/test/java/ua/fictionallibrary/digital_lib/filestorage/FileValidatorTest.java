package ua.fictionallibrary.digital_lib.filestorage;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.web.MockMultipartFile;
import ua.fictionallibrary.digital_lib.exception.InvalidUploadException;
import ua.fictionallibrary.digital_lib.integration.TestFiles;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class FileValidatorTest {

    private final FileValidator validator = new FileValidator();

    private static MockMultipartFile file(byte[] content) {
        return new MockMultipartFile("file", "upload", "application/octet-stream", content);
    }

    private static byte[] image(int width, int height, String format) throws IOException {
        var out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), format, out);
        return out.toByteArray();
    }

    private static void assertRejected(String message, Runnable call) {
        var ex = assertThrows(InvalidUploadException.class, call::run);
        assertEquals(message, ex.getMessage());
    }

    @Test
    void validPdfPasses() {
        assertDoesNotThrow(() -> validator.validatePdf(file(TestFiles.samplePdf())));
    }

    @Test
    void emptyPdfRejected() {
        assertRejected("File is empty", () -> validator.validatePdf(file(new byte[0])));
    }

    @Test
    void nonPdfHeaderRejected() {
        assertRejected("File is not a valid PDF",
                () -> validator.validatePdf(file("hello world".getBytes(StandardCharsets.US_ASCII))));
    }

    @Test
    void tooShortForHeaderRejected() {
        assertRejected("File is not a valid PDF",
                () -> validator.validatePdf(file("%PD".getBytes(StandardCharsets.US_ASCII))));
    }

    @Test
    void pdfHeaderWithGarbageRejectedAsCorrupted() {
        assertRejected("File is corrupted",
                () -> validator.validatePdf(file("%PDF-1.7 garbage".getBytes(StandardCharsets.US_ASCII))));
    }

    @Test
    void pdfWithoutPagesRejected() throws IOException {
        byte[] empty;
        try (var doc = new PDDocument(); var out = new ByteArrayOutputStream()) {
            doc.save(out);
            empty = out.toByteArray();
        }
        byte[] content = empty;
        assertRejected("Document has 0 pages", () -> validator.validatePdf(file(content)));
    }

    @Test
    void pngCoverReturnsPngType() throws IOException {
        assertEquals("image/png", validator.validateCoverImage(file(image(200, 300, "png"))));
    }

    @Test
    void jpegCoverReturnsJpegType() throws IOException {
        assertEquals("image/jpeg", validator.validateCoverImage(file(image(400, 600, "jpg"))));
    }

    @ParameterizedTest
    @CsvSource({"200, 303", "202, 300"})
    void coverWithinToleranceAccepted(int width, int height) throws IOException {
        assertEquals("image/png", validator.validateCoverImage(file(image(width, height, "png"))));
    }

    @ParameterizedTest
    @CsvSource({"200, 310", "300, 300", "300, 200"})
    void coverWithWrongRatioRejected(int width, int height) throws IOException {
        byte[] content = image(width, height, "png");
        assertRejected("Cover must have a 2:3 aspect ratio", () -> validator.validateCoverImage(file(content)));
    }

    @Test
    void gifCoverRejected() throws IOException {
        byte[] content = image(200, 300, "gif");
        assertRejected("Cover format must be JPEG or PNG", () -> validator.validateCoverImage(file(content)));
    }

    @Test
    void nonImageCoverRejected() {
        assertRejected("Image is not valid",
                () -> validator.validateCoverImage(file("not an image".getBytes(StandardCharsets.US_ASCII))));
    }
}