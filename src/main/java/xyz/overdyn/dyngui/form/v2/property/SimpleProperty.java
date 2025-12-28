package xyz.overdyn.dyngui.form.v2.property;

public record SimpleProperty<T>(
        String key,
        T defaultValue
) implements Property<T> {}
