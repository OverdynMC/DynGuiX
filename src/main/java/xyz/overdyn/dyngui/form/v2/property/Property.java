package xyz.overdyn.dyngui.form.v2.property;

import java.util.Map;

public interface Property<T> {

    String key();

    T defaultValue();

    @SuppressWarnings("unchecked")
    default T get(Map<String, Object> source) {
        return (T) source.getOrDefault(key(), defaultValue());
    }

    default void set(Map<String, Object> source, T value) {
        source.put(key(), value);
    }
}
