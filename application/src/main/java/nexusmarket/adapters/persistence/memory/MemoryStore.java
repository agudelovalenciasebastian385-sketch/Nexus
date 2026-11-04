package nexusmarket.adapters.persistence.memory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Thread-safe table of the memory profile. It keeps the insertion order, so the listings are stable,
 * and it reads over a copy, so nobody iterates over the table while another thread writes to it.
 */
final class MemoryStore<K, V> {

    private final Map<K, V> entries = Collections.synchronizedMap(new LinkedHashMap<>());

    void put(K key, V value) {
        entries.put(key, value);
    }

    Optional<V> get(K key) {
        return key == null ? Optional.empty() : Optional.ofNullable(entries.get(key));
    }

    List<V> values() {
        synchronized (entries) {
            return new ArrayList<>(entries.values());
        }
    }

    /** The half-open interval of the output ports: from is inclusive, toExclusive is exclusive. */
    static boolean inRange(LocalDateTime value, LocalDateTime from, LocalDateTime toExclusive) {
        return value != null && !value.isBefore(from) && value.isBefore(toExclusive);
    }
}
