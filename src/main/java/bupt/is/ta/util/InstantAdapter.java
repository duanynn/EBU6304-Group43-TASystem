package bupt.is.ta.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Instant;

/**
 * Gson TypeAdapter for java.time.Instant so JSON read/write does not cause 500.
 */
public class InstantAdapter extends TypeAdapter<Instant> {
    @Override
    public void write(JsonWriter out, Instant value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.toString());
        }
    }

    @Override
    public Instant read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        try {
            if (in.peek() == JsonToken.STRING) {
                String s = in.nextString();
                if (s == null || s.isEmpty()) return null;
                if (!s.endsWith("Z") && !s.contains("+") && s.indexOf('-', 10) < 0) {
                    s = s + "Z";
                }
                return Instant.parse(s);
            }
            if (in.peek() == JsonToken.NUMBER) {
                long epochMilli = in.nextLong();
                return Instant.ofEpochMilli(epochMilli);
            }
        } catch (Exception e) {
            return null;
        }
        in.nextNull();
        return null;
    }
}
