package bupt.is.ta.store;

import bupt.is.ta.model.Application;
import bupt.is.ta.model.Config;
import bupt.is.ta.model.Job;
import bupt.is.ta.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import bupt.is.ta.util.InstantAdapter;

/**
 * Singleton with in-memory cache + JSON persistence.
 */
public class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Instant.class, new InstantAdapter())
            .create();

    private Path baseDir;
    private Path usersFile;
    private Path usersDir;
    private Path jobsFile;
    private Path applicationsFile;
    private Path configFile;

    private final List<User> users = new ArrayList<>();
    private final List<Job> jobs = new ArrayList<>();
    private final List<Application> applications = new ArrayList<>();
    private Config config = new Config();

    private boolean initialized = false;

    public static DataStore getInstance() {
        return INSTANCE;
    }

    private DataStore() {
    }

    public synchronized void init(Path baseDir) throws IOException {
        if (initialized) {
            return;
        }
        this.baseDir = baseDir;
        if (!Files.exists(baseDir)) {
            Files.createDirectories(baseDir);
        }
        this.usersFile = baseDir.resolve("users.json");
        this.usersDir = baseDir.resolve("users");
        this.jobsFile = baseDir.resolve("jobs.json");
        this.applicationsFile = baseDir.resolve("applications.json");
        this.configFile = baseDir.resolve("config.json");

        loadUsers();
        if (users.isEmpty()) {
            seedDefaultUsers();
            saveUsers();
        }
        loadJobs();
        if (jobs.isEmpty()) {
            seedDefaultJob();
            saveJobs();
        }
        loadApplications();
        loadConfig();

        initialized = true;
    }

    private void loadUsers() throws IOException {
        users.clear();
        if (!Files.exists(usersDir)) {
            Files.createDirectories(usersDir);
        }
        try (Stream<Path> stream = Files.list(usersDir)) {
            List<Path> userFiles = stream
                    .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".json"))
                    .toList();
            for (Path file : userFiles) {
                try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                    User loaded = gson.fromJson(reader, User.class);
                    if (loaded != null && loaded.getId() != null && !loaded.getId().isBlank()) {
                        users.add(loaded);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        if (!users.isEmpty()) return;

        if (Files.exists(usersFile)) {
            try (Reader reader = Files.newBufferedReader(usersFile, StandardCharsets.UTF_8)) {
                Type listType = new TypeToken<List<User>>() {}.getType();
                List<User> loaded = gson.fromJson(reader, listType);
                if (loaded != null) {
                    users.addAll(loaded);
                }
            }
            if (!users.isEmpty()) {
                saveUsers();
            }
        }
    }

    /** Seed default test accounts on first startup if user list is empty. */
    private void seedDefaultUsers() {
        User ta = new User();
        ta.setId("2021001001");
        ta.setPassword("123");
        ta.setRole(User.Role.TA);
        ta.setName("Test Student");
        ta.setGpa(3.8);
        ta.setSkillTags(List.of("Java", "Python"));
        users.add(ta);

        User mo = new User();
        mo.setId("0000000001");
        mo.setPassword("123");
        mo.setRole(User.Role.MO);
        mo.setName("Test Instructor");
        users.add(mo);

        User admin = new User();
        admin.setId("admin");
        admin.setPassword("admin");
        admin.setRole(User.Role.ADMIN);
        admin.setName("Administrator");
        users.add(admin);
    }

    /** Seed one sample job on first startup if job list is empty. */
    private void seedDefaultJob() {
        Job job = new Job();
        job.setId(UUID.randomUUID().toString());
        job.setCourseName("Software Engineering");
        job.setMoId("0000000001");
        job.setRequiredCount(2);
        job.setRequiredSkills(List.of("Java", "Git"));
        job.setOpen(true);
        jobs.add(job);
    }

    private void saveUsers() throws IOException {
        if (!Files.exists(usersDir)) {
            Files.createDirectories(usersDir);
        }
        Set<String> alive = new HashSet<>();
        for (User user : users) {
            if (user == null || user.getId() == null || user.getId().isBlank()) continue;
            String safeId = sanitizeFileName(user.getId());
            alive.add(safeId + ".json");
            Path userFile = usersDir.resolve(safeId + ".json");
            try (Writer writer = Files.newBufferedWriter(userFile, StandardCharsets.UTF_8)) {
                gson.toJson(user, writer);
            }
        }
        try (Stream<Path> stream = Files.list(usersDir)) {
            for (Path path : stream.toList()) {
                String fileName = path.getFileName().toString();
                if (fileName.toLowerCase().endsWith(".json") && !alive.contains(fileName)) {
                    Files.deleteIfExists(path);
                }
            }
        }
        // Keep legacy aggregate file for compatibility / quick inspection.
        try (Writer writer = Files.newBufferedWriter(usersFile, StandardCharsets.UTF_8)) {
            gson.toJson(users, writer);
        }
    }

    private void loadJobs() throws IOException {
        if (!Files.exists(jobsFile)) {
            saveJobs();
            return;
        }
        try (Reader reader = Files.newBufferedReader(jobsFile, StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<Job>>() {}.getType();
            List<Job> loaded = gson.fromJson(reader, listType);
            jobs.clear();
            if (loaded != null) {
                jobs.addAll(loaded);
            }
        }
    }

    private void saveJobs() throws IOException {
        try (Writer writer = Files.newBufferedWriter(jobsFile, StandardCharsets.UTF_8)) {
            gson.toJson(jobs, writer);
        }
    }

    private void loadApplications() throws IOException {
        if (!Files.exists(applicationsFile)) {
            saveApplications();
            return;
        }
        try (Reader reader = Files.newBufferedReader(applicationsFile, StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<Application>>() {}.getType();
            List<Application> loaded = gson.fromJson(reader, listType);
            applications.clear();
            if (loaded != null) {
                applications.addAll(loaded);
            }
        }
    }

    private void saveApplications() throws IOException {
        try (Writer writer = Files.newBufferedWriter(applicationsFile, StandardCharsets.UTF_8)) {
            gson.toJson(applications, writer);
        }
    }

    private void loadConfig() throws IOException {
        if (!Files.exists(configFile)) {
            saveConfig();
            return;
        }
        try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
            Config loaded = gson.fromJson(reader, Config.class);
            if (loaded != null) {
                this.config = loaded;
                if (this.config.getCvRelativePath() == null) this.config.setCvRelativePath("/WEB-INF/data/cvs");
                if (this.config.getStorageMode() == null) this.config.setStorageMode("WEBAPP");
                if (this.config.getMaxCoursesPerTA() < 1) this.config.setMaxCoursesPerTA(2);
                if (this.config.getDashscopeEndpoint() == null || this.config.getDashscopeEndpoint().isBlank()) {
                    this.config.setDashscopeEndpoint("https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions");
                }
                if (this.config.getDashscopeModel() == null || this.config.getDashscopeModel().isBlank()) {
                    this.config.setDashscopeModel("qwen-plus");
                }
                if (this.config.getDashscopeApiKey() == null) {
                    this.config.setDashscopeApiKey("");
                }
            }
        } catch (Exception e) {
            this.config = new Config();
        }
    }

    private void saveConfig() throws IOException {
        try (Writer writer = Files.newBufferedWriter(configFile, StandardCharsets.UTF_8)) {
            gson.toJson(config, writer);
        }
    }

    public synchronized List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

    public synchronized List<Job> getJobs() {
        return Collections.unmodifiableList(jobs);
    }

    public synchronized List<Application> getApplications() {
        return Collections.unmodifiableList(applications);
    }

    public synchronized Config getConfig() {
        return config;
    }

    public synchronized void upsertUser(User user) {
        users.removeIf(u -> u.getId().equals(user.getId()));
        users.add(user);
    }

    public synchronized void addJob(Job job) {
        if (job.getId() == null) {
            job.setId(UUID.randomUUID().toString());
        }
        jobs.add(job);
    }

    public synchronized void updateJob(Job job) {
        jobs.removeIf(j -> j.getId().equals(job.getId()));
        jobs.add(job);
    }

    public synchronized void addApplication(Application app) {
        if (app.getId() == null) {
            app.setId(UUID.randomUUID().toString());
        }
        applications.add(app);
    }

    public synchronized void updateApplication(Application app) {
        applications.removeIf(a -> a.getId().equals(app.getId()));
        applications.add(app);
    }

    public synchronized void updateConfig(Config config) {
        this.config = config;
    }

    public synchronized void saveAll() throws IOException {
        saveUsers();
        saveJobs();
        saveApplications();
        saveConfig();
    }

    private String sanitizeFileName(String raw) {
        if (raw == null || raw.isBlank()) return "unknown";
        return raw.replaceAll("[^a-zA-Z0-9_\\-\\.]", "_");
    }
}

