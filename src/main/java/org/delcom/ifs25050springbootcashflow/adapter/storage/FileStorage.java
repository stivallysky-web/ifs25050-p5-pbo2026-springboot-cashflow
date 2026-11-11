package org.delcom.ifs25050springbootcashflow.adapter.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;

public class FileStorage {

    private final Path rootLocation;

    public FileStorage(String uploadDir) {
        this.rootLocation = Paths.get(uploadDir);
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Gagal membuat direktori upload: " + uploadDir, e);
        }
    }

    public String store(InputStream inputStream, String originalFilename) {
        try {
            if (originalFilename == null || originalFilename.isBlank()) {
                throw new IllegalArgumentException("Nama file tidak boleh kosong");
            }
            String extension = "";
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalFilename.substring(dotIndex);
            }
            String newFilename = UUID.randomUUID() + extension;
            Path destinationFile = this.rootLocation.resolve(Paths.get(newFilename)).normalize().toAbsolutePath();

            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new SecurityException("Tidak dapat menyimpan file di luar direktori target");
            }

            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            return newFilename;
        } catch (IOException e) {
            throw new RuntimeException("Gagal menyimpan file", e);
        }
    }

    public byte[] loadAsBytes(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize().toAbsolutePath();
            if (Files.exists(file) && Files.isReadable(file)) {
                return Files.readAllBytes(file);
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public void delete(String filename) {
        if (filename == null || filename.isBlank()) return;
        try {
            Path file = rootLocation.resolve(filename).normalize().toAbsolutePath();
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
        }
    }
}