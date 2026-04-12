package bupt.is.ta.service;

import bupt.is.ta.model.User;
import bupt.is.ta.store.DataStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private static DataStore store;
    private UserService service;

    @BeforeAll
    static void initStore() throws IOException {
        store = DataStore.getInstance();
        Path tmp = Files.createTempDirectory("ta-user-svc-test");
        tmp.toFile().deleteOnExit();
        store.init(tmp);
    }

    @BeforeEach
    void setUp() {
        service = new UserService();
    }

    // ── findById ──────────────────────────────────────────────────────────

    @Test
    void findById_existingUser_returnsUser() {
        User u = buildUser(uniqueId(), "Alice", User.Role.TA, "pass123");
        store.upsertUser(u);

        Optional<User> found = service.findById(u.getId());
        assertTrue(found.isPresent());
        assertEquals("Alice", found.get().getName());
    }

    @Test
    void findById_nonExistentUser_returnsEmpty() {
        Optional<User> found = service.findById("NON_EXISTENT_USER_" + UUID.randomUUID());
        assertTrue(found.isEmpty());
    }

    @Test
    void findById_nullId_returnsEmpty() {
        // findById streams over users and calls equals; null may throw or return empty
        // The stream filter "u.getId().equals(id)" would NPE if a user's id is null,
        // but all seeded/inserted users have ids. Calling with null simply finds nothing.
        Optional<User> found = service.findById(null);
        assertTrue(found.isEmpty());
    }

    // ── authenticate ─────────────────────────────────────────────────────

    @Test
    void authenticate_correctCredentials_returnsUser() {
        String id = uniqueId();
        User u = buildUser(id, "Bob", User.Role.TA, "mypass");
        store.upsertUser(u);

        Optional<User> auth = service.authenticate(id, "mypass");
        assertTrue(auth.isPresent());
        assertEquals("Bob", auth.get().getName());
    }

    @Test
    void authenticate_wrongPassword_returnsEmpty() {
        String id = uniqueId();
        User u = buildUser(id, "Carol", User.Role.MO, "rightpass");
        store.upsertUser(u);

        Optional<User> auth = service.authenticate(id, "wrongpass");
        assertTrue(auth.isEmpty());
    }

    @Test
    void authenticate_nonExistentUser_returnsEmpty() {
        Optional<User> auth = service.authenticate("NO_SUCH_" + UUID.randomUUID(), "any");
        assertTrue(auth.isEmpty());
    }

    @Test
    void authenticate_emptyPassword_doesNotMatchNonEmpty() {
        String id = uniqueId();
        User u = buildUser(id, "Dan", User.Role.TA, "realpass");
        store.upsertUser(u);

        Optional<User> auth = service.authenticate(id, "");
        assertTrue(auth.isEmpty());
    }

    // ── save ─────────────────────────────────────────────────────────────

    @Test
    void save_newUser_canBeFoundAfterward() throws Exception {
        String id = uniqueId();
        User u = buildUser(id, "Eve", User.Role.TA, "evepass");
        service.save(u);

        Optional<User> found = service.findById(id);
        assertTrue(found.isPresent());
        assertEquals("Eve", found.get().getName());
    }

    @Test
    void save_existingUser_updatesRecord() throws Exception {
        String id = uniqueId();
        User u = buildUser(id, "Frank", User.Role.TA, "pass");
        service.save(u);

        User updated = buildUser(id, "Franklin", User.Role.MO, "newpass");
        service.save(updated);

        Optional<User> found = service.findById(id);
        assertTrue(found.isPresent());
        assertEquals("Franklin", found.get().getName());
        assertEquals(User.Role.MO, found.get().getRole());

        // Should not create duplicate
        long count = store.getUsers().stream()
                .filter(x -> id.equals(x.getId())).count();
        assertEquals(1, count);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private String uniqueId() {
        return "UST-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private User buildUser(String id, String name, User.Role role, String password) {
        User u = new User();
        u.setId(id);
        u.setName(name);
        u.setRole(role);
        u.setPassword(password);
        return u;
    }
}
