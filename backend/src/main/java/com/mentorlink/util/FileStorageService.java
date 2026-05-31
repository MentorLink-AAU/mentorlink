package com.mentorlink.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/** Stores, retrieves, and deletes uploaded files in configurable upload directory. */
@Service
public class FileStorageService {

    private final Path rootLocation;

    public FileStorageService(@Value("${app.upload.dir:../uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + rootLocation + " - " + e.getMessage(), e);
        }
    }

    /** Save file under subdir; returns relative path (subdir/filename). */
    public String store(MultipartFile file, String subdir) throws IOException {
        String ext = file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")
                ? file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'))
                : "";
        String filename = UUID.randomUUID() + ext;
        Path target = rootLocation.resolve(subdir).resolve(filename);
        Files.createDirectories(target.getParent());
        Files.copy(file.getInputStream(), target);
        return subdir + "/" + filename;
    }

    /** Delete file by relative path. */
    public void delete(String relativePath) throws IOException {
        Path target = rootLocation.resolve(relativePath);
        if (Files.exists(target)) {
            Files.delete(target);
        }
    }

    /** Resolve relative path to absolute Path. */
    public Path resolve(String relativePath) {
        return rootLocation.resolve(relativePath);
    }
}
