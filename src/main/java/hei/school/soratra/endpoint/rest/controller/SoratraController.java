package hei.school.soratra.endpoint.rest.controller;
import hei.school.soratra.file.BucketComponent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@RestController
public class SoratraController {
    private final BucketComponent bucketComponent;

    public SoratraController(BucketComponent bucketComponent) {
        this.bucketComponent = bucketComponent;
    }
    @PutMapping("/soratra/{id}")
    public ResponseEntity<Void> processPoeticText(@PathVariable String id, @RequestBody String poeticText) {
        String transformedText = poeticText.toLowerCase();
        String objectKey = "poetic-texts/" + id;
        Path tempFilePath;
        try {
            tempFilePath = Files.createTempFile("poetic-text-", ".txt");
            Files.write(tempFilePath, transformedText.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temporary file for poetic text", e);
        }
        File tempFile = tempFilePath.toFile();
        bucketComponent.upload(tempFile, objectKey);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/soratra/{id}")
    public ResponseEntity<PoeticTextResponse> getPoeticText(@PathVariable String id) {
        String objectKey = "poetic-texts/" + id;
        File downloadedFile = bucketComponent.download(objectKey);
        String transformedText;
        try {
            transformedText = new String(Files.readAllBytes(downloadedFile.toPath()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read downloaded file", e);
        }
        URL presignedUrl = bucketComponent.presign(objectKey, Duration.ofMinutes(15));

        PoeticTextResponse response = new PoeticTextResponse();
        response.setOriginalText("This is an example of a poetic text.");
        response.setTransformedText(transformedText);
        response.setTransformedUrl(presignedUrl.toString());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    public static class PoeticTextResponse {
        private String originalText;
        private String transformedText;
        private String transformedUrl;

        public String getOriginalText() {
            return originalText;
        }
        public void setOriginalText(String originalText) {
            this.originalText = originalText;
        }
        public String getTransformedText() {
            return transformedText;
        }
        public void setTransformedText(String transformedText) {
            this.transformedText = transformedText;
        }
        public String getTransformedUrl() {
            return transformedUrl;
        }
        public void setTransformedUrl(String transformedUrl) {
            this.transformedUrl = transformedUrl;
        }
    }
}

