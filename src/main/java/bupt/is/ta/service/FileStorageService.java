package bupt.is.ta.service;

import bupt.is.ta.model.Config;
import bupt.is.ta.store.DataStore;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileStorageService {

    private final DataStore store = DataStore.getInstance();

    public String saveCv(ServletContext context, String studentId, Part filePart) throws IOException {
        Path baseDir = resolveCvBaseDir(context);
        if (!Files.exists(baseDir)) {
            Files.createDirectories(baseDir);
        }
        String fileName = filePart.getSubmittedFileName();
        String ext = extractExtension(fileName);
        if (ext.isEmpty()) {
            ext = ".pdf";
        }
        String targetName = studentId + ext.toLowerCase();
        String oldPath = findExistingCvPath(baseDir, studentId);
        if (oldPath != null) {
            Files.deleteIfExists(Path.of(oldPath));
        }
        Path target = baseDir.resolve(targetName);
        try (InputStream in = filePart.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return target.toString();
    }

    public void deleteCv(String cvPath) throws IOException {
        if (cvPath == null || cvPath.isBlank()) {
            return;
        }
        Files.deleteIfExists(Path.of(cvPath));
    }

    public Path resolveCvBaseDir(ServletContext context) {
        Config config = store.getConfig();
        if ("USER_HOME".equalsIgnoreCase(config.getStorageMode())) {
            String home = System.getProperty("user.home");
            return Path.of(home, "ebu_data", "cvs");
        }
        String realBase = context.getRealPath(config.getCvRelativePath());
        return Path.of(realBase);
    }

    private String findExistingCvPath(Path baseDir, String studentId) throws IOException {
        if (!Files.exists(baseDir)) {
            return null;
        }
        try (var files = Files.list(baseDir)) {
            return files
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.startsWith(studentId + "."))
                    .findFirst()
                    .map(name -> baseDir.resolve(name).toString())
                    .orElse(null);
        }
    }

    private String extractExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int idx = fileName.lastIndexOf('.');
        if (idx < 0 || idx == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(idx);
    }
}

