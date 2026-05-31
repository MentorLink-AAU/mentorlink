package com.mentorlink.controller;

import com.mentorlink.util.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

/** Serves uploaded files (profile photos, etc.) by folder and filename. */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    /**
     * Serve uploaded files (e.g. profile photos).
     * Path format: /api/files/profile-photos/{filename}
     */
    @GetMapping("/{folder}/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String folder, @PathVariable String filename) {
        String relativePath = folder + "/" + filename;
        try {
            Path file = fileStorageService.resolve(relativePath);
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            String contentType = "application/octet-stream";
            String fname = file.getFileName().toString();
            if (fname.endsWith(".jpg") || fname.endsWith(".jpeg")) contentType = "image/jpeg";
            else if (fname.endsWith(".png")) contentType = "image/png";
            else if (fname.endsWith(".gif")) contentType = "image/gif";
            else if (fname.endsWith(".webp")) contentType = "image/webp";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fname + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
