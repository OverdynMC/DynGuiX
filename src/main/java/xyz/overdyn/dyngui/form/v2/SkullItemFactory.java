package xyz.overdyn.dyngui.form.v2;

import xyz.overdyn.dyngui.form.v2.cache.SkullCache;
import xyz.overdyn.dyngui.items.ItemWrapper;

import java.util.concurrent.CompletableFuture;

public record SkullItemFactory(String base64) {

    public CompletableFuture<ItemWrapper> createAsync() {
        return CompletableFuture.supplyAsync(() -> SkullCache.get(base64));
    }
}
