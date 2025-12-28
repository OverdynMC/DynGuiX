package xyz.overdyn.dyngui.form.v2.property;

import java.util.Map;

public interface PropertyContainer {

    Map<Property<?>, Object> metadata();

    @SuppressWarnings("unchecked")
    default <T> T meta(Property<T> property) {
        return (T) metadata().getOrDefault(property, property.defaultValue());
    }

    default <T> void meta(Property<T> property, T value) {
        metadata().put(property, value);
    }

    default boolean has(Property<?> property) {
        return metadata().containsKey(property);
    }
}
