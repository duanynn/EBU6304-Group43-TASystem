package bupt.is.ta.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class InstantAdapterTest {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantAdapter())
            .create();

    @Test
    void writesInstantAsIsoString() {
        Instant instant = Instant.parse("2026-05-19T08:00:00Z");

        assertEquals("\"2026-05-19T08:00:00Z\"", gson.toJson(instant, Instant.class));
    }

    @Test
    void writesNullAsJsonNull() {
        assertEquals("null", gson.toJson(null, Instant.class));
    }

    @Test
    void readsIsoStringAndMissingZuluSuffix() {
        assertEquals(Instant.parse("2026-05-19T08:00:00Z"), gson.fromJson("\"2026-05-19T08:00:00Z\"", Instant.class));
        assertEquals(Instant.parse("2026-05-19T08:00:00Z"), gson.fromJson("\"2026-05-19T08:00:00\"", Instant.class));
    }

    @Test
    void readsEpochMillisNumber() {
        assertEquals(Instant.ofEpochMilli(1000), gson.fromJson("1000", Instant.class));
    }

    @Test
    void invalidOrEmptyInputReturnsNull() {
        assertNull(gson.fromJson("\"\"", Instant.class));
        assertNull(gson.fromJson("\"not-an-instant\"", Instant.class));
        assertNull(gson.fromJson("null", Instant.class));
    }
}
