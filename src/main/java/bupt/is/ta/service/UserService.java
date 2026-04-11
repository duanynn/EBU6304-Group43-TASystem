package bupt.is.ta.service;

import bupt.is.ta.model.User;
import bupt.is.ta.store.DataStore;

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
        return findById(id)
                .filter(u -> u.getPassword().equals(password));
    }

    public void save(User user) throws Exception {
        synchronized (store) {
            store.upsertUser(user);
            store.saveAll();
        }
    }
}

