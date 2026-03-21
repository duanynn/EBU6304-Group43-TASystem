package bupt.is.ta.service;

import bupt.is.ta.model.Config;
import bupt.is.ta.store.DataStore;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileStorageService {

    private final DataStore store = DataStore.getInstance();

    public String saveCv(ServletContext context, String studentId, Part filePart) throws IOException {
        Config config = store.getConfig();
        Path baseDir;
        if ("USER_HOME".equalsIgnoreCase(config.getStorageMode())) {
            String home = System.getProperty("user.home");
            baseDir = Path.of(home, "ebu_data", "cvs");
        } else {
            String realBase = context.getRealPath(config.getCvRelativePath());
            baseDir = Path.of(realBase);
        }
        if (!Files.exists(baseDir)) {
            Files.createDirectories(baseDir);
        }
        String fileName = studentId + ".pdf";
        Path target = baseDir.resolve(fileName);
        try (InputStream in = filePart.getInputStream()) {
            Files.copy(in, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        return target.toString();
    }
}

