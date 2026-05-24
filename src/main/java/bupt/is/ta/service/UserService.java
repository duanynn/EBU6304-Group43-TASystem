package bupt.is.ta.service;

import bupt.is.ta.model.User;
import bupt.is.ta.store.DataStore;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class UserService {

    private final DataStore store = DataStore.getInstance();

    public Optional<User> findById(String id) {
        List<User> users = store.getUsers();
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
    }

    public Optional<User> authenticate(String id, String password) {
        if (password == null || password.isBlank()) {
            return Optional.empty();
        }
        String attempt = password.trim();
        return findById(id)
                .filter(u -> u.getPassword() != null && u.getPassword().equals(attempt));
    }

    /**
     * Verify login password for {@code id} against persisted data ({@code users/{id}.json}).
     */
    public boolean verifyPasswordFromStore(String id, String password) {
        if (id == null || id.isBlank() || password == null || password.isBlank()) {
            return false;
        }
        boolean ok = store.passwordMatchesOnDisk(id, password);
        if (ok) {
            try {
                store.refreshUserFromDisk(id);
            } catch (IOException ignored) {
                // password already verified from disk
            }
        }
        return ok;
    }

    public Optional<User> findByIdFromStore(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        try {
            store.refreshUserFromDisk(id);
        } catch (Exception ignored) {
            // use in-memory cache
        }
        return findById(id);
    }

    public void save(User user) throws Exception {
        synchronized (store) {
            store.upsertUser(user);
            store.saveAll();
        }
    }
}

