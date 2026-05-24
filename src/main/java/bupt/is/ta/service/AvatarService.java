package bupt.is.ta.service;

import bupt.is.ta.model.Application;
import bupt.is.ta.model.User;
import bupt.is.ta.store.DataStore;
import jakarta.servlet.ServletContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public class AvatarService {

    public static final String MO_DEFAULT_WEB_PATH = "/assets/avatars/mo-default.png";
    private static final String PRESET_PREFIX = "preset/";

    private final DataStore store = DataStore.getInstance();
    private final FileStorageService fileStorageService = new FileStorageService();
    private final ApplicationService applicationService = new ApplicationService();

    public String resolveDisplayUrl(User user) {
        if (user == null) {
            return MO_DEFAULT_WEB_PATH;
        }
        if (user.getRole() == User.Role.MO) {
            return MO_DEFAULT_WEB_PATH;
        }
        return switch (user.getAvatarType()) {
            case PRESET -> presetWebPath(user.getAvatarKey());
            case UPLOAD -> "/avatar?userId=" + user.getId();
            default -> defaultPresetForRole(user.getRole());
        };
    }

    public String defaultPresetForRole(User.Role role) {
        if (role == User.Role.MO) {
            return MO_DEFAULT_WEB_PATH;
        }
        return "/assets/avatars/preset/1.png";
    }

    public String presetWebPath(String avatarKey) {
        String key = avatarKey == null ? "" : avatarKey.trim();
        if (key.isBlank()) {
            return "/assets/avatars/preset/1.png";
        }
        if (key.startsWith("/assets/")) {
            return key;
        }
        if (key.startsWith(PRESET_PREFIX)) {
            return "/assets/avatars/" + key;
        }
        return "/assets/avatars/preset/" + key.replace("preset/", "");
    }

    public Path resolveAvatarFile(ServletContext context, User user) throws IOException {
        if (user == null) {
            return null;
        }
        if (user.getRole() == User.Role.MO) {
            return presetResourcePath(context, "mo-default.png");
        }
        return switch (user.getAvatarType()) {
            case UPLOAD -> {
                Path uploaded = fileStorageService.resolveAvatarPath(context, user.getId());
                yield Files.exists(uploaded) ? uploaded : presetResourcePath(context, "1.png");
            }
            case PRESET -> presetResourcePath(context, normalizePresetFile(user.getAvatarKey()));
            default -> presetResourcePath(context, "1.png");
        };
    }

    public boolean canViewAvatar(User viewer, String targetUserId) {
        if (viewer == null || targetUserId == null || targetUserId.isBlank()) {
            return false;
        }
        if (targetUserId.equals(viewer.getId())) {
            return true;
        }
        if (viewer.getRole() == User.Role.ADMIN) {
            return true;
        }
        if (viewer.getRole() == User.Role.MO) {
            User target = findUser(targetUserId);
            if (target == null || target.getRole() != User.Role.TA) {
                return false;
            }
            return applicationService.listByStudent(targetUserId).stream()
                    .anyMatch(app -> isApplicantForMo(app, viewer.getId()));
        }
        return false;
    }

    public void applyPreset(User user, String presetKey) {
        if (user == null) {
            return;
        }
        user.setAvatarType(User.AvatarType.PRESET);
        user.setAvatarKey(PRESET_PREFIX + normalizePresetFile(presetKey));
    }

    public List<String> listPresetKeys(ServletContext context) {
        if (context == null) {
            return defaultPresetList();
        }
        String realPath = context.getRealPath("/assets/avatars/preset");
        if (realPath == null || realPath.isBlank()) {
            return defaultPresetList();
        }
        Path presetDir = Path.of(realPath);
        if (!Files.isDirectory(presetDir)) {
            return defaultPresetList();
        }
        try (var stream = Files.list(presetDir)) {
            return stream.filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .filter(name -> name.toLowerCase(Locale.ROOT).matches(".*\\.(png|jpg|jpeg|gif|webp)$"))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            return defaultPresetList();
        }
    }

    private static List<String> defaultPresetList() {
        return List.of("1.png", "2.png", "3.png", "4.png", "5.png", "6.png");
    }

    private boolean isApplicantForMo(Application app, String moId) {
        if (app == null || moId == null) {
            return false;
        }
        return store.getJobs().stream()
                .anyMatch(j -> j.getId() != null && j.getId().equals(app.getJobId()) && moId.equals(j.getMoId()));
    }

    private User findUser(String id) {
        return store.getUsers().stream().filter(u -> id.equals(u.getId())).findFirst().orElse(null);
    }

    private Path presetResourcePath(ServletContext context, String fileName) {
        String safe = normalizePresetFile(fileName);
        String real = context.getRealPath("/assets/avatars/preset/" + safe);
        if (real != null) {
            Path path = Path.of(real);
            if (Files.exists(path)) {
                return path;
            }
        }
        real = context.getRealPath("/assets/avatars/" + safe);
        if (real != null) {
            Path path = Path.of(real);
            if (Files.exists(path)) {
                return path;
            }
        }
        return Path.of(context.getRealPath("/assets/avatars/mo-default.png"));
    }

    private String normalizePresetFile(String key) {
        String v = key == null ? "" : key.trim();
        if (v.isBlank()) {
            return "1.png";
        }
        if (v.contains("/")) {
            v = v.substring(v.lastIndexOf('/') + 1);
        }
        if (!v.toLowerCase(Locale.ROOT).matches(".*\\.(png|jpg|jpeg|gif|webp)$")) {
            return v + ".png";
        }
        return v;
    }

    public String contentTypeFor(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        if (name.endsWith(".png")) {
            return "image/png";
        }
        if (name.endsWith(".gif")) {
            return "image/gif";
        }
        if (name.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/jpeg";
    }

    public void copyStreamTo(Path target, InputStream in) throws IOException {
        Files.createDirectories(target.getParent());
        Files.copy(in, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }
}
